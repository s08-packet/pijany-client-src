/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jdi.Bootstrap
 *  com.sun.jdi.ReferenceType
 *  com.sun.jdi.VirtualMachine
 *  com.sun.jdi.connect.AttachingConnector
 *  com.sun.jdi.connect.Connector
 *  com.sun.jdi.connect.Connector$Argument
 *  com.sun.jdi.connect.IllegalConnectorArgumentsException
 *  com.sun.jdi.event.Event
 *  com.sun.jdi.event.EventIterator
 *  com.sun.jdi.event.EventQueue
 *  com.sun.jdi.event.EventSet
 *  com.sun.jdi.event.MethodEntryEvent
 *  com.sun.jdi.request.EventRequest
 *  com.sun.jdi.request.EventRequestManager
 *  com.sun.jdi.request.MethodEntryRequest
 */
package com.viaversion.viaversion.libs.javassist.util;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.viaversion.viaversion.libs.javassist.util.Trigger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HotSwapper {
    private VirtualMachine jvm = null;
    private MethodEntryRequest request = null;
    private Map<ReferenceType, byte[]> newClassFiles = null;
    private Trigger trigger = new Trigger();
    private static final String HOST_NAME = "localhost";
    private static final String TRIGGER_NAME = Trigger.class.getName();

    public HotSwapper(int port) throws IOException, IllegalConnectorArgumentsException {
        this(Integer.toString(port));
    }

    public HotSwapper(String port) throws IOException, IllegalConnectorArgumentsException {
        AttachingConnector connector = (AttachingConnector)this.findConnector("com.sun.jdi.SocketAttach");
        Map arguments = connector.defaultArguments();
        ((Connector.Argument)arguments.get("hostname")).setValue(HOST_NAME);
        ((Connector.Argument)arguments.get("port")).setValue(port);
        this.jvm = connector.attach(arguments);
        EventRequestManager manager = this.jvm.eventRequestManager();
        this.request = HotSwapper.methodEntryRequests(manager, TRIGGER_NAME);
    }

    private Connector findConnector(String connector) throws IOException {
        Connector con;
        List connectors = Bootstrap.virtualMachineManager().allConnectors();
        Iterator iterator = connectors.iterator();
        do {
            if (!iterator.hasNext()) throw new IOException("Not found: " + connector);
        } while (!(con = (Connector)iterator.next()).name().equals(connector));
        return con;
    }

    private static MethodEntryRequest methodEntryRequests(EventRequestManager manager, String classpattern) {
        MethodEntryRequest mereq = manager.createMethodEntryRequest();
        mereq.addClassFilter(classpattern);
        mereq.setSuspendPolicy(1);
        return mereq;
    }

    private void deleteEventRequest(EventRequestManager manager, MethodEntryRequest request) {
        manager.deleteEventRequest((EventRequest)request);
    }

    public void reload(String className, byte[] classFile) {
        ReferenceType classtype = this.toRefType(className);
        HashMap<ReferenceType, byte[]> map = new HashMap<ReferenceType, byte[]>();
        map.put(classtype, classFile);
        this.reload2(map, className);
    }

    public void reload(Map<String, byte[]> classFiles) {
        HashMap<ReferenceType, byte[]> map = new HashMap<ReferenceType, byte[]>();
        String className = null;
        Iterator<Map.Entry<String, byte[]>> iterator = classFiles.entrySet().iterator();
        while (true) {
            if (!iterator.hasNext()) {
                if (className == null) return;
                this.reload2(map, className + " etc.");
                return;
            }
            Map.Entry<String, byte[]> e = iterator.next();
            className = e.getKey();
            map.put(this.toRefType(className), e.getValue());
        }
    }

    private ReferenceType toRefType(String className) {
        List list = this.jvm.classesByName(className);
        if (list == null) throw new RuntimeException("no such class: " + className);
        if (!list.isEmpty()) return (ReferenceType)list.get(0);
        throw new RuntimeException("no such class: " + className);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void reload2(Map<ReferenceType, byte[]> map, String msg) {
        Trigger trigger = this.trigger;
        synchronized (trigger) {
            this.startDaemon();
            this.newClassFiles = map;
            this.request.enable();
            this.trigger.doSwap();
            this.request.disable();
            Map<ReferenceType, byte[]> ncf = this.newClassFiles;
            if (ncf == null) return;
            this.newClassFiles = null;
            throw new RuntimeException("failed to reload: " + msg);
        }
    }

    private void startDaemon() {
        new Thread(){

            private void errorMsg(Throwable e) {
                System.err.print("Exception in thread \"HotSwap\" ");
                e.printStackTrace(System.err);
            }

            @Override
            public void run() {
                EventSet events = null;
                try {
                    events = HotSwapper.this.waitEvent();
                    EventIterator iter = events.eventIterator();
                    while (iter.hasNext()) {
                        Event event = iter.nextEvent();
                        if (!(event instanceof MethodEntryEvent)) continue;
                        HotSwapper.this.hotswap();
                        break;
                    }
                }
                catch (Throwable e) {
                    this.errorMsg(e);
                }
                try {
                    if (events == null) return;
                    events.resume();
                    return;
                }
                catch (Throwable e) {
                    this.errorMsg(e);
                }
            }
        }.start();
    }

    EventSet waitEvent() throws InterruptedException {
        EventQueue queue = this.jvm.eventQueue();
        return queue.remove();
    }

    void hotswap() {
        Map<ReferenceType, byte[]> map = this.newClassFiles;
        this.jvm.redefineClasses(map);
        this.newClassFiles = null;
    }
}


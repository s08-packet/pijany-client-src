/*
 * Decompiled with CFR 0.152.
 */
package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ServerboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.EntityTracker;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.Windows;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.utils.ChatUtil;
import java.util.UUID;

public class InventoryPackets {
    public static void register(Protocol1_7_6_10TO1_8 protocol) {
        protocol.registerClientbound(ClientboundPackets1_8.OPEN_WINDOW, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.handler(packetWrapper -> {
                    short windowId = packetWrapper.passthrough(Type.UNSIGNED_BYTE);
                    String windowType = packetWrapper.read(Type.STRING);
                    short windowtypeId = (short)Windows.getInventoryType(windowType);
                    packetWrapper.user().get(Windows.class).types.put(windowId, windowtypeId);
                    packetWrapper.write(Type.UNSIGNED_BYTE, windowtypeId);
                    JsonElement titleComponent = packetWrapper.read(Type.COMPONENT);
                    String title = ChatUtil.jsonToLegacy(titleComponent);
                    title = ChatUtil.removeUnusedColor(title, '8');
                    if (title.length() > 32) {
                        title = title.substring(0, 32);
                    }
                    packetWrapper.write(Type.STRING, title);
                    packetWrapper.passthrough(Type.UNSIGNED_BYTE);
                    packetWrapper.write(Type.BOOLEAN, true);
                    if (windowtypeId != 11) return;
                    packetWrapper.passthrough(Type.INT);
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.CLOSE_WINDOW, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.map(Type.UNSIGNED_BYTE);
                this.handler(packetWrapper -> {
                    short windowsId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
                    packetWrapper.user().get(Windows.class).remove(windowsId);
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.SET_SLOT, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.handler(packetWrapper -> {
                    short windowId = packetWrapper.read(Type.UNSIGNED_BYTE);
                    short windowType = packetWrapper.user().get(Windows.class).get(windowId);
                    packetWrapper.write(Type.UNSIGNED_BYTE, windowId);
                    short slot = packetWrapper.read(Type.SHORT);
                    if (windowType == 4) {
                        if (slot == 1) {
                            packetWrapper.cancel();
                            return;
                        }
                        if (slot >= 2) {
                            slot = (short)(slot - 1);
                        }
                    }
                    packetWrapper.write(Type.SHORT, slot);
                });
                this.map(Type.ITEM, Types1_7_6_10.COMPRESSED_NBT_ITEM);
                this.handler(packetWrapper -> {
                    Item item = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
                    ItemRewriter.toClient(item);
                    packetWrapper.set(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0, item);
                });
                this.handler(packetWrapper -> {
                    UUID uuid;
                    short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
                    if (windowId != 0) {
                        return;
                    }
                    short slot = packetWrapper.get(Type.SHORT, 0);
                    if (slot < 5) return;
                    if (slot > 8) {
                        return;
                    }
                    Item item = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
                    EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
                    Item[] equipment = tracker.getPlayerEquipment(uuid = packetWrapper.user().getProtocolInfo().getUuid());
                    if (equipment == null) {
                        equipment = new Item[5];
                        tracker.setPlayerEquipment(uuid, equipment);
                    }
                    equipment[9 - slot] = item;
                    if (tracker.getGamemode() != 3) return;
                    packetWrapper.cancel();
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.WINDOW_ITEMS, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.handler(packetWrapper -> {
                    short windowId = packetWrapper.read(Type.UNSIGNED_BYTE);
                    short windowType = packetWrapper.user().get(Windows.class).get(windowId);
                    packetWrapper.write(Type.UNSIGNED_BYTE, windowId);
                    Item[] items = packetWrapper.read(Type.ITEM_ARRAY);
                    if (windowType == 4) {
                        Item[] old = items;
                        items = new Item[old.length - 1];
                        items[0] = old[0];
                        System.arraycopy(old, 2, items, 1, old.length - 3);
                    }
                    int i = 0;
                    while (true) {
                        if (i >= items.length) {
                            packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM_ARRAY, items);
                            return;
                        }
                        items[i] = ItemRewriter.toClient(items[i]);
                        ++i;
                    }
                });
                this.handler(packetWrapper -> {
                    UUID uuid;
                    short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
                    if (windowId != 0) {
                        return;
                    }
                    Item[] items = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM_ARRAY, 0);
                    EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
                    Item[] equipment = tracker.getPlayerEquipment(uuid = packetWrapper.user().getProtocolInfo().getUuid());
                    if (equipment == null) {
                        equipment = new Item[5];
                        tracker.setPlayerEquipment(uuid, equipment);
                    }
                    int i = 5;
                    while (true) {
                        if (i >= 9) {
                            if (tracker.getGamemode() != 3) return;
                            GameProfileStorage.GameProfile profile = packetWrapper.user().get(GameProfileStorage.class).get(uuid);
                            if (profile == null) return;
                            items[5] = profile.getSkull();
                            return;
                        }
                        equipment[9 - i] = items[i];
                        if (tracker.getGamemode() == 3) {
                            items[i] = null;
                        }
                        ++i;
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.WINDOW_PROPERTY, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.SHORT);
                this.map(Type.SHORT);
                this.handler(packetWrapper -> {
                    short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
                    Windows windows = packetWrapper.user().get(Windows.class);
                    short windowType = windows.get(windowId);
                    short property = packetWrapper.get(Type.SHORT, 0);
                    short value = packetWrapper.get(Type.SHORT, 1);
                    if (windowType == -1) {
                        return;
                    }
                    if (windowType == 2) {
                        Windows.Furnace furnace = windows.furnace.computeIfAbsent(windowId, x -> new Windows.Furnace());
                        if (property == 0 || property == 1) {
                            if (property == 0) {
                                furnace.setFuelLeft(value);
                            } else {
                                furnace.setMaxFuel(value);
                            }
                            if (furnace.getMaxFuel() == 0) {
                                packetWrapper.cancel();
                                return;
                            }
                            value = (short)(200 * furnace.getFuelLeft() / furnace.getMaxFuel());
                            packetWrapper.set(Type.SHORT, 0, (short)1);
                            packetWrapper.set(Type.SHORT, 1, value);
                            return;
                        }
                        if (property != 2) {
                            if (property != 3) return;
                        }
                        if (property == 2) {
                            furnace.setProgress(value);
                        } else {
                            furnace.setMaxProgress(value);
                        }
                        if (furnace.getMaxProgress() == 0) {
                            packetWrapper.cancel();
                            return;
                        }
                        value = (short)(200 * furnace.getProgress() / furnace.getMaxProgress());
                        packetWrapper.set(Type.SHORT, 0, (short)0);
                        packetWrapper.set(Type.SHORT, 1, value);
                        return;
                    }
                    if (windowType == 4) {
                        if (property <= 2) return;
                        packetWrapper.cancel();
                        return;
                    }
                    if (windowType != 8) return;
                    windows.levelCost = value;
                    windows.anvilId = windowId;
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_7.CLOSE_WINDOW, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.map(Type.UNSIGNED_BYTE);
                this.handler(packetWrapper -> {
                    short windowsId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
                    packetWrapper.user().get(Windows.class).remove(windowsId);
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_7.CLICK_WINDOW, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.handler(packetWrapper -> {
                    short windowId = packetWrapper.read(Type.BYTE).byteValue();
                    packetWrapper.write(Type.UNSIGNED_BYTE, windowId);
                    short windowType = packetWrapper.user().get(Windows.class).get(windowId);
                    short slot = packetWrapper.read(Type.SHORT);
                    if (windowType == 4 && slot > 0) {
                        slot = (short)(slot + 1);
                    }
                    packetWrapper.write(Type.SHORT, slot);
                });
                this.map(Type.BYTE);
                this.map(Type.SHORT);
                this.map(Type.BYTE);
                this.map(Types1_7_6_10.COMPRESSED_NBT_ITEM, Type.ITEM);
                this.handler(packetWrapper -> {
                    Item item = packetWrapper.get(Type.ITEM, 0);
                    ItemRewriter.toServer(item);
                    packetWrapper.set(Type.ITEM, 0, item);
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_7.WINDOW_CONFIRMATION, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.map(Type.BYTE);
                this.map(Type.SHORT);
                this.map(Type.BOOLEAN);
                this.handler(packetWrapper -> {
                    short action = packetWrapper.get(Type.SHORT, 0);
                    if (action != -89) return;
                    packetWrapper.cancel();
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_7.CREATIVE_INVENTORY_ACTION, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.map(Type.SHORT);
                this.map(Types1_7_6_10.COMPRESSED_NBT_ITEM, Type.ITEM);
                this.handler(packetWrapper -> {
                    Item item = packetWrapper.get(Type.ITEM, 0);
                    ItemRewriter.toServer(item);
                    packetWrapper.set(Type.ITEM, 0, item);
                });
            }
        });
    }
}


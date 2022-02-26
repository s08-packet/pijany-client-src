/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.lwjgl.input.Keyboard
 *  org.lwjgl.input.Mouse
 *  tv.twitch.chat.ChatUserInfo
 */
package net.minecraft.client.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.stream.GuiTwitchUserMode;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityList;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import tv.twitch.chat.ChatUserInfo;

public abstract class GuiScreen
extends Gui
implements GuiYesNoCallback {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");
    private static final Splitter NEWLINE_SPLITTER = Splitter.on('\n');
    protected Minecraft mc;
    protected RenderItem itemRender;
    public int width;
    public int height;
    protected List<GuiButton> buttonList = Lists.newArrayList();
    protected List<GuiLabel> labelList = Lists.newArrayList();
    public boolean allowUserInput;
    protected FontRenderer fontRendererObj;
    private GuiButton selectedButton;
    protected int eventButton;
    private long lastMouseEvent;
    private int touchValue;
    private URI clickedLinkURI;

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < this.buttonList.size(); ++i) {
            this.buttonList.get(i).drawButton(this.mc, mouseX, mouseY);
        }
        int j = 0;
        while (j < this.labelList.size()) {
            this.labelList.get(j).drawLabel(this.mc, mouseX, mouseY);
            ++j;
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode != 1) return;
        this.mc.displayGuiScreen(null);
        if (this.mc.currentScreen != null) return;
        this.mc.setIngameSwider();
    }

    public static String getClipboardString() {
        try {
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (transferable == null) return "";
            if (!transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) return "";
            return (String)transferable.getTransferData(DataFlavor.stringFlavor);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return "";
    }

    public static void setClipboardString(String copyText) {
        if (StringUtils.isEmpty(copyText)) return;
        try {
            StringSelection stringselection = new StringSelection(copyText);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, null);
            return;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    protected void renderToolTip(ItemStack stack, int x, int y) {
        List<String> list = stack.getTooltip(Minecraft.thePlayer, this.mc.gameSettings.advancedItemTooltips);
        int i = 0;
        while (true) {
            if (i >= list.size()) {
                this.drawHoveringText(list, x, y);
                return;
            }
            if (i == 0) {
                list.set(i, (Object)((Object)stack.getRarity().rarityColor) + list.get(i));
            } else {
                list.set(i, (Object)((Object)EnumChatFormatting.GRAY) + list.get(i));
            }
            ++i;
        }
    }

    protected void drawCreativeTabHoveringText(String tabName, int mouseX, int mouseY) {
        this.drawHoveringText(Arrays.asList(tabName), mouseX, mouseY);
    }

    protected void drawHoveringText(List<String> textLines, int x, int y) {
        if (textLines.isEmpty()) return;
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        int i = 0;
        for (String s : textLines) {
            int j = this.fontRendererObj.getStringWidth(s);
            if (j <= i) continue;
            i = j;
        }
        int l1 = x + 12;
        int i2 = y - 12;
        int k = 8;
        if (textLines.size() > 1) {
            k += 2 + (textLines.size() - 1) * 10;
        }
        if (l1 + i > this.width) {
            l1 -= 28 + i;
        }
        if (i2 + k + 6 > this.height) {
            i2 = this.height - k - 6;
        }
        this.zLevel = 300.0f;
        this.itemRender.zLevel = 300.0f;
        int l = -267386864;
        this.drawGradientRect(l1 - 3, i2 - 4, l1 + i + 3, i2 - 3, l, l);
        this.drawGradientRect(l1 - 3, i2 + k + 3, l1 + i + 3, i2 + k + 4, l, l);
        this.drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 + k + 3, l, l);
        this.drawGradientRect(l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, l, l);
        this.drawGradientRect(l1 + i + 3, i2 - 3, l1 + i + 4, i2 + k + 3, l, l);
        int i1 = 0x505000FF;
        int j1 = (i1 & 0xFEFEFE) >> 1 | i1 & 0xFF000000;
        this.drawGradientRect(l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, i1, j1);
        this.drawGradientRect(l1 + i + 2, i2 - 3 + 1, l1 + i + 3, i2 + k + 3 - 1, i1, j1);
        this.drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 - 3 + 1, i1, i1);
        this.drawGradientRect(l1 - 3, i2 + k + 2, l1 + i + 3, i2 + k + 3, j1, j1);
        int k1 = 0;
        while (true) {
            if (k1 >= textLines.size()) {
                this.zLevel = 0.0f;
                this.itemRender.zLevel = 0.0f;
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                RenderHelper.enableStandardItemLighting();
                GlStateManager.enableRescaleNormal();
                return;
            }
            String s1 = textLines.get(k1);
            this.fontRendererObj.drawStringWithShadow(s1, l1, i2, -1);
            if (k1 == 0) {
                i2 += 2;
            }
            i2 += 10;
            ++k1;
        }
    }

    protected void handleComponentHover(IChatComponent p_175272_1_, int p_175272_2_, int p_175272_3_) {
        block20: {
            if (p_175272_1_ == null) return;
            if (p_175272_1_.getChatStyle().getChatHoverEvent() == null) return;
            HoverEvent hoverevent = p_175272_1_.getChatStyle().getChatHoverEvent();
            if (hoverevent.getAction() == HoverEvent.Action.SHOW_ITEM) {
                ItemStack itemstack = null;
                try {
                    NBTTagCompound nbtbase = JsonToNBT.getTagFromJson(hoverevent.getValue().getUnformattedText());
                    if (nbtbase instanceof NBTTagCompound) {
                        itemstack = ItemStack.loadItemStackFromNBT(nbtbase);
                    }
                }
                catch (NBTException nbtbase) {
                    // empty catch block
                }
                if (itemstack != null) {
                    this.renderToolTip(itemstack, p_175272_2_, p_175272_3_);
                } else {
                    this.drawCreativeTabHoveringText((Object)((Object)EnumChatFormatting.RED) + "Invalid Item!", p_175272_2_, p_175272_3_);
                }
            } else if (hoverevent.getAction() == HoverEvent.Action.SHOW_ENTITY) {
                if (this.mc.gameSettings.advancedItemTooltips) {
                    try {
                        NBTTagCompound nbtbase1 = JsonToNBT.getTagFromJson(hoverevent.getValue().getUnformattedText());
                        if (nbtbase1 instanceof NBTTagCompound) {
                            ArrayList<String> list1 = Lists.newArrayList();
                            NBTTagCompound nbttagcompound = nbtbase1;
                            list1.add(nbttagcompound.getString("name"));
                            if (nbttagcompound.hasKey("type", 8)) {
                                String s = nbttagcompound.getString("type");
                                list1.add("Type: " + s + " (" + EntityList.getIDFromString(s) + ")");
                            }
                            list1.add(nbttagcompound.getString("id"));
                            this.drawHoveringText(list1, p_175272_2_, p_175272_3_);
                            break block20;
                        }
                        this.drawCreativeTabHoveringText((Object)((Object)EnumChatFormatting.RED) + "Invalid Entity!", p_175272_2_, p_175272_3_);
                    }
                    catch (NBTException var10) {
                        this.drawCreativeTabHoveringText((Object)((Object)EnumChatFormatting.RED) + "Invalid Entity!", p_175272_2_, p_175272_3_);
                    }
                }
            } else if (hoverevent.getAction() == HoverEvent.Action.SHOW_TEXT) {
                this.drawHoveringText(NEWLINE_SPLITTER.splitToList(hoverevent.getValue().getFormattedText()), p_175272_2_, p_175272_3_);
            } else if (hoverevent.getAction() == HoverEvent.Action.SHOW_ACHIEVEMENT) {
                StatBase statbase = StatList.getOneShotStat(hoverevent.getValue().getUnformattedText());
                if (statbase != null) {
                    IChatComponent ichatcomponent = statbase.getStatName();
                    ChatComponentTranslation ichatcomponent1 = new ChatComponentTranslation("stats.tooltip.type." + (statbase.isAchievement() ? "achievement" : "statistic"), new Object[0]);
                    ichatcomponent1.getChatStyle().setItalic(true);
                    String s1 = statbase instanceof Achievement ? ((Achievement)statbase).getDescription() : null;
                    ArrayList<String> list = Lists.newArrayList(ichatcomponent.getFormattedText(), ichatcomponent1.getFormattedText());
                    if (s1 != null) {
                        list.addAll(this.fontRendererObj.listFormattedStringToWidth(s1, 150));
                    }
                    this.drawHoveringText(list, p_175272_2_, p_175272_3_);
                } else {
                    this.drawCreativeTabHoveringText((Object)((Object)EnumChatFormatting.RED) + "Invalid statistic/achievement!", p_175272_2_, p_175272_3_);
                }
            }
        }
        GlStateManager.disableLighting();
    }

    protected void setText(String newChatText, boolean shouldOverwrite) {
    }

    protected boolean handleComponentClick(IChatComponent p_175276_1_) {
        if (p_175276_1_ == null) {
            return false;
        }
        ClickEvent clickevent = p_175276_1_.getChatStyle().getChatClickEvent();
        if (GuiScreen.isShiftKeyDown()) {
            if (p_175276_1_.getChatStyle().getInsertion() == null) return false;
            this.setText(p_175276_1_.getChatStyle().getInsertion(), false);
            return false;
        }
        if (clickevent == null) return false;
        if (clickevent.getAction() == ClickEvent.Action.OPEN_URL) {
            if (!this.mc.gameSettings.chatLinks) {
                return false;
            }
            try {
                URI uri = new URI(clickevent.getValue());
                String s = uri.getScheme();
                if (s == null) {
                    throw new URISyntaxException(clickevent.getValue(), "Missing protocol");
                }
                if (!PROTOCOLS.contains(s.toLowerCase())) {
                    throw new URISyntaxException(clickevent.getValue(), "Unsupported protocol: " + s.toLowerCase());
                }
                if (this.mc.gameSettings.chatLinksPrompt) {
                    this.clickedLinkURI = uri;
                    this.mc.displayGuiScreen(new GuiConfirmOpenLink((GuiYesNoCallback)this, clickevent.getValue(), 31102009, false));
                    return true;
                }
                this.openWebLink(uri);
                return true;
            }
            catch (URISyntaxException urisyntaxexception) {
                LOGGER.error("Can't open url for " + clickevent, (Throwable)urisyntaxexception);
                return true;
            }
        }
        if (clickevent.getAction() == ClickEvent.Action.OPEN_FILE) {
            URI uri1 = new File(clickevent.getValue()).toURI();
            this.openWebLink(uri1);
            return true;
        }
        if (clickevent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
            this.setText(clickevent.getValue(), true);
            return true;
        }
        if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
            this.sendChatMessage(clickevent.getValue(), false);
            return true;
        }
        if (clickevent.getAction() != ClickEvent.Action.TWITCH_USER_INFO) {
            LOGGER.error("Don't know how to handle " + clickevent);
            return true;
        }
        ChatUserInfo chatuserinfo = this.mc.getTwitchStream().func_152926_a(clickevent.getValue());
        if (chatuserinfo != null) {
            this.mc.displayGuiScreen(new GuiTwitchUserMode(this.mc.getTwitchStream(), chatuserinfo));
            return true;
        }
        LOGGER.error("Tried to handle twitch user but couldn't find them!");
        return true;
    }

    public void sendChatMessage(String msg) {
        this.sendChatMessage(msg, true);
    }

    public void sendChatMessage(String msg, boolean addToChat) {
        if (addToChat) {
            this.mc.ingameGUI.getChatGUI().addToSentMessages(msg);
        }
        Minecraft.thePlayer.sendChatMessage(msg);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0) return;
        int i = 0;
        while (i < this.buttonList.size()) {
            GuiButton guibutton = this.buttonList.get(i);
            if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                this.selectedButton = guibutton;
                guibutton.playPressSound(this.mc.getSoundHandler());
                this.actionPerformed(guibutton);
            }
            ++i;
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.selectedButton == null) return;
        if (state != 0) return;
        this.selectedButton.mouseReleased(mouseX, mouseY);
        this.selectedButton = null;
    }

    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    }

    protected void actionPerformed(GuiButton button) throws IOException {
    }

    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        this.mc = mc;
        this.itemRender = mc.getRenderItem();
        this.fontRendererObj = mc.fontRendererObj;
        this.width = width;
        this.height = height;
        this.buttonList.clear();
        this.initGui();
    }

    public void initGui() {
    }

    public void handleInput() throws IOException {
        if (Mouse.isCreated()) {
            while (Mouse.next()) {
                this.handleMouseInput();
            }
        }
        if (!Keyboard.isCreated()) return;
        while (Keyboard.next()) {
            this.handleKeyboardInput();
        }
    }

    public void handleMouseInput() throws IOException {
        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int k = Mouse.getEventButton();
        if (Mouse.getEventButtonState()) {
            if (this.mc.gameSettings.touchscreen && this.touchValue++ > 0) {
                return;
            }
            this.eventButton = k;
            this.lastMouseEvent = Minecraft.getSystemTime();
            this.mouseClicked(i, j, this.eventButton);
            return;
        }
        if (k == -1) {
            if (this.eventButton == -1) return;
            if (this.lastMouseEvent <= 0L) return;
            long l = Minecraft.getSystemTime() - this.lastMouseEvent;
            this.mouseClickMove(i, j, this.eventButton, l);
            return;
        }
        if (this.mc.gameSettings.touchscreen && --this.touchValue > 0) {
            return;
        }
        this.eventButton = -1;
        this.mouseReleased(i, j, k);
    }

    public void handleKeyboardInput() throws IOException {
        if (Keyboard.getEventKeyState()) {
            this.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
        }
        this.mc.dispatchKeypresses();
    }

    public void updateScreen() {
    }

    public void onGuiClosed() {
    }

    public void drawDefaultBackground() {
        this.drawWorldBackground(0);
    }

    public void drawWorldBackground(int tint) {
        if (this.mc.theWorld != null) {
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
            return;
        }
        this.drawBackground(tint);
    }

    public void drawBackground(int tint) {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        this.mc.getTextureManager().bindTexture(optionsBackground);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        float f = 32.0f;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0, this.height, 0.0).tex(0.0, (float)this.height / 32.0f + (float)tint).color(64, 64, 64, 255).endVertex();
        worldrenderer.pos(this.width, this.height, 0.0).tex((float)this.width / 32.0f, (float)this.height / 32.0f + (float)tint).color(64, 64, 64, 255).endVertex();
        worldrenderer.pos(this.width, 0.0, 0.0).tex((float)this.width / 32.0f, tint).color(64, 64, 64, 255).endVertex();
        worldrenderer.pos(0.0, 0.0, 0.0).tex(0.0, tint).color(64, 64, 64, 255).endVertex();
        tessellator.draw();
    }

    public boolean doesGuiPauseGame() {
        return true;
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (id != 31102009) return;
        if (result) {
            this.openWebLink(this.clickedLinkURI);
        }
        this.clickedLinkURI = null;
        this.mc.displayGuiScreen(this);
    }

    private void openWebLink(URI p_175282_1_) {
        try {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null, new Object[0]);
            oclass.getMethod("browse", URI.class).invoke(object, p_175282_1_);
            return;
        }
        catch (Throwable throwable) {
            LOGGER.error("Couldn't open link", throwable);
        }
    }

    public static boolean isCtrlKeyDown() {
        if (Minecraft.isRunningOnMac) {
            if (Keyboard.isKeyDown((int)219)) return true;
            if (Keyboard.isKeyDown((int)220)) return true;
            return false;
        }
        if (Keyboard.isKeyDown((int)29)) return true;
        if (Keyboard.isKeyDown((int)157)) return true;
        return false;
    }

    public static boolean isShiftKeyDown() {
        if (Keyboard.isKeyDown((int)42)) return true;
        if (Keyboard.isKeyDown((int)54)) return true;
        return false;
    }

    public static boolean isAltKeyDown() {
        if (Keyboard.isKeyDown((int)56)) return true;
        if (Keyboard.isKeyDown((int)184)) return true;
        return false;
    }

    public static boolean isKeyComboCtrlX(int p_175277_0_) {
        if (p_175277_0_ != 45) return false;
        if (!GuiScreen.isCtrlKeyDown()) return false;
        if (GuiScreen.isShiftKeyDown()) return false;
        if (GuiScreen.isAltKeyDown()) return false;
        return true;
    }

    public static boolean isKeyComboCtrlV(int p_175279_0_) {
        if (p_175279_0_ != 47) return false;
        if (!GuiScreen.isCtrlKeyDown()) return false;
        if (GuiScreen.isShiftKeyDown()) return false;
        if (GuiScreen.isAltKeyDown()) return false;
        return true;
    }

    public static boolean isKeyComboCtrlC(int p_175280_0_) {
        if (p_175280_0_ != 46) return false;
        if (!GuiScreen.isCtrlKeyDown()) return false;
        if (GuiScreen.isShiftKeyDown()) return false;
        if (GuiScreen.isAltKeyDown()) return false;
        return true;
    }

    public static boolean isKeyComboCtrlA(int p_175278_0_) {
        if (p_175278_0_ != 30) return false;
        if (!GuiScreen.isCtrlKeyDown()) return false;
        if (GuiScreen.isShiftKeyDown()) return false;
        if (GuiScreen.isAltKeyDown()) return false;
        return true;
    }

    public void onResize(Minecraft mcIn, int p_175273_2_, int p_175273_3_) {
        this.setWorldAndResolution(mcIn, p_175273_2_, p_175273_3_);
    }
}


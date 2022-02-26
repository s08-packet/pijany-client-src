/*
 * Decompiled with CFR 0.152.
 */
package optfine;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

public class GuiPerformanceSettingsOF
extends GuiScreen {
    private GuiScreen prevScreen;
    protected String title = "Performance Settings";
    private GameSettings settings;
    private static GameSettings.Options[] enumOptions = new GameSettings.Options[]{GameSettings.Options.SMOOTH_FPS, GameSettings.Options.SMOOTH_WORLD, GameSettings.Options.FAST_RENDER, GameSettings.Options.FAST_MATH, GameSettings.Options.CHUNK_UPDATES, GameSettings.Options.CHUNK_UPDATES_DYNAMIC, GameSettings.Options.LAZY_CHUNK_LOADING};
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private long mouseStillTime = 0L;

    public GuiPerformanceSettingsOF(GuiScreen p_i37_1_, GameSettings p_i37_2_) {
        this.prevScreen = p_i37_1_;
        this.settings = p_i37_2_;
    }

    @Override
    public void initGui() {
        int i = 0;
        GameSettings.Options[] optionsArray = enumOptions;
        int n = optionsArray.length;
        int n2 = 0;
        while (true) {
            if (n2 >= n) {
                this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168 + 11, I18n.format("gui.done", new Object[0])));
                return;
            }
            GameSettings.Options gamesettings$options = optionsArray[n2];
            int j = this.width / 2 - 155 + i % 2 * 160;
            int k = this.height / 6 + 21 * (i / 2) - 10;
            if (!gamesettings$options.getEnumFloat()) {
                this.buttonList.add(new GuiOptionButton(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options, this.settings.getKeyBinding(gamesettings$options)));
            } else {
                this.buttonList.add(new GuiOptionSlider(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options));
            }
            ++i;
            ++n2;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        if (button.id < 200 && button instanceof GuiOptionButton) {
            this.settings.setOptionValue(((GuiOptionButton)button).returnEnumOptions(), 1);
            button.displayString = this.settings.getKeyBinding(GameSettings.Options.getEnumOptions(button.id));
        }
        if (button.id == 200) {
            this.mc.gameSettings.saveOptions();
            this.mc.displayGuiScreen(this.prevScreen);
        }
        if (button.id == GameSettings.Options.CLOUD_HEIGHT.ordinal()) return;
        ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        int i = scaledresolution.getScaledWidth();
        int j = scaledresolution.getScaledHeight();
        this.setWorldAndResolution(this.mc, i, j);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (Math.abs(mouseX - this.lastMouseX) <= 5 && Math.abs(mouseY - this.lastMouseY) <= 5) {
            int i = 700;
            if (System.currentTimeMillis() < this.mouseStillTime + (long)i) return;
            int j = this.width / 2 - 150;
            int k = this.height / 6 - 5;
            if (mouseY <= k + 98) {
                k += 105;
            }
            int l = j + 150 + 150;
            int i1 = k + 84 + 10;
            GuiButton guibutton = this.getSelectedButton(mouseX, mouseY);
            if (guibutton == null) return;
            String s = this.getButtonName(guibutton.displayString);
            String[] astring = this.getTooltipLines(s);
            if (astring == null) {
                return;
            }
            this.drawGradientRect(j, k, l, i1, -536870912, -536870912);
            int j1 = 0;
            while (j1 < astring.length) {
                String s1 = astring[j1];
                this.fontRendererObj.drawStringWithShadow(s1, j + 5, k + 5 + j1 * 11, 0xDDDDDD);
                ++j1;
            }
            return;
        }
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        this.mouseStillTime = System.currentTimeMillis();
    }

    private String[] getTooltipLines(String p_getTooltipLines_1_) {
        String[] stringArray;
        if (p_getTooltipLines_1_.equals("Smooth FPS")) {
            String[] stringArray2 = new String[5];
            stringArray2[0] = "Stabilizes FPS by flushing the graphic driver buffers";
            stringArray2[1] = "  OFF - no stabilization, FPS may fluctuate";
            stringArray2[2] = "  ON - FPS stabilization";
            stringArray2[3] = "This option is graphics driver dependant and its effect";
            stringArray = stringArray2;
            stringArray2[4] = "is not always visible";
            return stringArray;
        }
        if (p_getTooltipLines_1_.equals("Smooth World")) {
            String[] stringArray3 = new String[5];
            stringArray3[0] = "Removes lag spikes caused by the internal server.";
            stringArray3[1] = "  OFF - no stabilization, FPS may fluctuate";
            stringArray3[2] = "  ON - FPS stabilization";
            stringArray3[3] = "Stabilizes FPS by distributing the internal server load.";
            stringArray = stringArray3;
            stringArray3[4] = "Effective only for local worlds (single player).";
            return stringArray;
        }
        if (p_getTooltipLines_1_.equals("Load Far")) {
            String[] stringArray4 = new String[6];
            stringArray4[0] = "Loads the world chunks at distance Far.";
            stringArray4[1] = "Switching the render distance does not cause all chunks ";
            stringArray4[2] = "to be loaded again.";
            stringArray4[3] = "  OFF - world chunks loaded up to render distance";
            stringArray4[4] = "  ON - world chunks loaded at distance Far, allows";
            stringArray = stringArray4;
            stringArray4[5] = "       fast render distance switching";
            return stringArray;
        }
        if (p_getTooltipLines_1_.equals("Preloaded Chunks")) {
            String[] stringArray5 = new String[6];
            stringArray5[0] = "Defines an area in which no chunks will be loaded";
            stringArray5[1] = "  OFF - after 5m new chunks will be loaded";
            stringArray5[2] = "  2 - after 32m  new chunks will be loaded";
            stringArray5[3] = "  8 - after 128m new chunks will be loaded";
            stringArray5[4] = "Higher values need more time to load all the chunks";
            stringArray = stringArray5;
            stringArray5[5] = "and may decrease the FPS.";
            return stringArray;
        }
        if (p_getTooltipLines_1_.equals("Chunk Updates")) {
            String[] stringArray6 = new String[6];
            stringArray6[0] = "Chunk updates";
            stringArray6[1] = " 1 - slower world loading, higher FPS (default)";
            stringArray6[2] = " 3 - faster world loading, lower FPS";
            stringArray6[3] = " 5 - fastest world loading, lowest FPS";
            stringArray6[4] = "Number of chunk updates per rendered frame,";
            stringArray = stringArray6;
            stringArray6[5] = "higher values may destabilize the framerate.";
            return stringArray;
        }
        if (p_getTooltipLines_1_.equals("Dynamic Updates")) {
            String[] stringArray7 = new String[5];
            stringArray7[0] = "Dynamic chunk updates";
            stringArray7[1] = " OFF - (default) standard chunk updates per frame";
            stringArray7[2] = " ON - more updates while the player is standing still";
            stringArray7[3] = "Dynamic updates force more chunk updates while";
            stringArray = stringArray7;
            stringArray7[4] = "the player is standing still to load the world faster.";
            return stringArray;
        }
        if (p_getTooltipLines_1_.equals("Lazy Chunk Loading")) {
            String[] stringArray8 = new String[7];
            stringArray8[0] = "Lazy Chunk Loading";
            stringArray8[1] = " OFF - default server chunk loading";
            stringArray8[2] = " ON - lazy server chunk loading (smoother)";
            stringArray8[3] = "Smooths the integrated server chunk loading by";
            stringArray8[4] = "distributing the chunks over several ticks.";
            stringArray8[5] = "Turn it OFF if parts of the world do not load correctly.";
            stringArray = stringArray8;
            stringArray8[6] = "Effective only for local worlds and single-core CPU.";
            return stringArray;
        }
        if (p_getTooltipLines_1_.equals("Fast Math")) {
            String[] stringArray9 = new String[5];
            stringArray9[0] = "Fast Math";
            stringArray9[1] = " OFF - standard math (default)";
            stringArray9[2] = " ON - faster math";
            stringArray9[3] = "Uses optimized sin() and cos() functions which can";
            stringArray = stringArray9;
            stringArray9[4] = "better utilize the CPU cache and increase the FPS.";
            return stringArray;
        }
        if (!p_getTooltipLines_1_.equals("Fast Render")) return null;
        String[] stringArray10 = new String[5];
        stringArray10[0] = "Fast Render";
        stringArray10[1] = " OFF - standard rendering (default)";
        stringArray10[2] = " ON - optimized rendering (faster)";
        stringArray10[3] = "Uses optimized rendering algorithm which decreases";
        stringArray = stringArray10;
        stringArray10[4] = "the GPU load and may substantionally increase the FPS.";
        return stringArray;
    }

    private String getButtonName(String p_getButtonName_1_) {
        String string;
        int i = p_getButtonName_1_.indexOf(58);
        if (i < 0) {
            string = p_getButtonName_1_;
            return string;
        }
        string = p_getButtonName_1_.substring(0, i);
        return string;
    }

    private GuiButton getSelectedButton(int p_getSelectedButton_1_, int p_getSelectedButton_2_) {
        int i = 0;
        while (i < this.buttonList.size()) {
            boolean flag;
            GuiButton guibutton = (GuiButton)this.buttonList.get(i);
            int j = GuiVideoSettings.getButtonWidth(guibutton);
            int k = GuiVideoSettings.getButtonHeight(guibutton);
            boolean bl = flag = p_getSelectedButton_1_ >= guibutton.xPosition && p_getSelectedButton_2_ >= guibutton.yPosition && p_getSelectedButton_1_ < guibutton.xPosition + j && p_getSelectedButton_2_ < guibutton.yPosition + k;
            if (flag) {
                return guibutton;
            }
            ++i;
        }
        return null;
    }
}


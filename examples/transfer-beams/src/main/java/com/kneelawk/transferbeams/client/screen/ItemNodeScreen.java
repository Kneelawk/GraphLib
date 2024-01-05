/*
 * MIT License
 *
 * Copyright (c) 2023 Kneelawk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.kneelawk.transferbeams.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.kneelawk.transferbeams.screen.ItemNodeScreenHandler;

import static com.kneelawk.transferbeams.TransferBeamsMod.gui;
import static com.kneelawk.transferbeams.TransferBeamsMod.id;

public class ItemNodeScreen extends HandledScreen<ItemNodeScreenHandler> {
    private static final int WIDTH = 14 + 18 * 9;
    private static final int HEIGHT = 10 + 16 + 10 + 9 + 18 * 2 + 9 + 18 * 3 + 4 + 18;
    private static final Identifier BACKGROUND = id("textures/gui/container/item_node.png");
    private static final Identifier TAB = id("widget/tab");
    private static final Identifier TAB_SELECTED = id("widget/tab_selected");
    private static final Identifier TAB_HIGHLIGHT = id("widget/tab_highlight");
    private static final Identifier TAB_SELECTED_HIGHLIGHT = id("widget/tab_selected_highlight");
    private static final Identifier INPUT = id("icon/input");
    private static final Identifier OUTPUT = id("icon/output");
    private static final Identifier SIGNAL = id("icon/signal");

    private final TabButtonWidget inputTab = new TabButtonWidget(4, 0, 26, 26, gui("input"), INPUT, Tab.INPUT);
    private final TabButtonWidget outputTab =
        new TabButtonWidget(4 + 26 + 4, 0, 26, 26, gui("output"), OUTPUT, Tab.OUTPUT);
    private final TabButtonWidget signalTab =
        new TabButtonWidget(4 + 26 + 4 + 26 + 4, 0, 26, 26, gui("signal"), SIGNAL, Tab.SIGNAL);

    private Tab currentTab = Tab.INPUT;

    public ItemNodeScreen(ItemNodeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = WIDTH;
        backgroundHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        titleX = 5;
        titleY = 26 + 5;
        playerInventoryTitleX = 5;
        playerInventoryTitleY = 26 + 5 + 9 + 18 * 2;

        inputTab.setTooltip(Tooltip.create(gui("input")));
        addDrawableSelectableElement(inputTab);
        outputTab.setTooltip(Tooltip.create(gui("output")));
        addDrawableSelectableElement(outputTab);
        signalTab.setTooltip(Tooltip.create(gui("signal")));
        addDrawableSelectableElement(signalTab);
    }

    @Override
    protected void drawBackground(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        graphics.getMatrices().push();
        graphics.getMatrices().translate(x, y, 0);

        graphics.drawTexture(BACKGROUND, 0, 26, 0, 0, backgroundWidth, backgroundHeight - 26);

        graphics.getMatrices().pop();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
    }

    private enum Tab {
        INPUT,
        OUTPUT,
        SIGNAL
    }

    private class TabButtonWidget extends PressableWidget {
        private final Identifier icon;
        private final Tab tab;

        public TabButtonWidget(int x, int y, int width, int height, Text text, Identifier icon, Tab tab) {
            super(x, y, width, height, text);
            this.icon = icon;
            this.tab = tab;
        }

        @Override
        public int getX() {
            return super.getX() + ItemNodeScreen.this.x;
        }

        @Override
        public int getY() {
            return super.getY() + ItemNodeScreen.this.y;
        }

        @Override
        protected void updateNarration(NarrationMessageBuilder builder) {
            appendDefaultNarrations(builder);
        }

        @Override
        protected void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            Identifier texture;
            if (isSelected()) {
                if (isHoveredOrFocused()) {
                    texture = TAB_SELECTED_HIGHLIGHT;
                } else {
                    texture = TAB_SELECTED;
                }
            } else {
                if (isHoveredOrFocused()) {
                    texture = TAB_HIGHLIGHT;
                } else {
                    texture = TAB;
                }
            }

            graphics.drawGuiTexture(texture, getX(), getY(), getWidth(), getHeight());
            graphics.drawGuiTexture(icon, getX() + 5, getY() + 5, getWidth() - 10, getHeight() - 10);
        }

        @Override
        public void onPress() {
            currentTab = tab;
        }

        boolean isSelected() {
            return currentTab == tab;
        }
    }
}

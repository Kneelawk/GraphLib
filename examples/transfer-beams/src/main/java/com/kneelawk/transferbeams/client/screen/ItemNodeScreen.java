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

import java.util.Optional;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.screen.SpruceHandledScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.kneelawk.transferbeams.screen.ItemNodeScreenHandler;

import static com.kneelawk.transferbeams.TransferBeamsMod.gui;
import static com.kneelawk.transferbeams.TransferBeamsMod.id;

public class ItemNodeScreen extends SpruceHandledScreen<ItemNodeScreenHandler> {
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

    private final TabButtonWidget inputTab =
        new TabButtonWidget(Position.of(this, 4, 0), 26, 26, gui("input"), button -> currentTab = Tab.INPUT, INPUT,
            Tab.INPUT);
    private final TabButtonWidget outputTab =
        new TabButtonWidget(Position.of(this, 4 + 26 + 4, 0), 26, 26, gui("output"), button -> currentTab = Tab.OUTPUT,
            OUTPUT, Tab.OUTPUT);
    private final TabButtonWidget signalTab =
        new TabButtonWidget(Position.of(this, 4 + 26 + 4 + 26 + 4, 0), 26, 26, gui("signal"),
            button -> currentTab = Tab.SIGNAL, SIGNAL, Tab.SIGNAL);

    private Tab currentTab = Tab.INPUT;

    public ItemNodeScreen(ItemNodeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = WIDTH;
        backgroundHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        x = (width - backgroundWidth) / 2;
        y = (height - backgroundHeight) / 2;
        titleX = 5;
        titleY = 26 + 5;
        playerInventoryTitleX = 5;
        playerInventoryTitleY = 26 + 5 + 9 + 18 * 2;

        addDrawableSelectableElement(inputTab);
        addDrawableSelectableElement(outputTab);
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

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    private enum Tab {
        INPUT,
        OUTPUT,
        SIGNAL
    }

    private class TabButtonWidget extends SpruceButtonWidget {
        private final Identifier icon;
        private final Tab tab;

        public TabButtonWidget(Position position, int width, int height, Text message, PressAction action,
                               Identifier icon, Tab tab) {
            super(position, width, height, message, action);
            this.icon = icon;
            this.tab = tab;
        }

        @Override
        public Optional<Text> getTooltip() {
            return super.getTooltip().or(() -> Optional.of(getMessage()));
        }

        @Override
        protected void renderButton(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            graphics.drawGuiTexture(icon, getX() + 5, getY() + 5, getWidth() - 10, getHeight() - 10);
        }

        @Override
        protected void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            Identifier texture;
            if (currentTab == tab) {
                if (isFocusedOrHovered()) {
                    texture = TAB_SELECTED_HIGHLIGHT;
                } else {
                    texture = TAB_SELECTED;
                }
            } else {
                if (isFocusedOrHovered()) {
                    texture = TAB_HIGHLIGHT;
                } else {
                    texture = TAB;
                }
            }

            graphics.drawGuiTexture(texture, getX(), getY(), getWidth(), getHeight());
        }
    }
}

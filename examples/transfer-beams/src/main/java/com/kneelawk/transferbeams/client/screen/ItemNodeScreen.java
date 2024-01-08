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

import java.util.List;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ClickableWidgetStateTextures;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

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
    private static final Identifier SLOT = id("widget/slot");
    private static final Identifier SLOT_2 = id("widget/slot_2");
    private static final Identifier INPUT = id("icon/input");
    private static final Identifier OUTPUT = id("icon/output");
    private static final Identifier SIGNAL = id("icon/signal");

    private final List<ClickableWidget> inputWidgets = new ObjectArrayList<>();
    private final List<ClickableWidget> outputWidgets = new ObjectArrayList<>();
    private final List<ClickableWidget> signalWidgets = new ObjectArrayList<>();

    private final TabButtonWidget inputTab = new TabButtonWidget(4, 0, 26, 26, gui("input"), INPUT, Tab.INPUT);
    private final TabButtonWidget outputTab =
        new TabButtonWidget(4 + 26 + 4, 0, 26, 26, gui("output"), OUTPUT, Tab.OUTPUT);
    private final TabButtonWidget signalTab =
        new TabButtonWidget(4 + 26 + 4 + 26 + 4, 0, 26, 26, gui("signal"), SIGNAL, Tab.SIGNAL);

    private final AllowDenyButtonWidget inputAllowDeny =
        new AllowDenyButtonWidget(5, 26 + 5 + 9 + 8, 20, 20, gui("allow_deny.input"), 2, handler::getInputAllow,
            handler::setInputAllow);
    private final AllowDenyButtonWidget outputAllowDeny =
        new AllowDenyButtonWidget(5, 26 + 5 + 9 + 8, 20, 20, gui("allow_deny.output"), 2, handler::getOutputAllow,
            handler::setOutputAllow);

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

        addDrawableSelectableElement(inputTab);
        addDrawableSelectableElement(outputTab);
        addDrawableSelectableElement(signalTab);

        setTab(Tab.INPUT);
    }

    @Override
    protected void drawBackground(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        graphics.getMatrices().push();
        graphics.getMatrices().translate(x, y, 0);

        graphics.drawTexture(BACKGROUND, 0, 26, 0, 0, backgroundWidth, backgroundHeight - 26);

        graphics.getMatrices().pop();
    }

    @Override
    protected void drawForeground(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawText(textRenderer, title, titleX, titleY, 0xFF2A2A2A, false);
        graphics.drawText(textRenderer, playerInventoryTitle, playerInventoryTitleX, playerInventoryTitleY, 0xFF2A2A2A,
            false);
    }

    @Override
    protected void drawSlot(GuiGraphics graphics, Slot slot) {
        if (slot instanceof ItemNodeScreenHandler.FilterSlot filter) {
            graphics.drawGuiTexture(SLOT_2, filter.x - 1, filter.y - 1, 18, 18);
        } else if (slot instanceof ItemNodeScreenHandler.SignalSlot signal) {
            graphics.drawGuiTexture(SLOT, signal.x - 1, signal.y - 1, 18, 18);
        }

        super.drawSlot(graphics, slot);
    }

    public void setTab(Tab tab) {
        currentTab = tab;
        handler.tabSlots.forEach(slot -> slot.setEnabled(false));

        remove(inputAllowDeny);
        remove(outputAllowDeny);

        switch (tab) {
            case INPUT -> {
                handler.inputSlots.forEach(slot -> slot.setEnabled(true));
                addDrawableSelectableElement(inputAllowDeny);
            }
            case OUTPUT -> {
                handler.outputSlots.forEach(slot -> slot.setEnabled(true));
                addDrawableSelectableElement(outputAllowDeny);
            }
            case SIGNAL -> handler.signalSlots.forEach(slot -> slot.setEnabled(true));
        }
    }

    public enum Tab {
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
            setTooltip(Tooltip.create(text));
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
            setTab(tab);
        }

        boolean isSelected() {
            return currentTab == tab;
        }
    }

    private abstract class ButtonWidget extends PressableWidget {
        static final ClickableWidgetStateTextures TEXTURES =
            new ClickableWidgetStateTextures(id("widget/button"), id("widget/button_disabled"),
                id("widget/button_highlight"));

        public ButtonWidget(int x, int y, int width, int height, Text text) {
            super(x, y, width, height, text);
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
        protected void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            drawBackground(graphics);
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            int i = this.active ? 16777215 : 10526880;
            this.drawScrollableText(graphics, minecraftClient.textRenderer,
                i | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }

        protected void drawBackground(GuiGraphics graphics) {
            graphics.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            graphics.drawGuiTexture(TEXTURES.getTexture(this.active, this.isHoveredOrFocused()), this.getX(),
                this.getY(), this.getWidth(), this.getHeight());
            graphics.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @Override
        protected void updateNarration(NarrationMessageBuilder builder) {
            appendDefaultNarrations(builder);
        }
    }

    private abstract class IconButtonWidget extends ButtonWidget {
        private final int inset;

        public IconButtonWidget(int x, int y, int width, int height, Text text, int inset) {
            super(x, y, width, height, text);
            this.inset = inset;
        }

        protected abstract Identifier getIcon();

        @Override
        protected void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            drawBackground(graphics);
            graphics.drawGuiTexture(getIcon(), getX() + inset, getY() + inset, getWidth() - inset * 2,
                getHeight() - inset * 2);
        }
    }

    private class AllowDenyButtonWidget extends IconButtonWidget {
        private static final Identifier ALLOW = id("icon/check");
        private static final Identifier DENY = id("icon/deny");
        private static final Tooltip ALLOW_TOOLTIP = Tooltip.create(gui("allow_deny.allow"));
        private static final Tooltip DENY_TOOLTIP = Tooltip.create(gui("allow_deny.deny"));

        private final BooleanSupplier allow;
        private final BooleanConsumer setAllow;

        public AllowDenyButtonWidget(int x, int y, int width, int height, Text text, int inset, BooleanSupplier allow,
                                     BooleanConsumer setAllow) {
            super(x, y, width, height, text, inset);
            this.allow = allow;
            this.setAllow = setAllow;
        }

        @NotNull
        @Override
        public Tooltip getTooltip() {
            return allow.getAsBoolean() ? ALLOW_TOOLTIP : DENY_TOOLTIP;
        }

        @Override
        protected void updateNarration(NarrationMessageBuilder builder) {
            super.updateNarration(builder);
            getTooltip().appendNarrations(builder);
        }

        @Override
        protected void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            super.drawWidget(graphics, mouseX, mouseY, delta);
            getTooltip().method_54384(isHovered(), isFocused(), getArea());
        }

        @Override
        protected Identifier getIcon() {
            return allow.getAsBoolean() ? ALLOW : DENY;
        }

        @Override
        public void onPress() {
            setAllow.accept(!allow.getAsBoolean());
        }
    }
}

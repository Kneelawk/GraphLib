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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import com.kneelawk.transferbeams.screen.ItemNodeScreenHandler;

import static com.kneelawk.transferbeams.TransferBeamsMod.gui;
import static com.kneelawk.transferbeams.TransferBeamsMod.id;

public class ItemNodeScreen extends AbstractContainerScreen<ItemNodeScreenHandler> {
    private static final int WIDTH = 14 + 18 * 9;
    private static final int HEIGHT = 10 + 16 + 10 + 9 + 18 * 2 + 9 + 18 * 3 + 4 + 18;
    private static final ResourceLocation BACKGROUND = id("textures/gui/container/item_node.png");
    private static final ResourceLocation TAB = id("widget/tab");
    private static final ResourceLocation TAB_SELECTED = id("widget/tab_selected");
    private static final ResourceLocation TAB_HIGHLIGHT = id("widget/tab_highlight");
    private static final ResourceLocation TAB_SELECTED_HIGHLIGHT = id("widget/tab_selected_highlight");
    private static final ResourceLocation SLOT = id("widget/slot");
    private static final ResourceLocation SLOT_2 = id("widget/slot_2");
    private static final ResourceLocation INPUT = id("icon/input");
    private static final ResourceLocation OUTPUT = id("icon/output");
    private static final ResourceLocation SIGNAL = id("icon/signal");

    private final List<AbstractWidget> inputWidgets = new ObjectArrayList<>();
    private final List<AbstractWidget> outputWidgets = new ObjectArrayList<>();
    private final List<AbstractWidget> signalWidgets = new ObjectArrayList<>();

    private final TabButtonWidget inputTab = new TabButtonWidget(4, 0, 26, 26, gui("input"), INPUT, Tab.INPUT);
    private final TabButtonWidget outputTab =
        new TabButtonWidget(4 + 26 + 4, 0, 26, 26, gui("output"), OUTPUT, Tab.OUTPUT);
    private final TabButtonWidget signalTab =
        new TabButtonWidget(4 + 26 + 4 + 26 + 4, 0, 26, 26, gui("signal"), SIGNAL, Tab.SIGNAL);

    private final AllowDenyButtonWidget inputAllowDeny =
        new AllowDenyButtonWidget(5, 26 + 5 + 9 + 8, 20, 20, gui("allow_deny.input"), 2, menu::getInputAllow,
            menu::setInputAllow);
    private final AllowDenyButtonWidget outputAllowDeny =
        new AllowDenyButtonWidget(5, 26 + 5 + 9 + 8, 20, 20, gui("allow_deny.output"), 2, menu::getOutputAllow,
            menu::setOutputAllow);

    private Tab currentTab = Tab.INPUT;

    public ItemNodeScreen(ItemNodeScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 5;
        titleLabelY = 26 + 5;
        inventoryLabelX = 5;
        inventoryLabelY = 26 + 5 + 9 + 18 * 2;

        addRenderableWidget(inputTab);
        addRenderableWidget(outputTab);
        addRenderableWidget(signalTab);

        setTab(Tab.INPUT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos, 0);

        graphics.blit(BACKGROUND, 0, 26, 0, 0, imageWidth, imageHeight - 26);

        graphics.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFF2A2A2A, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF2A2A2A,
            false);
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        if (slot instanceof ItemNodeScreenHandler.FilterSlot filter) {
            graphics.blitSprite(SLOT_2, filter.x - 1, filter.y - 1, 18, 18);
        } else if (slot instanceof ItemNodeScreenHandler.SignalSlot signal) {
            graphics.blitSprite(SLOT, signal.x - 1, signal.y - 1, 18, 18);
        }

        super.renderSlot(graphics, slot);
    }

    public void setTab(Tab tab) {
        currentTab = tab;
        menu.tabSlots.forEach(slot -> slot.setEnabled(false));

        removeWidget(inputAllowDeny);
        removeWidget(outputAllowDeny);

        switch (tab) {
            case INPUT -> {
                menu.inputSlots.forEach(slot -> slot.setEnabled(true));
                addRenderableWidget(inputAllowDeny);
            }
            case OUTPUT -> {
                menu.outputSlots.forEach(slot -> slot.setEnabled(true));
                addRenderableWidget(outputAllowDeny);
            }
            case SIGNAL -> menu.signalSlots.forEach(slot -> slot.setEnabled(true));
        }
    }

    public enum Tab {
        INPUT,
        OUTPUT,
        SIGNAL
    }

    private class TabButtonWidget extends AbstractButton {
        private final ResourceLocation icon;
        private final Tab tab;

        public TabButtonWidget(int x, int y, int width, int height, Component text, ResourceLocation icon, Tab tab) {
            super(x, y, width, height, text);
            this.icon = icon;
            this.tab = tab;
            setTooltip(Tooltip.create(text));
        }

        @Override
        public int getX() {
            return super.getX() + ItemNodeScreen.this.leftPos;
        }

        @Override
        public int getY() {
            return super.getY() + ItemNodeScreen.this.topPos;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
            defaultButtonNarrationText(builder);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            ResourceLocation texture;
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

            graphics.blitSprite(texture, getX(), getY(), getWidth(), getHeight());
            graphics.blitSprite(icon, getX() + 5, getY() + 5, getWidth() - 10, getHeight() - 10);
        }

        @Override
        public void onPress() {
            setTab(tab);
        }

        boolean isSelected() {
            return currentTab == tab;
        }
    }

    private abstract class ButtonWidget extends AbstractButton {
        static final WidgetSprites TEXTURES =
            new WidgetSprites(id("widget/button"), id("widget/button_disabled"),
                id("widget/button_highlight"));

        public ButtonWidget(int x, int y, int width, int height, Component text) {
            super(x, y, width, height, text);
        }

        @Override
        public int getX() {
            return super.getX() + ItemNodeScreen.this.leftPos;
        }

        @Override
        public int getY() {
            return super.getY() + ItemNodeScreen.this.topPos;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            drawBackground(graphics);
            Minecraft minecraftClient = Minecraft.getInstance();
            int i = this.active ? 16777215 : 10526880;
            this.renderString(graphics, minecraftClient.font,
                i | Mth.ceil(this.alpha * 255.0F) << 24);
        }

        protected void drawBackground(GuiGraphics graphics) {
            graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            graphics.blitSprite(TEXTURES.get(this.active, this.isHoveredOrFocused()), this.getX(),
                this.getY(), this.getWidth(), this.getHeight());
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
            defaultButtonNarrationText(builder);
        }
    }

    private abstract class IconButtonWidget extends ButtonWidget {
        private final int inset;

        public IconButtonWidget(int x, int y, int width, int height, Component text, int inset) {
            super(x, y, width, height, text);
            this.inset = inset;
        }

        protected abstract ResourceLocation getIcon();

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            drawBackground(graphics);
            graphics.blitSprite(getIcon(), getX() + inset, getY() + inset, getWidth() - inset * 2,
                getHeight() - inset * 2);
        }
    }

    private class AllowDenyButtonWidget extends IconButtonWidget {
        private static final ResourceLocation ALLOW = id("icon/check");
        private static final ResourceLocation DENY = id("icon/deny");
        private static final Tooltip ALLOW_TOOLTIP = Tooltip.create(gui("allow_deny.allow"));
        private static final Tooltip DENY_TOOLTIP = Tooltip.create(gui("allow_deny.deny"));

        private final BooleanSupplier allow;
        private final BooleanConsumer setAllow;

        public AllowDenyButtonWidget(int x, int y, int width, int height, Component text, int inset, BooleanSupplier allow,
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
        protected void updateWidgetNarration(NarrationElementOutput builder) {
            super.updateWidgetNarration(builder);
            getTooltip().updateNarration(builder);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            super.renderWidget(graphics, mouseX, mouseY, delta);
            getTooltip().refreshTooltipForNextRenderPass(isHovered(), isFocused(), getRectangle());
        }

        @Override
        protected ResourceLocation getIcon() {
            return allow.getAsBoolean() ? ALLOW : DENY;
        }

        @Override
        public void onPress() {
            setAllow.accept(!allow.getAsBoolean());
        }
    }
}

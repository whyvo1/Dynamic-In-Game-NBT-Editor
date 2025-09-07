package com.whyvo.dine.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class CleanButtonWidget extends Button {

    public CleanButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
//        super.renderButton(context, mouseX, mouseY, delta);
        if(this.active && this.isHovered()) {
            context.fill(getX(), getY(), getX() + width, getY() + height, 0xFF444444);
        }
        this.renderString(context, Minecraft.getInstance().font, this.active ? 0xFFFFFFFF : 0xFFA0A0A0);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}

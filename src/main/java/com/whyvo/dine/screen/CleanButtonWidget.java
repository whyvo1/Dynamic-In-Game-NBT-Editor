package com.whyvo.dine.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class CleanButtonWidget extends ButtonWidget {

    public CleanButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
//        super.renderButton(context, mouseX, mouseY, delta);
        if(this.active && this.isHovered()) {
            context.fill(getX(), getY(), getX() + width, getY() + height, 0xFF444444);
        }
        this.drawMessage(context, MinecraftClient.getInstance().textRenderer, this.active ? 0xFFFFFFFF : 0xFFA0A0A0);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}

package com.whyvo.dine.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class MyEditBoxWidget extends MultiLineEditBox {
    public MyEditBoxWidget(Font textRenderer, int x, int y, int width, int height, Component placeholder, Component message) {
        super(textRenderer, x, y, width, height, placeholder, message);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.visible = false;
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        this.visible = true;
        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return keyCode != GLFW.GLFW_KEY_ENTER && super.keyPressed(keyCode, scanCode, modifiers);
    }
}

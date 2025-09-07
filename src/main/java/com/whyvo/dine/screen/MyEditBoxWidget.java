package com.whyvo.dine.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class MyEditBoxWidget extends MultiLineEditBox {
    public MyEditBoxWidget(Font textRenderer, int x, int y, int width, int height, Component placeholder, Component message) {
        super(textRenderer, x, y, width, height, placeholder, message, 0xFFE0E0E0, true, 0xFFD0D0D0, true, true);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return keyCode != GLFW.GLFW_KEY_ENTER && super.keyPressed(keyCode, scanCode, modifiers);
    }
}

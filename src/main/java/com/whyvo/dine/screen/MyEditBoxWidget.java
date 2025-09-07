package com.whyvo.dine.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class MyEditBoxWidget extends EditBoxWidget {
    public MyEditBoxWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text placeholder, Text message) {
        super(textRenderer, x, y, width, height, placeholder, message, 0xFFE0E0E0, true, 0xFFD0D0D0, true, true);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return keyCode != GLFW.GLFW_KEY_ENTER && super.keyPressed(keyCode, scanCode, modifiers);
    }
}

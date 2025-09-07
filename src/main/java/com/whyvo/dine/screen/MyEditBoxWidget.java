package com.whyvo.dine.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class MyEditBoxWidget extends EditBoxWidget {
    public MyEditBoxWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text placeholder, Text message) {
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

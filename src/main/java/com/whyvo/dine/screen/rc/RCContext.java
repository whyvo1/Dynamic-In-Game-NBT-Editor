package com.whyvo.dine.screen.rc;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class RCContext {

    private final int oX;
    private final int oY;

    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    private final List<RCButton> buttons;

    private int hoveredButton = -1;

    protected RCContext(int oX, int oY, int minX, int minY, int maxX, int maxY, List<RCButton> buttons) {
        this.oX = oX;
        this.oY = oY;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.buttons = buttons;
    }

    private int getButtonHeight() {
        return 20;
    }

    public void render(GuiGraphics context, int mouseX, int mouseY) {
        if(this.buttons.isEmpty()) {
            return;
        }
        int bHeight = getButtonHeight();
        int height  = bHeight * this.buttons.size();
        int startY = Math.max(this.minY, Math.min(this.maxY - height, this.oY));

        int m = 0;
        Font font = Minecraft.getInstance().font;
        for(RCButton button : this.buttons) {
            int n = font.width(button.getNameText());
            if(n > m) {
                m = n;
            }
        }
        int width = m + 10;
        int startX = Math.max(this.minX, Math.min(this.maxX - width, this.oX));

        context.fill(startX, startY, startX + width, startY + height, 0xFF000000);

        this.hoveredButton = -1;
        if(mouseX >= startX && mouseX < startX + width) {
            int i = (mouseY - startY) / bHeight;
            if(i >= 0 && i < this.buttons.size()) {
                if (this.buttons.get(i).active) {
                    int startY0 = startY + i * bHeight;
                    context.fill(startX, startY0, startX + width, startY0 + bHeight, 0xFF444444);
                }
                this.hoveredButton = i;
            }
        }

        int x = startX + 6;
        int y = startY + 5;
        for(RCButton button : this.buttons) {
            context.drawString(font, button.getNameText(), x, y, button.active ? 0xFFFFFFFF : 0xFF555555, false);
            y += bHeight;
        }
    }

    public boolean onMouseClicked(int mouseX, int mouseY) {
        if(this.hoveredButton < 0) return true;
        RCButton button = this.buttons.get(this.hoveredButton);
        return button.onClick();
    }

    public static Builder builder(int oX, int oY) {
        return new Builder(oX, oY);
    }

    public static class Builder {
        private final int oX;
        private final int oY;

        private int minX = 0;
        private int minY = 0;
        private int maxX = Integer.MAX_VALUE;
        private int maxY = Integer.MAX_VALUE;

        private final List<RCButton> buttons = new ArrayList<>();

        protected Builder(int oX, int oY) {
            this.oX = oX;
            this.oY = oY;
        }

        public Builder border(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            return this;
        }

        public Builder addButton(Component name, Runnable action) {
            this.buttons.add(new RCButton(name, action));
            return this;
        }

        public Builder addButtonInactive(Component name) {
            this.buttons.add(new RCButton(name));
            return this;
        }

        public RCContext build() {
            return new RCContext(oX, oY, minX, minY, maxX, maxY, buttons);
        }
    }
}

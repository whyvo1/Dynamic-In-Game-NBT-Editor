package com.whyvo.dine.screen;

import com.whyvo.dine.DINE;
import com.whyvo.dine.context.BScreenManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class DINEScreen extends Screen {
    private BScreenManager screens;

    private long ticks = 0L;
    private long lastClick = -8L;
    private double lastClickX = -10;
    private double lastClickY = 0;

    private EditBox shownTextField;
    private MultiLineEditBox shownEditBox;

    public DINEScreen() {
        super(Component.empty());
    }

    @Override
    public boolean isPauseScreen() {
        return this.screens.tryPauseGame;
    }

    @Override
    protected void init() {
        super.init();
        this.screens = DINE.SCREENS;
        this.screens.init(this);
    }

    @Override
    public void removed() {
        super.removed();
        this.screens.uninit();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if(this.screens.hasPopup()) {
            this.screens.render(context, mouseX, mouseY);
            super.render(context, mouseX, mouseY, delta);
            return;
        }
        super.render(context, mouseX, mouseY, delta);
        this.screens.render(context, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics context) {

    }

    @Override
    public void tick() {
        super.tick();
        this.ticks++;
//        for(Element element : this.children()) {
//            if(element instanceof TextFieldWidget widget) {
//                widget.tick();
//            }
//            if(element instanceof EditBoxWidget widget) {
//                widget.tick();
//            }
//        }
        if(this.shownTextField != null) {
            this.shownTextField.tick();
        }
        if(this.shownEditBox != null) {
            this.shownEditBox.tick();
        }

        this.screens.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(!super.keyPressed(keyCode, scanCode, modifiers)) {
            this.screens.keyPressed(keyCode, Screen.hasControlDown());
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return super.mouseScrolled(mouseX, mouseY, amount) || this.screens.mouseScrolled((int) mouseX, (int) mouseY, amount, amount, Screen.hasControlDown());
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button) || (this.shownEditBox != null && this.shownEditBox.mouseReleased(mouseX, mouseY, button));
    }

    private boolean handleDoubleClick(double mouseX, double mouseY) {
        if(ticks - lastClick >= 6L) return false;
        double dis = Math.sqrt(Math.pow(mouseX - lastClickX, 2) + Math.pow(mouseY - lastClickY, 2));
        return dis <= 4.0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int key = button == GLFW.GLFW_MOUSE_BUTTON_LAST ? 0 : button + 1;
        boolean result = super.mouseClicked(mouseX, mouseY, button) || this.screens.mouseClicked((int) mouseX, (int) mouseY, key);
        if(key == 1) {
            if(this.handleDoubleClick(mouseX, mouseY)) {
                this.doubleClick(mouseX, mouseY);
                lastClick = ticks - 8L;
            }
            else {
                lastClick = ticks;
            }
            lastClickX = mouseX;
            lastClickY = mouseY;
        }
        return result;
    }

    public void doubleClick(double mouseX, double mouseY) {
        this.screens.doubleClick((int) mouseX, (int) mouseY);
    }

    @Override
    public @Nullable GuiEventListener getFocused() {
        return this.screens.hasPopup() ? super.getFocused() : null;
    }

    @Override
    public void clearWidgets() {
        this.shownTextField = null;
        this.shownEditBox = null;
        super.clearWidgets();
        this.setFocused(null);
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T drawableElement) {
        if(drawableElement instanceof EditBox) {
            this.shownTextField = (EditBox)drawableElement;
        }
        if(drawableElement instanceof MultiLineEditBox) {
            this.shownEditBox = (MultiLineEditBox)drawableElement;
        }
        return super.addRenderableWidget(drawableElement);
    }
}

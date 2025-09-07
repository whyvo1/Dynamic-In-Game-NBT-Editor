package com.whyvo.dine.screen.popup;

import com.whyvo.dine.context.BScreen;
import com.whyvo.dine.context.BScreenManager;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public abstract class Popup {
    protected final int centerX;
    protected final int centerY;
    protected final BScreen parent;

    public Popup(BScreen parent) {
        BScreenManager manager = parent.getManager();
        this.centerX = manager.getWidth() / 2;
        this.centerY = manager.getTopHeight() + 1 + manager.getScreenHeight() / 2;
        this.parent = parent;
    }

    public void init() {
        this.parent.getBINE().clearWidgets();
        this.parent.getBINE().addRenderableWidget(getTitle());
    }

    public void uninit() {
        this.parent.getBINE().clearWidgets();
    }

    public abstract StringWidget getTitle();

    public void onEnter() {

    }

    protected final <T extends GuiEventListener & Renderable & NarratableEntry> void addDrawableChild(T drawableElement) {
        this.parent.getBINE().addRenderableWidget(drawableElement);
    }

}

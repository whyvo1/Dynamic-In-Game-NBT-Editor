package com.whyvo.dine.screen.popup;

import com.whyvo.dine.context.BScreen;
import com.whyvo.dine.context.BScreenManager;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.TextWidget;

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
        this.parent.getBINE().clearChildren();
        this.parent.getBINE().addDrawableChild(getTitle());
    }

    public void uninit() {
        this.parent.getBINE().clearChildren();
    }

    public abstract TextWidget getTitle();

    public void onEnter() {

    }

    protected final <T extends Element & Drawable & Selectable> void addDrawableChild(T drawableElement) {
        this.parent.getBINE().addDrawableChild(drawableElement);
    }

}

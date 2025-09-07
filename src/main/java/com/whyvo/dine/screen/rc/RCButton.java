package com.whyvo.dine.screen.rc;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RCButton {
    private final Component name;
    @Nullable
    private final Runnable action;
    public final boolean active;
    
    public RCButton(Component name, @NotNull Runnable action) {
        this.name = name;
        this.action = action;
        this.active = true;
    }

    public RCButton(Component name) {
        this.name = name;
        this.action = null;
        this.active = false;
    }

    public boolean onClick() {
        if (this.action != null) {
            this.action.run();
            return true;
        }
        return false;
    }

    public Component getNameText() {
        return this.name;
    }
    
}

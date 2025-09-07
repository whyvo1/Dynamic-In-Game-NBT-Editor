package com.whyvo.dine.screen.rc;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RCButton {
    private final Text name;
    @Nullable
    private final Runnable action;
    public final boolean active;
    
    public RCButton(Text name, @NotNull Runnable action) {
        this.name = name;
        this.action = action;
        this.active = true;
    }

    public RCButton(Text name) {
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

    public Text getNameText() {
        return this.name;
    }
    
}

package com.whyvo.dine.mixin;

import com.whyvo.dine.screen.HorizontalScrollable;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class MouseMixin {

    @Redirect(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDD)Z"))
    private boolean redirectOnMouseScroll(Screen instance, double mouseX, double mouseY, double amount, long window, double horizontal, double vertical) {
        if(instance instanceof HorizontalScrollable hs) {
            return hs.mouseScrolled(mouseX, mouseY, horizontal, vertical);
        }
        return instance.mouseScrolled(mouseX, mouseY, amount);
    }
}

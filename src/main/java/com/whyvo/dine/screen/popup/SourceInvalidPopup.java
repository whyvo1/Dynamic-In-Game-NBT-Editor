package com.whyvo.dine.screen.popup;

import com.whyvo.dine.context.BScreen;
import com.whyvo.dine.screen.CleanButtonWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class SourceInvalidPopup extends Popup {
    private static final Component TITLE = Component.translatable("dine.popup.source_invalid").withStyle(ChatFormatting.BOLD);
    private static final Component RETRY = Component.translatable("dine.popup.source_invalid.retry");
    private static final Component CLOSE = Component.translatable("dine.popup.source_invalid.close");

    private final CleanButtonWidget retryButton;
    private final CleanButtonWidget closeButton;

    public SourceInvalidPopup(BScreen parent) {
        super(parent);

        this.retryButton = new CleanButtonWidget(centerX - 130, centerY + 30, 100, 30, RETRY, button -> {
            if(this.parent.updateAndTestSource()) {
                this.parent.setPopup(null);
            }
        });

        this.closeButton = new CleanButtonWidget(centerX + 30, centerY + 30, 100, 30, CLOSE, button -> {
            this.parent.markClosed();
        });
    }

    @Override
    public void init() {
        super.init();
        this.addDrawableChild(retryButton);
        this.addDrawableChild(closeButton);
    }

    @Override
    public StringWidget getTitle() {
        Font font = Minecraft.getInstance().font;
        int width = font.width(TITLE);
        return new StringWidget(this.centerX - width / 2, this.centerY - 50, width, 30, TITLE, font);
    }
}

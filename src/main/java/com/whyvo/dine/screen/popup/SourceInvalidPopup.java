package com.whyvo.dine.screen.popup;

import com.whyvo.dine.context.BScreen;
import com.whyvo.dine.screen.CleanButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SourceInvalidPopup extends Popup {
    private static final Text TITLE = Text.translatable("dine.popup.source_invalid").formatted(Formatting.BOLD);
    private static final Text RETRY = Text.translatable("dine.popup.source_invalid.retry");
    private static final Text CLOSE = Text.translatable("dine.popup.source_invalid.close");

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
    public TextWidget getTitle() {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        int width = font.getWidth(TITLE);
        return new TextWidget(this.centerX - width / 2, this.centerY - 50, width, 30, TITLE, font);
    }
}

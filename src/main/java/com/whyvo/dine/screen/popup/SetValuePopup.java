package com.whyvo.dine.screen.popup;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.whyvo.dine.context.BScreen;
import com.whyvo.dine.context.nbt.NPath;
import com.whyvo.dine.screen.CleanButtonWidget;
import com.whyvo.dine.screen.MyEditBoxWidget;
import com.whyvo.dine.util.Compat1215;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SetValuePopup extends Popup {
    private static final Text APPLY = Text.translatable("dine.popup.set_value.apply");
    private static final Text CANCEL = Text.translatable("dine.popup.set_value.cancel");
    private static final Text KEY = Text.translatable("dine.popup.set_value.key");
    private static final Text INDEX = Text.translatable("dine.popup.set_value.index");
    private static final Text VALUE = Text.translatable("dine.popup.set_value.value");
    private static final Text ERROR_EMPTY_KEY = Text.translatable("dine.popup.set_value.error_empty_key").formatted(Formatting.RED);
    private static final Text ERROR_NEGATIVE_INDEX = Text.translatable("dine.popup.set_value.error_negative_index").formatted(Formatting.RED);
    private static final Text ERROR_PARSE_NBT = Text.translatable("dine.popup.set_value.error_parse_nbt").formatted(Formatting.RED);

    private final Text title;
    private final ClickAction action;

    private final boolean sElseI;
    private final NPath path;
    private final String osKey;
    private final int oiKey;
    private String sKey;
    private int iKey;
    private String value;

    private final CleanButtonWidget applyButton;
    private final CleanButtonWidget cancelButton;

    private final TextWidget errorEmptyKey;
    private final TextWidget errorNegativeIndex;
    private final TextWidget errorParseNbt;

    public SetValuePopup(BScreen parent, NPath path, String value, MutableText title, ClickAction action) {
        super(parent);

        this.title = title.formatted(Formatting.BOLD);
        this.action = action;

        NPath.Next next = path.current();
        if(next instanceof NPath.Child(String name)) {
            this.sElseI = true;
            this.osKey = name;
            this.oiKey = -1;
            this.sKey = this.osKey;
            this.iKey = -1;
        }
        else {
            NPath.Ele ele = (NPath.Ele)next;
            this.sElseI = false;
            this.osKey = null;
            this.oiKey = ele.index();
            this.sKey = null;
            this.iKey = this.oiKey;
        }
        this.path = path;
        this.value = value;

        this.applyButton = new CleanButtonWidget(centerX - 130, centerY + 75, 100, 25, APPLY, button -> {
            this.onApply();
        });

        this.cancelButton = new CleanButtonWidget(centerX + 30, centerY + 75, 100, 25, CANCEL, button -> {
            this.parent.setPopup(null);
        });

        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        int width = font.getWidth(ERROR_EMPTY_KEY);
        this.errorEmptyKey = new TextWidget(centerX - 80 - width / 2, centerY + 60, width, 15, ERROR_EMPTY_KEY, font);
        this.errorEmptyKey.visible = false;
        width = font.getWidth(ERROR_NEGATIVE_INDEX);
        this.errorNegativeIndex = new TextWidget(centerX - 80 - width / 2, centerY + 60, width, 15, ERROR_NEGATIVE_INDEX, font);
        this.errorNegativeIndex.visible = false;
        width = font.getWidth(ERROR_PARSE_NBT);
        this.errorParseNbt = new TextWidget(centerX - 80 - width / 2, centerY + 60, width, 15, ERROR_PARSE_NBT, font);
        this.errorParseNbt.visible = false;
    }

    private void onApply() {
        if(this.sElseI) {
            if(this.sKey.isEmpty()) {
                this.addErrorInfo(this.errorEmptyKey);
                return;
            }
        }
        else {
            if (this.iKey < 0) {
                this.addErrorInfo(this.errorNegativeIndex);
                return;
            }
        }
        NbtElement nbt = tryParseNbt(this.value);
        if(nbt != null) {
            NPath newPath = this.sElseI ? this.path.parent().resolve(this.sKey) : this.path.parent().resolve(this.iKey);
            this.action.action(nbt, newPath);
            this.parent.setPopup(null);
        }
        else {
            this.addErrorInfo(this.errorParseNbt);
        }
    }

    private static NbtElement tryParseNbt(String string) {
        try {
//            return new StringNbtReader(new StringReader(string)).parseElement();
            return Compat1215.parseNbt(new StringReader(string));
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    private void addErrorInfo(TextWidget text) {
        this.errorEmptyKey.visible = false;
        this.errorNegativeIndex.visible = false;
        this.errorParseNbt.visible = false;
        text.visible = true;
    }

    @Override
    public void init() {
        super.init();
        TextRenderer font = MinecraftClient.getInstance().textRenderer;

        Text first = sElseI ? KEY : INDEX;
        int width = font.getWidth(first);
        this.addDrawableChild(new TextWidget(this.centerX - 150, this.centerY - 67, width, 20, first, font));
        width = font.getWidth(VALUE);
        this.addDrawableChild(new TextWidget(this.centerX - 150, this.centerY - 32, width, 20, VALUE, font));

        TextFieldWidget keyInput = new TextFieldWidget(font, this.centerX - 80, this.centerY - 70, 230, 20, Text.empty());
        keyInput.setMaxLength(128);
        keyInput.setText(sElseI ? sKey : String.valueOf(iKey));
//        if(!sElseI) {
//            keyInput.setTextPredicate(s -> {
//                try {
//                    Integer.parseInt(s);
//                    return true;
//                } catch (NumberFormatException e) {
//                    return false;
//                }
//            });
//        }
        keyInput.setChangedListener(sElseI ? ((text) -> this.sKey = text) :
                ((text) -> {
                    try {
                        this.iKey = Integer.parseInt(text);
                    } catch (NumberFormatException e) {
                        this.iKey = -1;
                    }
                }));
        this.addDrawableChild(keyInput);

        MyEditBoxWidget valueInput = new MyEditBoxWidget(font, this.centerX - 80, this.centerY - 35, 230, 90, Text.empty(), Text.empty());
        valueInput.setMaxLength(4096);
        valueInput.setText(value);
        valueInput.setChangeListener((text) -> this.value = text);
        this.addDrawableChild(valueInput);

        this.addDrawableChild(applyButton);
        this.addDrawableChild(cancelButton);

        this.addDrawableChild(errorEmptyKey);
        this.addDrawableChild(errorNegativeIndex);
        this.addDrawableChild(errorParseNbt);
    }

    @Override
    public TextWidget getTitle() {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        int width = font.getWidth(this.title);
        return new TextWidget(this.centerX - width / 2, this.centerY - 105, width, 30, this.title, font);
    }

    @Override
    public void onEnter() {
        this.onApply();
    }

    public interface ClickAction {
        void action(NbtElement nbt, NPath newPath);
    }
}

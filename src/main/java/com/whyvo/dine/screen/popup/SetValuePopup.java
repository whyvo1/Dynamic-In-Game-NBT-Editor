package com.whyvo.dine.screen.popup;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.whyvo.dine.context.BScreen;
import com.whyvo.dine.context.nbt.NPath;
import com.whyvo.dine.screen.CleanButtonWidget;
import com.whyvo.dine.screen.MyEditBoxWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SetValuePopup extends Popup {
    private static final Component APPLY = Component.translatable("dine.popup.set_value.apply");
    private static final Component CANCEL = Component.translatable("dine.popup.set_value.cancel");
    private static final Component KEY = Component.translatable("dine.popup.set_value.key");
    private static final Component INDEX = Component.translatable("dine.popup.set_value.index");
    private static final Component VALUE = Component.translatable("dine.popup.set_value.value");
    private static final Component ERROR_EMPTY_KEY = Component.translatable("dine.popup.set_value.error_empty_key").withStyle(ChatFormatting.RED);
    private static final Component ERROR_NEGATIVE_INDEX = Component.translatable("dine.popup.set_value.error_negative_index").withStyle(ChatFormatting.RED);
    private static final Component ERROR_PARSE_NBT = Component.translatable("dine.popup.set_value.error_parse_nbt").withStyle(ChatFormatting.RED);

    private static final TagParser<Tag> NBT_READER = TagParser.create(NbtOps.INSTANCE);

    private final Component title;
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

    private final StringWidget errorEmptyKey;
    private final StringWidget errorNegativeIndex;
    private final StringWidget errorParseNbt;

    public SetValuePopup(BScreen parent, NPath path, String value, MutableComponent title, ClickAction action) {
        super(parent);

        this.title = title.withStyle(ChatFormatting.BOLD);
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

        Font font = Minecraft.getInstance().font;
        int width = font.width(ERROR_EMPTY_KEY);
        this.errorEmptyKey = new StringWidget(centerX - 80 - width / 2, centerY + 60, width, 15, ERROR_EMPTY_KEY, font);
        this.errorEmptyKey.visible = false;
        width = font.width(ERROR_NEGATIVE_INDEX);
        this.errorNegativeIndex = new StringWidget(centerX - 80 - width / 2, centerY + 60, width, 15, ERROR_NEGATIVE_INDEX, font);
        this.errorNegativeIndex.visible = false;
        width = font.width(ERROR_PARSE_NBT);
        this.errorParseNbt = new StringWidget(centerX - 80 - width / 2, centerY + 60, width, 15, ERROR_PARSE_NBT, font);
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
        Tag nbt = tryParseNbt(this.value);
        if(nbt != null) {
            NPath newPath = this.sElseI ? this.path.parent().resolve(this.sKey) : this.path.parent().resolve(this.iKey);
            this.action.action(nbt, newPath);
            this.parent.setPopup(null);
        }
        else {
            this.addErrorInfo(this.errorParseNbt);
        }
    }

    private static Tag tryParseNbt(String string) {
        try {
            return NBT_READER.parseAsArgument(new StringReader(string));
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    private void addErrorInfo(StringWidget text) {
        this.errorEmptyKey.visible = false;
        this.errorNegativeIndex.visible = false;
        this.errorParseNbt.visible = false;
        text.visible = true;
    }

    @Override
    public void init() {
        super.init();
        Font font = Minecraft.getInstance().font;

        Component first = sElseI ? KEY : INDEX;
        int width = font.width(first);
        this.addDrawableChild(new StringWidget(this.centerX - 150, this.centerY - 67, width, 20, first, font));
        width = font.width(VALUE);
        this.addDrawableChild(new StringWidget(this.centerX - 150, this.centerY - 32, width, 20, VALUE, font));

        EditBox keyInput = new EditBox(font, this.centerX - 80, this.centerY - 70, 230, 20, Component.empty());
        keyInput.setMaxLength(128);
        keyInput.setValue(sElseI ? sKey : String.valueOf(iKey));
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
        keyInput.setResponder(sElseI ? ((text) -> this.sKey = text) :
                ((text) -> {
                    try {
                        this.iKey = Integer.parseInt(text);
                    } catch (NumberFormatException e) {
                        this.iKey = -1;
                    }
                }));
        this.addDrawableChild(keyInput);

        MyEditBoxWidget valueInput = new MyEditBoxWidget(font, this.centerX - 80, this.centerY - 35, 230, 90, Component.empty(), Component.empty());
        valueInput.setCharacterLimit(4096);
        valueInput.setValue(value);
        valueInput.setValueListener((text) -> this.value = text);
        this.addDrawableChild(valueInput);

        this.addDrawableChild(applyButton);
        this.addDrawableChild(cancelButton);

        this.addDrawableChild(errorEmptyKey);
        this.addDrawableChild(errorNegativeIndex);
        this.addDrawableChild(errorParseNbt);
    }

    @Override
    public StringWidget getTitle() {
        Font font = Minecraft.getInstance().font;
        int width = font.width(this.title);
        return new StringWidget(this.centerX - width / 2, this.centerY - 105, width, 30, this.title, font);
    }

    @Override
    public void onEnter() {
        this.onApply();
    }

    public interface ClickAction {
        void action(Tag nbt, NPath newPath);
    }
}

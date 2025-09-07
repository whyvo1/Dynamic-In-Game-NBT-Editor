package com.whyvo.dine.context.nbt;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Node {

    private static final int MAX_RENDER_INFO_SIZE = 512;

    private static final MutableText COLON = Text.literal(" : ").formatted(Formatting.RESET);

    public final NPath path;

    public final NodeType type;

    @Nullable
    public final String name;
    @NotNull
    public final String info;
    @NotNull
    private final String renderInfo;

    public final int indent;

    public final boolean foldable;
    public final boolean folded;

    public boolean selected = false;

    public Node(NPath path, NodeType type, @Nullable String name, @NotNull String info, int indent, boolean folded) {
        this.path = path;
        this.type = type;
        this.name = name;
        this.info = info;
        if(info.length() > MAX_RENDER_INFO_SIZE) {
            this.renderInfo = info.substring(0, MAX_RENDER_INFO_SIZE);
        }
        else {
            this.renderInfo = info;
        }
        this.indent = indent;
        this.foldable = indent > 0;  // not foldable as root node
        this.folded = folded;
    }

    public Node(NPath path, NodeType type, @Nullable String name, @NotNull String info, int indent) {
        this.path = path;
        this.type = type;
        this.name = name;

        this.info = info;
        if(info.length() >= MAX_RENDER_INFO_SIZE) {
            this.renderInfo = info.substring(0, MAX_RENDER_INFO_SIZE);
        }
        else {
            this.renderInfo = info;
        }
        this.indent = indent;
        this.foldable = false;
        this.folded = false;
    }

    private Text[] getTexts() {
        MutableText textValue = Text.literal(this.renderInfo);
        if(this.foldable || this.indent == 0) {
            textValue.formatted(Formatting.GRAY);
        }
        if(this.name == null) {
            return new Text[] { textValue };
        }
        else {
            return new Text[] { Text.literal(this.name).formatted(Formatting.BOLD), COLON, textValue };
        }
    }

    public int render(DrawContext context, TextRenderer font, int iWidth, int startY) {
        int startX = iWidth * indent + 6;
//        Text text = this.name == null ? Text.literal(this.extra) : keyAndValue(this.name, this.extra);
        Text[] texts = getTexts();
        int i = 0;
        for(Text text : texts) {
            i += font.getWidth(text);
        }
        if(this.selected) {
            context.fill(startX, startY, startX + i + 4, startY + 11, 0xFF4C5863);
        }
        startX += 2;
        for(Text text : texts) {
            context.drawText(font, text, startX, startY + 2, 0xFFFFFFFF, false);
            startX += font.getWidth(text) + 1;
        }
        return startX;
    }
}

package com.whyvo.dine.context;

import com.whyvo.dine.context.source.NbtSource;
import com.whyvo.dine.context.nbt.*;
import com.whyvo.dine.screen.DINEScreen;
import com.whyvo.dine.screen.popup.Popup;
import com.whyvo.dine.screen.popup.SetValuePopup;
import com.whyvo.dine.screen.popup.SourceInvalidPopup;
import com.whyvo.dine.screen.rc.RCContext;
import com.whyvo.dine.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BScreen {

    private static final Text LOADING = Text.translatable("dine.screen.loading");
    private static final MutableText TITLE_EDIT_NODE = Text.translatable("dine.popup.set_value");
    private static final MutableText TITLE_ADD_NODE = Text.translatable("dine.popup.add_value");
    private static final Text EDIT_NODE = Text.translatable("dine.rc.set_value");
    private static final Text ADD_NODE = Text.translatable("dine.rc.add_value");
    private static final Text REMOVE_NODE = Text.translatable("dine.rc.remove_value");
    private static final Text COPY_PATH_TO_CLIPBOARD = Text.translatable("dine.rc.copy_path_to_clipboard");
    private static final Text COPY_VALUE_TO_CLIPBOARD = Text.translatable("dine.rc.copy_value_to_clipboard");
    private static final Text COPY = Text.translatable("dine.rc.copy");
    private static final Text CUT = Text.translatable("dine.rc.cut");
    private static final Text PASTE_VALUE = Text.translatable("dine.rc.paste_value");
    private static final Text PASTE_NODE = Text.translatable("dine.rc.paste_node");
    private static final Text PASTE_BEFORE = Text.translatable("dine.rc.paste_before");
    private static final Text SET_FROM_SERVER = Text.translatable("dine.rc.set_from_server");
    private static final Text SET_NOT_FROM_SERVER = Text.translatable("dine.rc.set_not_from_server");

    private static final String LIST_FOLD = "[ ... ]";
    private static final String LIST_UNFOLD = "[";
    private static final String COMPOUND_FOLD = "{ ... }";
    private static final String COMPOUND_UNFOLD = "{";

    private NPath selectedPath = null;
    private int selectedIndex = -1;

    private int hoveredIndex = -1;

    private double scrollAmount = 0.0;

    private List<Node> nodes;

    private List<NPath> unfoldedCPaths;
    private List<NPath> unfoldedLPaths;

    private List<NPath> unfoldedNCPaths;
    private List<NPath> unfoldedNLPaths;

    private final NbtSource source;
    public boolean fromServer;
    private String cachedTitle = null;

    private NbtCompound currentNbt;

    private final BScreenManager manager;

    private boolean sourceInvalid = false;
    private boolean close = false;

    private Popup currentPopup = null;

    public BScreen(NbtSource source, BScreenManager manager) {
        this.source = source;
        this.fromServer = source.defaultFromServer();
        this.manager = manager;
    }

    public BScreenManager getManager() {
        return this.manager;
    }

    public NbtSource getSource() {
        return this.source;
    }

    public boolean sameSourceOf(NbtSource another) {
        return this.source.isSame(another);
    }

    public String getTitle() {
        return this.cachedTitle == null ? this.source.getTitle() : this.cachedTitle;
    }

    public void setSelected(int index) {
        if(index < 0) {
            this.selectedIndex = -1;
            this.selectedPath = null;
            return;
        }
        Node node = this.nodes.get(index);
        this.selectedIndex = index;
        this.selectedPath = node.path;
        node.selected = true;
        this.handleScrollAmount(index);
    }

    public void foldOrUnfold(Node node) {
        if (!node.foldable) {
            return;
        }
        List<NPath> ps = node.type == NodeType.COMPOUND ? unfoldedCPaths : unfoldedLPaths;
        if (node.folded) {
            ps.add(node.path);
        }
        else {
            ps.remove(node.path);
        }
    }

    public boolean updateAndTestSource() {
        this.source.tick(this.fromServer);
        this.currentNbt = this.source.getSource();
        this.sourceInvalid = this.currentNbt == null;
        return !this.sourceInvalid;
    }

    public void markClosed() {
        this.close = true;
    }

    public boolean closed() {
        return this.close;
    }

    public void onShown() {
        if(this.currentPopup != null) {
            this.currentPopup.init();
        }
    }

    public void onHidden() {
        if(this.currentPopup != null) {
            this.currentPopup.uninit();
        }
    }

    public void setPopup(Popup popup) {
        if(this.currentPopup != null) {
            this.currentPopup.uninit();
        }
        this.currentPopup = popup;
        if(this.currentPopup != null) {
            manager.setRCContext(null);
            this.currentPopup.init();
        }
    }

    public boolean hasPopup() {
        return this.currentPopup != null;
    }

    public DINEScreen getBINE() {
        return manager.bineScreen;
    }

    public void tick() {
        if(this.sourceInvalid) {
            return;
        }
        this.source.tick(this.fromServer);
        NbtCompound nbt = this.source.getSource();
        if(nbt != null) {
            this.currentNbt = nbt;
            this.cachedTitle = this.source.getTitle();

            if(unfoldedCPaths == null) {
                unfoldedCPaths = new ArrayList<>();
                unfoldedCPaths.add(NPath.ROOT);
            }
            if(unfoldedLPaths == null) {
                unfoldedLPaths = new ArrayList<>();
            }
            unfoldedNCPaths = new ArrayList<>();
            unfoldedNLPaths = new ArrayList<>();
            this.nodes = new ArrayList<>();
            this.selectedIndex = -1;
            handleNodes(nbt, NPath.ROOT, 0);
            if(this.selectedIndex >= 0) {
                Node sn = this.nodes.get(this.selectedIndex);
                this.selectedPath = sn.path;
                sn.selected = true;
            }
            else {
                this.selectedIndex = -1;
                this.selectedPath = null;
            }
            this.unfoldedCPaths = this.unfoldedNCPaths;
            this.unfoldedLPaths = this.unfoldedNLPaths;
            return;
        }
        this.sourceInvalid = true;
        this.setPopup(new SourceInvalidPopup(this));
    }


    private void handleNodes(NbtElement nbt, NPath path, int indent) {
        if(path.equals(selectedPath)) {
            selectedIndex = this.nodes.size();
        }
        if(nbt instanceof NbtList list) {
            boolean unfolded = unfoldedLPaths.contains(path);
            this.nodes.add(new Node(path, NodeType.LIST, path.currentKey(), unfolded ? LIST_UNFOLD : LIST_FOLD, indent, !unfolded));
            if(unfolded) {
                unfoldedNLPaths.add(path);
                for (int i = 0; i < list.size(); i++) {
                    handleNodes(list.get(i), path.resolve(i), indent + 1);
                }
            }
        }
        else if(nbt instanceof NbtCompound compound) {
            boolean unfolded = unfoldedCPaths.contains(path);
            this.nodes.add(new Node(path, NodeType.COMPOUND, path.currentKey(), unfolded ? COMPOUND_UNFOLD : COMPOUND_FOLD, indent, !unfolded));
            if(unfolded) {
                unfoldedNCPaths.add(path);
                //  only read, no modification
                Map<String, NbtElement> map = compound.entries;
                for (Map.Entry<String, NbtElement> entry : map.entrySet()) {
                    handleNodes(entry.getValue(), path.resolve(entry.getKey()), indent + 1);
                }
            }
        }
        else {
            this.nodes.add(new Node(path, NodeType.PRIMITIVE, path.currentKey(), nbt.toString(), indent));
        }
    }

    private int getIndentWidth(int width) {
        return 15;
    }

    private int getMaxSize() {
        return this.nodes.size() + 2;
    }

    private int handleScrollAmountForSize() {
        int n = manager.getScreenHeight() / getNodeHeight();
        int s = getMaxSize();
        if(s <= n) {
            this.scrollAmount = 0.0;
            return s;
        }
        if(this.scrollAmount < 0.0) {
            this.scrollAmount = 0.0;
        }
        if(this.scrollAmount + n > s) {
            this.scrollAmount = s - n;
        }
        return n;
    }

    private void handleScrollAmount(int expect) {
        int n = manager.getScreenHeight() / getNodeHeight();
        if(expect < this.scrollAmount) {
            this.scrollAmount = expect;
            int s = getMaxSize();
            if(this.scrollAmount + n > s) {
                this.scrollAmount = s - n;
            }
        }
        else if(expect > this.scrollAmount + n - 1) {
            this.scrollAmount = expect -n + 1;
        }
    }

    private int getNodeHeight() {
        return 14;
    }

    // height is the whole screen's height
    public void render(DrawContext context, int mouseX, int mouseY) {
        int width = manager.getWidth();
        int height = manager.getScreenHeight();
        int startY = manager.getTopHeight() + 1;

        TextRenderer font = MinecraftClient.getInstance().textRenderer;

        this.hoveredIndex = -1;

        if(this.nodes == null) {
            Util.drawCenteredText(context, font, LOADING, width / 2, startY + height / 2, 0xFFFFFFFF);
            return;
        }

        int nHeight = getNodeHeight();
        int iWidth = getIndentWidth(width - 12);

        int n = this.handleScrollAmountForSize();

        startY += 1;

        HoverInfo hi = null;
        int s = this.nodes.size();
        for(int i = 0; i < n; i ++) {
            if(i + this.scrollAmount >= s) {
                break;
            }
            Node node = this.nodes.get((int) (i + this.scrollAmount));
            boolean hovered = mouseY >= startY - 1 && mouseY < startY + nHeight - 1;
            if(node.foldable) {
                int startX = iWidth * node.indent - 6;
//                boolean hovered = mouseX >= startX && mouseX < startX + nHeight && mouseY >= startY && mouseY < startY + nHeight;
                int color = hovered && mouseX >= startX && mouseX < startX + 10 ?
                        0xFFFFFFFF : 0x99FFFFFF;

                // draw a horizontal line
                context.fill(startX + 3, startY + 5, startX + 8, startY + 6, color);
                if(node.folded) {
                    // draw a vertical line
                    context.fill(startX + 5, startY + 3, startX + 6, startY + 8, color);
                }
            }
            if(hovered) {
                this.hoveredIndex = i;
            }
            int r = node.render(context, font, iWidth, startY);
            if(hovered && r >= width - 4) {
                hi = new HoverInfo(node.info, i);
            }
            startY += nHeight;
        }

        if(this.currentPopup != null) {
            int startX0 = width / 5;
            int startY0 = manager.getTopHeight() + 1 + height / 6;
//            int endX0 = startX0 + width / 2;
//            int endY0 = startY0 + height / 2;
            context.fill(startX0, startY0, startX0 + width * 3 / 5, startY0 + height * 2 / 3, 0xFF000000);
        }
        else {
            if(hi != null) {
                int ow = (width - 8) * 3 / 4;
                List<OrderedText> lines = Language.getInstance().reorder(font.getTextHandler().wrapLines(hi.info, ow, Style.EMPTY));
                int startX0 = width - ow - 4;
                int startY0 = manager.getTopHeight() + hi.index * nHeight + nHeight - 1;
                context.fill(startX0,
                        startY0,
                        startX0 + ow,
                        startY0 + lines.size() * 10 + 2,
                        0xFF2C2C2C);
                startX0 += 2;
                startY0 += 2;
                for(OrderedText line : lines) {
                    context.drawText(font, line, startX0, startY0, 0xFFFFFFFF, false);
                    startY0 += 10;
                }
            }
        }

    }

    public void onMouseScroll(double amount, boolean ctrlDown) {
        if(this.currentPopup != null) {
            return;
        }
        this.scrollAmount -= amount * (ctrlDown ? 8 : 2);
    }

    public void onMouseClicked(int mouseX, int mouseY, int key) {
        if(this.currentPopup != null) return;
        if(this.nodes == null || this.hoveredIndex < 0) {
            if(key == 2) {
                this.manager.setRCContext(RCContext.builder(mouseX + 1, mouseY + 1)
                        .border(5, 1, manager.getWidth() - 4, manager.bineScreen.height)
                        .addButton(this.fromServer ? SET_NOT_FROM_SERVER : SET_FROM_SERVER, () -> this.fromServer = !this.fromServer)
                        .build());
            }
            return;
        }

        int i = (int) (this.hoveredIndex + this.scrollAmount);
        if(i < 0 || i >= this.nodes.size()) {
            if(key == 2) {
                this.manager.setRCContext(RCContext.builder(mouseX + 1, mouseY + 1)
                        .border(5, 1, manager.getWidth() - 4, manager.bineScreen.height)
                        .addButton(this.fromServer ? SET_NOT_FROM_SERVER : SET_FROM_SERVER, () -> this.fromServer = !this.fromServer)
                        .build());
            }
            return;
        }

        Node node = this.nodes.get(i);
        if (key == 1 || key == 2) {
            boolean bl = true;
            if (node.foldable) {
                int startX = getIndentWidth(manager.getWidth() - 12) * node.indent - 6;
                if (mouseX >= startX && mouseX < startX + 10) {
                    this.foldOrUnfold(node);
                    bl = false;
                }
            }
            if (bl) {
                this.setSelected(i);

                if (key == 2) {
                    RCContext.Builder builder = RCContext.builder(mouseX + 1, mouseY + 1)
                            .border(5, 1, manager.getWidth() - 4, manager.bineScreen.height)
                            .addButton(EDIT_NODE, () -> this.setEditNodePopup(node.path, this.getNbt(node.path)));

                    NodeType type = node.type;
                    if (type == NodeType.COMPOUND) {
                        builder.addButton(ADD_NODE, () -> this.setAddNodePopup(node.path));
                    }
                    else if (type == NodeType.LIST) {
                        builder.addButton(ADD_NODE, () -> {
                            NbtElement nbt = this.getNbt(node.path);
                            if(nbt instanceof NbtList nbtList) {
                                this.setAddNodePopup(node.path, nbtList.size());
                            }
                        });
                    }
                    if (node.indent > 0) {
                        builder.addButton(REMOVE_NODE, () -> this.source.removeNode(node.path));
                    } else {
                        builder.addButtonInactive(REMOVE_NODE);
                    }

                    Clipboard clipboard = this.manager.clipboard;

                    builder.addButton(COPY, () -> clipboard.doCopy(this.selectedPath, this));
                    if (node.indent > 0) {
                        builder.addButton(CUT, () -> clipboard.doCut(this.selectedPath, this));
                    } else {
                        builder.addButtonInactive(CUT);
                    }

                    if (clipboard.hasValue())
                        builder.addButton(PASTE_VALUE, () -> clipboard.pasteValue(this.selectedPath, this));
                    else
                        builder.addButtonInactive(PASTE_VALUE);
                    if (type == NodeType.COMPOUND) {
                        if (clipboard.hasKey())
                            builder.addButton(PASTE_NODE, () -> clipboard.pasteNode(this.selectedPath, this));
                        else
                            builder.addButtonInactive(PASTE_NODE);
                    } else if (type == NodeType.LIST) {
                        if (clipboard.hasValue())
                            builder.addButton(PASTE_NODE, () -> clipboard.pasteAndAdd(this.selectedPath, -1, this));
                        else
                            builder.addButtonInactive(PASTE_NODE);
                    }
                    if (node.path.current() instanceof NPath.Ele(int index)) {
                        if (clipboard.hasValue())
                            builder.addButton(PASTE_BEFORE, () -> clipboard.pasteAndAdd(this.selectedPath.parent(), index, this));
                        else
                            builder.addButtonInactive(PASTE_BEFORE);
                    }

                    builder.addButton(COPY_PATH_TO_CLIPBOARD, () -> this.source.copyPathToClipboard(node.path))
                            .addButton(COPY_VALUE_TO_CLIPBOARD, () -> this.source.copyValueToClipboard(node.path))
                            .addButton(this.fromServer ? SET_NOT_FROM_SERVER : SET_FROM_SERVER, () -> this.fromServer = !this.fromServer);

                    this.manager.setRCContext(builder.build());
                }
            }

        }
    }

    public String getSourceName() {
        return this.source.getSourceName();
    }

    @Nullable
    public NbtElement getNbt(NPath path) {
        if(this.currentNbt == null) {
            return null;
        }
        return path.resolveNbt(this.currentNbt);
    }

    public void onDoubleClick(int mouseX, int mouseY) {
        if(this.currentPopup != null) return;
        if(this.nodes == null) return;
        if(this.hoveredIndex < 0) return;

        int i = (int) (this.hoveredIndex + this.scrollAmount);
        if(i < 0 || i >= this.nodes.size()) {
            return;
        }
        Node node = this.nodes.get(i);
        this.setEditNodePopup(node.path, this.getNbt(node.path));
//        this.setPopup(new SetValuePopup(this, op, nbt.asString(), EDIT_NODE, (nbt1, newPath) -> {
//            this.actionManager.setValue(op, newPath, nbt1.asString());
//        }));
    }

    private void setEditNodePopup(NPath path, NbtElement nbt) {
        String s = nbt == null ? "" : nbt.toString();
        this.setPopup(new SetValuePopup(this, path, s, TITLE_EDIT_NODE, (nbt1, newPath) -> {
            this.source.setValue(path, newPath, nbt1.toString());
        }));
    }

    private void setAddNodePopup(NPath path) {
//        String s = nbt == null ? "" : nbt.toString();
        this.setPopup(new SetValuePopup(this, path.resolve(""), "", TITLE_ADD_NODE, (nbt1, newPath) -> {
            this.source.addValue(path, newPath.current(), nbt1.toString());
        }));
    }

    private void setAddNodePopup(NPath path, int index) {
//        String s = nbt == null ? "" : nbt.toString();
        this.setPopup(new SetValuePopup(this, path.resolve(index), "", TITLE_ADD_NODE, (nbt1, newPath) -> {
            this.source.addValue(path, newPath.current(), nbt1.toString());
        }));
    }

    public void onKeyPressed(int keyCode, boolean ctrlDown) {
        if (this.currentPopup != null) {
            if(keyCode == GLFW.GLFW_KEY_ENTER) {
                this.currentPopup.onEnter();
            }
            return;
        }

        if(keyCode == GLFW.GLFW_KEY_DOWN && this.selectedIndex < this.nodes.size() - 1) {
            if(this.selectedIndex < 0) {
                this.setSelected(0);
            }
            else {
                this.setSelected(this.selectedIndex + 1);
            }
        }
        if(keyCode == GLFW.GLFW_KEY_UP && this.selectedIndex > 0) {
            this.setSelected(this.selectedIndex - 1);
        }

        if(this.selectedIndex < 0) return;

        if(ctrlDown) {
            Clipboard clipboard = this.manager.clipboard;
            if(keyCode == GLFW.GLFW_KEY_C) {
                clipboard.doCopy(this.selectedPath, this);
                return;
            }
            else if(keyCode == GLFW.GLFW_KEY_X) {
                if(!this.selectedPath.isRoot()){
                    clipboard.doCut(this.selectedPath, this);
                }
                return;
            }
            else if(keyCode == GLFW.GLFW_KEY_V) {
                Node node = this.nodes.get(this.selectedIndex);
                switch (node.type) {
                    case COMPOUND -> {
                        if(clipboard.hasKey()) {
                            this.manager.clipboard.pasteNode(this.selectedPath, this);
                            return;
                        }
                    }
                    case LIST -> {
                        if(clipboard.hasValue()) {
                            this.manager.clipboard.pasteAndAdd(this.selectedPath, -1, this);
                            return;
                        }
                    }
                    case PRIMITIVE ->  {
                        if(clipboard.hasValue()) {
                            this.manager.clipboard.pasteValue(this.selectedPath, this);
                            return;
                        }
                    }
                }
            }
        }

        if(keyCode == GLFW.GLFW_KEY_ENTER) {
            this.setEditNodePopup(this.selectedPath, this.getNbt(this.selectedPath));
            return;
        }

        if(keyCode == GLFW.GLFW_KEY_DELETE && !this.selectedPath.isRoot()) {
            this.source.removeNode(this.selectedPath);
        }

    }

    private static class HoverInfo {
        public final String info;
        public final int index;

        public HoverInfo(String info, int index) {
            this.info = info;
            this.index = index;
        }
    }



}


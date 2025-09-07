package com.whyvo.dine.context;

import com.whyvo.dine.config.Config;
import com.whyvo.dine.context.source.NbtSource;
import com.whyvo.dine.screen.DINEScreen;
import com.whyvo.dine.screen.rc.RCContext;
import com.whyvo.dine.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BScreenManager {
    private static final Text NO_SCREEN = Text.translatable("dine.screen.no_screen");
    private static final MutableText CLOSE_ALL = Text.translatable("dine.rc.close_all");
    private static final MutableText TRY_PAUSE_GAME = Text.translatable("dine.rc.try_pause_game");
    private static final MutableText CONTINUE_GAME = Text.translatable("dine.rc.continue_game");

    private final List<BScreen> screens = new ArrayList<>();
    private BScreen needToOpen = null;
    private int needToSwitch = -1;
    private int currentIndex = -1;

    public DINEScreen bineScreen;

    private double scrollAmount = 0.0;

    private int hoveredIndex = -1;

    private RCContext rcContext;

    public final Clipboard clipboard = new Clipboard();

    public boolean tryPauseGame = false;

    private boolean firstInit = false;

    public void init(DINEScreen bineScreen) {
        if(!this.firstInit) {
            tryPauseGame = Config.defaultTryPauseGame;
            this.firstInit = true;
        }
        this.bineScreen = bineScreen;
        if(this.needToOpen != null) {
            this.addScreen(this.needToOpen);
            this.needToOpen = null;
        }
        if(this.needToSwitch >= 0) {
            this.switchTo(this.needToSwitch);
            this.needToSwitch = -1;
        }
        else {
            BScreen cs = getCurrentScreen();
            if (cs != null) {
                cs.onShown();
            }
        }
        this.rcContext = null;
    }

    public void uninit() {
        BScreen cs = getCurrentScreen();
        if (cs != null) {
            cs.onHidden();
        }
        this.rcContext = null;
        this.bineScreen = null;
    }

    public boolean openScreen(@NotNull NbtSource nbtSource) {
        if(this.screens.isEmpty()) {
            return this.newScreenAndSwitch(nbtSource);
        }
        for(int index = 0; index < this.screens.size(); index++) {
            if(this.screens.get(index).sameSourceOf(nbtSource)) {
                this.switchTo(index);
                return true;
            }
        }
        return this.newScreenAndSwitch(nbtSource);
    }

    public void tick() {
        for(int index = 0; index < this.screens.size(); index++) {
            if(this.screens.get(index).closed()) {
                this.closeScreen(index);
                index--;
            }
        }

        BScreen currentScreen = this.getCurrentScreen();
        if(currentScreen != null) {
            currentScreen.tick();
        }
    }

    private void closeScreen(int index) {
        if(index < 0 || index >= this.screens.size()) {
            return;
        }
        BScreen o = this.screens.get(index);
        this.screens.remove(index);
        if(this.currentIndex >= this.screens.size()) {
            this.currentIndex = this.screens.size() - 1;
        }
        if(this.currentIndex < 0) {
            o.onHidden();
            return;
        }
        BScreen n = this.screens.get(this.currentIndex);
        if(o != n) {
            o.onHidden();
            n.onShown();
        }
    }

    private void closeAllScreen() {
        BScreen cs = this.getCurrentScreen();
        if(cs != null) {
            cs.onHidden();
        }
        this.screens.clear();
        this.currentIndex = -1;
    }

    private boolean newScreenAndSwitch(NbtSource nbtSource) {
        BScreen ns = new BScreen(nbtSource, this);
        if(!ns.updateAndTestSource()) {
            return false;
        }
//        this.currentIndex = this.screens.size();
        if(this.bineScreen == null) {
            this.needToOpen = ns;
            return true;
        }
        this.screens.add(ns);
        this.switchTo(this.screens.size() - 1);
        return true;
    }

    private void addScreen(BScreen ns) {
        this.screens.add(ns);
        this.switchTo(this.screens.size() - 1);
    }


    private void switchTo(int index) {
        if(index == this.currentIndex) {
            return;
        }
        if(this.bineScreen == null) {
            this.needToSwitch = index;
            return;
        }
        BScreen cs = this.getCurrentScreen();
        if(index < 0) {
            this.currentIndex = -1;
            if(cs != null) {
                cs.onHidden();
            }
            return;
        }
        if(index >= this.screens.size()) {
            return;
        }
        BScreen ns = this.screens.get(index);
        this.currentIndex = index;
        if(cs != ns) {
            if (cs != null) {
                cs.onHidden();
            }
            ns.onShown();
        }
        int bw = this.getButtonWidth();
        int left = bw * index;
        int right = left + bw;
        if(this.scrollAmount > left) {
            this.scrollAmount = left;
        }
        else if(this.scrollAmount + this.getWidth() - 8 < right) {
            this.scrollAmount = right - this.getWidth() + 8;
        }
    }

    @Nullable
    private BScreen getCurrentScreen() {
        if(this.currentIndex < 0) {
            return null;
        }
        return this.screens.get(this.currentIndex);
    }

    public boolean hasPopup() {
        BScreen cs = this.getCurrentScreen();
        if(cs != null) {
            return cs.hasPopup();
        }
        return false;
    }

    public void setRCContext(RCContext rcContext) {
        this.rcContext = rcContext;
    }

    private int getButtonWidth() {
        return 110;
    }

    public int getMouseInPart(int mouseY) {
        mouseY -= getTopHeight();
        if(mouseY <= 0) {
            return 0;
        }
        mouseY -= getScreenHeight();
        if(mouseY <= 0) {
            return 1;
        }
        return 2;
    }

    public int getTopHeight() {
        return 20;
    }

    public int getScreenHeight() {
        return this.bineScreen.height - getTopHeight();
    }

    public int getWidth() {
        return this.bineScreen.width;
    }

    private void handleScrollAmount() {
        int bw = this.getButtonWidth();
        int width = this.getWidth() - 8;
        int fullWidth = bw * this.screens.size();
        if(fullWidth <= width) {
            this.scrollAmount = 0.0;
            return;
        }
        if(this.scrollAmount + width > fullWidth) {
            this.scrollAmount = fullWidth - width;
        }
        if(this.scrollAmount < 0) {
            this.scrollAmount = 0.0;
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        int width = this.getWidth();
        int height = this.bineScreen.height;
        int topHeight = this.getTopHeight();

        context.fill(0, 0, width, height + 1, 0xCC303841);
        context.fill(0, 0, width, topHeight, 0xFF2B2D3A);

        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        boolean bl = this.getMouseInPart(mouseY) == 0;
        int bw = this.getButtonWidth();

        this.handleScrollAmount();

        this.hoveredIndex = -1;
        int mi = 3;
        int ma = width - 4;
        int start = 3 - (int) this.scrollAmount;
        for(int index = 0; index < this.screens.size(); index++) {
            int end = start + bw;
            if(end <= mi) {
                start += bw;
                continue;
            }
            if(start > ma) {
                break;
            }
            BScreen currentScreen = this.screens.get(index);
            if(bl && mouseX >= start && mouseX < end) {
                context.fill(start, 0, end, topHeight, 0xFF632D3A);
                this.hoveredIndex = index;
            }
            String title = currentScreen.getTitle();
            int i = font.getWidth(title);
            if(i > bw - 10) {
                int i0 = font.getWidth(" ...");
                String trimmedTitle = font.trimToWidth(title, bw - 10 - i0) + " ...";
                context.drawText(font, trimmedTitle, start + 5, 7, 0xFFFFFFFF, false);
            }
            else {
                context.drawText(font, title, start + 5, 7, 0xFFFFFFFF, false);
            }
            if(index == this.currentIndex) {
                context.fill(start, 19, end, topHeight, 0xFF649995);
            }
            start += bw;
        }

        BScreen currentScreen = this.getCurrentScreen();
        if(currentScreen != null) {
            currentScreen.render(context, mouseX, mouseY);
        }
        else {
            Util.drawCenteredText(context, font, NO_SCREEN, width / 2, topHeight + getScreenHeight() / 2, 0xFFFFFFFF);
        }

        if (this.rcContext != null) {
            this.rcContext.render(context, mouseX, mouseY);
        }

        context.fill(0, 0, 3, height + 1, 0xFF20222C);
        context.fill(width - 3, 0, width + 1, height + 1, 0xFF20222C);
    }

    public boolean mouseScrolled(int mouseX, int mouseY, double horizontalAmount, double verticalAmount, boolean ctrlDown) {
        int mip = this.getMouseInPart(mouseY);
        if(mip == 0) {
            this.scrollAmount -= horizontalAmount * (ctrlDown ? 8 : 2);
        }
        else if(mip == 1) {
            if(this.rcContext != null) return true;
            BScreen currentScreen = this.getCurrentScreen();
            if(currentScreen != null) {
                currentScreen.onMouseScroll(verticalAmount, ctrlDown);
            }
        }
        return true;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int key) {
        if(this.rcContext != null) {
            if(this.rcContext.onMouseClicked(mouseX, mouseY)) {
                this.setRCContext(null);
            }
            return true;
        }
        int mip = this.getMouseInPart(mouseY);
        if (mip == 0) {
            if(key == 2) {
                RCContext.Builder builder = RCContext.builder(mouseX + 1, mouseY + 1)
                        .border(5, 1, this.getWidth() - 4, this.bineScreen.height);
                if(this.screens.isEmpty()) {
                    builder.addButtonInactive(CLOSE_ALL);
                } else {
                    builder.addButton(CLOSE_ALL, this::closeAllScreen);
                }
                if(this.tryPauseGame) {
                    builder.addButton(CONTINUE_GAME, () -> this.tryPauseGame = false);
                } else {
                    builder.addButton(TRY_PAUSE_GAME, () -> this.tryPauseGame = true);
                }
                this.setRCContext(builder.build());
                return true;
            }
            if(this.hoveredIndex >= 0) {
                if(key == 1) {
                    this.switchTo(this.hoveredIndex);
                }
                else if(key == 3) {
                    this.closeScreen(this.hoveredIndex);
                }
            }
        }
        else if (mip == 1) {
            BScreen currentScreen = this.getCurrentScreen();
            if(currentScreen != null) {
                currentScreen.onMouseClicked(mouseX, mouseY, key);
            }
        }
        return true;
    }

    public void doubleClick(int mouseX, int mouseY) {
        if(this.rcContext != null) return;

        int mip = this.getMouseInPart(mouseY);
        if (mip == 1) {
            BScreen currentScreen = this.getCurrentScreen();
            if(currentScreen != null) {
                currentScreen.onDoubleClick(mouseX, mouseY);
            }
        }
    }

    public void keyPressed(int keyCode, boolean ctrlDown) {
        if(this.rcContext != null) return;
        BScreen cs = this.getCurrentScreen();
        if(cs != null) {
            cs.onKeyPressed(keyCode, ctrlDown);
        }
    }
}

package com.whyvo.dine.util;

import com.whyvo.dine.DINE;
import com.whyvo.dine.config.Config;
import com.whyvo.dine.context.source.BlockEntitySource;
import com.whyvo.dine.context.source.EntitySource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class Util {
    private static final Component ERROR_COMMAND = Component.translatable("dine.message.error_command").withStyle(ChatFormatting.RED);
    private static final Component FAILED_COMMAND = Component.translatable("dine.message.failed_sending_command").withStyle(ChatFormatting.RED);
    private static final Function<Integer, MutableComponent> SUCCESS_COMMAND = length -> Component.translatable("dine.message.send_command", length).withStyle(ChatFormatting.AQUA);
    private static final Function<Integer, MutableComponent> COPIED = length -> Component.translatable("dine.message.copied", length).withStyle(ChatFormatting.AQUA);

    private static final Runnable EMPTY = () -> {};

    @Nullable
    public static Entity getEntity(@NotNull Minecraft client, UUID uuid) {
        ClientLevel clientWorld = client.level;
        if(clientWorld == null) return null;
        return clientWorld.getEntities().get(uuid);
    }

    public static void drawCenteredText(GuiGraphics context, Font textRenderer, Component text, int centerX, int y, int color) {
        FormattedCharSequence orderedText = text.getVisualOrderText();
        context.drawString(textRenderer, orderedText, centerX - textRenderer.width(orderedText) / 2, y, color, false);
    }

    public static void tryOpenBine(Minecraft client) {
        DINE.setShouldOpenBine();
    }

    public static void tryOpenTarget(Minecraft client) {
        tryOpenTarget(client, EMPTY);
    }

    public static void tryOpenTarget(Minecraft client, Runnable ifFail) {
        if(client.crosshairPickEntity != null) {
            if(!DINE.SCREENS.openScreen(new EntitySource(client.crosshairPickEntity))) {
                ifFail.run();
                return;
            }
            DINE.setShouldOpenBine();
        }
        else {
            Entity entity = client.getCameraEntity();
            if(entity == null) {
                ifFail.run();
                return;
            }
            HitResult hitResult = entity.pick(20.0, 0.0F, false);
            if (hitResult.getType() != HitResult.Type.BLOCK) {
                ifFail.run();
                return;
            }
            BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
            if(!DINE.SCREENS.openScreen(new BlockEntitySource(blockPos))) {
                ifFail.run();
                return;
            }
            DINE.setShouldOpenBine();
        }
    }

    private static LocalPlayer findPlayer() {
        return Minecraft.getInstance().player;
    }

    public static void trySendCommand(String command) {
        LocalPlayer player = findPlayer();
        if(player == null) return;
        if (player.connection.sendUnsignedCommand(command)) {
            player.sendSystemMessage(SUCCESS_COMMAND.apply(command.length()));
            if(Config.logSentCommand) {
                DINE.LOGGER.info("Sent command: {}", command);
            }
        }
        else {
            player.sendSystemMessage(FAILED_COMMAND);
            if(Config.logSentCommand) {
                DINE.LOGGER.warn("Failed command: {}", command);
            }
        }
    }

    public static void sendErrorCommand() {
        LocalPlayer player = findPlayer();
        if(player == null) return;
        player.sendSystemMessage(ERROR_COMMAND);
    }

    public static void copyToClipboard(String string) {
        Minecraft.getInstance().keyboardHandler.setClipboard(string);
        LocalPlayer player = findPlayer();
        if(player == null) return;
        player.sendSystemMessage(COPIED.apply(string.length()));
    }
}

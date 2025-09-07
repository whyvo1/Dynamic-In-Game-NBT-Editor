package com.whyvo.dine.util;

import com.whyvo.dine.DINE;
import com.whyvo.dine.config.Config;
import com.whyvo.dine.context.source.BlockEntitySource;
import com.whyvo.dine.context.source.EntitySource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Function;

public class Util {
    private static final Text ERROR_COMMAND = Text.translatable("dine.message.error_command").formatted(Formatting.RED);
    private static final Text FAILED_COMMAND = Text.translatable("dine.message.failed_sending_command").formatted(Formatting.RED);
    private static final Function<Integer, MutableText> SUCCESS_COMMAND = length -> Text.translatable("dine.message.send_command", length).formatted(Formatting.AQUA);
    private static final Function<Integer, MutableText> COPIED = length -> Text.translatable("dine.message.copied", length).formatted(Formatting.AQUA);

    private static final Runnable EMPTY = () -> {};

    @Nullable
    public static Entity getEntity(@NotNull MinecraftClient client, UUID uuid) {
        ClientWorld clientWorld = client.world;
        if(clientWorld == null) return null;
        return clientWorld.getEntityLookup().get(uuid);
    }

    public static void drawCenteredText(DrawContext context, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        OrderedText orderedText = text.asOrderedText();
        context.drawText(textRenderer, orderedText, centerX - textRenderer.getWidth(orderedText) / 2, y, color, false);
    }

    public static void tryOpenBine(MinecraftClient client) {
        DINE.setShouldOpenBine();
    }

    public static void tryOpenTarget(MinecraftClient client) {
        tryOpenTarget(client, EMPTY);
    }

    public static void tryOpenTarget(MinecraftClient client, Runnable ifFail) {
        if(client.targetedEntity != null) {
            if(!DINE.SCREENS.openScreen(new EntitySource(client.targetedEntity))) {
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
            HitResult hitResult = entity.raycast(20.0, 0.0F, false);
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

    private static ClientPlayerEntity findPlayer() {
        return MinecraftClient.getInstance().player;
    }

    public static void trySendCommand(String command) {
        ClientPlayerEntity player = findPlayer();
        if(player == null) return;
        if (player.networkHandler.sendCommand(command)) {
            player.sendMessage(SUCCESS_COMMAND.apply(command.length()), false);
            if(Config.logSentCommand) {
                DINE.LOGGER.info("Sent command: {}", command);
            }
        }
        else {
            player.sendMessage(FAILED_COMMAND, false);
            if(Config.logSentCommand) {
                DINE.LOGGER.warn("Failed command: {}", command);
            }
        }
    }

    public static void sendErrorCommand() {
        ClientPlayerEntity player = findPlayer();
        if(player == null) return;
        player.sendMessage(ERROR_COMMAND, false);
    }

    public static void copyToClipboard(String string) {
        MinecraftClient.getInstance().keyboard.setClipboard(string);
        ClientPlayerEntity player = findPlayer();
        if(player == null) return;
        player.sendMessage(COPIED.apply(string.length()), false);
    }
}

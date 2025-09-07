package com.whyvo.dine.command;

import com.mojang.brigadier.CommandDispatcher;
import com.whyvo.dine.DINE;
import com.whyvo.dine.context.source.BlockEntitySource;
import com.whyvo.dine.context.source.EntitySource;
import com.whyvo.dine.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class DineCommand {
    private static final Text NO_TARGET = Text.translatable("dine.command.no_target");

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("dine")
                        .executes(context -> execute(context.getSource()))
                        .then(ClientCommandManager.literal("block")
                                .then(ClientCommandManager.argument("pos", ClientPosArgumentType.blockPos())
                                        .executes(context -> executeBlock(context.getSource(), ClientPosArgumentType.getBlockPos(context, "pos"))
                                        )
                                )
                        )
                        .then(ClientCommandManager.literal("entity")
                                .then(ClientCommandManager.argument("uuid", UuidArgumentType.uuid())
                                        .executes(context -> executeEntity(context.getSource(), UuidArgumentType.getUuid(context, "uuid"))
                                        )
                                )
                        )
                        .then(ClientCommandManager.literal("target")
                                .executes(context -> executeTarget(context.getSource()))
                        )

        );
    }

    private static int execute(FabricClientCommandSource source) {
        MinecraftClient client = source.getClient();
        client.execute(() -> {
            Util.tryOpenBine(client);
        });
        return 1;
    }

    private static int executeBlock(FabricClientCommandSource source, BlockPos pos) {
        MinecraftClient client = source.getClient();
        client.execute(() -> {
            ClientWorld world = client.world;
            if (world == null) {
                source.sendFeedback(NO_TARGET);
                return;
            }
            if(world.getBlockEntity(pos) == null) {
                source.sendFeedback(NO_TARGET);
                return;
            }

            if(!DINE.SCREENS.openScreen(new BlockEntitySource(pos))) {
                source.sendFeedback(NO_TARGET);
                return;
            }
            DINE.setShouldOpenBine();
        });
        return 1;
    }

    private static int executeTarget(FabricClientCommandSource source) {
        MinecraftClient client = source.getClient();
        client.execute(() -> {
            Util.tryOpenTarget(client, () -> source.sendFeedback(NO_TARGET));
        });
        return 1;
    }

    private static int executeEntity(FabricClientCommandSource source, UUID uuid) {
        MinecraftClient client = source.getClient();
        client.execute(() -> {
            ClientWorld clientWorld = client.world;
            if (clientWorld == null) {
                source.sendFeedback(NO_TARGET);
                return;
            }
            Entity entity = clientWorld.getEntityLookup().get(uuid);
            if (entity == null) {
                source.sendFeedback(NO_TARGET);
                return;
            }

            if(!DINE.SCREENS.openScreen(new EntitySource(entity))) {
                source.sendFeedback(NO_TARGET);
                return;
            }
            DINE.setShouldOpenBine();
        });
        return 1;
    }

}

package com.whyvo.dine.command;

import com.mojang.brigadier.CommandDispatcher;
import com.whyvo.dine.DINE;
import com.whyvo.dine.context.source.BlockEntitySource;
import com.whyvo.dine.context.source.EntitySource;
import com.whyvo.dine.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class DineCommand {
    private static final Component NO_TARGET = Component.translatable("dine.command.no_target");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dine")
                        .executes(context -> execute(context.getSource()))
                        .then(Commands.literal("block")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> executeBlock(context.getSource(), BlockPosArgument.getBlockPos(context, "pos"))
                                        )
                                )
                        )
                        .then(Commands.literal("entity")
                                .then(Commands.argument("uuid", UuidArgumentType.uuid())
                                        .executes(context -> executeEntity(context.getSource(), UuidArgumentType.getUuid(context, "uuid"))
                                        )
                                )
                        )
                        .then(Commands.literal("target")
                                .executes(context -> executeTarget(context.getSource()))
                        )

        );
    }

    private static int execute(CommandSourceStack source) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            Util.tryOpenBine(client);
        });
        return 1;
    }

    private static int executeBlock(CommandSourceStack source, BlockPos pos) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            ClientLevel world = client.level;
            if (world == null) {
                source.sendSystemMessage(NO_TARGET);
                return;
            }
            if(world.getBlockEntity(pos) == null) {
                source.sendSystemMessage(NO_TARGET);
                return;
            }

            if(!DINE.SCREENS.openScreen(new BlockEntitySource(pos))) {
                source.sendSystemMessage(NO_TARGET);
                return;
            }
            DINE.setShouldOpenBine();
        });
        return 1;
    }

    private static int executeTarget(CommandSourceStack source) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            Util.tryOpenTarget(client, () -> source.sendSystemMessage(NO_TARGET));
        });
        return 1;
    }

    private static int executeEntity(CommandSourceStack source, UUID uuid) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            ClientLevel clientWorld = client.level;
            if (clientWorld == null) {
                source.sendSystemMessage(NO_TARGET);
                return;
            }
            Entity entity = clientWorld.getEntities().get(uuid);
            if (entity == null) {
                source.sendSystemMessage(NO_TARGET);
                return;
            }

            if(!DINE.SCREENS.openScreen(new EntitySource(entity))) {
                source.sendSystemMessage(NO_TARGET);
                return;
            }
            DINE.setShouldOpenBine();
        });
        return 1;
    }

}

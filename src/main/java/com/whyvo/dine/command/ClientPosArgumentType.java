package com.whyvo.dine.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class ClientPosArgumentType implements ArgumentType<ClientPosArgument> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "~0.5 ~1 ~-5");

    public static ClientPosArgumentType blockPos() {
        return new ClientPosArgumentType();
    }

    @Override
    public ClientPosArgument parse(StringReader reader) throws CommandSyntaxException {
        return ClientPosArgument.parse(reader);
    }

    public static BlockPos getBlockPos(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, ClientPosArgument.class).toAbsoluteBlockPos(context.getSource());
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof CommandSource)) {
            return Suggestions.empty();
        } else {
            String string = builder.getRemaining();
            Collection<CommandSource.RelativePosition> collection = ((CommandSource)context.getSource()).getBlockPositionSuggestions();

            return CommandSource.suggestPositions(string, collection, builder, CommandManager.getCommandValidator(this::parse));
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

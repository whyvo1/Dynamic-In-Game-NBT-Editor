package com.whyvo.dine.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class UuidArgumentType implements ArgumentType<UUID> {
    private static final DynamicCommandExceptionType FAILED_PARSING = new DynamicCommandExceptionType(
            object -> Text.translatable("argument.uuid.failed_parsing", object)
    );

    public static UuidArgumentType uuid() {
        return new UuidArgumentType();
    }

    public static UUID getUuid(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, UUID.class);
    }

    @Override
    public UUID parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readUnquotedString();
        try {
            return UUID.fromString(string);
        } catch (Exception e) {
            throw FAILED_PARSING.create(string);
        }
    }


}

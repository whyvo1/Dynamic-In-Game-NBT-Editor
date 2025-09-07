package com.whyvo.dine.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.whyvo.dine.DINE;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.StringNbtReader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Compat1215 {
    private static final NbtParser PARSER;
    private static Object TARGET;
    private static MethodHandle MH;

    static {
        NbtParser parser1;
        try {
            Field field = StringNbtReader.class.getDeclaredField("field_58028");
            field.setAccessible(true);
            TARGET = field.get(null);
            Method method = StringNbtReader.class.getDeclaredMethod("method_67319", StringReader.class);
            MH = MethodHandles.lookup().unreflect(method);
            DINE.LOGGER.info("Compat1215 switch to 'new type'.");
            parser1 = reader -> {
                try {
                    return (NbtElement) MH.invoke(TARGET, reader);
                } catch (CommandSyntaxException | WrongMethodTypeException | ClassCastException e) {
                    throw e;
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            };
        } catch (ReflectiveOperationException e) {
            DINE.LOGGER.info("Compat1215 switch to 'old type'.");
            parser1 = reader -> new StringNbtReader(reader).parseElement();
        }
        PARSER = parser1;
    }

    public static void init() {}

    public static NbtElement parseNbt(StringReader reader) throws CommandSyntaxException {
        return PARSER.parse(reader);
    }

    private interface NbtParser {
        NbtElement parse(StringReader reader) throws CommandSyntaxException;
    }
}

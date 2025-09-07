package com.whyvo.dine.context.source;

import com.whyvo.dine.config.Config;
import com.whyvo.dine.context.BScreen;
import com.whyvo.dine.context.nbt.NPath;
import com.whyvo.dine.util.Util;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NbtSource {
    @Nullable
    NbtCompound getSource();

    void tick(boolean fromServer);

    default boolean defaultFromServer() {
        return false;
    }

    String getSourceName();

    String getTitle();

    boolean isSame(@NotNull NbtSource o);

    default NbtElement getNbt(NPath path) {
        NbtElement root = this.getSource();
        if(root == null) return null;
        return path.resolveNbt(root);
    }

    default void setValue(NPath oldPath, NPath newPath, String value) {
        if(!oldPath.equals(newPath) || this.getNbt(oldPath) == null) {
            if(oldPath.isRoot()) return;
            String command = Config.dataCommand + " remove " + this.getSourceName() + " " + oldPath.asPath();
            Util.trySendCommand(command);

            this.addValue(newPath.parent(), newPath.current(), value);
            return;
        }

        String command = Config.dataCommand + " modify " + this.getSourceName() + " " + newPath.asPath() + " set value " + value;
        Util.trySendCommand(command);

    }

    default void addValue(NPath parentPath, NPath.Next current, String value) {
        NbtElement parentNbt = this.getNbt(parentPath);

        if (current instanceof NPath.Ele(int index)) {
            if(!(parentNbt instanceof NbtList nbtList)) {
                Util.sendErrorCommand();
                return;
            }
            String command;
            if(index < 0 || nbtList.size() <= index) {
                command = Config.dataCommand + " modify " + this.getSourceName() + " " + parentPath.asPath() + " append value " + value;
            }
            else {
                command = Config.dataCommand + " modify " + this.getSourceName() + " " + parentPath.asPath() + " insert " + index + " value " + value;
            }
            Util.trySendCommand(command);
        }
        else if (current instanceof NPath.Child(String name)) {
            if(!(parentNbt instanceof NbtCompound)) {
                Util.sendErrorCommand();
                return;
            }

            String command = Config.dataCommand + " modify " + this.getSourceName() + " " + parentPath.resolve(name).asPath() + " set value " + value;
            Util.trySendCommand(command);
        }
        else {
            Util.sendErrorCommand();
        }
    }

    default void removeNode(NPath path) {
        if(path.isRoot()) return;
        if(this.getNbt(path) == null) {
            Util.sendErrorCommand();
            return;
        }

        String command = Config.dataCommand + " remove " + this.getSourceName() + " " + path.asPath();
        Util.trySendCommand(command);
    }

    default void copyValueToClipboard(NPath path) {
        NbtElement parentNbt = this.getNbt(path);
        if(parentNbt == null) return;
        Util.copyToClipboard(parentNbt.toString());
    }

    default void copyPathToClipboard(NPath path) {
        Util.copyToClipboard(path.asPath());
    }

    default boolean pasteValue(NPath path, BScreen source, NPath sourcePath) {
        if(this.getNbt(path) == null) {
            Util.sendErrorCommand();
            return false;
        }
        String command = Config.dataCommand + " modify " + this.getSourceName() + " " + path.asPath() + " set from " + source.getSourceName() + " " + sourcePath.asPath();
        Util.trySendCommand(command);
        return true;
    }

    default boolean pasteNode(NPath parentPath, BScreen source, NPath sourcePath) {
        if(!(this.getNbt(parentPath) instanceof NbtCompound)) {
            Util.sendErrorCommand();
            return false;
        }
        if(!(sourcePath.current() instanceof NPath.Child(String name))) {
            Util.sendErrorCommand();
            return false;
        }

        String command = Config.dataCommand + " modify " + this.getSourceName() + " " + parentPath.resolve(name).asPath() + " set from " + source.getSourceName() + " " + sourcePath.asPath();
        Util.trySendCommand(command);
        return true;
    }

    default boolean pasteAndAdd(NPath parentPath, int index, BScreen source, NPath sourcePath) {
        if(!(this.getNbt(parentPath) instanceof NbtList nbtList)) {
            Util.sendErrorCommand();
            return false;
        }

        String command;
        if(index < 0 || index >= nbtList.size()) {
            command = Config.dataCommand + " modify " + this.getSourceName() + " " + parentPath.asPath() + " append from " + source.getSourceName() + " " + sourcePath.asPath();
        }
        else {
            command = Config.dataCommand + " modify " + this.getSourceName() + " " + parentPath.asPath() + " insert " + index + " from " + source.getSourceName() + " " + sourcePath.asPath();
        }
        Util.trySendCommand(command);
        return true;
    }
}

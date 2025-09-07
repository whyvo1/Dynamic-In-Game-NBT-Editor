package com.whyvo.dine.context.nbt;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class NPath implements Iterable<NPath.Next> {
    public static final NPath ROOT = new NPath(new Next[0]);

    private final Next[] dirs;

    public NPath(Next[] dirs) {
        this.dirs = dirs;
    }

    public int depth() {
        return dirs.length;
    }

    public NPath parent() {
        if(isRoot()) {
            return this;
        }
        return new NPath(arrayRemoveLast(this.dirs));
    }

    public NPath resolve(String name) {
        return new NPath(arrayAppend(this.dirs, new Child(name)));
    }

    public NPath resolve(int index) {
        return new NPath(arrayAppend(this.dirs, new Ele(index)));
    }

    public NPath resolve(Next next) {
        return new NPath(arrayAppend(this.dirs, next));
    }

    public boolean isRoot() {
        return this.dirs.length == 0;
    }

    public Next current() {
        if (this.isRoot()) {
            return null;
        }
        return this.dirs[this.dirs.length - 1];
    }

    @Nullable
    public String currentKey() {
        if (this.isRoot()) {
            return null;
        }
        Next c = this.dirs[this.dirs.length - 1];
        return c instanceof Child(String name) ? name : null;
    }

    @Nullable
    public NbtElement resolveNbt(NbtElement element) {
        for(Next next : this.dirs) {
            if(next instanceof Ele(int index)) {
                if(element instanceof NbtList list && list.size() > index) {
                    element = list.get(index);
                }
                else {
                    return null;
                }
            }
            else if(next instanceof Child(String name)) {
                if(element instanceof NbtCompound compound && compound.contains(name)) {
                    element = compound.get(name);
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }
        }
        return element;
    }

    public String asPath() {
        StringBuilder sb = new StringBuilder("{}");
        int i = 0;
        for (; i < this.dirs.length; i++) {
            Next next = this.dirs[i];
            sb.append(next.asPath());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NPath nexts = (NPath) o;
        return Arrays.equals(dirs, nexts.dirs);
    }

    public boolean shallowEquals(NPath nexts) {
        if(nexts == null) return false;

        if(this.dirs.length != nexts.dirs.length) return false;

        Next next = this.current();
        if(next == null) return nexts.current() == null;

        return next.equals(nexts.current());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(dirs);
    }

    @Override
    public @NotNull Iterator<Next> iterator() {
        return new Iterator<>() {
            private int cursor = 0;
            private final Next[] a = NPath.this.dirs;

            @Override
            public boolean hasNext() {
                return cursor < a.length;
            }

            @Override
            public Next next() {
                if (cursor >= a.length) {
                    throw new NoSuchElementException();
                }
                cursor ++;
                return a[cursor - 1];
            }
        };
    }


    private static Next[] arrayAppend(Next[] array, Next element) {
        if (array == null) {
            return new Next[]{element};
        }
        Next[] newArray = new Next[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = element;
        return newArray;
    }

    public static Next[] arrayRemoveLast(Next[] array) {
        if (array == null || array.length == 0) {
            return new Next[0];
        }
        Next[] newArray = new Next[array.length - 1];
        System.arraycopy(array, 0, newArray, 0, array.length - 1);
        return newArray;
    }

    public interface Next {
        String asPath();

        boolean equals(Object obj);
    }

    public record Child(String name) implements Next {
        private static final Pattern PATTERN = Pattern.compile("[.:\\[\\]{} ]");
        @Override
        public String asPath() {
            if(PATTERN.matcher(this.name).matches()) {
                return ".\"" + this.name + "\"";
            }
            return "." + this.name;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Child && this.name.equals(((Child) obj).name);
        }
    }

    public record Ele(int index) implements Next {
        @Override
        public String asPath() {
            return "[" + this.index + "]";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Ele && this.index == ((Ele) obj).index;
        }
    }
}

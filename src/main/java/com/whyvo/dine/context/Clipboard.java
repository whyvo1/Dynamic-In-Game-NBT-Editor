package com.whyvo.dine.context;

import com.whyvo.dine.context.nbt.NPath;

public class Clipboard {
    private NPath sourcePath = null;
    private BScreen source = null;
    private boolean toCut = false;

    public Clipboard() {
    }

    public boolean hasValue() {
        return this.sourcePath != null;
    }

    public boolean hasKey() {
        return this.sourcePath != null && this.sourcePath.current() instanceof NPath.Child;
    }

    public void doCopy(NPath path, BScreen source) {
        this.sourcePath = path;
        this.source = source;
        this.toCut = false;
    }

    public void doCut(NPath path, BScreen source) {
        this.sourcePath = path;
        this.source = source;
        this.toCut = true;
    }

    public void pasteValue(NPath path, BScreen target) {
        if(this.sourcePath == null) return;
        if(target != this.source) {
            this.source.updateAndTestSource();
        }
        if(this.source.getNbt(this.sourcePath) == null) return;

        if(this.toCut) {
            if(target.getSource().pasteValue(path, this.source, this.sourcePath)) {
                source.getSource().removeNode(this.sourcePath);
            }
            this.doCopy(null, null);
        } else {
            target.getSource().pasteValue(path, this.source, this.sourcePath);
        }
    }

    public void pasteNode(NPath path, BScreen target) {
        if(this.sourcePath == null) return;
        if(target != this.source) {
            this.source.updateAndTestSource();
        }
        if(this.source.getNbt(this.sourcePath) == null) return;

        if(this.toCut) {
            if(target.getSource().pasteNode(path, this.source, this.sourcePath)) {
                source.getSource().removeNode(this.sourcePath);
            }
            this.doCopy(null, null);
        } else {
            target.getSource().pasteNode(path, this.source, this.sourcePath);
        }
    }

    public void pasteAndAdd(NPath path, int index, BScreen target) {
        if(this.sourcePath == null) return;
        if(target != this.source) {
            this.source.updateAndTestSource();
        }
        if(this.source.getNbt(path) == null) return;

        if(this.toCut) {
            if(target.getSource().pasteAndAdd(path, index, this.source, this.sourcePath)) {
                source.getSource().removeNode(this.sourcePath);
            }
            this.doCopy(null, null);
        } else {
            target.getSource().pasteAndAdd(path, index, this.source, this.sourcePath);
        }
    }

}

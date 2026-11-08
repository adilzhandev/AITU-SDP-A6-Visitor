package kz.aitu.filesystem.model;

import kz.aitu.filesystem.visitor.Visitor;

public final class FileNode extends AbstractElement {

    private final long size;

    public FileNode(String name, long size) {
        super(name);
        if (size < 0) {
            throw new IllegalArgumentException("File size must not be negative: " + size);
        }
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

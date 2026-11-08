package kz.aitu.filesystem.model;

public abstract class AbstractElement implements Element {

    private final String name;
    private DirectoryNode parent;

    AbstractElement(String name) {
        if (name == null || name.isBlank() || name.contains("/")) {
            throw new IllegalArgumentException("Invalid element name: " + name);
        }
        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    public final DirectoryNode getParent() {
        return parent;
    }

    final void attachTo(DirectoryNode newParent) {
        parent = newParent;
    }
}

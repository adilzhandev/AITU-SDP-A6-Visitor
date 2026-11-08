package kz.aitu.filesystem.model;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import kz.aitu.filesystem.visitor.Visitor;

public final class DirectoryNode extends AbstractElement {

    private final Map<String, AbstractElement> children = new TreeMap<>();

    public DirectoryNode(String name) {
        super(name);
    }

    public DirectoryNode add(AbstractElement child) {
        rejectInvalidChild(child);
        children.put(child.getName(), child);
        child.attachTo(this);
        return this;
    }

    public List<Element> getChildren() {
        return List.copyOf(children.values());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    private void rejectInvalidChild(AbstractElement child) {
        if (child == null) {
            throw new IllegalArgumentException("Child must not be null");
        }
        if (child.getParent() != null) {
            throw new IllegalArgumentException("Element already has a parent: " + child.getName());
        }
        if (children.containsKey(child.getName())) {
            throw new IllegalArgumentException("Duplicate sibling name: " + child.getName());
        }
        if (isSelfOrAncestor(child)) {
            throw new IllegalArgumentException("Adding would create a cycle: " + child.getName());
        }
    }

    private boolean isSelfOrAncestor(AbstractElement candidate) {
        for (DirectoryNode current = this; current != null; current = current.getParent()) {
            if (current == candidate) {
                return true;
            }
        }
        return false;
    }
}

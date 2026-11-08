package kz.aitu.filesystem.visitor;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.Element;
import kz.aitu.filesystem.model.FileNode;

public final class SearchVisitor implements Visitor {

    private final Predicate<String> nameCriterion;
    private final long minimumSize;
    private final Set<String> matchingPaths = new TreeSet<>();
    private String currentPrefix = "";
    private int depth;

    public SearchVisitor(Predicate<String> nameCriterion, long minimumSize) {
        if (nameCriterion == null) {
            throw new IllegalArgumentException("Name criterion must not be null");
        }
        if (minimumSize < 0) {
            throw new IllegalArgumentException("Minimum size must not be negative: " + minimumSize);
        }
        this.nameCriterion = nameCriterion;
        this.minimumSize = minimumSize;
    }

    @Override
    public void visit(FileNode file) {
        resetIfNewRun();
        if (matches(file)) {
            matchingPaths.add(currentPrefix + file.getName());
        }
    }

    @Override
    public void visit(DirectoryNode directory) {
        resetIfNewRun();
        String parentPrefix = currentPrefix;
        if (depth > 0) {
            currentPrefix = parentPrefix + directory.getName() + "/";
        }
        depth++;
        try {
            for (Element child : directory.getChildren()) {
                child.accept(this);
            }
        } finally {
            depth--;
            currentPrefix = parentPrefix;
        }
    }

    public List<String> getMatchingPaths() {
        return List.copyOf(matchingPaths);
    }

    private boolean matches(FileNode file) {
        return nameCriterion.test(file.getName()) && file.getSize() >= minimumSize;
    }

    private void resetIfNewRun() {
        if (depth == 0) {
            matchingPaths.clear();
        }
    }
}

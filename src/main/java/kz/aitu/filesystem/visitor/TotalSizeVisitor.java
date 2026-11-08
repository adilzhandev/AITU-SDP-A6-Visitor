package kz.aitu.filesystem.visitor;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.Element;
import kz.aitu.filesystem.model.FileNode;

public final class TotalSizeVisitor implements Visitor {

    private long totalSize;
    private int depth;

    @Override
    public void visit(FileNode file) {
        resetIfNewRun();
        totalSize += file.getSize();
    }

    @Override
    public void visit(DirectoryNode directory) {
        resetIfNewRun();
        depth++;
        for (Element child : directory.getChildren()) {
            child.accept(this);
        }
        depth--;
    }

    public long getTotalSize() {
        return totalSize;
    }

    private void resetIfNewRun() {
        if (depth == 0) {
            totalSize = 0;
        }
    }
}

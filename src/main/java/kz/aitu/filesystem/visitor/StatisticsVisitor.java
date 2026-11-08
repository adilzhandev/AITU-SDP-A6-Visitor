package kz.aitu.filesystem.visitor;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.Element;
import kz.aitu.filesystem.model.FileNode;

public final class StatisticsVisitor implements Visitor {

    private int fileCount;
    private int directoryCount;
    private int maxDepth;
    private int depth;

    @Override
    public void visit(FileNode file) {
        resetIfNewRun();
        fileCount++;
        maxDepth = Math.max(maxDepth, depth);
    }

    @Override
    public void visit(DirectoryNode directory) {
        resetIfNewRun();
        directoryCount++;
        maxDepth = Math.max(maxDepth, depth);
        depth++;
        for (Element child : directory.getChildren()) {
            child.accept(this);
        }
        depth--;
    }

    public int getFileCount() {
        return fileCount;
    }

    public int getDirectoryCount() {
        return directoryCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    private void resetIfNewRun() {
        if (depth == 0) {
            fileCount = 0;
            directoryCount = 0;
            maxDepth = 0;
        }
    }
}

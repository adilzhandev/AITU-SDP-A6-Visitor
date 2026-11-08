package kz.aitu.filesystem.visitor;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.FileNode;

public interface Visitor {

    void visit(FileNode file);

    void visit(DirectoryNode directory);
}

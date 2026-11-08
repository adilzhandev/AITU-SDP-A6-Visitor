package kz.aitu.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.FileNode;
import kz.aitu.filesystem.visitor.TotalSizeVisitor;
import org.junit.jupiter.api.Test;

class TotalSizeVisitorTest {

    private final TotalSizeVisitor visitor = new TotalSizeVisitor();

    private DirectoryNode album() {
        return new DirectoryNode("album");
    }

    private DirectoryNode nestedTree() {
        DirectoryNode raw = new DirectoryNode("raw").add(new FileNode("c.dng", 300));
        DirectoryNode photos = new DirectoryNode("photos").add(new FileNode("b.jpg", 20)).add(raw);
        return album().add(new FileNode("a.txt", 7)).add(photos);
    }

    @Test
    void sumsFilesAcrossMultipleNestingLevels() {
        nestedTree().accept(visitor);

        assertEquals(327, visitor.getTotalSize());
    }

    @Test
    void chosenSubtreeCountsOnlyItsOwnFiles() {
        DirectoryNode subtree = new DirectoryNode("sub").add(new FileNode("x.bin", 11));
        album().add(new FileNode("outside.bin", 1000)).add(subtree);

        subtree.accept(visitor);

        assertEquals(11, visitor.getTotalSize());
    }

    @Test
    void singleFileRootReportsItsSize() {
        new FileNode("alone.txt", 42).accept(visitor);

        assertEquals(42, visitor.getTotalSize());
    }

    @Test
    void emptyDirectoryHasZeroSize() {
        album().accept(visitor);

        assertEquals(0, visitor.getTotalSize());
    }

    @Test
    void repeatedRunDoesNotAccumulate() {
        DirectoryNode tree = nestedTree();

        tree.accept(visitor);
        tree.accept(visitor);

        assertEquals(327, visitor.getTotalSize());
    }

    @Test
    void failedRunDoesNotCorruptTheNextRun() {
        DirectoryNode tooDeep = new DirectoryNode("level");
        for (int level = 0; level < 300_000; level++) {
            tooDeep = new DirectoryNode("level").add(tooDeep);
        }
        DirectoryNode tree = album().add(new FileNode("a.txt", 100)).add(tooDeep);

        assertThrows(StackOverflowError.class, () -> tree.accept(visitor));
        new FileNode("next.txt", 5).accept(visitor);

        assertEquals(5, visitor.getTotalSize());
    }

    @Test
    void reuseOnAnotherTreeReportsOnlyTheLatestRun() {
        nestedTree().accept(visitor);

        new FileNode("next.txt", 5).accept(visitor);

        assertEquals(5, visitor.getTotalSize());
    }
}

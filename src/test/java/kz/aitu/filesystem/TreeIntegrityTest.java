package kz.aitu.filesystem;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.Element;
import kz.aitu.filesystem.model.FileNode;
import org.junit.jupiter.api.Test;

class TreeIntegrityTest {

    @Test
    void negativeFileSizeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("bad.bin", -1));
    }

    @Test
    void zeroFileSizeIsAllowed() {
        assertEquals(0, new FileNode("empty.log", 0).getSize());
    }

    @Test
    void blankNamesAndNamesWithSeparatorAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(null));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode("a/b"));
    }

    @Test
    void duplicateSiblingNameIsRejectedAndOriginalIsKept() {
        FileNode original = new FileNode("data.csv", 10);
        DirectoryNode directory = new DirectoryNode("reports").add(original);

        assertThrows(IllegalArgumentException.class,
                () -> directory.add(new DirectoryNode("data.csv")));

        assertEquals(List.of(original), directory.getChildren());
    }

    @Test
    void sameNameInDifferentDirectoriesIsAllowed() {
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");

        assertDoesNotThrow(() -> first.add(new FileNode("same.txt", 1)));
        assertDoesNotThrow(() -> second.add(new FileNode("same.txt", 2)));
    }

    @Test
    void elementCannotHaveTwoParents() {
        FileNode file = new FileNode("shared.txt", 5);
        DirectoryNode owner = new DirectoryNode("owner").add(file);
        DirectoryNode other = new DirectoryNode("other");

        assertThrows(IllegalArgumentException.class, () -> other.add(file));

        assertSame(owner, file.getParent());
        assertTrue(other.getChildren().isEmpty());
    }

    @Test
    void directoryCannotContainItself() {
        DirectoryNode directory = new DirectoryNode("loop");

        assertThrows(IllegalArgumentException.class, () -> directory.add(directory));

        assertNull(directory.getParent());
    }

    @Test
    void ancestorCannotBecomeDescendant() {
        DirectoryNode grandchild = new DirectoryNode("grandchild");
        DirectoryNode child = new DirectoryNode("child").add(grandchild);
        DirectoryNode root = new DirectoryNode("root").add(child);

        assertThrows(IllegalArgumentException.class, () -> grandchild.add(root));

        assertTrue(grandchild.getChildren().isEmpty());
    }

    @Test
    void nullChildIsRejected() {
        DirectoryNode directory = new DirectoryNode("root");

        assertThrows(IllegalArgumentException.class, () -> directory.add(null));
    }

    @Test
    void deepValidNestingIsSupported() {
        DirectoryNode level3 = new DirectoryNode("level3").add(new FileNode("deep.txt", 1));
        DirectoryNode level2 = new DirectoryNode("level2").add(level3);
        DirectoryNode level1 = new DirectoryNode("level1").add(level2);

        assertSame(level2, level3.getParent());
        assertSame(level1, level2.getParent());
    }

    @Test
    void childrenViewCannotChangeTheDirectory() {
        DirectoryNode directory = new DirectoryNode("root").add(new FileNode("a.txt", 1));
        List<Element> children = directory.getChildren();

        assertThrows(UnsupportedOperationException.class,
                () -> children.add(new FileNode("b.txt", 1)));

        assertEquals(1, directory.getChildren().size());
    }
}

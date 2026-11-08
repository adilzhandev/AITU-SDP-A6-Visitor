package kz.aitu.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Predicate;
import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.FileNode;
import kz.aitu.filesystem.visitor.SearchVisitor;
import org.junit.jupiter.api.Test;

class SearchVisitorTest {

    private static final Predicate<String> JAVA_FILES = name -> name.endsWith(".java");
    private static final Predicate<String> ANY_NAME = name -> true;

    private DirectoryNode workspace() {
        DirectoryNode core = new DirectoryNode("core")
                .add(new FileNode("Engine.java", 900))
                .add(new FileNode("notes.md", 900));
        DirectoryNode src = new DirectoryNode("src")
                .add(new FileNode("App.java", 100))
                .add(core);
        return new DirectoryNode("workspace")
                .add(src)
                .add(new FileNode("Main.java", 899));
    }

    private List<String> search(DirectoryNode root, Predicate<String> criterion, long minimumSize) {
        SearchVisitor visitor = new SearchVisitor(criterion, minimumSize);
        root.accept(visitor);
        return visitor.getMatchingPaths();
    }

    @Test
    void bothNameAndSizeMustMatch() {
        assertEquals(List.of("src/core/Engine.java"), search(workspace(), JAVA_FILES, 900));
    }

    @Test
    void minimumSizeIsInclusive() {
        DirectoryNode root = new DirectoryNode("root")
                .add(new FileNode("below.java", 49))
                .add(new FileNode("equal.java", 50))
                .add(new FileNode("above.java", 51));

        assertEquals(List.of("above.java", "equal.java"), search(root, JAVA_FILES, 50));
    }

    @Test
    void pathsAreRelativeToTheChosenRootAndUseSlash() {
        DirectoryNode core = new DirectoryNode("core").add(new FileNode("Deep.java", 1));
        DirectoryNode src = new DirectoryNode("src").add(core);
        new DirectoryNode("workspace").add(src);

        assertEquals(List.of("core/Deep.java"), search(src, JAVA_FILES, 0));
    }

    @Test
    void pathsAreSortedByFullPathLexicographically() {
        DirectoryNode dashed = new DirectoryNode("src-old").add(new FileNode("A.java", 1));
        DirectoryNode plain = new DirectoryNode("src").add(new FileNode("A.java", 1));
        DirectoryNode root = new DirectoryNode("root")
                .add(plain)
                .add(dashed)
                .add(new FileNode("Z.java", 1));

        assertEquals(List.of("Z.java", "src-old/A.java", "src/A.java"), search(root, ANY_NAME, 0));
    }

    @Test
    void resultDoesNotDependOnInsertionOrder() {
        DirectoryNode first = new DirectoryNode("root")
                .add(new FileNode("b.txt", 1))
                .add(new FileNode("a.txt", 1));
        DirectoryNode second = new DirectoryNode("root")
                .add(new FileNode("a.txt", 1))
                .add(new FileNode("b.txt", 1));

        assertEquals(search(first, ANY_NAME, 0), search(second, ANY_NAME, 0));
    }

    @Test
    void fileRootReportsItsOwnName() {
        SearchVisitor visitor = new SearchVisitor(JAVA_FILES, 0);

        new FileNode("Solo.java", 3).accept(visitor);

        assertEquals(List.of("Solo.java"), visitor.getMatchingPaths());
    }

    @Test
    void noMatchReturnsEmptyList() {
        assertEquals(List.of(), search(workspace(), name -> name.endsWith(".exe"), 0));
    }

    @Test
    void emptyDirectoryReturnsEmptyList() {
        assertEquals(List.of(), search(new DirectoryNode("empty"), ANY_NAME, 0));
    }

    @Test
    void repeatedRunDoesNotDuplicateOrKeepOldMatches() {
        SearchVisitor visitor = new SearchVisitor(JAVA_FILES, 900);
        DirectoryNode tree = workspace();

        tree.accept(visitor);
        tree.accept(visitor);
        assertEquals(List.of("src/core/Engine.java"), visitor.getMatchingPaths());

        new DirectoryNode("empty").accept(visitor);
        assertEquals(List.of(), visitor.getMatchingPaths());
    }

    @Test
    void failedRunDoesNotCorruptTheNextRun() {
        List<Boolean> shouldFail = new java.util.ArrayList<>(List.of(true));
        SearchVisitor visitor = new SearchVisitor(name -> {
            if (shouldFail.get(0)) {
                throw new IllegalStateException("criterion failed");
            }
            return true;
        }, 0);
        DirectoryNode nested = new DirectoryNode("nested").add(new FileNode("a.txt", 1));
        DirectoryNode root = new DirectoryNode("root").add(nested);

        assertThrows(IllegalStateException.class, () -> root.accept(visitor));
        shouldFail.set(0, false);
        root.accept(visitor);

        assertEquals(List.of("nested/a.txt"), visitor.getMatchingPaths());
    }

    @Test
    void returnedPathsCannotBeModified() {
        List<String> paths = search(workspace(), JAVA_FILES, 0);

        assertThrows(UnsupportedOperationException.class, () -> paths.add("hacked.java"));
    }

    @Test
    void invalidConfigurationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SearchVisitor(null, 0));
        assertThrows(IllegalArgumentException.class, () -> new SearchVisitor(ANY_NAME, -1));
    }
}

package kz.aitu.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.Element;
import kz.aitu.filesystem.model.FileNode;
import kz.aitu.filesystem.visitor.StatisticsVisitor;
import org.junit.jupiter.api.Test;

class StatisticsVisitorTest {

    private final StatisticsVisitor visitor = new StatisticsVisitor();

    private DirectoryNode library() {
        DirectoryNode fiction = new DirectoryNode("fiction")
                .add(new FileNode("novel.epub", 500))
                .add(new FileNode("story.epub", 200));
        DirectoryNode books = new DirectoryNode("books").add(fiction);
        return new DirectoryNode("library")
                .add(books)
                .add(new DirectoryNode("films"))
                .add(new FileNode("index.txt", 10));
    }

    private void assertStatistics(int files, int directories, int depth) {
        assertEquals(files, visitor.getFileCount());
        assertEquals(directories, visitor.getDirectoryCount());
        assertEquals(depth, visitor.getMaxDepth());
    }

    private void visit(Element root) {
        root.accept(visitor);
    }

    @Test
    void countsNestedElementsAndMaximumDepth() {
        visit(library());

        assertStatistics(3, 4, 3);
    }

    @Test
    void emptyDirectoryRootCountsItselfAtDepthZero() {
        visit(new DirectoryNode("empty"));

        assertStatistics(0, 1, 0);
    }

    @Test
    void fileRootHasNoDirectoriesAndDepthZero() {
        visit(new FileNode("alone.txt", 1));

        assertStatistics(1, 0, 0);
    }

    @Test
    void deepestEmptyDirectoryDefinesMaximumDepth() {
        DirectoryNode c = new DirectoryNode("c");
        DirectoryNode b = new DirectoryNode("b").add(c);
        DirectoryNode a = new DirectoryNode("a").add(b).add(new FileNode("shallow.txt", 1));

        visit(a);

        assertStatistics(1, 3, 2);
    }

    @Test
    void repeatedRunDoesNotMultiplyCounts() {
        DirectoryNode tree = library();

        visit(tree);
        visit(tree);

        assertStatistics(3, 4, 3);
    }

    @Test
    void smallerTreeAfterLargerTreeDoesNotKeepOldDepth() {
        visit(library());

        visit(new DirectoryNode("small").add(new FileNode("one.txt", 1)));

        assertStatistics(1, 1, 1);
    }
}

package kz.aitu.filesystem;

import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.FileNode;
import kz.aitu.filesystem.visitor.SearchVisitor;
import kz.aitu.filesystem.visitor.StatisticsVisitor;
import kz.aitu.filesystem.visitor.TotalSizeVisitor;

public final class Main {

    private static final String SEARCH_SUFFIX = ".java";
    private static final long SEARCH_MINIMUM_SIZE = 2048;

    private Main() {
    }

    public static void main(String[] args) {
        DirectoryNode root = buildCampusProject();

        printTotalSize(root);
        printSearch(root);
        printStatistics(root);
        printRepeatedRun(root);
        printRejectedChange(root);
    }

    private static DirectoryNode buildCampusProject() {
        DirectoryNode archive = new DirectoryNode("archive");
        DirectoryNode docs = new DirectoryNode("docs")
                .add(new FileNode("guide.pdf", 52000))
                .add(new FileNode("notes.txt", 800))
                .add(archive);
        DirectoryNode util = new DirectoryNode("util")
                .add(new FileNode("Parser.java", 2048))
                .add(new FileNode("Strings.java", 512));
        DirectoryNode src = new DirectoryNode("src")
                .add(new FileNode("App.java", 4096))
                .add(util);
        DirectoryNode legacy = new DirectoryNode("src-old")
                .add(new FileNode("App.java", 3000));
        return new DirectoryNode("campus-project")
                .add(new FileNode("README.md", 1200))
                .add(docs)
                .add(src)
                .add(legacy);
    }

    private static void printTotalSize(DirectoryNode root) {
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        root.accept(visitor);
        System.out.println("Total size: " + visitor.getTotalSize() + " bytes");
    }

    private static void printSearch(DirectoryNode root) {
        SearchVisitor visitor = new SearchVisitor(
                name -> name.endsWith(SEARCH_SUFFIX), SEARCH_MINIMUM_SIZE);
        root.accept(visitor);
        System.out.println("Search name ends with '" + SEARCH_SUFFIX
                + "' and size >= " + SEARCH_MINIMUM_SIZE + ":");
        visitor.getMatchingPaths().forEach(path -> System.out.println("  " + path));
    }

    private static void printStatistics(DirectoryNode root) {
        StatisticsVisitor visitor = new StatisticsVisitor();
        root.accept(visitor);
        System.out.println("Files: " + visitor.getFileCount());
        System.out.println("Directories: " + visitor.getDirectoryCount());
        System.out.println("Maximum depth: " + visitor.getMaxDepth());
    }

    private static void printRepeatedRun(DirectoryNode root) {
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        root.accept(visitor);
        root.accept(visitor);
        System.out.println("Total size after running the same visitor twice: "
                + visitor.getTotalSize() + " bytes");
    }

    private static void printRejectedChange(DirectoryNode root) {
        try {
            root.add(new FileNode("README.md", 1));
        } catch (IllegalArgumentException exception) {
            System.out.println("Rejected change: " + exception.getMessage());
        }
    }
}

# Visitor Pattern: Advanced File System

Assignment 06 for ShP-2216 Software Design Patterns. An in-memory hierarchical file system with three independent operations implemented as visitors.

Requirements: Java 17 or newer, Maven 3.8 or newer.

## Commands

Run the tests from the project root:

```bash
mvn test
```

Run the demonstration:

```bash
mvn -q compile exec:java
```

## Structure

| Path | Purpose |
| --- | --- |
| `model/Element` | Common interface with `getName()` and `accept(Visitor)` |
| `model/AbstractElement` | Shared name and parent ownership |
| `model/FileNode` | Leaf with a non-negative byte size |
| `model/DirectoryNode` | Named children and all tree integrity rules |
| `visitor/Visitor` | Overloaded `visit(FileNode)` and `visit(DirectoryNode)` |
| `visitor/TotalSizeVisitor` | Sum of file sizes in a subtree |
| `visitor/SearchVisitor` | Filename criterion plus inclusive minimum size |
| `visitor/StatisticsVisitor` | File count, directory count, maximum depth |
| `Main` | Deterministic demonstration on a fixed tree |

## Design decisions

### Double dispatch

`element.accept(visitor)` selects the implementation by the element's runtime type. Inside it, `visitor.visit(this)` selects the overload by the static type of `this`. No `instanceof`, `getClass()`, reflection or casts are used.

### Traversal owner

Visitors own the traversal. Each `visit(DirectoryNode)` loops over `getChildren()` and calls `child.accept(this)`. The tree has no cycles and each element has one parent, so every reachable node is visited exactly once per run. This keeps the elements small and lets each visitor track the path or depth it needs. The cost is one repeated loop per visitor.

### Tree integrity

`DirectoryNode.add` is the only way to change the tree. It rejects a null child, an element that already has a parent, a duplicate sibling name and any change that would create a cycle. Invalid input throws `IllegalArgumentException` before any state changes. Names must not be blank or contain `/`. `getChildren()` returns an immutable copy.

### Visitor state

Visitor instances are reusable. Each visitor keeps a `depth` counter, and a `visit` call at depth 0 starts a new run and clears earlier results. Results always describe the latest run only, so they do not depend on run order. Getters return values or immutable copies. Instances are not thread-safe.

### Paths and depth

Search paths are relative to the element on which `accept` was called, and the root's own name is left out. Directories are joined with `/`. When the root is a file, the path is the file's name. Paths are kept in a `TreeSet`, so they are sorted lexicographically by the full path string, which means `src-old/App.java` comes before `src/App.java`.

The root has depth 0 and each level below adds 1. Maximum depth is the largest depth of any visited file or directory, including empty directories.

### Extension boundary

A new operation is one new class implementing `Visitor`. `StatisticsVisitor` was added this way: its commit changes no file in `model/`. A new element type is the opposite case, because `Visitor` needs a new overload and every existing visitor must implement it. The compiler reports each place that needs the change, but all visitors have to be edited. Visitor therefore suits a stable set of element types with a growing set of operations.

## Tests

| Check | Test class |
| --- | --- |
| T1 Dispatch reaches the right overload | `ElementDispatchTest` |
| T2 Size across nesting levels | `TotalSizeVisitorTest` |
| T3 Search combines both filters | `SearchVisitorTest` |
| T4 Deterministic root-relative paths | `SearchVisitorTest` |
| T5 Counts and maximum depth | `StatisticsVisitorTest` |
| T6 Empty structures and no matches | `TotalSizeVisitorTest`, `SearchVisitorTest`, `StatisticsVisitorTest` |
| T7 Repeat runs keep no old state | `TotalSizeVisitorTest`, `SearchVisitorTest`, `StatisticsVisitorTest` |
| T8 Invalid mutations rejected, valid nesting kept | `TreeIntegrityTest` |

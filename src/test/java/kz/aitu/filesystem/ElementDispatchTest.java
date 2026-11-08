package kz.aitu.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import kz.aitu.filesystem.model.DirectoryNode;
import kz.aitu.filesystem.model.Element;
import kz.aitu.filesystem.model.FileNode;
import kz.aitu.filesystem.visitor.Visitor;
import org.junit.jupiter.api.Test;

class ElementDispatchTest {

    private static final class RecordingVisitor implements Visitor {

        private final List<String> calls = new ArrayList<>();

        @Override
        public void visit(FileNode file) {
            calls.add("file:" + file.getName());
        }

        @Override
        public void visit(DirectoryNode directory) {
            calls.add("directory:" + directory.getName());
        }
    }

    @Test
    void fileNodeReachesFileOverload() {
        RecordingVisitor visitor = new RecordingVisitor();

        new FileNode("photo.png", 300).accept(visitor);

        assertEquals(List.of("file:photo.png"), visitor.calls);
    }

    @Test
    void directoryNodeReachesDirectoryOverload() {
        RecordingVisitor visitor = new RecordingVisitor();

        new DirectoryNode("music").accept(visitor);

        assertEquals(List.of("directory:music"), visitor.calls);
    }

    @Test
    void elementReferencesDispatchByRuntimeType() {
        RecordingVisitor visitor = new RecordingVisitor();
        List<Element> elements = List.of(new DirectoryNode("music"), new FileNode("song.mp3", 9));

        elements.forEach(element -> element.accept(visitor));

        assertEquals(List.of("directory:music", "file:song.mp3"), visitor.calls);
    }
}

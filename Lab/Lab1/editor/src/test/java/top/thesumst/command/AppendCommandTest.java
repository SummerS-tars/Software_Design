package top.thesumst.command;

import org.junit.After;
import org.junit.Test;
import top.thesumst.workspace.EditorInstance;
import top.thesumst.observer.FileLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public class AppendCommandTest {

    private final Path tempLogPath = Path.of("append_test.txt");

    @After
    public void cleanup() throws IOException {
        // 删除测试生成的文件及日志
        Files.deleteIfExists(tempLogPath);
        Files.deleteIfExists(Path.of(".append_test.txt.log"));
    }

    @Test
    public void testSingleLineAppendUndoRedo() {
        EditorInstance editor = new EditorInstance(tempLogPath.toString());
        AppendCommand cmd = new AppendCommand(editor.getBuffer(), "Hello");
        editor.getHistory().push(cmd);
        assertEquals(1, editor.getBuffer().getSize());
        assertEquals("Hello", editor.getBuffer().getLine(1));

        assertTrue(editor.getHistory().undo());
        assertEquals(0, editor.getBuffer().getSize());

        assertTrue(editor.getHistory().redo());
        assertEquals(1, editor.getBuffer().getSize());
        assertEquals("Hello", editor.getBuffer().getLine(1));
    }

    @Test
    public void testMultiLineAppendUndoRedo() {
        EditorInstance editor = new EditorInstance(tempLogPath.toString());
        AppendCommand cmd = new AppendCommand(editor.getBuffer(), "A\nB\nC");
        editor.getHistory().push(cmd);
        assertEquals(3, editor.getBuffer().getSize());
        assertEquals(List.of("A","B","C"), editor.getBuffer().getLines());

        assertTrue(editor.getHistory().undo());
        assertEquals(0, editor.getBuffer().getSize());

        assertTrue(editor.getHistory().redo());
        assertEquals(3, editor.getBuffer().getSize());
    }

    @Test
    public void testAppendLogging() throws IOException {
        EditorInstance editor = new EditorInstance(tempLogPath.toString());
        FileLogger logger = new FileLogger(editor.getFilePath());
        editor.addObserver(logger);
        editor.setLoggingEnabled(true); // 标记开启日志（实际写入由观察者完成）

        AppendCommand cmd = new AppendCommand(editor.getBuffer(), "LogLine");
        editor.getHistory().push(cmd);

        Path logFile = Path.of(logger.getLogFilePath());
    assertTrue("日志文件未创建", Files.exists(logFile));
        String content = Files.readString(logFile);
    assertTrue("日志未包含 AppendCommand 记录: " + content, content.contains("AppendCommand"));
    }
}

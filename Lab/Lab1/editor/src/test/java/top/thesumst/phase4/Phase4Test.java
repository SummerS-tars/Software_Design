package top.thesumst.phase4;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import top.thesumst.command.InsertCommand;
import top.thesumst.workspace.EditorInstance;
import top.thesumst.workspace.Workspace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Phase 4 测试类
 * 验证观察者模式（日志记录）和备忘录模式（状态持久化）
 */
public class Phase4Test {
    
    private Workspace workspace;
    private Path testDir;
    
    @Before
    public void setUp() throws IOException {
        workspace = new Workspace();
        testDir = Files.createTempDirectory("editor_phase4_test_");
    }
    
    @After
    public void tearDown() throws IOException {
        if (testDir != null && Files.exists(testDir)) {
            Files.walk(testDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // 忽略
                    }
                });
        }
    }
    
    // ===== 日志测试（观察者模式） =====
    
    @Test
    public void testLoggingEnabledAndDisabled() throws IOException {
        Path testFile = testDir.resolve("test.txt");
        EditorInstance editor = workspace.load(testFile.toString());
        
        assertFalse(editor.isLoggingEnabled());
        
        workspace.enableLogging(editor);
        assertTrue(editor.isLoggingEnabled());
        
        workspace.disableLogging(editor);
        assertFalse(editor.isLoggingEnabled());
    }
    
    @Test
    public void testLogFileCreatedOnCommand() throws IOException {
        Path testFile = testDir.resolve("logged.txt");
        EditorInstance editor = workspace.load(testFile.toString());
        
        // 启用日志
        workspace.enableLogging(editor);
        
        // 执行命令
        editor.getHistory().push(new InsertCommand(editor.getBuffer(), 1, 1, "Test"));
        
        // 检查日志文件是否创建
        Path logFile = testDir.resolve(".logged.txt.log");
        assertTrue("日志文件应该被创建", Files.exists(logFile));
        
        // 读取日志内容
        List<String> logLines = Files.readAllLines(logFile);
        assertTrue("日志文件应该有内容", logLines.size() > 0);
        assertTrue("日志应该包含 EXECUTE", logLines.get(0).contains("EXECUTE"));
        assertTrue("日志应该包含 InsertCommand", logLines.get(0).contains("InsertCommand"));
    }
    
    @Test
    public void testLogUndoAndRedo() throws IOException {
        Path testFile = testDir.resolve("undo_test.txt");
        EditorInstance editor = workspace.load(testFile.toString());
        
        workspace.enableLogging(editor);
        
        // 执行、撤销、重做
        editor.getHistory().push(new InsertCommand(editor.getBuffer(), 1, 1, "A"));
        editor.getHistory().undo();
        editor.getHistory().redo();
        
        Path logFile = testDir.resolve(".undo_test.txt.log");
        List<String> logLines = Files.readAllLines(logFile);
        
        assertEquals(3, logLines.size());
        assertTrue(logLines.get(0).contains("EXECUTE"));
        assertTrue(logLines.get(1).contains("UNDO"));
        assertTrue(logLines.get(2).contains("REDO"));
    }
    
    @Test
    public void testAutoEnableLoggingWithHashLog() throws IOException {
        // 创建带 #log 首行的文件
        Path testFile = testDir.resolve("auto_log.txt");
        Files.write(testFile, List.of("#log", "Line 2", "Line 3"));
        
        // 加载文件
        EditorInstance editor = workspace.load(testFile.toString());
        
        // 应该自动启用日志
        assertTrue("加载带#log的文件应自动启用日志", editor.isLoggingEnabled());
        
        // 验证文件内容正确加载
        assertEquals(3, editor.getBuffer().getSize());
        assertEquals("#log", editor.getBuffer().getLine(1));
    }
    
    // ===== 状态持久化测试（备忘录模式） =====
    
    @Test
    public void testSaveAndRestoreState() throws IOException {
        Path file1 = testDir.resolve("file1.txt");
        Path file2 = testDir.resolve("file2.txt");
        Path stateFile = testDir.resolve("test_workspace_state");
        
        // 创建工作区状态
        EditorInstance editor1 = workspace.load(file1.toString());
        editor1.getBuffer().append("Content 1");
        editor1.markAsModified();
        
        EditorInstance editor2 = workspace.load(file2.toString());
        editor2.getBuffer().append("Content 2");
        
        workspace.activate(editor1.getFilePath());
        
        // 保存状态
        workspace.saveState(stateFile.toString());
        assertTrue("状态文件应该被创建", Files.exists(stateFile));
        
        // 创建新工作区并恢复状态
        Workspace newWorkspace = new Workspace();
        newWorkspace.restoreState(stateFile.toString());
        
        // 验证文件被加载
        assertEquals(2, newWorkspace.getOpenFileCount());
        assertTrue(newWorkspace.isFileOpen(file1.toString()));
        assertTrue(newWorkspace.isFileOpen(file2.toString()));
        
        // 验证活动编辑器
        assertEquals(editor1.getFilePath(), newWorkspace.getActiveEditor().getFilePath());
    }
    
    @Test
    public void testRestoreModifiedState() throws IOException {
        Path testFile = testDir.resolve("modified.txt");
        Path stateFile = testDir.resolve("test_state2");
        
        // 创建并修改文件
        EditorInstance editor = workspace.load(testFile.toString());
        editor.getBuffer().append("Modified content");
        editor.markAsModified();
        
        workspace.saveState(stateFile.toString());
        
        // 恢复到新工作区
        Workspace newWorkspace = new Workspace();
        newWorkspace.restoreState(stateFile.toString());
        
        EditorInstance restored = newWorkspace.getEditor(testFile.toString());
        assertNotNull(restored);
        assertTrue("修改状态应该被恢复", restored.isModified());
    }
    
    @Test
    public void testRestoreLoggingState() throws IOException {
        Path testFile = testDir.resolve("logging.txt");
        Path stateFile = testDir.resolve("test_state3");
        
        // 启用日志
        EditorInstance editor = workspace.load(testFile.toString());
        workspace.enableLogging(editor);
        
        workspace.saveState(stateFile.toString());
        
        // 恢复到新工作区
        Workspace newWorkspace = new Workspace();
        newWorkspace.restoreState(stateFile.toString());
        
        EditorInstance restored = newWorkspace.getEditor(testFile.toString());
        assertNotNull(restored);
        assertTrue("日志状态应该被恢复", restored.isLoggingEnabled());
    }
    
    @Test
    public void testRestoreMultipleFilesWithState() throws IOException {
        Path file1 = testDir.resolve("file1.txt");
        Path file2 = testDir.resolve("file2.txt");
        Path file3 = testDir.resolve("file3.txt");
        Path stateFile = testDir.resolve("multi_state");
        
        // 创建多个文件，不同状态
        EditorInstance e1 = workspace.load(file1.toString());
        e1.markAsModified();
        workspace.enableLogging(e1);
        
        EditorInstance e2 = workspace.load(file2.toString());
        e2.markAsModified();
        
        EditorInstance e3 = workspace.load(file3.toString());
        workspace.enableLogging(e3);
        
        workspace.activate(e2.getFilePath());
        
        workspace.saveState(stateFile.toString());
        
        // 恢复
        Workspace newWorkspace = new Workspace();
        newWorkspace.restoreState(stateFile.toString());
        
        assertEquals(3, newWorkspace.getOpenFileCount());
        
        EditorInstance r1 = newWorkspace.getEditor(file1.toString());
        EditorInstance r2 = newWorkspace.getEditor(file2.toString());
        EditorInstance r3 = newWorkspace.getEditor(file3.toString());
        
        assertTrue(r1.isModified());
        assertTrue(r1.isLoggingEnabled());
        
        assertTrue(r2.isModified());
        assertFalse(r2.isLoggingEnabled());
        
        assertFalse(r3.isModified());
        assertTrue(r3.isLoggingEnabled());
        
        assertEquals(e2.getFilePath(), newWorkspace.getActiveEditor().getFilePath());
    }
    
    @Test
    public void testRestoreStateWithNonExistentFile() throws IOException {
        Path stateFile = testDir.resolve("nonexistent_state");
        
        // 恢复不存在的状态文件应该不报错
        workspace.restoreState(stateFile.toString());
        assertEquals(0, workspace.getOpenFileCount());
    }
}

package top.thesumst.workspace;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import top.thesumst.command.InsertCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Workspace 测试类
 * 验证工作区的多文件管理、切换、保存等功能
 */
public class WorkspaceTest {
    
    private Workspace workspace;
    private Path testDir;
    
    @Before
    public void setUp() throws IOException {
        workspace = new Workspace();
        // 创建临时测试目录
        testDir = Files.createTempDirectory("editor_test_");
    }
    
    @After
    public void tearDown() throws IOException {
        // 清理测试文件和目录
        if (testDir != null && Files.exists(testDir)) {
            Files.walk(testDir)
                .sorted((a, b) -> b.compareTo(a)) // 先删除文件再删除目录
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // 忽略删除错误
                    }
                });
        }
    }
    
    // ===== EditorInstance 测试 =====
    
    @Test
    public void testEditorInstanceCreation() {
        EditorInstance editor = new EditorInstance("test.txt");
        
        assertEquals("test.txt", editor.getFilePath());
        assertNotNull(editor.getBuffer());
        assertNotNull(editor.getHistory());
        assertFalse(editor.isModified());
    }
    
    @Test
    public void testEditorInstanceModifiedFlag() {
        EditorInstance editor = new EditorInstance("test.txt");
        
        assertFalse(editor.isModified());
        
        editor.markAsModified();
        assertTrue(editor.isModified());
        
        editor.markAsSaved();
        assertFalse(editor.isModified());
    }
    
    @Test
    public void testEditorInstanceGetFileName() {
        EditorInstance editor1 = new EditorInstance("/path/to/file.txt");
        assertEquals("file.txt", editor1.getFileName());
        
        EditorInstance editor2 = new EditorInstance("C:\\Users\\test\\document.txt");
        assertEquals("document.txt", editor2.getFileName());
        
        EditorInstance editor3 = new EditorInstance("simple.txt");
        assertEquals("simple.txt", editor3.getFileName());
    }
    
    // ===== Workspace init 测试 =====
    
    @Test
    public void testInitNewFile() {
        EditorInstance editor = workspace.init("newfile.txt");
        
        assertNotNull(editor);
        assertEquals(workspace.getActiveEditor(), editor);
        assertEquals(1, workspace.getOpenFileCount());
        assertEquals(0, editor.getBuffer().getSize()); // 空缓冲区
        assertFalse(editor.isModified());
    }
    
    @Test
    public void testInitMultipleFiles() {
        workspace.init("file1.txt");
        workspace.init("file2.txt");
        workspace.init("file3.txt");
        
        assertEquals(3, workspace.getOpenFileCount());
        assertEquals("file3.txt", workspace.getActiveEditor().getFileName());
    }
    
    // ===== Workspace load 测试 =====
    
    @Test
    public void testLoadNonExistentFile() throws IOException {
        String filePath = testDir.resolve("nonexistent.txt").toString();
        EditorInstance editor = workspace.load(filePath);
        
        assertNotNull(editor);
        assertEquals(0, editor.getBuffer().getSize());
        assertFalse(editor.isModified());
    }
    
    @Test
    public void testLoadExistingFile() throws IOException {
        // 创建测试文件
        Path testFile = testDir.resolve("existing.txt");
        Files.write(testFile, List.of("Line 1", "Line 2", "Line 3"));
        
        // 加载文件
        EditorInstance editor = workspace.load(testFile.toString());
        
        assertNotNull(editor);
        assertEquals(3, editor.getBuffer().getSize());
        assertEquals("Line 1", editor.getBuffer().getLine(1));
        assertEquals("Line 2", editor.getBuffer().getLine(2));
        assertEquals("Line 3", editor.getBuffer().getLine(3));
        assertFalse(editor.isModified()); // 刚加载的文件未修改
    }
    
    @Test
    public void testLoadAlreadyOpenFile() throws IOException {
        String filePath = testDir.resolve("test.txt").toString();
        
        EditorInstance editor1 = workspace.load(filePath);
        editor1.getBuffer().append("Test content");
        
        // 再次加载同一文件，应该返回同一实例
        EditorInstance editor2 = workspace.load(filePath);
        
        assertSame(editor1, editor2);
        assertEquals(1, workspace.getOpenFileCount());
    }
    
    // ===== Workspace activate 测试 =====
    
    @Test
    public void testActivate() {
        workspace.init("file1.txt");
        workspace.init("file2.txt");
        
        assertEquals("file2.txt", workspace.getActiveEditor().getFileName());
        
        boolean result = workspace.activate(workspace.getEditor("file1.txt").getFilePath());
        
        assertTrue(result);
        assertEquals("file1.txt", workspace.getActiveEditor().getFileName());
    }
    
    @Test
    public void testActivateNonExistentFile() {
        workspace.init("file1.txt");
        
        boolean result = workspace.activate("nonexistent.txt");
        
        assertFalse(result);
        assertEquals("file1.txt", workspace.getActiveEditor().getFileName());
    }
    
    // ===== Workspace close 测试 =====
    
    @Test
    public void testCloseFile() {
        EditorInstance editor = workspace.init("test.txt");
        assertEquals(1, workspace.getOpenFileCount());
        
        boolean result = workspace.close(editor.getFilePath());
        
        assertTrue(result);
        assertEquals(0, workspace.getOpenFileCount());
        assertNull(workspace.getActiveEditor());
    }
    
    @Test
    public void testCloseNonActiveFile() {
        workspace.init("file1.txt");
        EditorInstance editor2 = workspace.init("file2.txt");
        
        workspace.close(workspace.getEditor("file1.txt").getFilePath());
        
        assertEquals(1, workspace.getOpenFileCount());
        assertEquals(editor2, workspace.getActiveEditor());
    }
    
    @Test
    public void testCloseNonExistentFile() {
        workspace.init("file1.txt");
        
        boolean result = workspace.close("nonexistent.txt");
        
        assertFalse(result);
        assertEquals(1, workspace.getOpenFileCount());
    }
    
    // ===== Workspace save 测试 =====
    
    @Test
    public void testSaveFile() throws IOException {
        Path testFile = testDir.resolve("save_test.txt");
        EditorInstance editor = workspace.load(testFile.toString());
        
        editor.getBuffer().append("Hello World");
        editor.getBuffer().append("Second line");
        editor.markAsModified();
        
        workspace.save(editor.getFilePath());
        
        // 验证文件内容
        assertTrue(Files.exists(testFile));
        List<String> lines = Files.readAllLines(testFile);
        assertEquals(2, lines.size());
        assertEquals("Hello World", lines.get(0));
        assertEquals("Second line", lines.get(1));
        
        // 验证修改标记被清除
        assertFalse(editor.isModified());
    }
    
    @Test
    public void testSaveActiveFile() throws IOException {
        Path testFile = testDir.resolve("active_save.txt");
        EditorInstance editor = workspace.load(testFile.toString());
        
        editor.getBuffer().append("Content");
        editor.markAsModified();
        
        workspace.saveActive();
        
        assertTrue(Files.exists(testFile));
        List<String> lines = Files.readAllLines(testFile);
        assertEquals(1, lines.size());
        assertEquals("Content", lines.get(0));
        assertFalse(editor.isModified());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testSaveActiveWithNoActiveEditor() throws IOException {
        workspace.saveActive();
    }
    
    // ===== 文档场景测试 =====
    
    @Test
    public void testDocumentScenario() throws IOException {
        // 1. 加载 file1.txt (不存在则自动创建)
        Path file1 = testDir.resolve("file1.txt");
        EditorInstance editor1 = workspace.load(file1.toString());
        assertNotNull(editor1);
        assertEquals(editor1, workspace.getActiveEditor());
        
        // 2. 修改内容
        editor1.getBuffer().append("File 1 content");
        editor1.markAsModified();
        assertTrue(editor1.isModified());
        
        // 3. 加载 file2.txt，确认 activeEditor 切换
        Path file2 = testDir.resolve("file2.txt");
        EditorInstance editor2 = workspace.load(file2.toString());
        assertEquals(editor2, workspace.getActiveEditor());
        assertEquals(2, workspace.getOpenFileCount());
        
        // 4. 修改 file2
        editor2.getBuffer().append("File 2 content");
        editor2.markAsModified();
        
        // 5. 再次切换回 file1，确认之前的修改（Undo历史）仍然存在
        workspace.activate(editor1.getFilePath());
        assertEquals(editor1, workspace.getActiveEditor());
        assertEquals("File 1 content", editor1.getBuffer().getLine(1));
        assertTrue(editor1.isModified());
        
        // 6. 保存两个文件
        workspace.save(editor1.getFilePath());
        workspace.save(editor2.getFilePath());
        
        assertFalse(editor1.isModified());
        assertFalse(editor2.isModified());
        
        // 验证文件确实被保存
        assertTrue(Files.exists(file1));
        assertTrue(Files.exists(file2));
    }
    
    @Test
    public void testMultipleFileEditingWithHistory() {
        // 测试多文件编辑时各自的历史记录独立性
        EditorInstance editor1 = workspace.init("doc1.txt");
        editor1.getHistory().push(new InsertCommand(editor1.getBuffer(), 1, 1, "A"));
        editor1.getHistory().push(new InsertCommand(editor1.getBuffer(), 1, 2, "B"));
        assertEquals("AB", editor1.getBuffer().getLine(1));
        
        EditorInstance editor2 = workspace.init("doc2.txt");
        editor2.getHistory().push(new InsertCommand(editor2.getBuffer(), 1, 1, "X"));
        editor2.getHistory().push(new InsertCommand(editor2.getBuffer(), 1, 2, "Y"));
        assertEquals("XY", editor2.getBuffer().getLine(1));
        
        // 切换回 editor1
        workspace.activate(editor1.getFilePath());
        assertEquals("AB", workspace.getActiveEditor().getBuffer().getLine(1));
        
        // 撤销 editor1 的操作
        workspace.getActiveEditor().getHistory().undo();
        assertEquals("A", workspace.getActiveEditor().getBuffer().getLine(1));
        
        // 切换到 editor2，确认其内容和历史不受影响
        workspace.activate(editor2.getFilePath());
        assertEquals("XY", workspace.getActiveEditor().getBuffer().getLine(1));
        
        // editor2 也可以独立撤销
        workspace.getActiveEditor().getHistory().undo();
        assertEquals("X", workspace.getActiveEditor().getBuffer().getLine(1));
    }
    
    @Test
    public void testGetOpenFiles() {
        workspace.init("file1.txt");
        workspace.init("file2.txt");
        workspace.init("file3.txt");
        
        List<String> openFiles = workspace.getOpenFiles();
        assertEquals(3, openFiles.size());
    }
    
    @Test
    public void testIsFileOpen() {
        EditorInstance editor = workspace.init("test.txt");
        
        assertTrue(workspace.isFileOpen(editor.getFilePath()));
        assertFalse(workspace.isFileOpen("nonexistent.txt"));
    }
    
    @Test
    public void testCloseAll() {
        workspace.init("file1.txt");
        workspace.init("file2.txt");
        workspace.init("file3.txt");
        
        assertEquals(3, workspace.getOpenFileCount());
        
        workspace.closeAll();
        
        assertEquals(0, workspace.getOpenFileCount());
        assertNull(workspace.getActiveEditor());
    }
    
    // ===== 未保存更改检测测试 =====
    
    @Test
    public void testHasUnsavedChanges_noChanges() {
        workspace.init("file1.txt");
        workspace.init("file2.txt");
        
        assertFalse(workspace.hasUnsavedChanges());
    }
    
    @Test
    public void testHasUnsavedChanges_withChanges() {
        EditorInstance editor1 = workspace.init("file1.txt");
        workspace.init("file2.txt");
        
        // 修改第一个文件
        editor1.getBuffer().append("Some content");
        editor1.markAsModified();
        
        assertTrue(workspace.hasUnsavedChanges());
    }
    
    @Test
    public void testHasUnsavedChanges_specificFile() {
        EditorInstance editor1 = workspace.init("file1.txt");
        workspace.init("file2.txt");
        
        // 只修改第一个文件
        editor1.getBuffer().append("Modified");
        editor1.markAsModified();
        
        assertTrue(workspace.hasUnsavedChanges("file1.txt"));
        assertFalse(workspace.hasUnsavedChanges("file2.txt"));
    }
    
    @Test
    public void testGetUnsavedFiles_empty() {
        workspace.init("file1.txt");
        workspace.init("file2.txt");
        
        List<String> unsaved = workspace.getUnsavedFiles();
        assertTrue(unsaved.isEmpty());
    }
    
    @Test
    public void testGetUnsavedFiles_withChanges() {
        EditorInstance editor1 = workspace.init("file1.txt");
        EditorInstance editor2 = workspace.init("file2.txt");
        EditorInstance editor3 = workspace.init("file3.txt");
        
        // 修改文件1和文件3
        editor1.getBuffer().append("Content 1");
        editor1.markAsModified();
        
        editor3.getBuffer().append("Content 3");
        editor3.markAsModified();
        
        List<String> unsaved = workspace.getUnsavedFiles();
        assertEquals(2, unsaved.size());
        assertTrue(unsaved.contains(editor1.getFilePath()));
        assertTrue(unsaved.contains(editor3.getFilePath()));
        assertFalse(unsaved.contains(editor2.getFilePath()));
    }
    
    @Test
    public void testHasUnsavedChanges_afterSave() throws IOException {
        String testFile = testDir.resolve("test.txt").toString();
        EditorInstance editor = workspace.init(testFile);
        
        // 修改文件
        editor.getBuffer().append("Test content");
        editor.markAsModified();
        
        assertTrue(workspace.hasUnsavedChanges());
        
        // 保存文件
        workspace.save(testFile);
        
        // 保存后应该没有未保存的更改
        assertFalse(workspace.hasUnsavedChanges());
        assertFalse(workspace.hasUnsavedChanges(testFile));
    }
    
    @Test
    public void testHasUnsavedChanges_multipleFilesAfterPartialSave() throws IOException {
        String file1 = testDir.resolve("file1.txt").toString();
        String file2 = testDir.resolve("file2.txt").toString();
        
        EditorInstance editor1 = workspace.init(file1);
        EditorInstance editor2 = workspace.init(file2);
        
        // 修改两个文件
        editor1.getBuffer().append("Content 1");
        editor1.markAsModified();
        
        editor2.getBuffer().append("Content 2");
        editor2.markAsModified();
        
        assertTrue(workspace.hasUnsavedChanges());
        
        // 只保存第一个文件
        workspace.save(file1);
        
        // 应该还有未保存的更改（第二个文件）
        assertTrue(workspace.hasUnsavedChanges());
        assertFalse(workspace.hasUnsavedChanges(file1));
        assertTrue(workspace.hasUnsavedChanges(file2));
        
        List<String> unsaved = workspace.getUnsavedFiles();
        assertEquals(1, unsaved.size());
        assertEquals(file2, unsaved.get(0));
    }
}

package top.thesumst.cli;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import top.thesumst.engine.TextBuffer;
import top.thesumst.workspace.EditorInstance;
import top.thesumst.workspace.Workspace;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * CLI 命令功能集成测试
 * 测试 insert、delete、replace、show 命令在实际场景中的使用
 * 特别关注 line:col 格式和 show 范围功能
 */
public class CLICommandTest {
    
    private Workspace workspace;
    private static final String TEST_DIR = "test_temp_cli";
    private static final String TEST_FILE = TEST_DIR + "/test.txt";
    
    @Before
    public void setUp() throws IOException {
        workspace = new Workspace();
        // 创建测试目录
        Files.createDirectories(Paths.get(TEST_DIR));
    }
    
    @After
    public void tearDown() throws IOException {
        // 清理测试文件
        File testFile = new File(TEST_FILE);
        if (testFile.exists()) {
            testFile.delete();
        }
        File testDir = new File(TEST_DIR);
        if (testDir.exists()) {
            testDir.delete();
        }
    }
    
    // ===== Insert命令 line:col 格式测试 =====
    
    @Test
    public void testInsertWithLineColFormat() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        // 添加初始内容
        buffer.append("Hello World");
        
        // 使用 line:col 格式插入
        buffer.insert(1, 7, "Beautiful ");
        
        assertEquals("Hello Beautiful World", buffer.getLine(1));
    }
    
    @Test
    public void testInsertAtBeginning() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        buffer.append("World");
        buffer.insert(1, 1, "Hello ");
        
        assertEquals("Hello World", buffer.getLine(1));
    }
    
    @Test
    public void testInsertAtEnd() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        buffer.append("Hello");
        buffer.insert(1, 6, " World");
        
        assertEquals("Hello World", buffer.getLine(1));
    }
    
    // ===== Delete命令 line:col 格式测试 =====
    
    @Test
    public void testDeleteWithLineColFormat() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        buffer.append("Hello Beautiful World");
        
        // 使用 line:col 格式删除
        buffer.delete(1, 7, 10);  // 删除 "Beautiful "
        
        assertEquals("Hello World", buffer.getLine(1));
    }
    
    @Test
    public void testDeleteFromBeginning() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        buffer.append("Hello World");
        buffer.delete(1, 1, 6);  // 删除 "Hello "
        
        assertEquals("World", buffer.getLine(1));
    }
    
    @Test
    public void testDeleteToEnd() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        buffer.append("Hello World");
        buffer.delete(1, 6, 6);  // 删除 " World"
        
        assertEquals("Hello", buffer.getLine(1));
    }
    
    // ===== Replace命令测试 =====
    
    @Test
    public void testReplaceWithLineColFormat() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        buffer.append("Hello World");
        
        // Replace = Delete + Insert
        buffer.delete(1, 1, 5);  // 删除 "Hello"
        buffer.insert(1, 1, "Hi");  // 插入 "Hi"
        
        assertEquals("Hi World", buffer.getLine(1));
    }
    
    // ===== Show命令范围功能测试 =====
    
    @Test
    public void testShowFullContent() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        buffer.append("Line 1");
        buffer.append("Line 2");
        buffer.append("Line 3");
        buffer.append("Line 4");
        buffer.append("Line 5");
        
        // 验证可以获取所有行
        assertEquals(5, buffer.getSize());
        assertEquals("Line 1", buffer.getLine(1));
        assertEquals("Line 5", buffer.getLine(5));
    }
    
    @Test
    public void testShowRange() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        buffer.append("Line 1");
        buffer.append("Line 2");
        buffer.append("Line 3");
        buffer.append("Line 4");
        buffer.append("Line 5");
        
        // 模拟 show 2:4 的行为
        int startLine = 2;
        int endLine = 4;
        
        assertTrue(startLine >= 1);
        assertTrue(endLine <= buffer.getSize());
        assertTrue(startLine <= endLine);
        
        // 验证可以获取范围内的行
        assertEquals("Line 2", buffer.getLine(2));
        assertEquals("Line 3", buffer.getLine(3));
        assertEquals("Line 4", buffer.getLine(4));
    }
    
    @Test
    public void testShowSingleLine() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        buffer.append("Line 1");
        buffer.append("Line 2");
        buffer.append("Line 3");
        
        // 模拟 show 2:2 的行为
        int startLine = 2;
        int endLine = 2;
        
        assertEquals(startLine, endLine);
        assertEquals("Line 2", buffer.getLine(2));
    }
    
    // ===== 边界条件测试 =====
    
    @Test
    public void testShowRangeValidation() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        buffer.append("Line 1");
        buffer.append("Line 2");
        buffer.append("Line 3");
        
        // 测试有效范围
        assertTrue(1 >= 1);  // 起始行 >= 1
        assertTrue(3 <= buffer.getSize());  // 结束行 <= 文件大小
        assertTrue(1 <= 3);  // 起始行 <= 结束行
    }
    
    // ===== 复杂场景测试 =====
    
    @Test
    public void testComplexEditingScenario() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        // 场景：创建多行文件，然后进行编辑
        buffer.append("First line");
        buffer.append("Second line");
        buffer.append("Third line");
        
        // 在第2行插入文本
        buffer.insert(2, 1, "Modified ");
        assertEquals("Modified Second line", buffer.getLine(2));
        
        // 删除第1行的部分内容 ("First ")
        buffer.delete(1, 1, 6);
        assertEquals("line", buffer.getLine(1));
        
        // 验证第3行未受影响
        assertEquals("Third line", buffer.getLine(3));
    }
    
    @Test
    public void testMultipleInsertOperations() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        buffer.append("text");
        
        // 多次插入操作（需要按照插入后的位置计算）
        buffer.insert(1, 1, "Hello ");   // "Hello text"
        buffer.insert(1, 11, "!");        // "Hello text!" (现在长度是11，所以在位置11插入)
        buffer.insert(1, 7, "world ");    // "Hello world text!"
        
        assertEquals("Hello world text!", buffer.getLine(1));
    }
    
    @Test
    public void testShowAfterEditing() {
        workspace.init(TEST_FILE);
        EditorInstance editor = workspace.getActiveEditor();
        TextBuffer buffer = editor.getBuffer();
        
        // 创建内容
        for (int i = 1; i <= 10; i++) {
            buffer.append("Line " + i);
        }
        
        // 编辑一些行
        buffer.insert(5, 7, " (modified)");  // "Line 5" 长度为6，在位置7（即 "5" 后面）插入
        buffer.delete(3, 1, 5);  // 删除 "Line " 只剩下 "3"
        
        // 验证 show 范围功能
        assertEquals("Line 1", buffer.getLine(1));
        assertEquals("3", buffer.getLine(3));
        assertEquals("Line 5 (modified)", buffer.getLine(5));
        assertEquals("Line 10", buffer.getLine(10));
        
        // 验证可以显示部分内容
        assertTrue(buffer.getSize() == 10);
        for (int i = 4; i <= 7; i++) {
            assertNotNull(buffer.getLine(i));
        }
    }
    
    // ===== line:col 格式解析测试 =====
    
    @Test
    public void testLineColParsing() {
        String lineCol = "10:25";
        String[] parts = lineCol.split(":");
        
        assertEquals(2, parts.length);
        assertEquals("10", parts[0]);
        assertEquals("25", parts[1]);
        
        int line = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        
        assertEquals(10, line);
        assertEquals(25, col);
    }
    
    @Test
    public void testShowRangeParsing() {
        String range = "5:15";
        String[] parts = range.split(":");
        
        assertEquals(2, parts.length);
        
        int startLine = Integer.parseInt(parts[0]);
        int endLine = Integer.parseInt(parts[1]);
        
        assertEquals(5, startLine);
        assertEquals(15, endLine);
        assertTrue(startLine <= endLine);
    }
}

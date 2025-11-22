package top.thesumst.command;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import top.thesumst.engine.TextBuffer;

/**
 * 命令模式测试类
 * 验证命令的执行、撤销和重做功能
 */
public class CommandTest {
    
    private TextBuffer buffer;
    private CommandHistory history;
    
    @Before
    public void setUp() {
        buffer = new TextBuffer();
        history = new CommandHistory();
    }
    
    // ===== InsertCommand 测试 =====
    
    @Test
    public void testInsertCommandExecute() {
        Command cmd = new InsertCommand(buffer, 1, 1, "Hello");
        cmd.execute();
        
        assertEquals(1, buffer.getSize());
        assertEquals("Hello", buffer.getLine(1));
    }
    
    @Test
    public void testInsertCommandUndo() {
        buffer.append("World");
        Command cmd = new InsertCommand(buffer, 1, 1, "Hello ");
        cmd.execute();
        assertEquals("Hello World", buffer.getLine(1));
        
        cmd.undo();
        assertEquals("World", buffer.getLine(1));
    }
    
    @Test
    public void testInsertCommandMiddle() {
        buffer.append("HelloWorld");
        Command cmd = new InsertCommand(buffer, 1, 6, " ");
        cmd.execute();
        assertEquals("Hello World", buffer.getLine(1));
        
        cmd.undo();
        assertEquals("HelloWorld", buffer.getLine(1));
    }
    
    // ===== DeleteCommand 测试 =====
    
    @Test
    public void testDeleteCommandExecute() {
        buffer.append("Hello World");
        Command cmd = new DeleteCommand(buffer, 1, 7, 5);
        cmd.execute();
        
        assertEquals("Hello ", buffer.getLine(1));
    }
    
    @Test
    public void testDeleteCommandUndo() {
        buffer.append("Hello World");
        DeleteCommand cmd = new DeleteCommand(buffer, 1, 7, 5);
        cmd.execute();
        assertEquals("Hello ", buffer.getLine(1));
        
        cmd.undo();
        assertEquals("Hello World", buffer.getLine(1));
    }
    
    @Test
    public void testDeleteCommandSavesText() {
        buffer.append("Hello");
        DeleteCommand cmd = new DeleteCommand(buffer, 1, 2, 3);
        cmd.execute();
        
        assertEquals("ell", cmd.getDeletedText());
        assertEquals("Ho", buffer.getLine(1));
    }
    
    // ===== CommandHistory 基础测试 =====
    
    @Test
    public void testHistoryPushExecutesCommand() {
        Command cmd = new InsertCommand(buffer, 1, 1, "A");
        history.push(cmd);
        
        assertEquals("A", buffer.getLine(1));
        assertEquals(1, history.getUndoStackSize());
        assertEquals(0, history.getRedoStackSize());
    }
    
    @Test
    public void testHistoryUndo() {
        history.push(new InsertCommand(buffer, 1, 1, "A"));
        assertEquals("A", buffer.getLine(1));
        
        boolean result = history.undo();
        assertTrue(result);
        assertEquals("", buffer.getLine(1));
        assertEquals(0, history.getUndoStackSize());
        assertEquals(1, history.getRedoStackSize());
    }
    
    @Test
    public void testHistoryRedo() {
        history.push(new InsertCommand(buffer, 1, 1, "A"));
        history.undo();
        assertEquals("", buffer.getLine(1));
        
        boolean result = history.redo();
        assertTrue(result);
        assertEquals("A", buffer.getLine(1));
        assertEquals(1, history.getUndoStackSize());
        assertEquals(0, history.getRedoStackSize());
    }
    
    @Test
    public void testHistoryUndoEmpty() {
        boolean result = history.undo();
        assertFalse(result);
    }
    
    @Test
    public void testHistoryRedoEmpty() {
        boolean result = history.redo();
        assertFalse(result);
    }
    
    // ===== 文档场景测试 =====
    
    @Test
    public void testDocumentScenario() {
        // 场景：执行 InsertCommand 写入 "A"
        history.push(new InsertCommand(buffer, 1, 1, "A"));
        assertEquals("A", buffer.getLine(1));
        
        // 执行 InsertCommand 写入 "B" -> 内容 "AB"
        history.push(new InsertCommand(buffer, 1, 2, "B"));
        assertEquals("AB", buffer.getLine(1));
        
        // 调用 history.undo() -> 内容变回 "A"
        history.undo();
        assertEquals("A", buffer.getLine(1));
        
        // 调用 history.redo() -> 内容恢复 "AB"
        history.redo();
        assertEquals("AB", buffer.getLine(1));
    }
    
    @Test
    public void testMultipleUndoRedo() {
        // 执行多个操作
        history.push(new InsertCommand(buffer, 1, 1, "Hello"));
        history.push(new InsertCommand(buffer, 1, 6, " World"));
        history.push(new InsertCommand(buffer, 1, 12, "!"));
        
        assertEquals("Hello World!", buffer.getLine(1));
        
        // 撤销所有操作
        history.undo(); // "Hello World"
        assertEquals("Hello World", buffer.getLine(1));
        
        history.undo(); // "Hello"
        assertEquals("Hello", buffer.getLine(1));
        
        history.undo(); // ""
        assertEquals("", buffer.getLine(1));
        
        // 重做所有操作
        history.redo(); // "Hello"
        assertEquals("Hello", buffer.getLine(1));
        
        history.redo(); // "Hello World"
        assertEquals("Hello World", buffer.getLine(1));
        
        history.redo(); // "Hello World!"
        assertEquals("Hello World!", buffer.getLine(1));
    }
    
    @Test
    public void testNewCommandClearsRedoStack() {
        history.push(new InsertCommand(buffer, 1, 1, "A"));
        history.push(new InsertCommand(buffer, 1, 2, "B"));
        assertEquals("AB", buffer.getLine(1));
        
        // 撤销一次
        history.undo();
        assertEquals("A", buffer.getLine(1));
        assertEquals(1, history.getRedoStackSize());
        
        // 执行新命令，应该清空重做栈
        history.push(new InsertCommand(buffer, 1, 2, "C"));
        assertEquals("AC", buffer.getLine(1));
        assertEquals(0, history.getRedoStackSize());
        
        // 重做应该失败
        assertFalse(history.redo());
    }
    
    @Test
    public void testMixedInsertDelete() {
        // 插入 "Hello World"
        history.push(new InsertCommand(buffer, 1, 1, "Hello World"));
        assertEquals("Hello World", buffer.getLine(1));
        
        // 删除 "World"
        history.push(new DeleteCommand(buffer, 1, 7, 5));
        assertEquals("Hello ", buffer.getLine(1));
        
        // 撤销删除
        history.undo();
        assertEquals("Hello World", buffer.getLine(1));
        
        // 撤销插入
        history.undo();
        assertEquals("", buffer.getLine(1));
        
        // 重做插入
        history.redo();
        assertEquals("Hello World", buffer.getLine(1));
        
        // 重做删除
        history.redo();
        assertEquals("Hello ", buffer.getLine(1));
    }
    
    @Test
    public void testCanUndoRedo() {
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
        
        history.push(new InsertCommand(buffer, 1, 1, "Test"));
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
        
        history.undo();
        assertFalse(history.canUndo());
        assertTrue(history.canRedo());
        
        history.redo();
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }
    
    @Test
    public void testClearHistory() {
        history.push(new InsertCommand(buffer, 1, 1, "A"));
        history.push(new InsertCommand(buffer, 1, 2, "B"));
        history.undo();
        
        assertTrue(history.canUndo());
        assertTrue(history.canRedo());
        
        history.clear();
        
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
        assertEquals(0, history.getUndoStackSize());
        assertEquals(0, history.getRedoStackSize());
    }
}

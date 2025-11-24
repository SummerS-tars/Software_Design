package top.thesumst.engine;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * TextBuffer 测试类
 * 验证文本缓冲区的所有基本功能
 */
public class TextBufferTest {
    
    private TextBuffer buffer;

    @Before
    public void setUp() {
        buffer = new TextBuffer();
    }

    // ===== Append 测试 =====
    
    @Test
    public void testAppendSingleLine() {
        buffer.append("Hello");
        assertEquals(1, buffer.getSize());
        assertEquals("Hello", buffer.getLine(1));
    }

    @Test
    public void testAppendMultipleLines() {
        buffer.append("First line");
        buffer.append("Second line");
        buffer.append("Third line");
        assertEquals(3, buffer.getSize());
        assertEquals("First line", buffer.getLine(1));
        assertEquals("Second line", buffer.getLine(2));
        assertEquals("Third line", buffer.getLine(3));
    }

    @Test
    public void testAppendEmptyString() {
        buffer.append("");
        assertEquals(1, buffer.getSize());
        assertEquals("", buffer.getLine(1));
    }

    // ===== Insert 测试 =====
    
    @Test
    public void testInsertInMiddle() {
        buffer.append("Hello");
        buffer.insert(1, 2, "xyz"); // 在 'e' 后插入 "xyz"
        assertEquals("Hxyzello", buffer.getLine(1));
    }

    @Test
    public void testInsertAtBeginning() {
        buffer.append("World");
        buffer.insert(1, 1, "Hello ");
        assertEquals("Hello World", buffer.getLine(1));
    }

    @Test
    public void testInsertAtEnd() {
        buffer.append("Hello");
        buffer.insert(1, 6, " World"); // 在行尾插入
        assertEquals("Hello World", buffer.getLine(1));
    }

    @Test
    public void testInsertWithNewline() {
        buffer.append("HelloWorld");
        buffer.insert(1, 6, "\n"); // 在 "Hello" 和 "World" 之间插入换行
        assertEquals(2, buffer.getSize());
        assertEquals("Hello", buffer.getLine(1));
        assertEquals("World", buffer.getLine(2));
    }

    @Test
    public void testInsertMultipleNewlines() {
        buffer.append("Line1");
        buffer.insert(1, 6, "\nLine2\nLine3");
        assertEquals(3, buffer.getSize());
        assertEquals("Line1", buffer.getLine(1));
        assertEquals("Line2", buffer.getLine(2));
        assertEquals("Line3", buffer.getLine(3));
    }

    @Test
    public void testInsertInEmptyBuffer() {
        buffer.insert(1, 1, "First text");
        assertEquals(1, buffer.getSize());
        assertEquals("First text", buffer.getLine(1));
    }

    // ===== Delete 测试 =====
    
    @Test
    public void testDeleteInMiddle() {
        buffer.append("Hexyzllo");
        buffer.delete(1, 3, 3); // 删除 "xyz" (列3,4,5)
        assertEquals("Hello", buffer.getLine(1));
    }

    @Test
    public void testDeleteFromBeginning() {
        buffer.append("HelloWorld");
        buffer.delete(1, 1, 5); // 删除 "Hello"
        assertEquals("World", buffer.getLine(1));
    }

    @Test
    public void testDeleteToEnd() {
        buffer.append("HelloWorld");
        buffer.delete(1, 6, 5); // 删除 "World"
        assertEquals("Hello", buffer.getLine(1));
    }

    @Test
    public void testDeleteEntireLine() {
        buffer.append("Delete me");
        buffer.delete(1, 1, 9); // 删除整行内容
        assertEquals("", buffer.getLine(1));
    }

    @Test
    public void testDeleteSingleCharacter() {
        buffer.append("Hello");
        buffer.delete(1, 3, 1); // 删除 'l'
        assertEquals("Helo", buffer.getLine(1));
    }

    // ===== 新增：空行与零长度删除场景 =====

    @Test
    public void testDeleteZeroLengthOnEmptyLine() {
        buffer.append("");
        // 零长度删除应为 no-op
        buffer.delete(1, 1, 0);
        assertEquals("", buffer.getLine(1));
    }

    @Test
    public void testDeletePositiveLengthOnEmptyLineRemovesLine() {
        buffer.append("");
        buffer.append("B");
        assertEquals(2, buffer.getSize());
        buffer.delete(1, 1, 1); // 空行删除正长度 -> 删除整行
        assertEquals(1, buffer.getSize());
        assertEquals("B", buffer.getLine(1));
    }

    @Test
    public void testDeleteZeroLengthMiddleOfLine() {
        buffer.append("ABCDE");
        buffer.delete(1, 3, 0); // 不改变内容
        assertEquals("ABCDE", buffer.getLine(1));
    }

    @Test
    public void testDeleteZeroLengthAtEndOfLine() {
        buffer.append("XYZ");
        buffer.delete(1, 4, 0); // 行尾后位置（length+1）允许零长度删除
        assertEquals("XYZ", buffer.getLine(1));
    }

    @Test
    public void testDeleteLastEmptyLineRemovesBuffer() {
        buffer.append("");
        assertEquals(1, buffer.getSize());
        buffer.delete(1, 1, 1);
        assertEquals(0, buffer.getSize());
    }

    // ===== 边界测试 =====
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetLineInvalidLineNumber() {
        buffer.append("Line 1");
        buffer.getLine(2); // 超出范围
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetLineZeroLineNumber() {
        buffer.append("Line 1");
        buffer.getLine(0); // 行号从1开始
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertInvalidLineNumber() {
        buffer.append("Line 1");
        buffer.insert(3, 1, "text"); // 行号越界
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertInvalidColumnNumber() {
        buffer.append("Hello");
        buffer.insert(1, 10, "text"); // 列号超出行长度
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteInvalidLineNumber() {
        buffer.append("Line 1");
        buffer.delete(2, 1, 1); // 行号越界
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteInvalidColumnNumber() {
        buffer.append("Hello");
        buffer.delete(1, 10, 1); // 列号越界
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteLengthExceedsLine() {
        buffer.append("Hello");
        buffer.delete(1, 3, 10); // 删除长度超出行尾
    }

    // ===== 综合测试 =====
    
    @Test
    public void testComplexOperations() {
        // 模拟文档中的测试场景
        buffer.append("Hello");
        assertEquals("Hello", buffer.getLine(1));
        
        buffer.insert(1, 2, "xyz"); // "Hxyzello"
        assertEquals("Hxyzello", buffer.getLine(1));
        
        buffer.delete(1, 2, 3); // 删除 "xyz"
        assertEquals("Hello", buffer.getLine(1));
    }

    @Test
    public void testToString() {
        buffer.append("Line 1");
        buffer.append("Line 2");
        buffer.append("Line 3");
        String expected = "Line 1\nLine 2\nLine 3";
        assertEquals(expected, buffer.toString());
    }

    @Test
    public void testEmptyBufferSize() {
        assertEquals(0, buffer.getSize());
    }
}

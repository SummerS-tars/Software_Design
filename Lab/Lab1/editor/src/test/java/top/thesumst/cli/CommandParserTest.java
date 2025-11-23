package top.thesumst.cli;

import static org.junit.Assert.*;
import org.junit.Test;
import top.thesumst.cli.CommandParser.ParsedCommand;

/**
 * CommandParser 单元测试
 * 测试命令解析器对各种输入格式的处理能力
 * 
 * 主要测试内容：
 * 1. 基本命令解析
 * 2. line:col 格式解析（用于 insert/delete/replace/show）
 * 3. 双引号参数解析
 * 4. 边界条件和错误处理
 */
public class CommandParserTest {
    
    // ===== 基本命令解析测试 =====
    
    @Test
    public void testParseSimpleCommand() {
        ParsedCommand cmd = CommandParser.parse("show");
        assertEquals("show", cmd.getCommand());
        assertEquals(0, cmd.getArgCount());
    }
    
    @Test
    public void testParseCommandWithOneArg() {
        ParsedCommand cmd = CommandParser.parse("load test.txt");
        assertEquals("load", cmd.getCommand());
        assertEquals(1, cmd.getArgCount());
        assertEquals("test.txt", cmd.getArg(0));
    }
    
    @Test
    public void testParseCommandWithMultipleArgs() {
        ParsedCommand cmd = CommandParser.parse("insert 1:5 hello");
        assertEquals("insert", cmd.getCommand());
        assertEquals(2, cmd.getArgCount());
        assertEquals("1:5", cmd.getArg(0));
        assertEquals("hello", cmd.getArg(1));
    }
    
    // ===== line:col 格式解析测试 =====
    
    @Test
    public void testParseLineColFormat() {
        ParsedCommand cmd = CommandParser.parse("insert 1:5 text");
        assertEquals("insert", cmd.getCommand());
        assertEquals("1:5", cmd.getArg(0));
        
        // 验证可以正确分割
        String[] parts = cmd.getArg(0).split(":");
        assertEquals(2, parts.length);
        assertEquals("1", parts[0]);
        assertEquals("5", parts[1]);
    }
    
    @Test
    public void testParseDeleteWithLineCol() {
        ParsedCommand cmd = CommandParser.parse("delete 2:10 5");
        assertEquals("delete", cmd.getCommand());
        assertEquals(2, cmd.getArgCount());
        assertEquals("2:10", cmd.getArg(0));
        assertEquals("5", cmd.getArg(1));
    }
    
    @Test
    public void testParseReplaceWithLineCol() {
        ParsedCommand cmd = CommandParser.parse("replace 3:1 10 newtext");
        assertEquals("replace", cmd.getCommand());
        assertEquals(3, cmd.getArgCount());
        assertEquals("3:1", cmd.getArg(0));
        assertEquals("10", cmd.getArg(1));
        assertEquals("newtext", cmd.getArg(2));
    }
    
    @Test
    public void testParseShowWithRange() {
        ParsedCommand cmd = CommandParser.parse("show 1:10");
        assertEquals("show", cmd.getCommand());
        assertEquals(1, cmd.getArgCount());
        assertEquals("1:10", cmd.getArg(0));
        
        // 验证范围格式
        String[] parts = cmd.getArg(0).split(":");
        assertEquals(2, parts.length);
        assertEquals("1", parts[0]);
        assertEquals("10", parts[1]);
    }
    
    @Test
    public void testParseShowWithoutRange() {
        ParsedCommand cmd = CommandParser.parse("show");
        assertEquals("show", cmd.getCommand());
        assertEquals(0, cmd.getArgCount());
    }
    
    // ===== 引号解析测试 =====
    
    @Test
    public void testParseQuotedString() {
        ParsedCommand cmd = CommandParser.parse("append \"hello world\"");
        assertEquals("append", cmd.getCommand());
        assertEquals(1, cmd.getArgCount());
        assertEquals("hello world", cmd.getArg(0));
    }
    
    @Test
    public void testParseInsertWithQuotedText() {
        ParsedCommand cmd = CommandParser.parse("insert 1:5 \"text with spaces\"");
        assertEquals("insert", cmd.getCommand());
        assertEquals(2, cmd.getArgCount());
        assertEquals("1:5", cmd.getArg(0));
        assertEquals("text with spaces", cmd.getArg(1));
    }
    
    @Test
    public void testParseReplaceWithQuotedText() {
        ParsedCommand cmd = CommandParser.parse("replace 2:1 5 \"new text\"");
        assertEquals("replace", cmd.getCommand());
        assertEquals(3, cmd.getArgCount());
        assertEquals("2:1", cmd.getArg(0));
        assertEquals("5", cmd.getArg(1));
        assertEquals("new text", cmd.getArg(2));
    }
    
    @Test
    public void testParseMultipleQuotedStrings() {
        ParsedCommand cmd = CommandParser.parse("command \"arg1\" \"arg2\"");
        assertEquals("command", cmd.getCommand());
        assertEquals(2, cmd.getArgCount());
        assertEquals("arg1", cmd.getArg(0));
        assertEquals("arg2", cmd.getArg(1));
    }
    
    // ===== 边界条件测试 =====
    
    @Test
    public void testParseEmptyString() {
        ParsedCommand cmd = CommandParser.parse("");
        assertEquals("", cmd.getCommand());
        assertEquals(0, cmd.getArgCount());
    }
    
    @Test
    public void testParseWhitespaceOnly() {
        ParsedCommand cmd = CommandParser.parse("   ");
        assertEquals("", cmd.getCommand());
        assertEquals(0, cmd.getArgCount());
    }
    
    @Test
    public void testParseExtraSpaces() {
        ParsedCommand cmd = CommandParser.parse("  show   1:5  ");
        assertEquals("show", cmd.getCommand());
        assertEquals(1, cmd.getArgCount());
        assertEquals("1:5", cmd.getArg(0));
    }
    
    @Test
    public void testParseColonInQuotes() {
        ParsedCommand cmd = CommandParser.parse("append \"line:col format\"");
        assertEquals("append", cmd.getCommand());
        assertEquals(1, cmd.getArgCount());
        assertEquals("line:col format", cmd.getArg(0));
    }
    
    // ===== 特殊字符测试 =====
    
    @Test
    public void testParsePathWithSpaces() {
        ParsedCommand cmd = CommandParser.parse("load \"C:/My Documents/file.txt\"");
        assertEquals("load", cmd.getCommand());
        assertEquals(1, cmd.getArgCount());
        assertEquals("C:/My Documents/file.txt", cmd.getArg(0));
    }
    
    @Test
    public void testParseSpecialCharacters() {
        ParsedCommand cmd = CommandParser.parse("append \"Hello, World! @#$%\"");
        assertEquals("append", cmd.getCommand());
        assertEquals(1, cmd.getArgCount());
        assertEquals("Hello, World! @#$%", cmd.getArg(0));
    }
    
    // ===== 实际使用场景测试 =====
    
    @Test
    public void testParseCompleteInsertCommand() {
        ParsedCommand cmd = CommandParser.parse("insert 10:25 \"This is a test\"");
        assertEquals("insert", cmd.getCommand());
        assertEquals(2, cmd.getArgCount());
        
        String[] pos = cmd.getArg(0).split(":");
        assertEquals(2, pos.length);
        assertEquals("10", pos[0]);
        assertEquals("25", pos[1]);
        assertEquals("This is a test", cmd.getArg(1));
    }
    
    @Test
    public void testParseCompleteDeleteCommand() {
        ParsedCommand cmd = CommandParser.parse("delete 5:10 20");
        assertEquals("delete", cmd.getCommand());
        assertEquals(2, cmd.getArgCount());
        
        String[] pos = cmd.getArg(0).split(":");
        assertEquals("5", pos[0]);
        assertEquals("10", pos[1]);
        assertEquals("20", cmd.getArg(1));
    }
    
    @Test
    public void testParseCompleteReplaceCommand() {
        ParsedCommand cmd = CommandParser.parse("replace 1:1 5 \"New Content\"");
        assertEquals("replace", cmd.getCommand());
        assertEquals(3, cmd.getArgCount());
        
        String[] pos = cmd.getArg(0).split(":");
        assertEquals("1", pos[0]);
        assertEquals("1", pos[1]);
        assertEquals("5", cmd.getArg(1));
        assertEquals("New Content", cmd.getArg(2));
    }
    
    @Test
    public void testParseShowRangeCommand() {
        ParsedCommand cmd = CommandParser.parse("show 15:30");
        assertEquals("show", cmd.getCommand());
        assertEquals(1, cmd.getArgCount());
        
        String[] range = cmd.getArg(0).split(":");
        assertEquals("15", range[0]);
        assertEquals("30", range[1]);
    }
    
    // ===== 多种 line:col 格式测试 =====
    
    @Test
    public void testParseLargeLineNumbers() {
        ParsedCommand cmd = CommandParser.parse("insert 1000:500 text");
        String[] pos = cmd.getArg(0).split(":");
        assertEquals("1000", pos[0]);
        assertEquals("500", pos[1]);
    }
    
    @Test
    public void testParseSmallLineNumbers() {
        ParsedCommand cmd = CommandParser.parse("delete 1:1 1");
        String[] pos = cmd.getArg(0).split(":");
        assertEquals("1", pos[0]);
        assertEquals("1", pos[1]);
    }
    
    @Test
    public void testParseShowSingleLine() {
        ParsedCommand cmd = CommandParser.parse("show 5:5");
        String[] range = cmd.getArg(0).split(":");
        assertEquals("5", range[0]);
        assertEquals("5", range[1]);
    }
}

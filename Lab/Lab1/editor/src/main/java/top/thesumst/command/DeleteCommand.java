package top.thesumst.command;

import top.thesumst.engine.TextBuffer;

/**
 * DeleteCommand - 删除文本命令
 * 执行：从指定位置删除指定长度的文本
 * 撤销：将删除的文本重新插入原位置
 */
public class DeleteCommand implements Command {
    
    private final TextBuffer buffer;
    private final int line;
    private final int col;
    private final int length;
    private String deletedText; // 保存被删除的文本，用于撤销
    
    /**
     * 构造函数
     * @param buffer 文本缓冲区
     * @param line 行号（从1开始）
     * @param col 列号（从1开始）
     * @param length 要删除的字符数
     */
    public DeleteCommand(TextBuffer buffer, int line, int col, int length) {
        this.buffer = buffer;
        this.line = line;
        this.col = col;
        this.length = length;
        this.deletedText = null;
    }
    
    @Override
    public void execute() {
        // 在删除前，保存被删除的文本
        String currentLine = buffer.getLine(line);
        deletedText = currentLine.substring(col - 1, col - 1 + length);
        
        // 执行删除操作
        buffer.delete(line, col, length);
    }
    
    @Override
    public void undo() {
        // 将之前保存的文本插回原位置
        if (deletedText != null) {
            buffer.insert(line, col, deletedText);
        }
    }
    
    /**
     * 获取命令描述（用于日志）
     */
    @Override
    public String toString() {
        return String.format("DeleteCommand(line=%d, col=%d, length=%d)", line, col, length);
    }
    
    /**
     * 获取被删除的文本（用于测试）
     */
    public String getDeletedText() {
        return deletedText;
    }
}

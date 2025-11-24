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
        String currentLine = buffer.getLine(line);

        // 零长度删除：不入历史，可视为 no-op，但如果仍被调用，记录为空串并直接返回
        if (length == 0) {
            deletedText = "";
            return; // 不调用底层 delete 以免产生无意义历史记录
        }

        // 空行且 length>0：解释为删除整行，不进行 substring
        if (currentLine.length() == 0 && length > 0) {
            deletedText = "\n"; // 用换行符表示被删除了一行，便于 undo 还原空行
            buffer.delete(line, col, length);
            return;
        }

        // 边界安全后再截取待删除文本
        if (col - 1 < 0 || col - 1 + length > currentLine.length()) {
            throw new IndexOutOfBoundsException("删除范围越界: 起始列 " + col + ", 长度 " + length + ", 行长度 " + currentLine.length());
        }
        deletedText = currentLine.substring(col - 1, col - 1 + length);
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

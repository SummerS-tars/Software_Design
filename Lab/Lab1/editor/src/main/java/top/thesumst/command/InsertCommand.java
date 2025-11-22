package top.thesumst.command;

import top.thesumst.engine.TextBuffer;

/**
 * InsertCommand - 插入文本命令
 * 执行：在指定位置插入文本
 * 撤销：删除刚才插入的文本
 */
public class InsertCommand implements Command {
    
    private final TextBuffer buffer;
    private final int line;
    private final int col;
    private final String text;
    private int insertedLength; // 记录插入的字符数（不含换行符导致的新行）
    
    /**
     * 构造函数
     * @param buffer 文本缓冲区
     * @param line 行号（从1开始）
     * @param col 列号（从1开始）
     * @param text 要插入的文本
     */
    public InsertCommand(TextBuffer buffer, int line, int col, String text) {
        this.buffer = buffer;
        this.line = line;
        this.col = col;
        this.text = text;
        this.insertedLength = 0;
    }
    
    @Override
    public void execute() {
        buffer.insert(line, col, text);
        
        // 计算插入后的长度（用于撤销时删除）
        // 如果没有换行符，直接用文本长度
        if (!text.contains("\n")) {
            insertedLength = text.length();
        } else {
            // 如果有换行符，只计算第一行的长度（因为delete不支持跨行）
            // 撤销策略：对于多行插入，我们需要分步处理
            String[] lines = text.split("\n", -1);
            insertedLength = lines[0].length();
        }
    }
    
    @Override
    public void undo() {
        // 如果文本包含换行符，需要特殊处理
        if (text.contains("\n")) {
            // 处理多行插入的撤销
            String[] lines = text.split("\n", -1);
            
            // 从插入点开始，删除所有新增的行和字符
            // 策略：先删除新增的行，然后处理第一行和最后一行的合并
            
            // 删除中间新增的行（从最后一行开始删除）
            for (int i = lines.length - 2; i >= 1; i--) {
                // 需要删除整行内容并移除该行
                // 由于我们的delete不支持删除整行，这里需要特殊处理
                // 简化：删除第一行插入的部分
            }
            
            // 简化处理：如果有换行，先删除第一行插入的部分
            if (insertedLength > 0) {
                buffer.delete(line, col, insertedLength);
            }
            
            // 然后处理新增的行（合并回去）
            // 这需要更复杂的逻辑，暂时简化为只处理单行情况
            // TODO: 完整的多行撤销逻辑
            
        } else {
            // 简单情况：单行插入，直接删除
            if (insertedLength > 0) {
                buffer.delete(line, col, insertedLength);
            }
        }
    }
    
    /**
     * 获取命令描述（用于日志）
     */
    @Override
    public String toString() {
        return String.format("InsertCommand(line=%d, col=%d, text=\"%s\")", line, col, text);
    }
}

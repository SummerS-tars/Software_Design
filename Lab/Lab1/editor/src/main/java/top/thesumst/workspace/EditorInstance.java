package top.thesumst.workspace;

import top.thesumst.engine.TextBuffer;
import top.thesumst.command.CommandHistory;

/**
 * EditorInstance - 代表一个打开的文件会话
 * 包含文件路径、文本缓冲区、命令历史和修改状态
 */
public class EditorInstance {
    
    private final String filePath;           // 文件路径
    private final TextBuffer buffer;         // 文本内容
    private final CommandHistory history;    // 命令历史（撤销/重做）
    private boolean isModified;              // 修改标记
    
    /**
     * 构造函数
     * @param filePath 文件路径
     */
    public EditorInstance(String filePath) {
        this.filePath = filePath;
        this.buffer = new TextBuffer();
        this.history = new CommandHistory();
        this.isModified = false;
    }
    
    /**
     * 获取文件路径
     * @return 文件路径
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * 获取文本缓冲区
     * @return TextBuffer 实例
     */
    public TextBuffer getBuffer() {
        return buffer;
    }
    
    /**
     * 获取命令历史
     * @return CommandHistory 实例
     */
    public CommandHistory getHistory() {
        return history;
    }
    
    /**
     * 检查文件是否被修改
     * @return true 如果文件已修改
     */
    public boolean isModified() {
        return isModified;
    }
    
    /**
     * 设置修改标记
     * @param modified 修改状态
     */
    public void setModified(boolean modified) {
        this.isModified = modified;
    }
    
    /**
     * 标记文件为已修改
     */
    public void markAsModified() {
        this.isModified = true;
    }
    
    /**
     * 标记文件为未修改（通常在保存后调用）
     */
    public void markAsSaved() {
        this.isModified = false;
    }
    
    /**
     * 获取文件名（不含路径）
     * @return 文件名
     */
    public String getFileName() {
        int lastSeparator = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        if (lastSeparator >= 0) {
            return filePath.substring(lastSeparator + 1);
        }
        return filePath;
    }
    
    @Override
    public String toString() {
        return String.format("EditorInstance[file=%s, modified=%s, lines=%d]", 
                           filePath, isModified, buffer.getSize());
    }
}

package top.thesumst.workspace;

import top.thesumst.engine.TextBuffer;
import top.thesumst.command.CommandHistory;
import top.thesumst.command.Command;
import top.thesumst.observer.EditorObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * EditorInstance - 代表一个打开的文件会话
 * 包含文件路径、文本缓冲区、命令历史和修改状态
 * 支持观察者模式，可以监听命令执行事件
 */
public class EditorInstance {
    
    private final String filePath;           // 文件路径
    private final TextBuffer buffer;         // 文本内容
    private final CommandHistory history;    // 命令历史（撤销/重做）
    private boolean isModified;              // 修改标记
    private final List<EditorObserver> observers; // 观察者列表
    private boolean loggingEnabled;          // 日志开关
    
    /**
     * 构造函数
     * @param filePath 文件路径
     */
    public EditorInstance(String filePath) {
        this.filePath = filePath;
        this.buffer = new TextBuffer();
        this.history = new CommandHistory();
        this.isModified = false;
        this.observers = new ArrayList<>();
        this.loggingEnabled = false;
        
        // 设置命令历史的回调，自动通知观察者
        this.history.setOnExecute(this::notifyCommandExecuted);
        this.history.setOnUndo(this::notifyCommandUndone);
        this.history.setOnRedo(this::notifyCommandRedone);
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
    
    // ===== 观察者模式支持 =====
    
    /**
     * 注册观察者
     * @param observer 观察者对象
     */
    public void addObserver(EditorObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * 移除观察者
     * @param observer 观察者对象
     */
    public void removeObserver(EditorObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * 通知所有观察者命令已执行
     * @param command 已执行的命令
     */
    public void notifyCommandExecuted(Command command) {
        for (EditorObserver observer : observers) {
            observer.onCommandExecuted(command);
        }
    }
    
    /**
     * 通知所有观察者命令已撤销
     * @param command 已撤销的命令
     */
    public void notifyCommandUndone(Command command) {
        for (EditorObserver observer : observers) {
            observer.onCommandUndone(command);
        }
    }
    
    /**
     * 通知所有观察者命令已重做
     * @param command 已重做的命令
     */
    public void notifyCommandRedone(Command command) {
        for (EditorObserver observer : observers) {
            observer.onCommandRedone(command);
        }
    }
    
    /**
     * 检查是否启用日志
     * @return true 如果日志已启用
     */
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }
    
    /**
     * 设置日志开关
     * @param enabled 是否启用日志
     */
    public void setLoggingEnabled(boolean enabled) {
        this.loggingEnabled = enabled;
    }
    
    @Override
    public String toString() {
        return String.format("EditorInstance[file=%s, modified=%s, lines=%d, logging=%s]", 
                           filePath, isModified, buffer.getSize(), loggingEnabled);
    }
}

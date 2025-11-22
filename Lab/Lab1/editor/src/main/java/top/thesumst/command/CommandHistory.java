package top.thesumst.command;

import java.util.Stack;

/**
 * CommandHistory - 命令历史管理器
 * 使用两个栈实现撤销/重做功能
 */
public class CommandHistory {
    
    private final Stack<Command> undoStack; // 撤销栈
    private final Stack<Command> redoStack; // 重做栈
    
    /**
     * 构造函数
     */
    public CommandHistory() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }
    
    /**
     * 执行并记录新命令
     * 执行新命令时会清空重做栈
     * @param command 要执行的命令
     */
    public void push(Command command) {
        // 执行命令
        command.execute();
        
        // 将命令压入撤销栈
        undoStack.push(command);
        
        // 清空重做栈（执行新命令后，之前的重做历史失效）
        redoStack.clear();
    }
    
    /**
     * 撤销最近的一次操作
     * @return 是否成功撤销
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        
        // 从撤销栈弹出命令
        Command command = undoStack.pop();
        
        // 执行撤销操作
        command.undo();
        
        // 将命令压入重做栈
        redoStack.push(command);
        
        return true;
    }
    
    /**
     * 重做最近被撤销的操作
     * @return 是否成功重做
     */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        
        // 从重做栈弹出命令
        Command command = redoStack.pop();
        
        // 重新执行命令
        command.execute();
        
        // 将命令压入撤销栈
        undoStack.push(command);
        
        return true;
    }
    
    /**
     * 检查是否可以撤销
     * @return 如果撤销栈不为空则返回 true
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * 检查是否可以重做
     * @return 如果重做栈不为空则返回 true
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * 清空所有历史记录
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
    
    /**
     * 获取撤销栈的大小（用于测试）
     * @return 撤销栈中命令的数量
     */
    public int getUndoStackSize() {
        return undoStack.size();
    }
    
    /**
     * 获取重做栈的大小（用于测试）
     * @return 重做栈中命令的数量
     */
    public int getRedoStackSize() {
        return redoStack.size();
    }
}

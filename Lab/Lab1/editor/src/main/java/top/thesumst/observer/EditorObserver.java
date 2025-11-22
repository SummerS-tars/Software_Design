package top.thesumst.observer;

import top.thesumst.command.Command;

/**
 * EditorObserver - 观察者接口
 * 用于监听编辑器事件，实现观察者模式
 */
public interface EditorObserver {
    
    /**
     * 当命令执行后被调用
     * @param command 已执行的命令
     */
    void onCommandExecuted(Command command);
    
    /**
     * 当命令撤销后被调用
     * @param command 已撤销的命令
     */
    void onCommandUndone(Command command);
    
    /**
     * 当命令重做后被调用
     * @param command 已重做的命令
     */
    void onCommandRedone(Command command);
}

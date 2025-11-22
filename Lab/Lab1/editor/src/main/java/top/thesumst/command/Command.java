package top.thesumst.command;

/**
 * Command 接口 - 命令模式的核心
 * 所有可撤销的编辑操作都必须实现此接口
 */
public interface Command {
    
    /**
     * 执行命令
     */
    void execute();
    
    /**
     * 撤销命令（执行逆操作）
     */
    void undo();
}

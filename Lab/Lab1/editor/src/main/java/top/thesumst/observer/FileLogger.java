package top.thesumst.observer;

import top.thesumst.command.Command;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FileLogger - 文件日志记录器
 * 实现观察者模式，监听命令执行并记录到日志文件
 */
public class FileLogger implements EditorObserver {
    
    private final String logFilePath;
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 构造函数
     * @param sourceFilePath 源文件路径
     */
    public FileLogger(String sourceFilePath) {
        this.logFilePath = generateLogFilePath(sourceFilePath);
    }
    
    /**
     * 根据源文件路径生成日志文件路径
     * 规则：在同一目录下创建 .filename.log
     * @param sourceFilePath 源文件路径
     * @return 日志文件路径
     */
    private String generateLogFilePath(String sourceFilePath) {
        Path sourcePath = Paths.get(sourceFilePath);
        Path parent = sourcePath.getParent();
        String fileName = sourcePath.getFileName().toString();
        String logFileName = "." + fileName + ".log";
        
        if (parent != null) {
            return parent.resolve(logFileName).toString();
        } else {
            return logFileName;
        }
    }
    
    /**
     * 获取日志文件路径
     * @return 日志文件路径
     */
    public String getLogFilePath() {
        return logFilePath;
    }
    
    @Override
    public void onCommandExecuted(Command command) {
        logEvent("EXECUTE", command);
    }
    
    @Override
    public void onCommandUndone(Command command) {
        logEvent("UNDO", command);
    }
    
    @Override
    public void onCommandRedone(Command command) {
        logEvent("REDO", command);
    }
    
    /**
     * 记录事件到日志文件
     * @param eventType 事件类型
     * @param command 命令对象
     */
    private void logEvent(String eventType, Command command) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String commandInfo = command.toString();
        String logEntry = String.format("[%s] %s: %s%n", timestamp, eventType, commandInfo);
        
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(logFilePath, true))) { // append mode
            writer.write(logEntry);
        } catch (IOException e) {
            System.err.println("无法写入日志文件: " + e.getMessage());
        }
    }
}

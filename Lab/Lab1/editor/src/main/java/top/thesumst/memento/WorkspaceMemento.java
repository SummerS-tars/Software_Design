package top.thesumst.memento;

import java.util.ArrayList;
import java.util.List;

/**
 * WorkspaceMemento - 工作区状态备忘录
 * 实现备忘录模式，用于保存和恢复工作区状态
 */
public class WorkspaceMemento {
    
    /**
     * 文件状态信息
     */
    public static class FileState {
        private final String filePath;
        private final boolean isModified;
        private final boolean loggingEnabled;
        
        public FileState(String filePath, boolean isModified, boolean loggingEnabled) {
            this.filePath = filePath;
            this.isModified = isModified;
            this.loggingEnabled = loggingEnabled;
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public boolean isModified() {
            return isModified;
        }
        
        public boolean isLoggingEnabled() {
            return loggingEnabled;
        }
        
        @Override
        public String toString() {
            return String.format("FileState[path=%s, modified=%s, logging=%s]", 
                               filePath, isModified, loggingEnabled);
        }
    }
    
    private final List<FileState> fileStates;
    private final String activeFilePath;
    
    /**
     * 构造函数
     * @param fileStates 文件状态列表
     * @param activeFilePath 当前活动文件路径
     */
    public WorkspaceMemento(List<FileState> fileStates, String activeFilePath) {
        this.fileStates = new ArrayList<>(fileStates);
        this.activeFilePath = activeFilePath;
    }
    
    /**
     * 获取文件状态列表
     * @return 文件状态列表的副本
     */
    public List<FileState> getFileStates() {
        return new ArrayList<>(fileStates);
    }
    
    /**
     * 获取活动文件路径
     * @return 活动文件路径
     */
    public String getActiveFilePath() {
        return activeFilePath;
    }
    
    /**
     * 将备忘录序列化为字符串（简单的文本格式）
     * 格式：
     * ACTIVE:<path>
     * FILE:<path>|<modified>|<logging>
     * FILE:<path>|<modified>|<logging>
     * ...
     * @return 序列化的字符串
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        
        // 保存活动文件
        sb.append("ACTIVE:").append(activeFilePath != null ? activeFilePath : "").append("\n");
        
        // 保存所有文件状态
        for (FileState state : fileStates) {
            sb.append("FILE:")
              .append(state.getFilePath()).append("|")
              .append(state.isModified()).append("|")
              .append(state.isLoggingEnabled())
              .append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 从字符串反序列化备忘录
     * @param data 序列化的字符串
     * @return WorkspaceMemento 实例
     */
    public static WorkspaceMemento deserialize(String data) {
        String[] lines = data.split("\n");
        String activeFilePath = null;
        List<FileState> fileStates = new ArrayList<>();
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            if (line.startsWith("ACTIVE:")) {
                activeFilePath = line.substring(7);
                if (activeFilePath.isEmpty()) {
                    activeFilePath = null;
                }
            } else if (line.startsWith("FILE:")) {
                String fileInfo = line.substring(5);
                String[] parts = fileInfo.split("\\|");
                if (parts.length == 3) {
                    String path = parts[0];
                    boolean modified = Boolean.parseBoolean(parts[1]);
                    boolean logging = Boolean.parseBoolean(parts[2]);
                    fileStates.add(new FileState(path, modified, logging));
                }
            }
        }
        
        return new WorkspaceMemento(fileStates, activeFilePath);
    }
    
    @Override
    public String toString() {
        return String.format("WorkspaceMemento[files=%d, active=%s]", 
                           fileStates.size(), activeFilePath);
    }
}

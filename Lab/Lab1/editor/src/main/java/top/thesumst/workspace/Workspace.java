package top.thesumst.workspace;

import top.thesumst.observer.FileLogger;
import top.thesumst.memento.WorkspaceMemento;
import top.thesumst.memento.WorkspaceMemento.FileState;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Workspace - 工作区管理器
 * 管理所有打开的文件，支持多文件编辑、切换、保存等操作
 */
public class Workspace {
    
    private final Map<String, EditorInstance> files;  // 所有打开的文件 (路径 -> EditorInstance)
    private EditorInstance activeEditor;              // 当前活动的编辑器
    private final Map<String, FileLogger> loggers;    // 每个文件的日志记录器
    
    private static final String WORKSPACE_STATE_FILE = ".editor_workspace"; // 工作区状态文件
    
    /**
     * 构造函数
     */
    public Workspace() {
        this.files = new HashMap<>();
        this.activeEditor = null;
        this.loggers = new HashMap<>();
    }
    
    /**
     * 加载文件到工作区
     * 如果文件存在则读取内容，如果不存在则创建新文件
     * @param path 文件路径
     * @return 加载的 EditorInstance
     * @throws IOException 如果文件读取失败
     */
    public EditorInstance load(String path) throws IOException {
        // 规范化路径
        String normalizedPath = normalizePath(path);
        
        // 如果文件已经打开，直接返回并设为活动编辑器
        if (files.containsKey(normalizedPath)) {
            activeEditor = files.get(normalizedPath);
            return activeEditor;
        }
        
        // 创建新的编辑器实例
        EditorInstance editor = new EditorInstance(normalizedPath);
        
        // 检查文件是否存在
        Path filePath = Paths.get(normalizedPath);
        if (Files.exists(filePath)) {
            // 读取文件内容
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            
            // 检查首行是否为 #log，自动启用日志
            boolean autoEnableLog = false;
            if (!lines.isEmpty() && lines.get(0).trim().equals("#log")) {
                autoEnableLog = true;
            }
            
            for (String line : lines) {
                editor.getBuffer().append(line);
            }
            editor.markAsSaved(); // 刚加载的文件标记为未修改
            
            // 自动启用日志
            if (autoEnableLog) {
                enableLogging(editor);
            }
        } else {
            // 文件不存在，创建空缓冲区
            // 标记为未修改（新文件）
            editor.markAsSaved();
        }
        
        // 添加到工作区
        files.put(normalizedPath, editor);
        activeEditor = editor;
        
        return editor;
    }
    
    /**
     * 初始化新文件（不从磁盘读取）
     * @param path 文件路径
     * @return 创建的 EditorInstance
     */
    public EditorInstance init(String path) {
        String normalizedPath = normalizePath(path);
        
        // 如果文件已经打开，直接返回并设为活动编辑器
        if (files.containsKey(normalizedPath)) {
            activeEditor = files.get(normalizedPath);
            return activeEditor;
        }
        
        // 创建新的编辑器实例（空缓冲区）
        EditorInstance editor = new EditorInstance(normalizedPath);
        editor.markAsSaved(); // 新建文件标记为未修改
        
        // 添加到工作区
        files.put(normalizedPath, editor);
        activeEditor = editor;
        
        return editor;
    }
    
    /**
     * 切换当前活动编辑器
     * @param path 要切换到的文件路径
     * @return true 如果切换成功
     */
    public boolean activate(String path) {
        String normalizedPath = normalizePath(path);
        
        if (files.containsKey(normalizedPath)) {
            activeEditor = files.get(normalizedPath);
            return true;
        }
        
        return false;
    }
    
    /**
     * 关闭文件
     * @param path 文件路径
     * @return true 如果关闭成功
     */
    public boolean close(String path) {
        String normalizedPath = normalizePath(path);
        
        if (!files.containsKey(normalizedPath)) {
            return false;
        }
        
        EditorInstance editor = files.get(normalizedPath);
        
        // 如果是当前活动编辑器，需要处理
        if (activeEditor == editor) {
            activeEditor = null;
        }
        
        // 从工作区移除
        files.remove(normalizedPath);
        
        return true;
    }
    
    /**
     * 保存文件到磁盘
     * @param path 文件路径（如果为null则保存当前活动文件）
     * @throws IOException 如果保存失败
     */
    public void save(String path) throws IOException {
        String normalizedPath = (path == null) ? 
            (activeEditor != null ? activeEditor.getFilePath() : null) : 
            normalizePath(path);
        
        if (normalizedPath == null) {
            throw new IllegalStateException("没有要保存的文件");
        }
        
        EditorInstance editor = files.get(normalizedPath);
        if (editor == null) {
            throw new IllegalArgumentException("文件未打开: " + normalizedPath);
        }
        
        // 确保父目录存在
        Path filePath = Paths.get(normalizedPath);
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        // 写入文件
        List<String> lines = editor.getBuffer().getLines();
        Files.write(filePath, lines, StandardCharsets.UTF_8);
        
        // 标记为未修改
        editor.markAsSaved();
    }
    
    /**
     * 保存当前活动文件
     * @throws IOException 如果保存失败
     */
    public void saveActive() throws IOException {
        if (activeEditor == null) {
            throw new IllegalStateException("没有活动的编辑器");
        }
        save(activeEditor.getFilePath());
    }
    
    /**
     * 获取当前活动编辑器
     * @return 当前活动的 EditorInstance，如果没有则返回 null
     */
    public EditorInstance getActiveEditor() {
        return activeEditor;
    }
    
    /**
     * 获取指定路径的编辑器实例
     * @param path 文件路径
     * @return EditorInstance 或 null
     */
    public EditorInstance getEditor(String path) {
        return files.get(normalizePath(path));
    }
    
    /**
     * 获取所有打开的文件列表
     * @return 文件路径列表
     */
    public List<String> getOpenFiles() {
        return new ArrayList<>(files.keySet());
    }
    
    /**
     * 获取打开的文件数量
     * @return 文件数量
     */
    public int getOpenFileCount() {
        return files.size();
    }
    
    /**
     * 检查文件是否已打开
     * @param path 文件路径
     * @return true 如果文件已打开
     */
    public boolean isFileOpen(String path) {
        return files.containsKey(normalizePath(path));
    }
    
    /**
     * 关闭所有文件
     */
    public void closeAll() {
        files.clear();
        activeEditor = null;
    }
    
    /**
     * 检查是否有未保存的文件
     * @return true 如果有未保存的文件
     */
    public boolean hasUnsavedChanges() {
        for (EditorInstance editor : files.values()) {
            if (editor.isModified()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取所有未保存的文件路径列表
     * @return 未保存的文件路径列表
     */
    public List<String> getUnsavedFiles() {
        List<String> unsavedFiles = new ArrayList<>();
        for (EditorInstance editor : files.values()) {
            if (editor.isModified()) {
                unsavedFiles.add(editor.getFilePath());
            }
        }
        return unsavedFiles;
    }
    
    /**
     * 检查指定文件是否有未保存的更改
     * @param path 文件路径
     * @return true 如果文件有未保存的更改
     */
    public boolean hasUnsavedChanges(String path) {
        String normalizedPath = normalizePath(path);
        EditorInstance editor = files.get(normalizedPath);
        return editor != null && editor.isModified();
    }
    
    // ===== 日志管理 =====
    
    /**
     * 为指定编辑器启用日志
     * @param editor 编辑器实例
     */
    public void enableLogging(EditorInstance editor) {
        String path = editor.getFilePath();
        if (!loggers.containsKey(path)) {
            FileLogger logger = new FileLogger(path);
            editor.addObserver(logger);
            editor.setLoggingEnabled(true);
            loggers.put(path, logger);
        }
    }
    
    /**
     * 为当前活动编辑器启用日志
     */
    public void enableLoggingForActive() {
        if (activeEditor != null) {
            enableLogging(activeEditor);
        }
    }
    
    /**
     * 为指定编辑器禁用日志
     * @param editor 编辑器实例
     */
    public void disableLogging(EditorInstance editor) {
        String path = editor.getFilePath();
        FileLogger logger = loggers.get(path);
        if (logger != null) {
            editor.removeObserver(logger);
            editor.setLoggingEnabled(false);
            loggers.remove(path);
        }
    }
    
    /**
     * 为当前活动编辑器禁用日志
     */
    public void disableLoggingForActive() {
        if (activeEditor != null) {
            disableLogging(activeEditor);
        }
    }
    
    /**
     * 检查指定编辑器是否启用了日志
     * @param editor 编辑器实例
     * @return true 如果日志已启用
     */
    public boolean isLoggingEnabled(EditorInstance editor) {
        return editor.isLoggingEnabled();
    }
    
    // ===== 状态持久化（备忘录模式） =====
    
    /**
     * 保存工作区状态到文件
     * @throws IOException 如果保存失败
     */
    public void saveState() throws IOException {
        saveState(WORKSPACE_STATE_FILE);
    }
    
    /**
     * 保存工作区状态到指定文件
     * @param stateFile 状态文件路径
     * @throws IOException 如果保存失败
     */
    public void saveState(String stateFile) throws IOException {
        // 创建备忘录
        List<FileState> fileStates = new ArrayList<>();
        for (EditorInstance editor : files.values()) {
            FileState state = new FileState(
                editor.getFilePath(),
                editor.isModified(),
                editor.isLoggingEnabled()
            );
            fileStates.add(state);
        }
        
        String activeFilePath = (activeEditor != null) ? activeEditor.getFilePath() : null;
        WorkspaceMemento memento = new WorkspaceMemento(fileStates, activeFilePath);
        
        // 序列化并保存
        String data = memento.serialize();
        Files.write(Paths.get(stateFile), data.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 从文件恢复工作区状态
     * @throws IOException 如果读取失败
     */
    public void restoreState() throws IOException {
        restoreState(WORKSPACE_STATE_FILE);
    }
    
    /**
     * 从指定文件恢复工作区状态
     * @param stateFile 状态文件路径
     * @throws IOException 如果读取失败
     */
    public void restoreState(String stateFile) throws IOException {
        Path statePath = Paths.get(stateFile);
        if (!Files.exists(statePath)) {
            return; // 状态文件不存在，跳过恢复
        }
        
        // 读取并反序列化
        String data = new String(Files.readAllBytes(statePath), StandardCharsets.UTF_8);
        WorkspaceMemento memento = WorkspaceMemento.deserialize(data);
        
        // 恢复文件状态
        for (FileState state : memento.getFileStates()) {
            try {
                EditorInstance editor = load(state.getFilePath());
                editor.setModified(state.isModified());
                
                // 恢复日志状态
                if (state.isLoggingEnabled()) {
                    enableLogging(editor);
                }
            } catch (IOException e) {
                System.err.println("无法加载文件: " + state.getFilePath() + " - " + e.getMessage());
            }
        }
        
        // 恢复活动编辑器
        String activeFilePath = memento.getActiveFilePath();
        if (activeFilePath != null && files.containsKey(normalizePath(activeFilePath))) {
            activate(activeFilePath);
        }
    }
    
    /**
     * 规范化文件路径（处理不同操作系统的路径分隔符）
     * @param path 原始路径
     * @return 规范化后的路径
     */
    private String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        
        // 转换为绝对路径
        Path p = Paths.get(path);
        try {
            return p.toAbsolutePath().normalize().toString();
        } catch (Exception e) {
            // 如果转换失败，返回原路径
            return path.replace('\\', '/');
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Workspace[files=").append(files.size());
        if (activeEditor != null) {
            sb.append(", active=").append(activeEditor.getFileName());
        }
        sb.append("]");
        return sb.toString();
    }
}

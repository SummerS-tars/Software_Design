package top.thesumst.workspace;

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
    
    /**
     * 构造函数
     */
    public Workspace() {
        this.files = new HashMap<>();
        this.activeEditor = null;
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
            for (String line : lines) {
                editor.getBuffer().append(line);
            }
            editor.markAsSaved(); // 刚加载的文件标记为未修改
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

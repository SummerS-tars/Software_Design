package top.thesumst.cli;

import top.thesumst.workspace.Workspace;
import top.thesumst.workspace.EditorInstance;
import top.thesumst.engine.TextBuffer;
import top.thesumst.command.InsertCommand;
import top.thesumst.command.DeleteCommand;
import top.thesumst.cli.CommandParser.ParsedCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * CommandLineApp - 命令行交互程序
 * 实现文本编辑器的命令行界面
 */
public class CommandLineApp {
    
    private final Workspace workspace;
    private final BufferedReader reader;
    private boolean running;
    
    public CommandLineApp() {
        this.workspace = new Workspace();
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.running = true;
    }
    
    /**
     * 启动命令行应用
     */
    public void run() {
        System.out.println("=== 文本编辑器 ===");
        System.out.println("输入 'help' 查看帮助信息");
        System.out.println();
        
        // 尝试恢复工作区状态
        try {
            workspace.restoreState();
            if (workspace.getOpenFileCount() > 0) {
                System.out.println("已恢复工作区，打开了 " + workspace.getOpenFileCount() + " 个文件");
            }
        } catch (IOException e) {
            // 忽略恢复错误
        }
        
        while (running) {
            try {
                System.out.print("> ");
                String input = reader.readLine();
                
                if (input == null) {
                    break; // EOF
                }
                
                if (input.trim().isEmpty()) {
                    continue;
                }
                
                executeCommand(input);
                
            } catch (IOException e) {
                System.err.println("读取输入错误: " + e.getMessage());
                break;
            } catch (Exception e) {
                System.err.println("错误: " + e.getMessage());
            }
        }
        
        System.out.println("再见！");
    }
    
    /**
     * 执行命令
     */
    private void executeCommand(String input) {
        ParsedCommand cmd = CommandParser.parse(input);
        String command = cmd.getCommand().toLowerCase();
        
        try {
            switch (command) {
                // 工作区命令
                case "load" -> cmdLoad(cmd);
                case "save" -> cmdSave(cmd);
                case "init" -> cmdInit(cmd);
                case "close" -> cmdClose(cmd);
                case "edit" -> cmdEdit(cmd);
                case "editor-list" -> cmdEditorList(cmd);
                case "undo" -> cmdUndo(cmd);
                case "redo" -> cmdRedo(cmd);
                case "exit" -> cmdExit(cmd);
                
                // 编辑命令
                case "append" -> cmdAppend(cmd);
                case "insert" -> cmdInsert(cmd);
                case "delete" -> cmdDelete(cmd);
                case "replace" -> cmdReplace(cmd);
                case "show" -> cmdShow(cmd);
                
                // 日志命令
                case "log-on" -> cmdLogOn(cmd);
                case "log-off" -> cmdLogOff(cmd);
                case "log-show" -> cmdLogShow(cmd);
                
                // 辅助命令
                case "dir-tree" -> cmdDirTree(cmd);
                case "help" -> cmdHelp(cmd);
                
                default -> System.out.println("未知命令: " + command + "。输入 'help' 查看帮助。");
            }
        } catch (Exception e) {
            System.err.println("命令执行失败: " + e.getMessage());
        }
    }
    
    // ===== 工作区命令 =====
    
    private void cmdLoad(ParsedCommand cmd) throws IOException {
        if (cmd.getArgCount() < 1) {
            System.out.println("用法: load <文件路径>");
            return;
        }
        
        String path = cmd.getArg(0);
        EditorInstance editor = workspace.load(path);
        System.out.println("已加载文件: " + editor.getFileName());
    }
    
    private void cmdSave(ParsedCommand cmd) throws IOException {
        if (cmd.getArgCount() == 0) {
            // 默认保存当前文件
            EditorInstance editor = workspace.getActiveEditor();
            if (editor == null) {
                System.out.println("没有活动的编辑器");
                return;
            }
            
            try {
                workspace.saveActive();
                System.out.println("已保存: " + editor.getFileName());
            } catch (IOException e) {
                System.err.println("保存失败: " + e.getMessage());
                throw e;
            }
        } else {
            String arg = cmd.getArg(0);
            
            if ("all".equalsIgnoreCase(arg)) {
                // 保存所有文件
                List<String> openFiles = workspace.getOpenFiles();
                if (openFiles.isEmpty()) {
                    System.out.println("没有打开的文件");
                    return;
                }
                
                int savedCount = 0;
                int errorCount = 0;
                List<String> errors = new ArrayList<>();
                
                for (String filePath : openFiles) {
                    try {
                        workspace.save(filePath);
                        savedCount++;
                        EditorInstance editor = workspace.getEditor(filePath);
                        System.out.println("已保存: " + editor.getFileName());
                    } catch (IOException e) {
                        errorCount++;
                        EditorInstance editor = workspace.getEditor(filePath);
                        String errorMsg = editor.getFileName() + ": " + e.getMessage();
                        errors.add(errorMsg);
                        System.err.println("保存失败: " + errorMsg);
                    }
                }
                
                System.out.println("---");
                System.out.println("保存完成: 成功 " + savedCount + " 个，失败 " + errorCount + " 个");
                
                if (!errors.isEmpty() && errorCount > 0) {
                    throw new IOException("部分文件保存失败");
                }
            } else {
                // 保存指定文件
                try {
                    workspace.save(arg);
                    // 尝试通过路径获取编辑器实例来显示文件名
                    EditorInstance editor = workspace.getEditor(arg);
                    if (editor == null) {
                        // 如果直接获取失败，遍历查找
                        for (String path : workspace.getOpenFiles()) {
                            EditorInstance e = workspace.getEditor(path);
                            if (e != null && (e.getFilePath().equals(arg) || 
                                              e.getFilePath().endsWith(arg) ||
                                              arg.endsWith(e.getFileName()))) {
                                editor = e;
                                break;
                            }
                        }
                    }
                    System.out.println("已保存: " + (editor != null ? editor.getFileName() : arg));
                } catch (IOException e) {
                    System.err.println("保存失败: " + e.getMessage());
                    throw e;
                }
            }
        }
    }
    
    private void cmdInit(ParsedCommand cmd) {
        if (cmd.getArgCount() < 1) {
            System.out.println("用法: init <文件路径>");
            return;
        }
        
        String path = cmd.getArg(0);
        EditorInstance editor = workspace.init(path);
        System.out.println("已创建文件: " + editor.getFileName());
    }
    
    private void cmdClose(ParsedCommand cmd) {
        if (cmd.getArgCount() < 1) {
            System.out.println("用法: close <文件路径>");
            return;
        }
        
        String path = cmd.getArg(0);
        boolean success = workspace.close(path);
        if (success) {
            System.out.println("已关闭文件: " + path);
        } else {
            System.out.println("文件未打开: " + path);
        }
    }
    
    private void cmdEdit(ParsedCommand cmd) {
        if (cmd.getArgCount() < 1) {
            System.out.println("用法: edit <文件路径>");
            return;
        }
        
        String path = cmd.getArg(0);
        boolean success = workspace.activate(path);
        if (success) {
            System.out.println("已切换到文件: " + path);
        } else {
            System.out.println("文件未打开: " + path);
        }
    }
    
    private void cmdEditorList(ParsedCommand cmd) {
        List<String> files = workspace.getOpenFiles();
        if (files.isEmpty()) {
            System.out.println("没有打开的文件");
            return;
        }
        
        System.out.println("打开的文件列表:");
        EditorInstance active = workspace.getActiveEditor();
        
        for (String path : files) {
            EditorInstance editor = workspace.getEditor(path);
            String marker = (editor == active) ? "> " : "  ";
            String modified = editor.isModified() ? " [modified]" : "";
            System.out.println(marker + editor.getFileName() + modified);
        }
    }
    
    private void cmdUndo(ParsedCommand cmd) {
        EditorInstance editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动的编辑器");
            return;
        }
        
        boolean success = editor.getHistory().undo();
        if (success) {
            System.out.println("已撤销");
            editor.markAsModified();
        } else {
            System.out.println("没有可撤销的操作");
        }
    }
    
    private void cmdRedo(ParsedCommand cmd) {
        EditorInstance editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动的编辑器");
            return;
        }
        
        boolean success = editor.getHistory().redo();
        if (success) {
            System.out.println("已重做");
            editor.markAsModified();
        } else {
            System.out.println("没有可重做的操作");
        }
    }
    
    private void cmdExit(ParsedCommand cmd) {
        // 保存工作区状态
        try {
            workspace.saveState();
            System.out.println("已保存工作区状态");
        } catch (IOException e) {
            System.err.println("保存工作区状态失败: " + e.getMessage());
        }
        
        running = false;
    }
    
    // ===== 编辑命令 =====
    
    private void cmdAppend(ParsedCommand cmd) {
        EditorInstance editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动的编辑器");
            return;
        }
        
        if (cmd.getArgCount() < 1) {
            System.out.println("用法: append <文本>");
            return;
        }
        
        String text = cmd.getArg(0);
        editor.getBuffer().append(text);
        editor.markAsModified();
        System.out.println("已追加文本");
    }
    
    private void cmdInsert(ParsedCommand cmd) {
        EditorInstance editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动的编辑器");
            return;
        }
        
        if (cmd.getArgCount() < 2) {
            System.out.println("用法: insert <行:列> <文本>");
            return;
        }
        
        try {
            String[] pos = cmd.getArg(0).split(":");
            if (pos.length != 2) {
                System.out.println("位置格式错误，应为 <行:列>");
                return;
            }
            
            int line = Integer.parseInt(pos[0]);
            int col = Integer.parseInt(pos[1]);
            String text = cmd.getArg(1);
            
            InsertCommand command = new InsertCommand(editor.getBuffer(), line, col, text);
            editor.getHistory().push(command);
            editor.markAsModified();
            System.out.println("已插入文本");
        } catch (NumberFormatException e) {
            System.out.println("行号和列号必须是数字");
        }
    }
    
    private void cmdDelete(ParsedCommand cmd) {
        EditorInstance editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动的编辑器");
            return;
        }
        
        if (cmd.getArgCount() < 2) {
            System.out.println("用法: delete <行:列> <长度>");
            return;
        }
        
        try {
            String[] pos = cmd.getArg(0).split(":");
            if (pos.length != 2) {
                System.out.println("位置格式错误，应为 <行:列>");
                return;
            }
            
            int line = Integer.parseInt(pos[0]);
            int col = Integer.parseInt(pos[1]);
            int length = Integer.parseInt(cmd.getArg(1));
            
            DeleteCommand command = new DeleteCommand(editor.getBuffer(), line, col, length);
            editor.getHistory().push(command);
            editor.markAsModified();
            System.out.println("已删除文本");
        } catch (NumberFormatException e) {
            System.out.println("行号、列号和长度必须是数字");
        }
    }
    
    private void cmdReplace(ParsedCommand cmd) {
        EditorInstance editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动的编辑器");
            return;
        }
        
        if (cmd.getArgCount() < 3) {
            System.out.println("用法: replace <行:列> <长度> <新文本>");
            return;
        }
        
        try {
            String[] pos = cmd.getArg(0).split(":");
            if (pos.length != 2) {
                System.out.println("位置格式错误，应为 <行:列>");
                return;
            }
            
            int line = Integer.parseInt(pos[0]);
            int col = Integer.parseInt(pos[1]);
            int length = Integer.parseInt(cmd.getArg(1));
            String newText = cmd.getArg(2);
            
            // Replace = Delete + Insert
            DeleteCommand deleteCmd = new DeleteCommand(editor.getBuffer(), line, col, length);
            editor.getHistory().push(deleteCmd);
            
            InsertCommand insertCmd = new InsertCommand(editor.getBuffer(), line, col, newText);
            editor.getHistory().push(insertCmd);
            
            editor.markAsModified();
            System.out.println("已替换文本");
        } catch (NumberFormatException e) {
            System.out.println("行号、列号和长度必须是数字");
        }
    }
    
    private void cmdShow(ParsedCommand cmd) {
        EditorInstance editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动的编辑器");
            return;
        }
        
        TextBuffer buffer = editor.getBuffer();
        if (buffer.getSize() == 0) {
            System.out.println("(空文件)");
            return;
        }
        
        int startLine = 1;
        int endLine = buffer.getSize();
        
        // 如果提供了范围参数
        if (cmd.getArgCount() > 0) {
            String range = cmd.getArg(0);
            String[] parts = range.split(":");
            
            if (parts.length != 2) {
                System.out.println("范围格式错误，应为 <起始行:结束行>");
                return;
            }
            
            try {
                startLine = Integer.parseInt(parts[0]);
                endLine = Integer.parseInt(parts[1]);
                
                // 验证范围
                if (startLine < 1) {
                    System.out.println("起始行号不能小于1");
                    return;
                }
                
                if (endLine > buffer.getSize()) {
                    System.out.println("结束行号超出文件范围 (文件共 " + buffer.getSize() + " 行)");
                    return;
                }
                
                if (startLine > endLine) {
                    System.out.println("起始行号不能大于结束行号");
                    return;
                }
                
            } catch (NumberFormatException e) {
                System.out.println("行号必须是数字");
                return;
            }
        }
        
        // 显示指定范围的行
        for (int i = startLine; i <= endLine; i++) {
            System.out.println(i + ": " + buffer.getLine(i));
        }
    }
    
    // ===== 日志命令 =====
    
    private void cmdLogOn(ParsedCommand cmd) {
        EditorInstance editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动的编辑器");
            return;
        }
        
        workspace.enableLogging(editor);
        System.out.println("已启用日志");
    }
    
    private void cmdLogOff(ParsedCommand cmd) {
        EditorInstance editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动的编辑器");
            return;
        }
        
        workspace.disableLogging(editor);
        System.out.println("已禁用日志");
    }
    
    private void cmdLogShow(ParsedCommand cmd) throws IOException {
        EditorInstance editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动的编辑器");
            return;
        }
        
        if (!editor.isLoggingEnabled()) {
            System.out.println("当前文件未启用日志");
            return;
        }
        
        // 生成日志文件路径
        String filePath = editor.getFilePath();
        Path logPath = Paths.get(filePath).resolveSibling("." + Paths.get(filePath).getFileName() + ".log");
        
        if (!Files.exists(logPath)) {
            System.out.println("日志文件不存在");
            return;
        }
        
        List<String> lines = Files.readAllLines(logPath);
        System.out.println("=== 日志内容 ===");
        for (String line : lines) {
            System.out.println(line);
        }
    }
    
    // ===== 辅助命令 =====
    
    private void cmdDirTree(ParsedCommand cmd) {
        String path = cmd.getArgCount() > 0 ? cmd.getArg(0) : ".";
        File dir = new File(path);
        
        if (!dir.exists()) {
            System.out.println("目录不存在: " + path);
            return;
        }
        
        if (!dir.isDirectory()) {
            System.out.println("不是目录: " + path);
            return;
        }
        
        System.out.println(dir.getAbsolutePath());
        printDirTree(dir, "", true);
    }
    
    private void printDirTree(File dir, String prefix, boolean isLast) {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        // 排序：目录在前，文件在后
        java.util.Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() == b.isDirectory()) {
                return a.getName().compareToIgnoreCase(b.getName());
            }
            return a.isDirectory() ? -1 : 1;
        });
        
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            boolean last = (i == files.length - 1);
            
            String connector = last ? "└── " : "├── ";
            String name = file.getName();
            if (file.isDirectory()) {
                name += "/";
            }
            
            System.out.println(prefix + connector + name);
            
            if (file.isDirectory()) {
                String newPrefix = prefix + (last ? "    " : "│   ");
                printDirTree(file, newPrefix, last);
            }
        }
    }
    
    private void cmdHelp(ParsedCommand cmd) {
        System.out.println("=== 命令帮助 ===");
        System.out.println();
        System.out.println("工作区命令:");
        System.out.println("  load <文件>          - 加载文件");
        System.out.println("  save [file|all]      - 保存文件（默认当前文件，all保存所有）");
        System.out.println("  init <文件>          - 创建新文件");
        System.out.println("  close <文件>         - 关闭文件");
        System.out.println("  edit <文件>          - 切换当前文件");
        System.out.println("  editor-list          - 列出打开的文件");
        System.out.println("  undo                 - 撤销");
        System.out.println("  redo                 - 重做");
        System.out.println("  exit                 - 退出程序");
        System.out.println();
        System.out.println("编辑命令:");
        System.out.println("  append <文本>        - 追加一行");
        System.out.println("  insert <行:列> <文本> - 插入文本");
        System.out.println("  delete <行:列> <长度> - 删除文本");
        System.out.println("  replace <行:列> <长度> <新文本> - 替换文本");
        System.out.println("  show [起始行:结束行] - 显示文件内容（可指定范围）");
        System.out.println();
        System.out.println("日志命令:");
        System.out.println("  log-on               - 启用日志");
        System.out.println("  log-off              - 禁用日志");
        System.out.println("  log-show             - 显示日志");
        System.out.println();
        System.out.println("辅助命令:");
        System.out.println("  dir-tree [目录]      - 显示目录树");
        System.out.println("  help                 - 显示此帮助");
        System.out.println();
        System.out.println("提示: 使用双引号括起带空格的参数，如: append \"hello world\"");
    }
    
    /**
     * 主入口
     */
    public static void main(String[] args) {
        CommandLineApp app = new CommandLineApp();
        app.run();
    }
}

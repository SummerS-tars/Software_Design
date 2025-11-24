package top.thesumst.cli;

import top.thesumst.workspace.Workspace;
import top.thesumst.workspace.EditorInstance;
import top.thesumst.engine.TextBuffer;
import top.thesumst.command.InsertCommand;
import top.thesumst.command.DeleteCommand;
import top.thesumst.command.AppendCommand;
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
            System.out.println("用法: init <文件路径> [with-log]");
            return;
        }

        String path = cmd.getArg(0);
        boolean withLog = cmd.getArgCount() > 1 && "with-log".equalsIgnoreCase(cmd.getArg(1));

        EditorInstance editor = withLog ? workspace.initWithLog(path) : workspace.init(path);
        if (withLog) {
            System.out.println("已创建文件并启用日志: " + editor.getFileName());
        } else {
            System.out.println("已创建文件: " + editor.getFileName());
        }
    }
    
    private void cmdClose(ParsedCommand cmd) {
        String pathToClose = null;

        if (cmd.getArgCount() < 1) {
            // 没有参数时，关闭当前活动文件
            EditorInstance active = workspace.getActiveEditor();
            if (active == null) {
                System.out.println("没有活动的编辑器");
                return;
            }
            pathToClose = active.getFilePath();
        } else {
            String input = cmd.getArg(0);
            
            // 1. 先尝试精确匹配（完整路径）
            if (workspace.isFileOpen(input)) {
                pathToClose = input;
            } else {
                // 2. 按文件名查找
                List<String> matches = workspace.findFilesByName(input);
                
                if (matches.isEmpty()) {
                    System.out.println("文件未打开: " + input);
                    return;
                } else if (matches.size() == 1) {
                    // 只有一个匹配
                    pathToClose = matches.get(0);
                } else {
                    // 多个匹配，让用户选择
                    System.out.println("当前打开了多个名为 \"" + input + "\" 的文件：");
                    for (int i = 0; i < matches.size(); i++) {
                        System.out.printf("%d. %s%n", i + 1, matches.get(i));
                    }
                    System.out.print("请输入要操作的文件编号: ");
                    
                    try {
                        String choice = reader.readLine().trim();
                        int index = Integer.parseInt(choice) - 1;
                        
                        if (index >= 0 && index < matches.size()) {
                            pathToClose = matches.get(index);
                        } else {
                            System.out.println("无效的编号");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("请输入有效的数字");
                        return;
                    } catch (IOException e) {
                        System.err.println("读取输入失败: " + e.getMessage());
                        return;
                    }
                }
            }
        }
        
        // 检查文件是否有未保存的更改
        if (workspace.hasUnsavedChanges(pathToClose)) {
            System.out.println("警告: 文件 '" + pathToClose + "' 有未保存的更改");
            System.out.print("是否保存更改？(y/n/c - 保存/不保存/取消): ");
            
            try {
                String choice = reader.readLine().trim().toLowerCase();
                
                switch (choice) {
                    case "y", "yes" -> {
                        // 保存文件
                        try {
                            workspace.save(pathToClose);
                            System.out.println("已保存文件: " + pathToClose);
                        } catch (IOException e) {
                            System.err.println("保存失败: " + e.getMessage());
                            System.out.println("文件未关闭");
                            return;
                        }
                    }
                    case "n", "no" -> {
                        // 不保存，直接关闭
                        System.out.println("放弃更改");
                    }
                    case "c", "cancel" -> {
                        // 取消关闭操作
                        System.out.println("已取消关闭操作");
                        return;
                    }
                    default -> {
                        System.out.println("无效的选择，已取消关闭操作");
                        return;
                    }
                }
            } catch (IOException e) {
                System.err.println("读取输入失败: " + e.getMessage());
                return;
            }
        }
        
        // 关闭文件
        boolean success = workspace.close(pathToClose);
        if (success) {
            System.out.println("已关闭文件: " + pathToClose);
        }
    }
    
    private void cmdEdit(ParsedCommand cmd) {
        if (cmd.getArgCount() < 1) {
            System.out.println("用法: edit <文件路径或文件名>");
            return;
        }
        
        String input = cmd.getArg(0);
        
        // 1. 先尝试精确匹配（完整路径）
        if (workspace.activate(input)) {
            System.out.println("已切换到文件: " + input);
            return;
        }
        
        // 2. 按文件名查找
        List<String> matches = workspace.findFilesByName(input);
        
        if (matches.isEmpty()) {
            System.out.println("文件未打开: " + input);
        } else if (matches.size() == 1) {
            // 只有一个匹配，直接切换
            String matchedPath = matches.get(0);
            workspace.activate(matchedPath);
            System.out.println("已切换到文件: " + matchedPath);
        } else {
            // 多个匹配，让用户选择
            System.out.println("当前打开了多个名为 \"" + input + "\" 的文件：");
            for (int i = 0; i < matches.size(); i++) {
                System.out.printf("%d. %s%n", i + 1, matches.get(i));
            }
            System.out.print("请输入要操作的文件编号: ");
            
            try {
                String choice = reader.readLine().trim();
                int index = Integer.parseInt(choice) - 1;
                
                if (index >= 0 && index < matches.size()) {
                    String selectedPath = matches.get(index);
                    workspace.activate(selectedPath);
                    System.out.println("已切换到文件: " + selectedPath);
                } else {
                    System.out.println("无效的编号");
                }
            } catch (NumberFormatException e) {
                System.out.println("请输入有效的数字");
            } catch (IOException e) {
                System.err.println("读取输入失败: " + e.getMessage());
            }
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
        
        // 找出最长的文件名，用于对齐
        int maxFileNameLen = 0;
        for (String path : files) {
            EditorInstance editor = workspace.getEditor(path);
            maxFileNameLen = Math.max(maxFileNameLen, editor.getFileName().length());
        }
        
        for (String path : files) {
            EditorInstance editor = workspace.getEditor(path);
            String marker = (editor == active) ? "*" : " ";
            String modified = editor.isModified() ? " [modified]" : "";
            
            // 格式：* 文件名 (完整路径) [modified]
            System.out.printf("%s %-" + maxFileNameLen + "s  (%s)%s%n", 
                marker, 
                editor.getFileName(), 
                editor.getFilePath(),
                modified);
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
        // 检查是否有未保存的文件
        if (workspace.hasUnsavedChanges()) {
            List<String> unsavedFiles = workspace.getUnsavedFiles();
            System.out.println("警告: 以下文件有未保存的更改:");
            for (String file : unsavedFiles) {
                System.out.println("  - " + file);
            }
            System.out.print("是否保存所有更改？(y/n/c - 保存/不保存/取消): ");
            
            try {
                String choice = reader.readLine().trim().toLowerCase();
                
                switch (choice) {
                    case "y", "yes" -> {
                        // 保存所有文件
                        int successCount = 0;
                        int failCount = 0;
                        
                        for (String filePath : unsavedFiles) {
                            try {
                                workspace.save(filePath);
                                successCount++;
                            } catch (IOException e) {
                                System.err.println("保存文件失败 (" + filePath + "): " + e.getMessage());
                                failCount++;
                            }
                        }
                        
                        System.out.printf("保存完成: 成功 %d 个，失败 %d 个%n", successCount, failCount);
                        
                        if (failCount > 0) {
                            System.out.print("部分文件保存失败，是否仍要退出？(y/n): ");
                            String confirmExit = reader.readLine().trim().toLowerCase();
                            if (!confirmExit.equals("y") && !confirmExit.equals("yes")) {
                                System.out.println("已取消退出");
                                return;
                            }
                        }
                    }
                    case "n", "no" -> {
                        // 不保存，直接退出
                        System.out.println("放弃所有未保存的更改");
                    }
                    case "c", "cancel" -> {
                        // 取消退出操作
                        System.out.println("已取消退出");
                        return;
                    }
                    default -> {
                        System.out.println("无效的选择，已取消退出");
                        return;
                    }
                }
            } catch (IOException e) {
                System.err.println("读取输入失败: " + e.getMessage());
                return;
            }
        }
        
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
        try {
            AppendCommand command = new AppendCommand(editor.getBuffer(), text);
            editor.getHistory().push(command);
            editor.markAsModified();
            System.out.println("已追加文本");
        } catch (Exception e) {
            System.out.println("追加失败: " + e.getMessage());
        }
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

            if (length == 0) {
                System.out.println("删除长度为0，未执行任何操作");
                return;
            }
            
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
        EditorInstance target = null;
        if (cmd.getArgCount() > 0) {
            String input = cmd.getArg(0);
            if (workspace.isFileOpen(input)) {
                target = workspace.getEditor(input);
            } else {
                List<String> matches = workspace.findFilesByName(input);
                if (matches.isEmpty()) {
                    System.out.println("文件未打开: " + input);
                    return;
                } else if (matches.size() == 1) {
                    target = workspace.getEditor(matches.get(0));
                } else {
                    System.out.println("当前打开了多个名为 \"" + input + "\" 的文件：");
                    for (int i = 0; i < matches.size(); i++) {
                        System.out.printf("%d. %s%n", i + 1, matches.get(i));
                    }
                    System.out.print("请输入要操作的文件编号: ");
                    try {
                        String choice = reader.readLine().trim();
                        int index = Integer.parseInt(choice) - 1;
                        if (index >= 0 && index < matches.size()) {
                            target = workspace.getEditor(matches.get(index));
                        } else {
                            System.out.println("无效的编号");
                            return;
                        }
                    } catch (Exception e) {
                        System.out.println("输入无效");
                        return;
                    }
                }
            }
        } else {
            target = workspace.getActiveEditor();
            if (target == null) {
                System.out.println("没有活动的编辑器");
                return;
            }
        }

        workspace.enableLogging(target);
        System.out.println("已启用日志");
    }
    
    private void cmdLogOff(ParsedCommand cmd) {
        EditorInstance target = null;
        if (cmd.getArgCount() > 0) {
            String input = cmd.getArg(0);
            if (workspace.isFileOpen(input)) {
                target = workspace.getEditor(input);
            } else {
                List<String> matches = workspace.findFilesByName(input);
                if (matches.isEmpty()) {
                    System.out.println("文件未打开: " + input);
                    return;
                } else if (matches.size() == 1) {
                    target = workspace.getEditor(matches.get(0));
                } else {
                    System.out.println("当前打开了多个名为 \"" + input + "\" 的文件：");
                    for (int i = 0; i < matches.size(); i++) {
                        System.out.printf("%d. %s%n", i + 1, matches.get(i));
                    }
                    System.out.print("请输入要操作的文件编号: ");
                    try {
                        String choice = reader.readLine().trim();
                        int index = Integer.parseInt(choice) - 1;
                        if (index >= 0 && index < matches.size()) {
                            target = workspace.getEditor(matches.get(index));
                        } else {
                            System.out.println("无效的编号");
                            return;
                        }
                    } catch (Exception e) {
                        System.out.println("输入无效");
                        return;
                    }
                }
            }
        } else {
            target = workspace.getActiveEditor();
            if (target == null) {
                System.out.println("没有活动的编辑器");
                return;
            }
        }

        workspace.disableLogging(target);
        System.out.println("已禁用日志");
    }
    
    private void cmdLogShow(ParsedCommand cmd) throws IOException {
        EditorInstance target = null;
        if (cmd.getArgCount() > 0) {
            String input = cmd.getArg(0);
            if (workspace.isFileOpen(input)) {
                target = workspace.getEditor(input);
            } else {
                List<String> matches = workspace.findFilesByName(input);
                if (matches.isEmpty()) {
                    System.out.println("文件未打开: " + input);
                    return;
                } else if (matches.size() == 1) {
                    target = workspace.getEditor(matches.get(0));
                } else {
                    System.out.println("当前打开了多个名为 \"" + input + "\" 的文件：");
                    for (int i = 0; i < matches.size(); i++) {
                        System.out.printf("%d. %s%n", i + 1, matches.get(i));
                    }
                    System.out.print("请输入要操作的文件编号: ");
                    try {
                        String choice = reader.readLine().trim();
                        int index = Integer.parseInt(choice) - 1;
                        if (index >= 0 && index < matches.size()) {
                            target = workspace.getEditor(matches.get(index));
                        } else {
                            System.out.println("无效的编号");
                            return;
                        }
                    } catch (Exception e) {
                        System.out.println("输入无效");
                        return;
                    }
                }
            }
        } else {
            target = workspace.getActiveEditor();
            if (target == null) {
                System.out.println("没有活动的编辑器");
                return;
            }
        }

        if (!target.isLoggingEnabled()) {
            System.out.println("当前文件未启用日志");
            return;
        }

        // 生成日志文件路径
        String filePath = target.getFilePath();
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
    System.out.println("  load <file>             - 加载文件 (支持自动识别 #log)");
    System.out.println("  save [file|all]         - 保存当前文件、指定文件或所有文件");
    System.out.println("  init <file> [with-log]  - 创建新文件 (可选自动开启日志)");
    System.out.println("  close [file]            - 关闭当前或指定文件");
    System.out.println("  edit <file>             - 切换当前活动文件");
    System.out.println("  editor-list             - 列出打开的文件及状态");
    System.out.println("  undo                    - 撤销");
    System.out.println("  redo                    - 重做");
    System.out.println("  exit                    - 退出程序 (自动保存工作区)");
    System.out.println();
    System.out.println("编辑命令:");
    System.out.println("  append <text>                - 在末尾追加一行");
    System.out.println("  insert <line:col> <text>     - 在指定位置插入文本 (例: insert 1:5 \"text\")");
    System.out.println("  delete <line:col> <len>      - 删除指定长度字符");
    System.out.println("  replace <line:col> <len> <text> - 替换文本");
    System.out.println("  show [start:end]             - 显示全文或指定行范围 (例: show 1:10)");
    System.out.println();
    System.out.println("日志命令:");
    System.out.println("  log-on [file]           - 启用日志");
    System.out.println("  log-off [file]          - 禁用日志");
    System.out.println("  log-show [file]         - 显示日志内容");
    System.out.println();
    System.out.println("辅助命令:");
    System.out.println("  dir-tree [path]         - 显示目录树");
    System.out.println("  help                    - 显示此帮助");
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

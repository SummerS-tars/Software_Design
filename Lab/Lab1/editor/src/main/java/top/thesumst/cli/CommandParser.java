package top.thesumst.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CommandParser - 命令解析器
 * 解析用户输入的命令行，支持双引号参数
 */
public class CommandParser {
    
    /**
     * 解析命令行输入
     * 支持双引号括起来的参数（如 append "hello world"）
     * @param input 用户输入的命令行
     * @return ParsedCommand 对象
     */
    public static ParsedCommand parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ParsedCommand("", new String[0]);
        }
        
        input = input.trim();
        List<String> tokens = new ArrayList<>();
        
        // 使用正则表达式匹配：引号内的内容 或 非空格字符
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|\\S+");
        Matcher matcher = pattern.matcher(input);
        
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // 引号内的内容
                tokens.add(matcher.group(1));
            } else {
                // 普通单词
                tokens.add(matcher.group());
            }
        }
        
        if (tokens.isEmpty()) {
            return new ParsedCommand("", new String[0]);
        }
        
        String command = tokens.get(0);
        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);
        
        return new ParsedCommand(command, args);
    }
    
    /**
     * ParsedCommand - 解析后的命令对象
     */
    public static class ParsedCommand {
        private final String command;
        private final String[] args;
        
        public ParsedCommand(String command, String[] args) {
            this.command = command;
            this.args = args;
        }
        
        public String getCommand() {
            return command;
        }
        
        public String[] getArgs() {
            return args;
        }
        
        public int getArgCount() {
            return args.length;
        }
        
        public String getArg(int index) {
            if (index < 0 || index >= args.length) {
                return null;
            }
            return args[index];
        }
        
        public String getArg(int index, String defaultValue) {
            String arg = getArg(index);
            return arg != null ? arg : defaultValue;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Command: ").append(command);
            if (args.length > 0) {
                sb.append(", Args: [");
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append("\"").append(args[i]).append("\"");
                }
                sb.append("]");
            }
            return sb.toString();
        }
    }
}

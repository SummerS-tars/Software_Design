package top.thesumst.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * TextBuffer - 核心文本缓冲区
 * 使用 List<String> 存储文本行，提供基本的文本操作功能
 */
public class TextBuffer {
    private List<String> lines;

    /**
     * 构造函数，初始化空的文本缓冲区
     */
    public TextBuffer() {
        this.lines = new ArrayList<>();
    }

    /**
     * 获取总行数
     * @return 文本总行数
     */
    public int getSize() {
        return lines.size();
    }

    /**
     * 获取指定行的内容
     * @param lineNumber 行号（从1开始）
     * @return 指定行的文本内容
     * @throws IndexOutOfBoundsException 如果行号越界
     */
    public String getLine(int lineNumber) {
        if (lineNumber < 1 || lineNumber > lines.size()) {
            throw new IndexOutOfBoundsException("行号越界: " + lineNumber + "，有效范围: 1-" + lines.size());
        }
        return lines.get(lineNumber - 1); // 转换为0索引
    }

    /**
     * 在文本末尾追加一行
     * @param text 要追加的文本
     */
    public void append(String text) {
        lines.add(text);
    }

    /**
     * 在指定位置插入文本
     * @param line 行号（从1开始）
     * @param col 列号（从1开始）
     * @param text 要插入的文本
     * @throws IndexOutOfBoundsException 如果行号或列号越界
     */
    public void insert(int line, int col, String text) {
        // 处理空缓冲区的情况：如果缓冲区为空且要在第1行插入，先添加空行
        if (lines.isEmpty() && line == 1) {
            lines.add("");
        }

        // 检查行号是否有效
        if (line < 1 || line > lines.size()) {
            throw new IndexOutOfBoundsException("行号越界: " + line + "，有效范围: 1-" + lines.size());
        }

        String currentLine = lines.get(line - 1);
        
        // 检查列号是否有效（列号从1开始，最大可以是当前行长度+1，即行尾后）
        if (col < 1 || col > currentLine.length() + 1) {
            throw new IndexOutOfBoundsException("列号越界: " + col + "，有效范围: 1-" + (currentLine.length() + 1));
        }

        // 处理包含换行符的文本
        if (text.contains("\n")) {
            String[] parts = text.split("\n", -1); // -1 保留末尾空字符串
            
            // 第一部分插入到当前行
            String before = currentLine.substring(0, col - 1);
            String after = currentLine.substring(col - 1);
            lines.set(line - 1, before + parts[0]);
            
            // 中间的部分作为新行插入
            for (int i = 1; i < parts.length - 1; i++) {
                lines.add(line - 1 + i, parts[i]);
            }
            
            // 最后一部分与原行的剩余部分合并
            if (parts.length > 1) {
                lines.add(line - 1 + parts.length - 1, parts[parts.length - 1] + after);
            }
        } else {
            // 简单插入（无换行符）
            String before = currentLine.substring(0, col - 1);
            String after = currentLine.substring(col - 1);
            lines.set(line - 1, before + text + after);
        }
    }

    /**
     * 从指定位置删除指定长度的字符
     * @param line 行号（从1开始）
     * @param col 列号（从1开始）
     * @param length 要删除的字符数
     * @throws IndexOutOfBoundsException 如果行号、列号或删除长度越界
     * @throws IllegalArgumentException 如果尝试跨行删除
     */
    public void delete(int line, int col, int length) {
        // 检查行号是否有效
        if (line < 1 || line > lines.size()) {
            throw new IndexOutOfBoundsException("行号越界: " + line + "，有效范围: 1-" + lines.size());
        }

        String currentLine = lines.get(line - 1);

        // 允许零长度删除作为 no-op（不改变内容，不抛异常）
        if (length == 0) {
            // 对零长度删除，列可以在 1..行长度+1（与 insert 行尾规则一致）
            if (col < 1 || col > currentLine.length() + 1) {
                throw new IndexOutOfBoundsException("列号越界: " + col + "，有效范围: 1-" + (currentLine.length() + 1));
            }
            return; // 不执行任何修改
        }

        // 空行且 length>0：支持删除整行（行内无字符可删，解释为移除该行）
        if (currentLine.length() == 0 && length > 0) {
            // 对空行删除，列号限定为1（行内无字符）
            if (col != 1) {
                throw new IndexOutOfBoundsException("列号越界: " + col + "，有效范围: 1-1");
            }
            lines.remove(line - 1);
            return;
        }
        
        // 检查列号是否有效
        if (col < 1 || col > currentLine.length()) {
            throw new IndexOutOfBoundsException("列号越界: " + col + "，有效范围: 1-" + currentLine.length());
        }

        // 检查删除长度是否超出行尾
        if (col - 1 + length > currentLine.length()) {
            throw new IndexOutOfBoundsException("删除长度超出行尾: 列 " + col + " 开始删除 " + length + " 个字符，但行长度为 " + currentLine.length());
        }

        // 执行删除操作
        String before = currentLine.substring(0, col - 1);
        String after = currentLine.substring(col - 1 + length);
        lines.set(line - 1, before + after);
    }

    /**
     * 返回所有文本内容，用于调试
     * @return 所有行的文本内容
     */
    @Override
    public String toString() {
        return String.join("\n", lines);
    }

    /**
     * 获取所有行（用于保存文件等操作）
     * @return 文本行列表的副本
     */
    public List<String> getLines() {
        return new ArrayList<>(lines);
    }
}

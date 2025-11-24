package top.thesumst.command;

import top.thesumst.engine.TextBuffer;

import java.util.Arrays;
import java.util.List;

/**
 * AppendCommand - 追加文本行命令
 * 执行：在缓冲区末尾追加一行或多行（支持传入带换行符的文本）
 * 撤销：移除刚刚追加的行
 */
public class AppendCommand implements Command {

    private final TextBuffer buffer;
    private final List<String> linesToAdd; // 需要追加的行列表
    private final int originalSize;        // 执行前的行数，用于校验撤销

    /**
     * 构造函数
     * @param buffer 文本缓冲区
     * @param text   要追加的文本（可包含 \n 代表多行）
     */
    public AppendCommand(TextBuffer buffer, String text) {
        this.buffer = buffer;
        // 按换行符切分，保留空行（例如末尾的空字符串也代表一行）
        this.linesToAdd = Arrays.asList(text.split("\n", -1));
        this.originalSize = buffer.getSize();
    }

    @Override
    public void execute() {
        for (String line : linesToAdd) {
            buffer.append(line);
        }
    }

    @Override
    public void undo() {
        // 仅当当前行数 >= originalSize + 新增行数 时才执行撤销
        int expectedSize = originalSize + linesToAdd.size();
        if (buffer.getSize() < expectedSize) {
            // 缓冲区状态异常（可能被外部直接修改），避免抛出未捕获异常
            int diff = expectedSize - buffer.getSize();
            int removable = Math.min(linesToAdd.size(), diff);
            if (removable > 0) {
                buffer.removeLastLines(removable);
            }
            return;
        }
        buffer.removeLastLines(linesToAdd.size());
    }

    @Override
    public String toString() {
        if (linesToAdd.size() == 1) {
            return String.format("AppendCommand(line=+%d, text=\"%s\")", linesToAdd.size(), linesToAdd.get(0));
        }
        return String.format("AppendCommand(lines=+%d)", linesToAdd.size());
    }
}

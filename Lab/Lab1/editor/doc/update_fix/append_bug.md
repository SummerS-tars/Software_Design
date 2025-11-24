# append 命令缺失撤销与日志的修复说明

## 问题概述

原实现中 `append` 直接调用 `TextBuffer.append()`：

- 未通过 `CommandHistory.push()` 执行，因此不会触发观察者回调，日志文件中缺少记录。
- 没有对应的命令对象，无法参与 `undo` / `redo`。

## 根因分析

系统的日志与撤销/重做均依赖“命令模式 + 观察者模式”链路：
`CommandLineApp` -> `CommandHistory.push()` -> 回调通知 `EditorInstance` -> 通知 `FileLogger` 写入。
`append` 绕过了这一链路，导致功能缺失。

## 修复方案

1. 新增 `AppendCommand`，实现：
   - 支持单行与多行追加（输入中包含 `\n` 时拆分）。
   - `execute()` 末尾追加所有行。
   - `undo()` 通过新增的 `TextBuffer.removeLastLines(count)` 一次性移除刚追加的行。
2. 在 `CommandLineApp.cmdAppend` 中改为：

   ```java
   AppendCommand command = new AppendCommand(editor.getBuffer(), text);
   editor.getHistory().push(command);
   editor.markAsModified();
   ```

3. 为撤销实现支持添加了 `TextBuffer.removeLastLines(int count)` 辅助方法（安全参数校验）。

## 测试验证

新增测试 `AppendCommandTest`：

| 用例 | 目标 |
|------|------|
| 单行追加撤销重做 | 行数变化及内容一致 |
| 多行追加撤销重做 | 3 行追加后全部回退再恢复 |
| 日志记录 | 启用日志后包含 `AppendCommand` 记录 |

所有现有测试 + 新增测试通过 (`mvn test`)。

## 日志示例

```text
[2025-11-24 10:12:00] EXECUTE: AppendCommand(line=+1, text="Hello")
[2025-11-24 10:12:02] UNDO: AppendCommand(line=+1, text="Hello")
[2025-11-24 10:12:04] REDO: AppendCommand(line=+1, text="Hello")
```

## 后续建议

- 若未来支持“批量脚本”执行，可考虑合并多条连续 Append 为单条复合命令。
- 目前撤销多行插入逻辑简单直接，若需要更细粒度行级日志，可扩展为每行一个命令（会增加历史长度）。

## 结论

`append` 已纳入命令模式：支持撤销/重做，日志记录正常，问题解决。

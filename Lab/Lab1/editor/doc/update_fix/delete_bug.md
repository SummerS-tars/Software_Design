# delete 功能bug

## 问题描述

```txt
> show
1: hello world 1
2: hello world 2
3: hello world 3
4: hello world 4
5: hello world 5
6:
> delete 6 1 1
命令执行失败: Range [0, 1) out of bounds for length 0
> delete 6 1 0
命令执行失败: 列号越界: 1，有效范围: 1-0
> delete 6 0 1
命令执行失败: Range [-1, 0) out of bounds for length 0
```

无法正确处理空行的情况  

## 修复方案（已实施）

采用策略 A+E：

1. 支持零长度删除作为 no-op，不改变内容，不进入历史（在 CLI 层拦截 `length==0`）。
2. TextBuffer.delete 对 `length==0` 允许列范围 `1..行长度+1`（与插入行尾逻辑统一）。
3. 空行上删除正长度：解释为“删除整行”，需要 `col==1`，删除后后续行上移，删除最后一条空行则缓冲区变空。
4. DeleteCommand.execute 重写：空行行删除不再 substring，记录 `deletedText="\n"` 以便 undo 通过 insert 还原空行；其它情况保留正常截取流程。
5. CLI `delete` 命令：长度为 0 时提示“删除长度为0，未执行任何操作”。

## 新增测试

在 `TextBufferTest` 中新增：
- `testDeleteZeroLengthOnEmptyLine`
- `testDeletePositiveLengthOnEmptyLine`
- `testDeleteZeroLengthMiddleOfLine`
- `testDeleteZeroLengthAtEndOfLine`

## 行为总结

| 场景              | 旧行为     | 新行为                       |
| ----------------- | ---------- | ---------------------------- |
| 空行 delete 1     | Range 异常 | 友好异常（空行无法删除内容） |
| 空行 delete 0     | 列号越界   | 无操作，静默通过             |
| 非空行 delete 0   | 列号越界   | 无操作，静默通过             |
| delete 0 历史记录 | 仍入栈     | 不入栈（命令未创建）         |

## 后续可选改进

- 在日志中区分 no-op 与真实操作以便审计。
- CLI 增加 `delete-line <行>` 快速删除整行（等价于从列1删除行长度）。
- 支持跨行删除（需要新数据结构处理）。

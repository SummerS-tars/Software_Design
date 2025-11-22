# 阶段 2：命令模式与撤销/重做 (Command System)

## 任务描述
基于阶段 1 的 `TextBuffer`，引入**命令模式**来实现编辑操作的封装，从而支持 Undo/Redo。

## 需求细节

### 1. 接口定义：`Command`
* `void execute()`: 执行逻辑。
* `void undo()`: 执行逆操作。

### 2. 具体命令实现
* **`InsertCommand`**:
    * *构造函数*: 接收 `TextBuffer`, `line`, `col`, `text`。
    * *Execute*: 调用 `buffer.insert(...)`。
    * *Undo*: 计算插入后的范围，调用 `buffer.delete(...)` 删除刚才插入的内容。
* **`DeleteCommand`**:
    * *构造函数*: 接收 `TextBuffer`, `line`, `col`, `length`。
    * *Execute*: 保存被删除的文本（用于恢复），然后调用 `buffer.delete(...)`。
    * *Undo*: 调用 `buffer.insert(...)` 将被删文本插回原位。

### 3. 历史管理：`CommandHistory`
* **数据结构**: 两个栈 `Stack<Command> undoStack` 和 `Stack<Command> redoStack`。
* **方法**:
    * `push(Command cmd)`: 执行新命令时调用。清空 redoStack，压入 undoStack。
    * `undo()`: 弹出 undoStack，调用 `cmd.undo()`，压入 redoStack。
    * `redo()`: 弹出 redoStack，调用 `cmd.execute()`，压入 undoStack。

## 验证计划
编写测试模拟用户操作：
1.  执行 `InsertCommand` 写入 "A"。
2.  执行 `InsertCommand` 写入 "B" -> 内容 "AB"。
3.  调用 `history.undo()` -> 内容变回 "A"。
4.  调用 `history.redo()` -> 内容恢复 "AB"。

# 阶段 4：日志与持久化 (Logging & Persistence)

## 任务描述
实现观察者模式用于操作日志，以及备忘录模式用于程序退出后的状态恢复。

## 需求细节

### 1. 日志模块 (Observer Pattern)
* **需求**: 记录命令执行的时间戳和内容到 `.filename.log`。
* **实现**:
    * 定义 `EditorObserver` 接口。
    * 实现 `FileLogger`: 监听命令事件。当 `Command.execute()` 成功时，格式化字符串 `timestamp command_args` 并追加写入日志文件。
    * **自动开启**: `load` 文件时，若首行为 `#log`，自动注册 Logger。

### 2. 持久化模块 (Memento Pattern)
* **需求**: 退出程序时保存工作区状态，重启时恢复。
* **保存内容**: 打开的文件列表及其文件路径、当前活动文件、文件修改状态、日志开关状态。
* **实现**:
    * `WorkspaceMemento`: 存储状态的数据类 (POJO)。
    * 在 `Workspace` 中增加 `saveState()`: 将当前状态序列化（JSON或自定义文本格式）写入 `.editor_workspace`。
    * 增加 `restoreState()`: 读取配置文件并重新 `load` 所有文件。

## 验证计划
1.  **日志测试**: 修改带 `#log` 的文件，检查同级目录是否生成 `.log` 文件，内容是否包含时间戳。
2.  **恢复测试**: 打开多个文件后调用 `saveState()`。重启程序调用 `restoreState()`，检查文件列表是否自动加载。

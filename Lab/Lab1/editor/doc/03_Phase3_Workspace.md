# 阶段 3：工作区与文件管理 (Workspace)

## 任务描述
实现多文件管理系统。每个打开的文件应拥有独立的 `TextBuffer` 和 `CommandHistory`。

## 需求细节

### 1. 类设计：`EditorInstance`
* **职责**: 代表一个打开的文件会话。
* **属性**:
    * `String filePath`: 文件路径。
    * `TextBuffer buffer`: 文本内容。
    * `CommandHistory history`: 该文件的撤销栈。
    * `boolean isModified`: 修改标记。

### 2. 类设计：`Workspace`
* **职责**: 全局状态管理。
* **属性**:
    * `Map<String, EditorInstance> files`: 所有打开的文件。
    * `EditorInstance activeEditor`: 当前活动文件。
* **核心方法**:
    * `load(String path)`: 读取文件内容初始化 `EditorInstance`。若文件不存在则新建。
    * `init(String path)`: 创建新缓冲区（不读取磁盘）。
    * `activate(String path)`: 切换 `activeEditor` (实现 `edit` 命令逻辑)。
    * `close(String path)`: 关闭文件，若有修改需提示（逻辑预留）。
    * `save(String path)`: 将 `buffer` 内容写入磁盘，重置 `isModified`。

## 验证计划
1.  创建 `test_files` 目录。
2.  调用 `workspace.load("file1.txt")` (不存在则自动创建)。
3.  切换 `activeEditor` 并修改内容。
4.  调用 `workspace.load("file2.txt")`，确认 `activeEditor` 切换。
5.  再次切换回 `file1.txt`，确认之前的修改（Undo历史）仍然存在。

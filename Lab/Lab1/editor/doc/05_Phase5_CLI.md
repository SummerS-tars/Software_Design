# 阶段 5：命令行交互界面 (CLI Shell)

## 任务描述
解析用户输入，连接前四个阶段的功能，完成最终的应用程序。

## 需求细节

### 1. 主程序 `CommandLineApp`
* 启动循环 `while(true)` 读取 `System.in`。
* 解析输入字符串，识别命令和参数。
    * *注意*: 处理带双引号的参数（如 `append "hello world"`）。

### 2. 命令映射与分发
实现 Lab 文档中定义的 18 个命令：
* **工作区**: `load`, `save`, `init`, `close`, `edit`, `editor-list`, `undo`, `redo`, `exit`, `dir-tree`.
* **编辑**: `append`, `insert`, `delete`, `replace`, `show`.
* **日志**: `log-on`, `log-off`, `log-show`.

### 3. 输出格式
* `show`: 需按 `lineNo: content` 格式输出。
* `dir-tree`: 使用缩进符号绘制树形结构。
* `editor-list`: 标记当前文件(`*` 或 `>`) 和修改状态(`*` 或 `[modified]`)。

## 验证计划 (集成测试)
按 Lab 文档示例场景操作：
1.  启动程序。
2.  `init test.txt`
3.  `append "Hello World"`
4.  `show` -> 看到内容。
5.  `undo` -> 内容清空。
6.  `save` -> 检查磁盘文件。
7.  `exit` -> 退出。
# 阶段 1：核心文本缓冲 (Text Engine)

## 任务描述
实现编辑器的核心数据结构，用于存储和操作纯文本。暂时不考虑撤销、IO 或用户交互。

## 需求细节

### 1. 类设计：`TextBuffer`
* **职责**: 内存中的文本容器。
* **数据结构**: `private List<String> lines;` (使用 ArrayList)。
* **核心方法**:
    * `int getSize()`: 返回总行数。
    * `String getLine(int lineNumber)`: 获取指定行内容（行号从1开始，需转换为0索引）。
    * `void append(String text)`: 在末尾追加一行。
    * `void insert(int line, int col, String text)`: 在指定位置插入文本。
        * *注意*: 如果 `text` 包含换行符 `\n`，需要拆分为多行处理。
        * *异常*: 行号/列号越界需抛出异常。
    * `void delete(int line, int col, int length)`: 从指定位置删除指定长度的字符。
        * *限制*: 不支持跨行删除。
        * *异常*: 长度超出行尾需抛出异常。

### 2. 辅助功能
* 重写 `toString()` 以便调试时打印当前所有行。

## 验证计划 (Test Plan)
请编写一个 JUnit 测试类 `TextBufferTest` 或包含 `main` 方法的测试脚本：
1.  **Append测试**: 追加 "Hello"，验证 `getLine(1)` 等于 "Hello"。
2.  **Insert测试**: 在 "Hello" 的 'e' 后插入 "xyz"，验证结果为 "Hexyzllo"。
3.  **Delete测试**: 从 "Hexyzllo" 删除 "xyz"，验证恢复为 "Hello"。
4.  **边界测试**: 尝试向空 Buffer 的 (1,1) 插入文本；尝试访问不存在的行号。

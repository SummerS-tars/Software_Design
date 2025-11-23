# line col argument 正确模式

line:col 格式差异 (关键)
你的帮助信息显示 insert <行> <列> <文本>，这意味着你可能打算用空格分隔行和列（例如 insert 1 5 "abc"）。

文档要求：必须使用 line:col 格式 。

正确示例：insert 1:5 "abc"

受影响命令：insert, delete, replace。你需要调整代码里的解析逻辑，以识别冒号 : 分隔符。

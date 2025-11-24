# init

## requirement

init - 创建新缓冲区
功能：创建⼀个未保存的新缓冲⽂件，并初始化基础结构。
参数说明:
file ：⽂件路径，如 test.txt
with-log (可选)：是否在第⼀⾏添加 # log 以启⽤⽇志
初始化内容:
1 load \<file>
1 save [file|all]
1 init \<file> [with-log]创建⽂本⽂件( init test.txt with-log ):
不带 with-log 则创建空⽂件。
说明:
新缓冲区标记为已修改，需要使⽤ save 命令指定路径保存
创建后⾃动成为当前活动⽂件
若⽂件已存在，提示错误

## question

目前已支持可选参数 `with-log`：

用法示例:
`init sample.txt`           -> 创建空缓冲区，不启用日志（保持未保存状态但当前实现不自动标记修改）
`init sample.txt with-log`  -> 创建并在首行写入 `#log`，自动启用日志记录，标记为已修改（需执行 save）

行为说明:  

1. with-log: 创建后缓冲区首行是 `#log`，已开启日志（等价于后续执行 log-on），并设置修改标记。
2. 无 with-log: 创建空缓冲区，未开启日志，当前实现保持“未修改”标记（与课程描述“新缓冲区标记为已修改”略有差异，后续可统一调整）。
3. 若希望严格符合课程要求（init 均标记为已修改），可将 Workspace.init 中的 markAsSaved 改为 markAsModified，并同步更新相关测试。
4. 未来改进: init 若目标文件已存在，可选择直接报错或引导使用 load；目前仍允许与现有文件同名以保持兼容。

测试覆盖:
新增 WorkspaceTest.testInitWithLog 验证首行内容、修改标记与日志启用状态。

建议后续任务:  

- 统一 init 与 initWithLog 的修改标记语义。
- 在 help/USAGE 文档中补充 init with-log 用例与差异说明。
- 对 “文件已存在” 行为添加明确的用户提示并更新文档。

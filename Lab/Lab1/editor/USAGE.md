# 文本编辑器 - 使用说明
## 命令概览


### 工作区命令

- `load <file>`  加载文件 (若首行是 `#log` 自动启用日志)
- `save [file|all]` 保存当前文件 / 指定文件 / 所有文件：
  - 无参数：保存活动文件
  - `<path>`：保存该路径或已打开的匹配文件
  - `all`：保存所有打开文件
- `init <file> [with-log]` 创建新文件；`with-log` 插入首行 `#log` 并开启日志
- `close [file]` 关闭当前或指定文件；无参数关闭活动文件
- `edit <file>` 切换活动文件（支持文件名匹配与冲突选择）
- `editor-list` 显示文件：活动(*)、修改[modified]、完整路径
- `undo` 撤销
- `redo` 重做
- `exit` 退出（未保存提示 + 工作区状态保存）

### 编辑命令

- `append <text>` 末尾追加一行
- `insert <line:col> <text>` 指定行列插入；列可为行长度+1；支持多行拆分
- `delete <line:col> <len>` 删除：
  - 普通行：从列删除 `len` 字符
  - 空行：`len>0 且 col=1` 删除整行；`len=0` no-op
  - 零长度删除不入历史
- `replace <line:col> <len> <text>` 删除+插入组合
- `show [start:end]` 显示全文或范围（闭区间）

### 日志命令

- `log-on [file]` 启用日志
- `log-off [file]` 禁用日志
- `log-show [file]` 查看日志 (`.<filename>.log`)

### 辅助命令

- `dir-tree [path]` 目录树
- `help` 帮助

### 命令示例速览

```bash
init demo.txt with-log              # 创建并启用日志
append "First line"                # 追加一行
insert 1:7 " (extra)"             # 行1列7插入文本
delete 1:1 5                       # 删除前5字符
append ""                         # 添加空行
delete 3:1 1                       # 第3行为空行 -> 删除整行
replace 1:3 4 "XYZ"               # 用 XYZ 替换从列3开始长度4的文本
show 1:10                          # 显示范围(闭区间)
log-on demo.txt                    # 启用日志(可带文件名)
log-show demo.txt                  # 查看日志
save all                           # 保存全部文件
exit                               # 退出并触发未保存提示
```

### 场景2：加载现有文件并编辑

```bash
load myfile.txt
show
insert 2:1 "New text at beginning of line 2"
show
save
exit
```

### 场景3：多文件编辑

```bash
init file1.txt
append "Content in file 1"
init file2.txt
append "Content in file 2"
editor-list
edit file1.txt
show
exit
```

### 场景3-1：保存所有文件

```bash
init file1.txt
append "Content in file 1"
init file2.txt
append "Content in file 2"
init file3.txt
append "Content in file 3"
editor-list
save all
# 输出：
# 已保存: file1.txt
# 已保存: file2.txt
# 已保存: file3.txt
# ---
# 保存完成: 成功 3 个，失败 0 个
exit
```

### 场景4：撤销/重做

```bash
init test.txt
append "Line 1"
append "Line 2"
show
undo
show
redo
show
```

### 场景5：显示指定范围

```bash
init test.txt
append "Line 1"
append "Line 2"
append "Line 3"
append "Line 4"
append "Line 5"
show            # 显示全文
show 2:4        # 显示第2-4行
show 1:2        # 显示第1-2行
```

### 场景6：启用日志

```bash
init logged.txt
log-on
append "Test"
insert 1:5 " data"
log-show
exit
```

## 简化命令清单（快速查看）

工作区：`load` `save[all]` `init[with-log]` `close` `edit` `editor-list` `undo` `redo` `exit`
编辑：`append` `insert` `delete` `replace` `show`
日志：`log-on[file]` `log-off[file]` `log-show[file]`
辅助：`dir-tree` `help`

## 特殊功能

### 双引号参数

当参数包含空格时，使用双引号括起来：

```bash
append "This is a line with spaces"
insert 1:1 "Text with spaces"
```

### 自动日志开启

如果文件首行为 `#log`，加载时会自动启用日志记录：

```bash
init auto_log.txt
append "#log"
append "Content"   # 日志已自动启用
```

### 工作区状态持久化

- 退出时自动保存工作区状态到 `.editor_workspace`
- 启动时自动恢复之前的工作区状态
- 保存内容包括：打开的文件、当前活动文件、修改状态、日志开关

### 未保存更改提示

在关闭文件或退出编辑器时，如果有未保存的更改，系统会自动提示：

#### Close 命令保存提示

```text
> close file.txt
警告: 文件 'file.txt' 有未保存的更改
是否保存更改？(y/n/c - 保存/不保存/取消):
```

- **y/yes** - 保存更改并关闭文件
- **n/no** - 放弃更改并关闭文件
- **c/cancel** - 取消关闭操作

#### Exit 命令保存提示

```text
> exit
警告: 以下文件有未保存的更改:
  - file1.txt
  - file2.txt
是否保存所有更改？(y/n/c - 保存/不保存/取消):
```

- **y/yes** - 保存所有文件并退出
- **n/no** - 放弃所有更改并退出
- **c/cancel** - 取消退出操作

如果保存失败，系统会询问是否继续退出：

```text
保存完成: 成功 1 个，失败 1 个
部分文件保存失败，是否仍要退出？(y/n):
```

### 日志文件格式

日志文件保存在与源文件相同的目录，命名格式：`.filename.log`（例如原文件 `demo.txt` 日志为 `.demo.txt.log`）。
内容格式：`[时间戳] 事件类型: 命令信息`

## 快速测试脚本

按照 Phase 5 文档的验证计划：

```bash
# 1. 启动程序
java -jar target/editor-1.0-SNAPSHOT.jar

# 2. 在程序中执行以下命令
init test.txt
append "Hello World"
show
undo
show   # (空文件)
append "Hello World"
save
exit

# 3. 验证文件已创建
# 在命令行执行：
cat test.txt   # Linux/Mac
type test.txt  # Windows
```

## 开发测试

运行所有单元测试：

```bash
mvn test
```

测试结果应显示：

- Phase 1 (TextBufferTest): 24 个测试 ✅
- Phase 2 (CommandTest): 17 个测试 ✅
- Phase 3 (WorkspaceTest): 21 个测试 ✅
- Phase 4 (Phase4Test): 9 个测试 ✅
- 总计：72 个测试全部通过 ✅

## 项目结构

```text
src/main/java/top/thesumst/
├── App.java                  # 主程序入口
├── cli/                      # 命令行界面
│   ├── CommandLineApp.java   # CLI 主程序
│   └── CommandParser.java    # 命令解析器
├── command/                  # 命令模式实现
│   ├── Command.java          # 命令接口
│   ├── CommandHistory.java   # 历史管理
│   ├── InsertCommand.java    # 插入命令
│   └── DeleteCommand.java    # 删除命令
├── engine/                   # 文本引擎
│   └── TextBuffer.java       # 文本缓冲区
├── memento/                  # 备忘录模式
│   └── WorkspaceMemento.java # 工作区状态
├── observer/                 # 观察者模式
│   ├── EditorObserver.java   # 观察者接口
│   └── FileLogger.java       # 文件日志器
└── workspace/                # 工作区管理
    ├── EditorInstance.java   # 编辑器实例
    └── Workspace.java        # 工作区管理器
```

## 设计模式应用

1. **命令模式** (Command Pattern)
   - 封装编辑操作为命令对象
   - 支持撤销/重做功能

2. **观察者模式** (Observer Pattern)
   - FileLogger 监听命令执行事件
   - 自动记录操作日志

3. **备忘录模式** (Memento Pattern)

- WorkspaceMemento 保存工作区状态（打开的文件 / 活动文件 / 日志状态 / 修改标记）
- 支持程序重启后恢复

## 注意事项

1. 行号与列号均从 1 开始。
2. 插入支持在 “行长度+1” 位置追加内容；删除普通行不允许越界。
3. 空行删除：`delete <line>:1 <len>` 若 `<len> > 0` 删除整行；`<len> = 0` 无操作。
4. 文件读写使用 UTF-8。
5. 日志文件命名：`.filename.log`。
6. 工作区状态文件：`.editor_workspace` 自动保存/恢复。
7. 不支持跨行删除；空行整行删除是特例而非跨行。
8. `with-log` 创建的文件标记为已修改，退出前会提示保存。
9. 日志命令可接受可选文件名参数；省略时作用于当前活动文件。

# 文本编辑器 - 使用说明

## 运行方法

### 方法1：使用 Maven 直接运行
```bash
mvn clean compile exec:java -Dexec.mainClass="top.thesumst.App"
```

### 方法2：打包后运行 JAR
```bash
# 打包项目
mvn clean package -DskipTests

# 运行 JAR
java -jar target/editor-1.0-SNAPSHOT.jar
```

### 方法3：使用 Maven exec 插件
```bash
mvn exec:java -Dexec.mainClass="top.thesumst.cli.CommandLineApp"
```

## 基本使用示例

### 场景1：创建并编辑新文件
```
> init test.txt
> append "Hello World"
> append "Second Line"
> show
> save
> exit
```

### 场景2：加载现有文件并编辑
```
> load myfile.txt
> show
> insert 2:1 "New text at beginning of line 2"
> show
> save
> exit
```

### 场景3：多文件编辑
```
> init file1.txt
> append "Content in file 1"
> init file2.txt
> append "Content in file 2"
> editor-list
> edit file1.txt
> show
> exit
```

### 场景3-1：保存所有文件
```
> init file1.txt
> append "Content in file 1"
> init file2.txt
> append "Content in file 2"
> init file3.txt
> append "Content in file 3"
> editor-list
  file1.txt [modified]
  file2.txt [modified]
> file3.txt [modified]

> save all
已保存: file1.txt
已保存: file2.txt
已保存: file3.txt
---
保存完成: 成功 3 个，失败 0 个

> exit
```

### 场景4：撤销/重做
```
> init test.txt
> append "Line 1"
> append "Line 2"
> show
> undo
> show
> redo
> show
```

### 场景5：显示指定范围
```
> init test.txt
> append "Line 1"
> append "Line 2"
> append "Line 3"
> append "Line 4"
> append "Line 5"
> show
1: Line 1
2: Line 2
3: Line 3
4: Line 4
5: Line 5

> show 2:4
2: Line 2
3: Line 3
4: Line 4

> show 1:2
1: Line 1
2: Line 2
```

### 场景6：启用日志
```
> init logged.txt
> log-on
> append "Test"
> insert 1:5 " data"
> log-show
> exit
```

## 完整命令列表

### 工作区命令
- `load <文件路径>` - 加载文件到工作区
- `save [file|all]` - 保存文件
  - 无参数：保存当前活动文件
  - `file`：保存指定文件
  - `all`：保存所有打开的文件
- `init <文件路径>` - 创建新文件
- `close <文件路径>` - 关闭文件
- `edit <文件路径>` - 切换当前编辑文件
- `editor-list` - 列出所有打开的文件
- `undo` - 撤销上一次操作
- `redo` - 重做被撤销的操作
- `exit` - 退出程序（自动保存工作区状态）

### 编辑命令
- `append <文本>` - 在文件末尾追加一行
- `insert <行:列> <文本>` - 在指定位置插入文本
- `delete <行:列> <长度>` - 从指定位置删除文本
- `replace <行:列> <长度> <新文本>` - 替换指定位置的文本
- `show [起始行:结束行]` - 显示当前文件内容（可指定范围，不指定则显示全文）

### 日志命令
- `log-on` - 为当前文件启用日志记录
- `log-off` - 为当前文件禁用日志记录
- `log-show` - 显示当前文件的日志内容

### 辅助命令
- `dir-tree [目录]` - 显示目录树结构
- `help` - 显示帮助信息

## 特殊功能

### 双引号参数
当参数包含空格时，使用双引号括起来：
```
> append "This is a line with spaces"
> insert 1:1 "Text with spaces"
```

### 自动日志开启
如果文件首行为 `#log`，加载时会自动启用日志记录：
```
> init auto_log.txt
> append "#log"
> append "Content"
> (日志已自动启用)
```

### 工作区状态持久化
- 退出时自动保存工作区状态到 `.editor_workspace`
- 启动时自动恢复之前的工作区状态
- 保存内容包括：打开的文件、当前活动文件、修改状态、日志开关

### 日志文件格式
日志文件保存在与源文件相同的目录，命名格式：`.filename.log`
内容格式：`[时间戳] 事件类型: 命令信息`

## 快速测试脚本

按照 Phase 5 文档的验证计划：

```bash
# 1. 启动程序
java -jar target/editor-1.0-SNAPSHOT.jar

# 2. 在程序中执行以下命令
> init test.txt
> append "Hello World"
> show
1: Hello World

> undo
> show
(空文件)

> append "Hello World"
> save
> exit

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
- Phase 1 (TextBufferTest): 24个测试 ✅
- Phase 2 (CommandTest): 17个测试 ✅
- Phase 3 (WorkspaceTest): 21个测试 ✅
- Phase 4 (Phase4Test): 9个测试 ✅
- **总计：72个测试全部通过**

## 项目结构

```
src/main/java/top/thesumst/
├── App.java                    # 主程序入口
├── cli/                        # 命令行界面
│   ├── CommandLineApp.java    # CLI 主程序
│   └── CommandParser.java     # 命令解析器
├── command/                    # 命令模式
│   ├── Command.java            # 命令接口
│   ├── CommandHistory.java    # 历史管理
│   ├── InsertCommand.java     # 插入命令
│   └── DeleteCommand.java     # 删除命令
├── engine/                     # 文本引擎
│   └── TextBuffer.java         # 文本缓冲区
├── memento/                    # 备忘录模式
│   └── WorkspaceMemento.java  # 工作区状态
├── observer/                   # 观察者模式
│   ├── EditorObserver.java    # 观察者接口
│   └── FileLogger.java         # 文件日志器
└── workspace/                  # 工作区管理
    ├── EditorInstance.java    # 编辑器实例
    └── Workspace.java          # 工作区管理器
```

## 设计模式应用

1. **命令模式** (Command Pattern)
   - 封装编辑操作为命令对象
   - 支持撤销/重做功能

2. **观察者模式** (Observer Pattern)
   - FileLogger 监听命令执行事件
   - 自动记录操作日志

3. **备忘录模式** (Memento Pattern)
   - WorkspaceMemento 保存工作区状态
   - 支持程序重启后恢复

## 注意事项

1. 行号和列号从 1 开始计数
2. 所有文件操作使用 UTF-8 编码
3. 日志文件以隐藏文件形式存储（.filename.log）
4. 工作区状态文件为 `.editor_workspace`
5. 删除操作不支持跨行删除

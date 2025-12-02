# 软件设计讨论准备分析

## 一、 优秀设计实践 (3点)

当前 Lab1 的代码库展现了良好的设计基础，特别是在以下几个方面：

### 1. 核心功能与UI解耦：命令模式的经典应用

- **技术依据**: 项目通过 `command` 包（`Command` 接口、具体命令类如 `InsertCommand`、`DeleteCommand` 以及 `CommandHistory`）完美实现了命令模式。这成功地将“命令的调用者”(`CommandLineApp`)与“命令的执行者”(`TextBuffer`)解耦。
- **优点**:
  - **可撤销/重做**: `CommandHistory` 利用栈轻松实现了 undo/redo，这是命令模式的核心优势。
  - **高内聚**: 每个命令封装了单一职责的操作，逻辑清晰。
  - **易扩展**: 添加新编辑操作只需实现 `Command` 接口，不影响调用方。

- **代码示例** (`CommandHistory.java`):

  ```java
  // ...
  public class CommandHistory {
      private final Stack<Command> undoStack;
      private final Stack<Command> redoStack;
  
      public void push(Command command) {
          command.execute();
          undoStack.push(command);
          redoStack.clear();
          // ...
      }
  
      public boolean undo() {
          if (undoStack.isEmpty()) return false;
          Command command = undoStack.pop();
          command.undo();
          redoStack.push(command);
          // ...
          return true;
      }
      // ...
  }
  ```

### 2. 横切关注点分离：观察者模式的有效实践

- **技术依据**: 通过 `EditorObserver` 接口和 `FileLogger` 实现，将日志记录这一横切关注点从核心编辑逻辑中分离。`EditorInstance` 作为被观察者，在命令执行、撤销、重做时通知所有观察者。
- **优点**:
  - **低耦合**: 日志模块的开启、关闭或修改，完全不影响 `EditorInstance` 和 `Command` 的核心代码。
  - **扩展性强**: Lab2 新增的“编辑时长统计”功能，可以完美复用此模式。只需创建一个新的 `StatisticsObserver`，监听文件激活/切换事件即可，符合开闭原则。

- **代码示例** (`EditorInstance.java`):

  ```java
  // ...
  public class EditorInstance {
      private final List<EditorObserver> observers;
  
      public void addObserver(EditorObserver observer) { /* ... */ }
  
      public void notifyCommandExecuted(Command command) {
          for (EditorObserver observer : observers) {
              observer.onCommandExecuted(command);
          }
      }
      // ...
  }
  ```

### 3. 状态管理清晰：备忘录模式的应用

- **技术依据**: `WorkspaceMemento` 作为一个纯粹的数据载体，负责封装 `Workspace` 的状态（打开的文件列表、活动文件、修改状态等）。`Workspace` 本身负责创建和恢复备忘录，实现了状态的外部化存储。
- **优点**:
  - **简化 `Workspace`**: `Workspace` 无需关心状态的序列化格式和存储细节，只管调用 `saveState` 和 `restoreState`。
  - **封装性好**: 状态的内部结构对外界隐藏，仅 `Workspace` 可见。

- **代码示例** (`Workspace.java`):

  ```java
  // ...
  public void saveState(String stateFile) throws IOException {
      // 1. 创建备忘录对象
      WorkspaceMemento memento = new WorkspaceMemento(...);
      // 2. 序列化并保存
      String data = memento.serialize();
      Files.write(Paths.get(stateFile), data.getBytes(StandardCharsets.UTF_8));
  }
  
  public void restoreState(String stateFile) throws IOException {
      // 1. 读取并反序列化
      String data = ...;
      WorkspaceMemento memento = WorkspaceMemento.deserialize(data);
      // 2. 恢复状态
      // ...
  }
  ```

---

## 二、 待改进的设计点与扩展性分析 (3点)

结合 Lab2 的需求（引入 XML 编辑器、统计、拼写检查），当前设计在扩展性方面暴露出一些问题，主要体现在对“开闭原则”的违反上。

### 1. 问题：编辑器类型耦合，难以扩展

- **现状**: `Workspace` 和 `EditorInstance` 与具体的 `TextBuffer` 强耦合。`Workspace` 直接创建 `new EditorInstance()`，而 `EditorInstance` 内部直接创建 `new TextBuffer()`。这使得引入 Lab2 的 `XmlEditor` 变得困难，因为它的数据结构不是 `List<String>`，而是 DOM 树。
- **改进建议**:
  1. **引入 `Editor` 抽象**: 创建一个 `Editor` 接口或抽象基类，定义通用的编辑行为（如 `save`, `getContent`, `getHistory`）。
  2. **具体实现**: 让 `TextEditor` 和 `XmlEditor` 实现 `Editor` 接口。`TextEditor` 内部管理 `TextBuffer`，而 `XmlEditor` 内部管理 DOM 树结构。
  3. **使用工厂模式**: 在 `Workspace` 中，使用工厂方法（Factory Method）根据文件类型（`.txt` 或 `.xml`）创建对应的 `Editor` 实例，而不是直接 `new`。

- **代码重构示例** (引入 `Editor` 接口和工厂):

  ```java
  // 1. 新建接口
  public interface Editor {
      void save() throws IOException;
      CommandHistory getHistory();
      // ... 其他通用方法
  }
  
  // 2. Workspace 使用工厂创建
  public class Workspace {
      // private final Map<String, EditorInstance> files; // 旧
      private final Map<String, Editor> editors; // 新
  
      public Editor load(String path) {
          // ...
          // Editor editor = new EditorInstance(path); // 旧
          Editor editor = EditorFactory.createEditor(path); // 新：根据路径或类型创建
          editors.put(normalizedPath, editor);
          activeEditor = editor;
          return editor;
      }
  }
  ```

  这样，`Workspace` 只依赖于 `Editor` 抽象，未来再增加 Markdown 编辑器等也无需修改 `Workspace`。

### 2. 问题：`CommandLineApp` 的巨大 `switch` 语句

- **现状**: `CommandLineApp.executeCommand` 方法使用一个庞大的 `switch` 语句来分发命令。Lab2 将新增 7 个命令，这将使 `switch` 进一步膨胀，违反了开闭原则（每次增删命令都要修改此类）。
- **改进建议**:
  - **命令分发器**: 引入“命令分发器”模式。创建一个 `Map<String, CommandHandler>`，其中 `CommandHandler` 是一个函数式接口或普通接口（如 `void handle(ParsedCommand cmd)`）。
  - **注册命令**: 程序启动时，将所有命令的处理器实例注册到 Map 中。`executeCommand` 只需从 Map 中查找并执行对应的 Handler 即可。

- **代码重构示例**:

  ```java
  public class CommandLineApp {
      private final Map<String, Consumer<ParsedCommand>> commandHandlers = new HashMap<>();
  
      public CommandLineApp() {
          // ...
          registerCommands();
      }
  
      private void registerCommands() {
          commandHandlers.put("load", this::cmdLoad);
          commandHandlers.put("save", this::cmdSave);
          // ... Lab2 新增命令
          commandHandlers.put("insert-before", this::cmdInsertBefore);
      }
  
      private void executeCommand(String input) {
          ParsedCommand cmd = CommandParser.parse(input);
          Consumer<ParsedCommand> handler = commandHandlers.get(cmd.getCommand().toLowerCase());
          if (handler != null) {
              handler.accept(cmd);
          } else {
              System.out.println("未知命令...");
          }
      }
      // ... (cmdLoad, cmdSave 等方法保持不变)
  }
  ```

  这样，添加新命令只需在 `registerCommands` 中增加一行，无需修改 `executeCommand` 的逻辑。

### 3. 问题：编辑命令与 `TextBuffer` 强耦合

- **现状**: `InsertCommand`、`DeleteCommand` 等直接依赖具体的 `TextBuffer`。这使得它们无法复用于 `XmlEditor`，因为后者的操作对象是 DOM 节点，而非文本行。
- **改进建议**:
  - **命令与编辑器绑定**: 命令应该与它所属的 `Editor` 实例绑定，而不是直接操作底层数据结构。
  - **抽象操作**: `Editor` 接口应提供更高层次的抽象操作，如 `insert(params)`, `delete(params)`。具体的 `TextEditor` 和 `XmlEditor` 各自实现这些操作。命令类则调用这些抽象接口。

- **代码重构示例**:

  ```java
  // 1. Editor 接口提供高层操作
  public interface Editor {
      void insert(int line, int col, String text);
      void delete(int line, int col, int length);
      // ...
  }
  
  // 2. 命令依赖 Editor 接口
  public class InsertCommand implements Command {
      private final Editor editor; // 不再是 TextBuffer
      // ...
  
      public InsertCommand(Editor editor, int line, int col, String text) {
          this.editor = editor;
          // ...
      }
  
      @Override
      public void execute() {
          editor.insert(line, col, text); // 调用接口方法
      }
      // ...
  }
  ```

  通过这种方式，同一套 `Command` 框架可以服务于不同类型的编辑器，只要它们都提供了相同的抽象编辑接口。

---

### **总结**

Lab1 的架构在解耦和核心模式应用上做得很好，为功能扩展打下了坚实基础。然而，为了优雅地实现 Lab2 的需求，必须在 **抽象层次** 上进行提升：从依赖具体实现（`TextBuffer`）转向依赖抽象（`Editor` 接口），并优化命令分发机制，以更好地遵循“开闭原则”，实现“对扩展开放，对修改关闭”的设计目标。

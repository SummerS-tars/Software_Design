# 单元测试快速参考

## 新增测试文件

### 1. CommandParserTestNew.java
**位置**: `src/test/java/top/thesumst/cli/CommandParserTestNew.java`  
**测试数量**: 28 个  
**主要测试**: 命令解析器的各种输入格式

```bash
# 运行此测试类
mvn test -Dtest=CommandParserTestNew
```

### 2. CLICommandTest.java
**位置**: `src/test/java/top/thesumst/cli/CLICommandTest.java`  
**测试数量**: 16 个  
**主要测试**: CLI 命令的实际功能

```bash
# 运行此测试类
mvn test -Dtest=CLICommandTest
```

## 测试覆盖的功能

### ✅ line:col 格式
- insert 命令: `insert 1:5 "text"`
- delete 命令: `delete 1:5 10`
- replace 命令: `replace 1:5 10 "new"`

### ✅ show 范围功能
- 全文显示: `show`
- 范围显示: `show 1:10`
- 单行显示: `show 5:5`

### ✅ 引号参数
- 带空格的文本: `append "hello world"`
- 路径: `load "C:/My Files/test.txt"`

### ✅ 边界条件
- 空输入
- 仅空格
- 特殊字符

## 快速运行所有测试

```bash
# 运行所有测试
mvn test

# 仅运行新增测试
mvn test -Dtest=CommandParserTestNew,CLICommandTest

# 查看测试报告
cat target/surefire-reports/*.txt
```

## 测试结果预期

```
总测试数: 116
- 原有测试: 72
- 新增测试: 44
  - CommandParserTestNew: 28
  - CLICommandTest: 16

预期结果: ✅ 全部通过
```

## 关键测试方法

### CommandParserTestNew
```java
testParseLineColFormat()        // line:col 基本解析
testParseShowWithRange()        // show 范围解析
testParseQuotedString()         // 引号字符串
testParseCompleteInsertCommand() // 完整命令场景
```

### CLICommandTest
```java
testInsertWithLineColFormat()   // insert 功能
testDeleteWithLineColFormat()   // delete 功能
testShowRange()                 // show 范围功能
testComplexEditingScenario()    // 复杂场景
```

## 测试断点调试

如果需要调试特定测试：

```bash
# 使用 Maven 调试模式
mvnDebug test -Dtest=CommandParserTestNew#testParseLineColFormat
```

在 IDE 中：
1. 打开测试文件
2. 在测试方法上右键 -> Debug
3. 设置断点进行调试

## 测试数据目录

临时文件位置: `test_temp_cli/`  
(测试后自动清理)

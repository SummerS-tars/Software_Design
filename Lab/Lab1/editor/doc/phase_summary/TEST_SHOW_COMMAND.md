# show 命令测试文档

## 功能说明

`show` 命令用于显示文件内容，支持两种模式：
1. **全文显示**：`show` - 显示整个文件
2. **范围显示**：`show <起始行:结束行>` - 显示指定范围的行

## 命令格式

```
show [起始行:结束行]
```

- `起始行` 和 `结束行` 用冒号 `:` 分隔
- 参数可选，不提供则显示全文
- 行号从 1 开始

## 测试用例

### 测试1：显示全文

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
```

### 测试2：显示部分行（中间范围）

```
> show 2:4
2: Line 2
3: Line 3
4: Line 4
```

### 测试3：显示开头几行

```
> show 1:2
1: Line 1
2: Line 2
```

### 测试4：显示末尾几行

```
> show 4:5
4: Line 4
5: Line 5
```

### 测试5：显示单行

```
> show 3:3
3: Line 3
```

### 测试6：显示全部（使用范围）

```
> show 1:5
1: Line 1
2: Line 2
3: Line 3
4: Line 4
5: Line 5
```

## 异常处理测试

### 测试7：格式错误（缺少冒号）

```
> show 1 5
错误: 范围格式错误，应为 <起始行:结束行>
```

### 测试8：格式错误（多个冒号）

```
> show 1:2:3
错误: 范围格式错误，应为 <起始行:结束行>
```

### 测试9：起始行小于1

```
> show 0:3
错误: 起始行号不能小于1
```

### 测试10：结束行超出范围

```
> show 1:100
错误: 结束行号超出文件范围 (文件共 5 行)
```

### 测试11：起始行大于结束行

```
> show 5:2
错误: 起始行号不能大于结束行号
```

### 测试12：非数字行号

```
> show abc:def
错误: 行号必须是数字
```

### 测试13：空文件

```
> init empty.txt
> show
(空文件)

> show 1:1
(空文件)
```

## 完整测试脚本

```bash
# 启动编辑器
java -jar target/editor-1.0-SNAPSHOT.jar

# 在编辑器中执行以下命令序列

# 准备测试文件
> init show_test.txt
> append "First line"
> append "Second line"
> append "Third line"
> append "Fourth line"
> append "Fifth line"
> append "Sixth line"
> append "Seventh line"
> append "Eighth line"
> append "Ninth line"
> append "Tenth line"

# 测试全文显示
> show
(应显示1-10行)

# 测试范围显示
> show 1:3
(应显示1-3行)

> show 5:7
(应显示5-7行)

> show 8:10
(应显示8-10行)

# 测试单行显示
> show 5:5
(应显示第5行)

# 测试边界情况
> show 1:1
(应显示第1行)

> show 10:10
(应显示第10行)

# 测试错误处理
> show 0:5
(应报错：起始行号不能小于1)

> show 5:100
(应报错：结束行号超出文件范围)

> show 8:5
(应报错：起始行号不能大于结束行号)

> show abc
(应报错：范围格式错误)

> save
> exit
```

## 与其他命令的组合测试

### 测试：编辑后立即查看范围

```
> init combo.txt
> append "Line 1"
> append "Line 2"
> append "Line 3"

> show 1:2
1: Line 1
2: Line 2

> insert 2:1 "Modified "
> show 1:3
1: Line 1
2: Modified Line 2
3: Line 3

> delete 2:1 9
> show 2:2
2: Line 2

> undo
> show 2:2
2: Modified Line 2
```

## 性能测试（可选）

对于大文件，测试范围显示的性能：

```
# 创建包含1000行的文件
> init large.txt
(使用脚本添加1000行)

# 测试显示不同范围
> show 1:10      # 显示前10行
> show 500:510   # 显示中间10行
> show 990:1000  # 显示后10行
> show           # 显示全部1000行（注意性能）
```

## 预期结果

所有测试用例应该：
1. ✅ 正确显示指定范围的行
2. ✅ 行号格式为 `行号: 内容`
3. ✅ 空文件显示 `(空文件)`
4. ✅ 错误输入给出清晰的错误提示
5. ✅ 边界条件处理正确

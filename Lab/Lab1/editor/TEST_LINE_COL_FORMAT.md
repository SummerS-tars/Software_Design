# 测试 line:col 格式的命令

## 测试命令示例

### insert 命令
正确格式：`insert 1:5 "text"`
- 在第1行第5列插入文本 "text"

示例：
```
> init test.txt
> append "Hello World"
> show
1: Hello World

> insert 1:7 "Beautiful "
> show
1: Hello Beautiful World
```

### delete 命令
正确格式：`delete 1:7 9`
- 从第1行第7列开始删除9个字符

示例：
```
> init test.txt
> append "Hello Beautiful World"
> show
1: Hello Beautiful World

> delete 1:7 10
> show
1: Hello World
```

### replace 命令
正确格式：`replace 1:1 5 "Hi"`
- 从第1行第1列开始替换5个字符为 "Hi"

示例：
```
> init test.txt
> append "Hello World"
> show
1: Hello World

> replace 1:1 5 "Hi"
> show
1: Hi World
```

## 完整测试流程

```bash
# 1. 启动编辑器
java -jar target/editor-1.0-SNAPSHOT.jar

# 2. 在编辑器中执行以下命令：
> init test_format.txt
> append "Line 1"
> append "Line 2"
> show
1: Line 1
2: Line 2

> insert 1:1 "Start "
> show
1: Start Line 1
2: Line 2

> delete 2:1 5
> show
1: Start Line 1
2:  2

> replace 2:1 2 "Line"
> show
1: Start Line 1
2: Line

> save
> exit
```

## 错误处理测试

### 测试格式错误
```
> init test.txt
> append "test"
> insert 1 5 "wrong"
错误: 位置格式错误，应为 <行:列>

> delete 1 2 3
错误: 位置格式错误，应为 <行:列>

> replace 1 2 3 "wrong"
错误: 位置格式错误，应为 <行:列>
```

### 测试正确格式
```
> insert 1:5 "right"
已插入文本

> delete 1:5 5
已删除文本

> replace 1:1 4 "new"
已替换文本
```

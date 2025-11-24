# delete 功能bug

## 问题描述

```txt
> show
1: hello world 1
2: hello world 2
3: hello world 3
4: hello world 4
5: hello world 5
6:
> delete 6 1 1
命令执行失败: Range [0, 1) out of bounds for length 0
> delete 6 1 0
命令执行失败: 列号越界: 1，有效范围: 1-0
> delete 6 0 1
命令执行失败: Range [-1, 0) out of bounds for length 0
```

无法正确处理空行的情况  

请先分析当前实现逻辑，然后提出几种可能的解决方案  

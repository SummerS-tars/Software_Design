# 路径问题

首先，暂时不确定当前实现在不同的系统上是否都能正常工作  

然后主要问题在于  
如何权衡全路径与仅文件名之间的关系  

当前处理中  
所有的文件均使用全路径进行标识  
但是在editor-list中仅显示文件名部分  
这不太方便（找不到打开的文件在哪）  

考虑在editor-list中同时显示全路径和文件名  
例如如下形式：  

```txt
* test.txt       (E:\_ComputerLearning\15_SoftwareDesign\Lab\Lab1\editor\doc\update_fix\test.txt)
  another.txt    (D:\Documents\another.txt)
```

(为了与prompt区分，建议此处活动文件前使用 * 标记，然后已修改的文件使用[modified]后缀标记)  

为了方便，也需要支持直接通过文件名进行编辑切换等操作  
例如 edit test.txt  
需要考虑文件名冲突的情况（不同路径下有同名文件）  
目前想法是如果存在这种情况  
为用户提供一个选择列表，列出所有匹配的文件路径  
让用户选择具体要操作的文件  
例如：  

```txt
当前打开了多个名为 test.txt 的文件：
1. E:\_ComputerLearning\15_SoftwareDesign\Lab\Lab1\editor\doc\update_fix\test.txt
2. D:\Documents\test.txt
请输入要操作的文件编号：
> 1
```

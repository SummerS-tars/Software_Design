# help menu 改变

为了更贴合文档，在进行了之前的功能更新后，帮助菜单进行部分调整  

```txt
工作区命令:
  load <file>             - 加载文件 (支持自动识别 #log)
  save [file|all]         - 保存当前文件、指定文件或所有文件
  init <file> [with-log]  - 创建新文件 (可选自动开启日志)
  close [file]            - 关闭当前或指定文件
  edit <file>             - 切换当前活动文件
  editor-list             - 列出打开的文件及状态
  undo                    - 撤销
  redo                    - 重做
  exit                    - 退出程序 (自动保存工作区)

编辑命令:
  append <text>                - 在末尾追加一行
  insert <line:col> <text>     - 在指定位置插入文本 (例: insert 1:5 "text")
  delete <line:col> <len>      - 删除指定长度字符
  replace <line:col> <len> <text> - 替换文本
  show [start:end]             - 显示全文或指定行范围 (例: show 1:10)

日志命令:
  log-on [file]           - 启用日志
  log-off [file]          - 禁用日志
  log-show [file]         - 显示日志内容

辅助命令:
  dir-tree [path]         - 显示目录树
  help                    - 显示此帮助
```

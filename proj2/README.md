附加功能：

- .gitletignore文件支持ignore
- `show [commitID]`显示某个commit的详细信息

```bash
# cd proj2/
alias gitlet='java gitlet/Main'
alias dump='java gitlet/DumpObj'

# cd proj2/testing/test_folder/
alias gitlet='java -cp ../.. gitlet/Main'
alias dump='java gitlet/DumpObj'

# Demo
rm -dr .gitlet/
gitlet init
echo $'gitlet-design.md\nMakefile\nREADME.md'>.gitletignore
gitlet add .gitletignore
gitlet commit "ignore"

gitlet branch a
gitlet branch b

gitlet checkout a
echo "a">a.txt
gitlet add a.txt
gitlet commit "add a.txt"
echo "whatever">>.gitletignore
gitlet add .gitletignore
gitlet commit "change ignore in a"

gitlet checkout b
echo "b">b.txt
gitlet add b.txt
gitlet commit "add b.txt"
echo "whereever">>.gitletignore
gitlet add .gitletignore
gitlet commit "change ignore in b"

gitlet status
gitlet merge a
```


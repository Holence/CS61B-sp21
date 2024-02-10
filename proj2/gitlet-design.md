# Gitlet Design Document

**Name**: Holence

参考阅读：[Git from the inside out](https://www.youtube.com/watch?v=fCtZWGhQBvo)

```
.gitlet/
  objects/ # Blob or Commit
    a1/
      b2xxxx # hashID of Commit (Commit的Serialization)
    c3/
      b4xxxx # hashID of Commit's stage (commit时的“文件-hashID”映射，状态符全部为UNCHANGED)
    e5/
      f6xxxx # hashID of file (任何staged或comitted的文件，只增不删)
  refs/
    heads/ # Branch
      master (master's tip commit的hashID)
      deputy (其他branch's tip commit的hashID)
  HEAD # 当前指向的commit的hashID
  stage # 类似.git中的index文件，保存当前的“文件-hashID”映射，状态符为ADDED、REMOVED的表示add和rm的
```

## Repository

### variables

- `String HEAD`

  💾`.getlet/HEAD`

- `String branch`

  💾`.getlet/refs/heads/[branchname]`

- `Stage stage`

  💾`.gitlet/stage`

  注意！add和rm都会进入staging area

### methods

- `gitlet init`

  ```java
  void init(){
      if (.gitlet exist){
          "A Gitlet version-control system already exists in the current directory.";
          return;
      }
      建立.gitlet文件树;
      commit("initial commit");
  }
  ```

- `gitlet add [filename]`

  ```java
  // stage中state为ADDED的是否有一样的文件
  boolean containAdd(String hashID);
  
  void add(String filename){
      if (filename not exist){
          "File does not exist.";
          return;
      }
      fileHashID = hashFile(filename);
      if (!HEAD_Commit.contain(fileHashID)){
          // 最新的Commit中不包含file
          if (!containAdd(fileHashID)){
              // staging area的ADDED中不包含file
              复制file到objects;
              新建或更新stage中对应file的state为ADDED;
          }
      }
      else{
          // 最新的Commit中包含file
          // 两种可能，不管咋样，都变为UNCHANGED就行了
          // ①file修改后add了，又修改返回了上一个Commit中的样子
          // ②rm file后，又添加了一个一模一样的回来
          更新stage中对应file的state为UNCHANGED;
          // 不用删掉object中的之前add的Blob
          // git中用prune去除dangling object
      }
  }
  ```

- `gitlet commit`

  ```java
  // stage中是否有state为ADDED或REMOVED的
  boolean hasStaged();
  
  void commit(String message){
      if (!hasStaged()){
          "No changes added to the commit."
      }
      把stage中state为REMOVED的删除;
      把stage中所有state设为UNCHANGED;
      计算hashID，复制到object;
      Commit c = new Commit();
      c.setMessage(message);
      c.setstage(hashID);
      c.setCurrentTimestamp();
      c.parent=HEAD;
  
      存储commit到object;
      branch存储c.hashcode();
      HEAD存储branch;
  }
  ```

- `gitlet rm`

  ```java
  void remove(String filename){
      fileHashID = hashFile(filename);
      if (containAdd(fileHashID)){
          更新stage中对应file的state为UNCHANGED;
          return;
      }
      else if (HEAD_Commit.contain(fileHashID)){
          更新stage中对应file的state为REMOVED;
          如果文件在的话，删除文件;
          return;
      }
      else{
          "No reason to remove the file.";
      }
  }
  ```

- `gitlet log`

  ```java
  void log(){
      p=Commit.fromHash(HEAD);
      while(p.hashParent()){
          print();
          p=p.firstParent();
      }
  }
  ```

- `gitlet global-log`

  ```java
  void globalLog(){
      List<String> commitList = Utiles.plainFilenamesIn(OBJECT_DIR);
      for(commitHashID in commitList){
          print();
      }
  }
  ```

- `gitlet find [message]`

  ```java
  void find(String message){
      List<String> commitList = Utiles.plainFilenamesIn(OBJECT_DIR);
      boolean found=false;
      for(commitHashID in commitList){
          if (commit.containMessage(message)){
              print(commitHashID);
              found=true;
          }
      }
      if(!found){
          "Found no commit with that message.";
      }
  }
  ```

- `gitlet status`

  ```java
  void status(){
      // entry in lexicographic order
      "=== Branches ===";
      "*master";
      "deputy";
  
      "=== Staged Files ===";
      stage中stauts为ADDED;
      // 即使在这里出现，也可能在(deleted)或(modified)中再次出现
  
      "=== Removed Files ===";
      stage中stauts为REMOVED;
      // 即使在这里出现，也可能在Untracked中再次出现
  
      "=== Modifications Not Staged For Commit ===";
      // (deleted)
      stage中stauts不为REMOVED的文件名 且 现在not exist了;
  
      // (modified)
      stage中stauts不为REMOVED的文件 且 被修改后hashID不一样了;
  
      "=== Untracked Files ===";
      不在stage中 且 exist;
      stage中status为REMOVED的文件名 且 现在exist了;
  }
  ```

- `gitlet checkout -- [filename]`

  ```java
  void checkoutFileInHEAD(String filename){
      if (!在HEAD中找filename){
          "File does not exist in that commit.";
      }
      删除，复制objects;
  }
  ```

- `gitlet checkout [commitID] -- [filename]`

  ```java
  void checkoutFileInCommit(String filename, String commitID){
      "No commit with that id exists.";
      "File does not exist in that commit.";
      删除，复制objects;
  }
  // commitID要和git一样支持4位以上的缩写
  ```

- `gitlet checkout [branchname]`

  ```java
  void checkoutBranch(String branchname){
      "No such branch exists.";
      if (branch == branchID(branchname)){
          "No need to checkout the current branch.";
          return;
      }
      if (hasUntracked()){
          "There is an untracked file in the way; delete it, or add and commit it first.";        
      }
      checkoutCommit(branch);
  }
  
  void checkoutCommit(String commitID){
      删光;
      复制commit的objects;
      复制commit的stage;
      更新HEAD;
  }
  ```

- `gitlet branch [branchname]`

  ```java
  void createBranch(String branchname){
      复制branch到新文件.getlet/refs/heads/branchname;
  }
  ```

- `gitlet rm-branch [branchname]`

  ```java
  void removeBranch(String branchname){
  	"A branch with that name does not exist.";
      if (Branch.name(branch)==branchname){
          "Cannot remove the current branch.";
      }
      删除.getlet/refs/heads/branchname;
      // 不用管branch中的commit
      // git中会在一定的expire时间后自动prune dangling commit
  }
  ```

- `gitlet reset [commitID]`

  ```java
  // 和 git reset --hard [commitID] 一样
  // 可以跨branch随意reset，只用把branch的指针设为commitID就行了
  void reset(String commitID){
      "No commit with that id exists.";
      checkoutCommit(commitID);
      设置branch为commitID;
      // 不用管branch中的commit
      // git中会在一定的expire时间后自动prune dangling commit
  }
  ```

## Commit

### variables

- `String message`
- `String timestamp`
- `List<String> parent` ？？？？？？
- `Stage stage` commit的stage失去了staging area的作用，state都为`UNCHANGED`

### methods

- `boolean contain(String hashID)` stage中是否有一样的文件
- `boolean containMessage(String s)` message中是否包含s

## Stage

作为repo的stage（当前的working directory以及staging area）或 commit的stage

Map?/List? of File？？？？？？

Inner Class File：

- `String filepath` 文件路径（因为Gitlet是flat的，所以不涉及文件夹路径，只是文件名）

- `String hashID`

- `int state`

  > `UNCHANGED` 未改变
  >
  > `STAGED` add
  >
  > `REMOVED` rm

- `int collionID` 冲突??

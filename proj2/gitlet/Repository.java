package gitlet;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Holence
 */
public class Repository {
    /**
     * add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File BLOB_DIR = join(GITLET_DIR, "blob");
    public static final File COMMIT_DIR = join(GITLET_DIR, "commit");
    public static final File BRANCH_DIR = join(GITLET_DIR, "refs/heads");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File STAGE_FILE = join(GITLET_DIR, "stage");

    private static Stage stage;

    public static void checkInitialized() {
        if (!GITLET_DIR.exists()) {
            message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /**
     * 从HEAD中读取branch的名字
     * @return
     */
    private static String getBranch() {
        return readContentsAsString(HEAD_FILE);
    }

    /**
     * 从refs/heads/读取所有branch的名字
     * @return
     */
    private static List<String> getBranches() {
        return plainFilenamesIn(BRANCH_DIR);
    }

    /**
     * 在HEAD中写入branchname
     * @param branchName
     */
    private static void writeBranch(String branchName) {
        writeContents(HEAD_FILE, branchName);
    }

    /**
     * 用HEAD新建branch
     * @param branchName
     */
    private static void createBranch(String branchName) {
        writeContents(join(BRANCH_DIR, branchName), getHead());
    }

    /**
     * 是否存在名为branchName的branch
     * @param branchName
     * @return
     */
    private static boolean isBranchExist(String branchName) {
        return join(BRANCH_DIR, branchName).exists();
    }

    /**
     * 从refs/heads/[branchName]中读取commitHashID
     * @param branchName
     * @return
     */
    private static String getBranchTip(String branchName) {
        return readContentsAsString(join(BRANCH_DIR, branchName));
    }

    /**
     * 从refs/heads/[currentbranch]中读取commitHashID
     * @return
     */
    private static String getHead() {
        return readContentsAsString(join(BRANCH_DIR, getBranch()));
    }

    /**
     * 在refs/heads/[currentbranch]中写入commitHashID
     * @param commitHashID
     */
    private static void writeHead(String commitHashID) {
        writeContents(join(BRANCH_DIR, getBranch()), commitHashID);
    }

    /**
     * 当前branch's tip的commit
     * @return
     */
    private static Commit getHeadCommit() {
        return Commit.load(getHead());
    }

    /**
     * 读取stage
     */
    private static void loadStage() {
        if (!STAGE_FILE.exists()) {
            stage = new Stage();
        } else {
            stage = Stage.load();
        }
    }

    /**
     * 写入stage
     */
    private static void saveStage() {
        stage.save();
    }

    /////////////////////////////////////////////////////////////////

    public static void init() {
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        // 建立.gitlet文件树;
        GITLET_DIR.mkdirs();
        BLOB_DIR.mkdirs();
        COMMIT_DIR.mkdirs();
        BRANCH_DIR.mkdirs();
        writeBranch("master");

        loadStage();
        Commit c = Commit.initCommit();
        c.save();
        writeHead(c.getHashID());
        saveStage();
    }

    public static void add(String filename) {
        File f = join(CWD, filename);
        if (!f.exists()) {
            message("File does not exist.");
            System.exit(0);
        }

        loadStage();
        String fileHashID = Blob.getHashID(f);

        // 是否恢复到HeadCommit中的样子
        if (!getHeadCommit().containsTracked(filename, fileHashID)) {
            // HeadCommit中不包含file
            if (!stage.containsAdded(filename, fileHashID)) {
                // staging area的ADDED中不包含file

                // 复制file到objects
                Blob.writeBlob(f, fileHashID);
                // stage的added中写入（如果在unchanged或是removed中，则要去除）
                stage.changeState(filename, fileHashID, Stage.STATE.ADDED);
            }
        } else {
            // 恢复到HeadCommit的样子
            // 三种可能，不管咋样，都变为UNCHANGED就行了
            // 1. 和上一个Commit时比没有任何变化
            // 2. file修改后add了，又修改返回了上一个Commit中的样子
            // 3. rm file后，又添加了一个一模一样的回来
            stage.changeState(filename, fileHashID, Stage.STATE.UNCHANGED);
            // 不用删掉object中的之前add的Blob
            // git中用prune去除dangling object
        }

        saveStage();
    }

    public static void commit(String m) {
        // gitlet commit ""
        // 实际上这个错误不可能发生，因为在Main.java中就会在checkOperands时被判断为"Incorrect operands."
        // 但gradescope上测试点test17-empty-commit-message-err.in并不会报错
        if (m.isEmpty()) {
            message("Please enter a commit message.");
            System.exit(0);
        }

        loadStage();
        if (!stage.hasStaged()) {
            message("No changes added to the commit.");
            System.exit(0);
        }

        stage.performCommit();
        Commit c = new Commit(m, new Date(), getHead(), stage.getUnchanged());
        c.save();
        writeHead(c.getHashID());

        saveStage();
    }

    /**
     * merge时的commit
     * @param m
     * @param parent2
     */
    private static void commit(String m, Commit parent2) {
        if (m.isEmpty()) {
            message("Please enter a commit message.");
            System.exit(0);
        }

        loadStage();
        if (!stage.hasStaged()) {
            message("No changes added to the commit.");
            System.exit(0);
        }

        stage.performCommit();
        Commit c = new Commit(m, new Date(), getHead(), parent2.getHashID(), stage.getUnchanged());
        c.save();
        writeHead(c.getHashID());

        saveStage();
    }

    public static void remove(String filename) {
        File f = join(CWD, filename);
        String oldHashID = getHeadCommit().getFileHashID(filename);

        loadStage();
        if (!f.exists()) {
            // 已经在gitlet不知情时(deleted)了
            stage.changeState(filename, oldHashID, Stage.STATE.REMOVED);
        } else {
            String fileHashID = Blob.getHashID(f);
            if (stage.containsAdded(filename, fileHashID)) {
                // stage中为added
                // rm表示unstage

                // unstage的情况要考虑是否tracked
                if (getHeadCommit().containsTracked(filename)) {
                    // 如果是tracked则恢复至unchanged
                    stage.changeState(filename, oldHashID, Stage.STATE.UNCHANGED);
                } else {
                    //否则恢复至untracked
                    stage.removeAdded(filename);
                }

            } else if (stage.containsUnchanged(filename, fileHashID)) {
                // stage中为unchanged（保持HeadCommit中的样子）
                // rm表示删除
                stage.changeState(filename, oldHashID, Stage.STATE.REMOVED);
                restrictedDelete(f);
            } else {
                // modified (but haven't added) or Untracked
                message("No reason to remove the file.");
                System.exit(0);
            }
        }
        saveStage();
    }

    public static void log() {
        Commit c = getHeadCommit();
        while (true) {
            message(c.getLog());
            if (c.hasParentCommit()) {
                c = c.getParent1Commit();
            } else {
                break;
            }
        }
    }

    public static void show(String commitHashID) {
        message(Commit.load(commitHashID).getFullLog());
    }

    public static void globalLog() {
        List<String> allObjHashID = Commit.getAllObjHashID(COMMIT_DIR);
        for (String commitHashID : allObjHashID) {
            message(Commit.load(commitHashID).getLog());
        }
    }

    public static void find(String s) {
        List<String> allObjHashID = Commit.getAllObjHashID(COMMIT_DIR);
        Commit c;
        boolean found = false;
        for (String commitHashID : allObjHashID) {
            c = Commit.load(commitHashID);
            if (c.getMessage().contains(s)) {
                message(c.getHashID());
                found = true;
            }
        }
        if (!found) {
            message("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status() {
        // entry in lexicographic order
        loadStage();

        message("=== Branches ===");
        String currentBranch = getBranch();
        for (String branchname : getBranches()) {
            if (currentBranch.equals(branchname)) {
                message("*" + branchname);
            } else {
                message(branchname);
            }
        }

        message("");
        message("=== Staged Files ===");
        // 即使在这里出现，也可能在(deleted)或(modified)中再次出现
        for (String filename : stage.getAdded().keySet()) {
            message(filename);
        }

        message("");
        message("=== Removed Files ===");
        // 即使在这里出现，也可能在Untracked中再次出现
        for (String filename : stage.getRemoved().keySet()) {
            message(filename);
        }

        message("");
        message("=== Modifications Not Staged For Commit ===");
        SortedSet<String> sortedSet = new TreeSet<>();
        for (String filename : stage.getDeleted()) {
            sortedSet.add(filename + " (deleted)");
        }
        for (String filename : stage.getModified()) {
            sortedSet.add(filename + " (modified)");
        }
        for (String s : sortedSet) {
            message(s);
        }

        message("");
        message("=== Untracked Files ===");
        for (String s : stage.getUntracked()) {
            message(s);
        }
        message("");
    }

    public static void checkoutFileInHeadDCommit(String filename) {
        checkoutFileInCommit(getHead(), filename);
    }

    public static void checkoutFileInCommit(String commitHashID, String filename) {
        Commit c = Commit.load(commitHashID);

        if (!c.containsTracked(filename)) {
            message("File does not exist in that commit.");
            System.exit(0);
        }
        File f = join(CWD, filename);
        restrictedDelete(f);
        writeContents(f, c.getFileContent(filename));
    }

    private static Commit checkoutCommit(String commitHashID) {
        // gitlet没有detached HEAD state，所以这个不能让外界调用，但是checkout branch和reset要用
        loadStage();
        Commit c = Commit.load(commitHashID);

        // 检查是否有Untracked或Modifications Not Staged会在checkoutCommit中被修改
        // 必须提前检查，不然在checkoutCommit会执行删除文件、修改文件等无法撤销的命令
        SortedSet<String> checkFileList = stage.getUntracked();
        checkFileList.addAll(stage.getModified());
        for (String filename : checkFileList) {
            if (c.containsTracked(filename)) {
                message("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        // 清空
        for (String filename : Stage.getNotIgnoredFiles()) {
            restrictedDelete(join(CWD, filename));
        }

        // 写入
        SortedMap<String, String> tracked = c.getTracked();
        for (String filename : tracked.keySet()) {
            writeContents(join(CWD, filename), c.getFileContent(filename));
        }

        stage = new Stage(tracked);
        saveStage();
        return c;
    }

    public static void checkoutBranch(String branchname) {
        if (!isBranchExist(branchname)) {
            message("No such branch exists.");
            System.exit(0);
        }
        if (getBranch().equals(branchname)) {
            message("No need to checkout the current branch.");
            System.exit(0);
        }
        checkoutCommit(getBranchTip(branchname));
        writeBranch(branchname);
    }

    public static void branch(String branchname) {
        if (getBranches().contains(branchname)) {
            message("A branch with that name already exists.");
            System.exit(0);
        }
        createBranch(branchname);
    }

    public static void removeBranch(String branchname) {
        if (!isBranchExist(branchname)) {
            message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (getBranch().equals(branchname)) {
            message("Cannot remove the current branch.");
            System.exit(0);
        }
        File f = join(BRANCH_DIR, branchname);
        f.delete();
        // 不用管branch中的commit
        // git中会在一定的expire时间后自动prune dangling commit
    }

    // 和 git reset --hard [commitID] 一样
    // 可以跨branch随意reset，只用把branch的指针设为commitID就行了
    public static void reset(String commitHashID) {
        Commit c = checkoutCommit(commitHashID);
        // 防止传进来的commitHashID是短链，这里用返回的Commit获得HashID
        writeHead(c.getHashID());
        // 不用管branch中的commit
        // git中会在一定的expire时间后自动prune dangling commit
    }

    private static Map<String, Integer> BFS(Commit start) {
        Map<String, Integer> hashIDtoDepth = new HashMap<>();
        Deque<Commit> neighbors = new ArrayDeque<>();

        neighbors.add(start);
        int depth = 0;
        Commit commit;
        while (!neighbors.isEmpty()) {
            for (int i = 0; i < neighbors.size(); i++) {
                commit = neighbors.removeFirst();
                // 得保证不会重复访问
                if (!hashIDtoDepth.containsKey(commit.getHashID())) {
                    hashIDtoDepth.put(commit.getHashID(), depth);
                    if (commit.hasParent1Commit()) {
                        neighbors.addLast(commit.getParent1Commit());
                    }
                    if (commit.hasParent2Commit()) {
                        neighbors.addLast(commit.getParent2Commit());
                    }
                }
            }
            depth++;
        }

        return hashIDtoDepth;
    }

    /**
     * 关于splitPoint，有向无环图的最近交点。
     * 思路是从两者往根回溯，在对方路径上且深度最浅的节点就是splitPoint。
     * （若二者之一属于对方的树，那么就是不需要任何操作的前两种情况）
     * 
     * 考虑用BFS的深度，即回溯树的深度，splitPoint是两棵树“相交且深度同时最小”的节点。
     * 若二者之一属于对方的树，那么没有split。
     * （两次BFS，把commitID和深度的映射分别记录在两个`Map<String, int>`中）
     * @param currentCommit
     * @param branchCommit
     * @return
     */
    private static Commit splitPoint(Commit currentCommit, Commit branchCommit) {
        Map<String, Integer> currentCommitMap = BFS(currentCommit);
        Map<String, Integer> branchCommitMap = BFS(branchCommit);

        Commit splitPoint = null;
        if (branchCommitMap.containsKey(currentCommit.getHashID())) {
            splitPoint = currentCommit;
        } else if (currentCommitMap.containsKey(branchCommit.getHashID())) {
            splitPoint = branchCommit;
        } else {
            // “相交且深度同时最小”：取其中一棵树按照深度从小到大遍历，第一个属于另一棵树的节点，就是splitPoint
            List<Map.Entry<String, Integer>> list = new ArrayList<>(currentCommitMap.entrySet());
            list.sort(Map.Entry.comparingByValue());
            for (Map.Entry<String, Integer> entry : list) {
                if (branchCommitMap.containsKey(entry.getKey())) {
                    splitPoint = Commit.load(entry.getKey());
                    break;
                }
            }
        }
        return splitPoint;
    }

    public static void merge(String branchname) {
        loadStage();
        if (stage.hasStaged()) {
            message("You have uncommitted changes.");
            System.exit(0);
        }
        if (!isBranchExist(branchname)) {
            message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (getBranch().equals(branchname)) {
            message("Cannot merge a branch with itself.");
            System.exit(0);
        }

        Commit currentCommit = getHeadCommit();
        Commit branchCommit = Commit.load(getBranchTip(branchname));
        Commit splitPoint = splitPoint(currentCommit, branchCommit);

        if (splitPoint.getHashID().equals(branchCommit.getHashID())) {
            // ①
            // root - branch - commit - commit - *current
            // root - branch - commit - commit - *current
            message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitPoint.getHashID().equals(currentCommit.getHashID())) {
            // ② fast-forward
            // root - *current - commit - commit - branch
            // root - commit - commit - commit - branch/*current
            reset(branchCommit.getHashID());
            message("Current branch fast-forwarded.");
            System.exit(0);
        } else {
            // ③
            Set<String> allfiles = new HashSet<>();
            allfiles.addAll(currentCommit.getTracked().keySet());
            allfiles.addAll(branchCommit.getTracked().keySet());
            allfiles.addAll(splitPoint.getTracked().keySet());

            // 检查是否有Untracked或Modifications Not Staged会在merge中被修改
            // 必须提前检查，不然在mergeOptions会执行删除文件、修改stage等无法撤销的命令
            SortedSet<String> checkFileList = stage.getUntracked();
            checkFileList.addAll(stage.getModified());
            for (String filename : checkFileList) {
                if (mergeWillOverwriteFile(filename, currentCommit, branchCommit, splitPoint)) {
                    message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }

            // 对号入座执行命令
            boolean conflicted = false;
            for (String filename : allfiles) {
                if (mergeOptions(filename, currentCommit, branchCommit, splitPoint)) {
                    conflicted = true;
                }
            }

            commit(String.format("Merged %s into %s.", branchname, getBranch()), branchCommit);
            if (conflicted) {
                message("Encountered a merge conflict.");
            }

        }
    }

    /**
     * 和mergeOptions的逻辑一模一样，但不进行任何操作，只是用来检查
     * @param filename
     * @param currentCommit
     * @param branchCommit
     * @param splitPoint
     * @return
     */
    private static boolean mergeWillOverwriteFile(String filename, Commit currentCommit, Commit branchCommit, Commit splitPoint) {
        if (splitPoint.containsTracked(filename)) {
            if (branchCommit.containsTracked(filename) && currentCommit.containsTracked(filename)) {
                // 1 4
                if (!commitFileHashIDSame(filename, branchCommit, splitPoint)) {
                    if (commitFileHashIDSame(filename, currentCommit, splitPoint)) {
                        // 1
                        return true;
                    } else {
                        if (!commitFileHashIDSame(filename, branchCommit, currentCommit)) {
                            // 4
                            return true;
                        }
                    }
                }
            } else if (branchCommit.containsTracked(filename)) {
                if (!commitFileHashIDSame(filename, branchCommit, splitPoint)) {
                    // 7
                    return true;
                }
            } else if (currentCommit.containsTracked(filename)) {
                // 3 6
                if (commitFileHashIDSame(filename, currentCommit, splitPoint)) {
                    // 3
                    return true;
                } else {
                    return true;
                }
            }
        } else {
            if (branchCommit.containsTracked(filename)) {
                if (!currentCommit.containsTracked(filename)) {
                    // 2
                    return true;
                } else {
                    // 5
                    if (!commitFileHashIDSame(filename, branchCommit, currentCommit)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean commitFileHashIDSame(String filename, Commit c1, Commit c2) {
        return c1.getFileHashID(filename).equals(c2.getFileHashID(filename));
    }

    /**
     * 7中情况的逻辑分支，详见getlet-design.md中的表格
     * @param filename
     * @param currentCommit
     * @param branchCommit
     * @param splitPoint
     * @return 是否有conflict
     */
    private static boolean mergeOptions(String filename, Commit currentCommit, Commit branchCommit, Commit splitPoint) {
        if (splitPoint.containsTracked(filename)) {
            if (branchCommit.containsTracked(filename) && currentCommit.containsTracked(filename)) {
                // 1 4
                if (!commitFileHashIDSame(filename, branchCommit, splitPoint)) {
                    if (commitFileHashIDSame(filename, currentCommit, splitPoint)) {
                        // 1
                        mergeWithCheckoutBranch(branchCommit, filename);
                        return false;
                    } else {
                        if (!commitFileHashIDSame(filename, branchCommit, currentCommit)) {
                            // 4
                            mergeWithConflict(filename, currentCommit, branchCommit);
                            return true;
                        }
                    }
                }
            } else if (branchCommit.containsTracked(filename)) {
                if (!commitFileHashIDSame(filename, branchCommit, splitPoint)) {
                    // 7
                    mergeWithConflict(filename, currentCommit, branchCommit);
                    return true;
                }
            } else if (currentCommit.containsTracked(filename)) {
                // 3 6
                if (commitFileHashIDSame(filename, currentCommit, splitPoint)) {
                    // 3
                    remove(filename);
                    return false;
                } else {
                    mergeWithConflict(filename, currentCommit, branchCommit);
                    return true;
                }
            }
        } else {
            if (branchCommit.containsTracked(filename)) {
                if (!currentCommit.containsTracked(filename)) {
                    // 2
                    mergeWithCheckoutBranch(branchCommit, filename);
                    return false;
                } else {
                    // 5
                    if (!commitFileHashIDSame(filename, branchCommit, currentCommit)) {
                        mergeWithConflict(filename, currentCommit, branchCommit);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void mergeWithCheckoutBranch(Commit branch, String filename) {
        checkoutFileInCommit(branch.getHashID(), filename);
        add(filename);
    }

    private static void mergeWithConflict(String filename, Commit c1, Commit c2) {
        String t1 = "";
        if (c1.containsTracked(filename)) {
            t1 = c1.getFileContentAsString(filename);
        }
        String t2 = "";
        if (c2.containsTracked(filename)) {
            t2 = c2.getFileContentAsString(filename);
        }
        String text = String.format("""
                <<<<<<< HEAD
                %s=======
                %s>>>>>>>
                """, t1, t2);
        writeContents(join(CWD, filename), text);
        add(filename);
    }

}

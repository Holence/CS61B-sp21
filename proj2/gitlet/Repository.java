package gitlet;

import java.io.File;
import java.util.Date;
import java.util.List;
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
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
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
        OBJECTS_DIR.mkdirs();
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
                Obj.writeObj(readContents(f), fileHashID);
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

    public static void remove(String filename) {
        File f = join(CWD, filename);
        if (!f.exists()) {
            System.exit(0);
        }

        loadStage();
        String fileHashID = Blob.getHashID(f);
        if (stage.containsAdded(filename, fileHashID)) {
            // stage中为added
            // rm表示unstage
            String oldHashID = getHeadCommit().getTracked().get(filename);
            stage.changeState(filename, oldHashID, Stage.STATE.UNCHANGED);
        } else if (stage.containsUnchanged(filename, fileHashID)) {
            // stage中为unchanged（保持HeadCommit中的样子）
            // rm表示删除
            stage.changeState(filename, fileHashID, Stage.STATE.REMOVED);
            restrictedDelete(f);
        } else {
            // modified (but haven't added) or Untracked
            message("No reason to remove the file.");
            System.exit(0);
        }
        saveStage();
    }

    public static void log() {
        Commit c = getHeadCommit();
        while (true) {
            message(c.getLog());
            if (c.hasParentCommit()) {
                c = c.getParentCommit();
            } else {
                break;
            }
        }
    }

    public static void globalLog() {
        List<String> fileList = plainFilenamesIn(OBJECTS_DIR);
        for (String filename : fileList) {
            try {
                message(Obj.readCommit(filename).getLog());
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
    }

    public static void find(String s) {
        List<String> fileList = plainFilenamesIn(OBJECTS_DIR);
        Commit c;
        boolean found = false;
        for (String filename : fileList) {
            try {
                c = Obj.readCommit(filename);
                if (c.getMessage().contains(s)) {
                    message(c.getHashID());
                    found = true;
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        if (!found) {
            message("Found no commit with that message.");
        }
    }

    public static void status() {
        // entry in lexicographic order
        loadStage();

        // begin branch
        message("=== Branches ===");
        String currentBranch = getBranch();
        for (String filename : plainFilenamesIn(BRANCH_DIR)) {
            if (currentBranch.equals(filename)) {
                message("*" + filename);
            } else {
                message(filename);
            }
        }
        // end branch

        SortedMap<String, String> unchangedMap = stage.getUnchanged();
        SortedMap<String, String> addedMap = stage.getAdded();
        SortedMap<String, String> removedMap = stage.getRemoved();
        Set<String> unchangedSet = unchangedMap.keySet();
        Set<String> addedSet = addedMap.keySet();
        Set<String> removedSet = removedMap.keySet();
        List<String> cwdFileList = plainFilenamesIn(CWD);

        SortedSet<String> sortedSet = new TreeSet<>();

        message("");
        message("=== Staged Files ===");
        // 即使在这里出现，也可能在(deleted)或(modified)中再次出现
        for (String filename : addedSet) {
            message(filename);
        }

        message("");
        message("=== Removed Files ===");
        // 即使在这里出现，也可能在Untracked中再次出现
        for (String filename : removedSet) {
            message(filename);
        }

        message("");
        message("=== Modifications Not Staged For Commit ===");
        // (deleted)
        // stage中不为REMOVED的文件名 且 现在not exist了;
        for (String filename : unchangedSet) {
            if (!cwdFileList.contains(filename)) {
                sortedSet.add(filename + " (deleted)");
            }
        }
        for (String filename : addedSet) {
            if (!cwdFileList.contains(filename)) {
                sortedSet.add(filename + " (deleted)");
            }
        }
        // (modified)
        // stage中不为REMOVED的文件名 且 hashID不一样了;
        for (String filename : unchangedSet) {
            if (!Blob.getHashID(join(CWD, filename)).equals(unchangedMap.get(filename))) {
                sortedSet.add(filename + " (modified)");
            }
        }
        for (String filename : addedSet) {
            if (!Blob.getHashID(join(CWD, filename)).equals(addedMap.get(filename))) {
                sortedSet.add(filename + " (modified)");
            }
        }

        for (String s : sortedSet) {
            message(s);
        }

        message("");
        message("=== Untracked Files ===");
        sortedSet.clear();

        for (String filename : cwdFileList) {
            if (unchangedSet.contains(filename) || addedSet.contains(filename)) {
                continue;
            } else {
                // exist但在stage中不是added或unchanged;
                // exist但在stage中是removed;
                sortedSet.add(filename);
            }
        }
        for (String s : sortedSet) {
            message(s);
        }
        message("");
    }

    public static void branch(String branchname) {
        createBranch(branchname);
    }
}

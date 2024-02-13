package gitlet;

import java.io.File;
import java.util.Date;

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
     * 读取stage
     */
    private static void getStage() {
        if (!STAGE_FILE.exists()) {
            stage = new Stage();
        } else {
            stage = Stage.load();
        }
    }

    /**
     * 写入stage
     */
    private static void writeStage() {
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

        getStage();
        // 最初的commit的两个parent都是空字符串
        Commit c = new Commit("initial commit", new Date(0), "", stage.getUnchanged());
        c.save();
        writeHead(c.getHashID());
        writeStage();
    }

    public static void add(String filename) {
        getStage();

        File f = join(CWD, filename);
        if (!f.exists()) {
            message("File does not exist.");
            System.exit(0);
        }

        String fileHashID = sha1(readContents(f));

        if (!Commit.load(getHead()).containsTracked(fileHashID)) {
            // 最新的Commit中不包含file
            if (!stage.containsAdded(fileHashID)) {
                // staging area的ADDED中不包含file

                // 复制file到objects;
                Blob.writeBlob(readContents(f), fileHashID);
                // stage的added中写入;
                stage.addAdded(filename, fileHashID);
            }
        } else {
            // 最新的Commit中包含file
            // 三种可能，不管咋样，都变为UNCHANGED就行了
            // 1. 和上一个Commit时比没有任何变化
            // 2. file修改后add了，又修改返回了上一个Commit中的样子
            // 3. rm file后，又添加了一个一模一样的回来
            stage.setBackToUnchanged(filename, fileHashID);
            // 不用删掉object中的之前add的Blob
            // git中用prune去除dangling object
        }

        writeStage();
    }

    public static void commit(String m) {
        getStage();

        if (!stage.hasStaged()) {
            message("No changes added to the commit.");
            System.exit(0);
        }
        if (m.isEmpty()) {
            message("Please enter a commit message.");
            System.exit(0);
        }

        stage.performCommit();
        Commit c = new Commit(m, new Date(), getHead(), stage.getUnchanged());
        c.save();
        writeHead(c.getHashID());

        writeStage();
    }

}

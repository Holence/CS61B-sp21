package gitlet;

import static gitlet.Repository.COMMIT_DIR;
import static gitlet.Utils.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Holence
 */
public class Commit implements Dumpable {
    /**
     * add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String hashID;
    private String message;
    private String timestamp;
    private String parent1 = "";
    private String parent2 = "";
    private SortedMap<String, String> tracked;

    /////////////////////////////////////////////
    // Commit和Blob的路径方式一模一样，应该都继承自Obj
    // 但subclass没法修改superclass的static类型
    // 没法指定ROOT_DIR啊？？？
    public static File getPath(String hashID) {
        // 要用前2位作文件夹，后38位作文件名
        // 但要写global-lob，要获取所有的Commit，遍历文件夹太麻烦了，懒得弄了
        return join(COMMIT_DIR, hashID);
    }

    public static List<String> getAllObjHashID() {
        return plainFilenamesIn(COMMIT_DIR);
    }
    /////////////////////////////////////////////

    public Commit(String m, Date d, String parent1, SortedMap<String, String> tracked) {
        message = m;
        timestamp = formatDate(d);
        this.parent1 = parent1;
        this.tracked = tracked;
        generateHashID();
    }

    public static Commit initCommit() {
        // 最初的commit的两个parent都是空字符串
        return new Commit("initial commit", new Date(0), "", new TreeMap<>());
    }

    @Override
    public void dump() {
        System.out.println(hashID);
        System.out.println(message);
        System.out.println(timestamp);
        System.out.println(parent1);
        System.out.println(parent2);
        System.out.println(tracked);
    }

    private String formatDate(Date d) {
        return new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z").format(d);
    }

    private void generateHashID() {
        hashID = sha1(message, timestamp, parent1, parent2, tracked.toString());
    }

    public String getHashID() {
        return hashID;
    }

    public static Commit load(String commitHashID) {
        try {
            if (commitHashID.length() == 40) {
                return readObject(getPath(commitHashID), Commit.class);
            } else {
                // 支持短链访问
                List<String> allObjHashID = getAllObjHashID();
                for (String hashID : allObjHashID) {
                    if (commitHashID.equals(hashID.substring(0, commitHashID.length()))) {
                        return readObject(getPath(hashID), Commit.class);
                    }
                }
                message("No commit with that id exists.");
                System.exit(0);
                return null;
            }
        } catch (IllegalArgumentException e) {
            message("No commit with that id exists.");
            System.exit(0);
            return null;
        }
    }

    public void save() {
        writeObject(getPath(getHashID()), this);
    }

    public SortedMap<String, String> getTracked() {
        return tracked;
    }

    /**
     * Commit的tracked中是否含有和fileHashID相同的文件
     * @param fileHashID
     * @return
      */
    public boolean containsTracked(String filename, String fileHashID) {
        String s = tracked.get(filename);
        return s != null && s.equals(fileHashID);
    }

    public boolean hasParentCommit() {
        return !parent1.isEmpty();
    }

    public Commit getParentCommit() {
        return Commit.load(this.parent1);
    }

    public String getLog() {
        // TODO: 如果是Merge: 4975af1 2c1ead1
        return String.format("""
                ===
                commit %s
                Date: %s
                %s
                """, hashID, timestamp, message);
    }

    public String getMessage() {
        return message;
    }
}

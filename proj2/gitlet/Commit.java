package gitlet;

import static gitlet.Repository.COMMIT_DIR;
import static gitlet.Utils.*;

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
public class Commit extends Obj implements Dumpable {
    private static final long serialVersionUID = -6830305721978036177L;

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

    public Commit(String m, Date d, String parent1, SortedMap<String, String> tracked) {
        message = m;
        timestamp = formatDate(d);
        this.parent1 = parent1;
        this.tracked = tracked;
        generateHashID();
    }

    public Commit(String m, Date d, String parent1, String parent2, SortedMap<String, String> tracked) {
        message = m;
        timestamp = formatDate(d);
        this.parent1 = parent1;
        this.parent2 = parent2;
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
            if (commitHashID.length() == UID_LENGTH) {
                return readObject(getPath(COMMIT_DIR, commitHashID), Commit.class);
            } else {
                // 支持短链访问
                List<String> allObjHashID = getAllObjHashID(COMMIT_DIR);
                for (String hashID : allObjHashID) {
                    if (commitHashID.equals(hashID.substring(0, commitHashID.length()))) {
                        return readObject(getPath(COMMIT_DIR, hashID), Commit.class);
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
        writeObject(getPath(COMMIT_DIR, getHashID()), this);
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

    /**
     * Commit的tracked中是否含有文件名为filename的
     * @param filename
     * @return
     */
    public boolean containsTracked(String filename) {
        return tracked.containsKey(filename);
    }

    public String getFileHashID(String filename) {
        return tracked.get(filename);
    }

    public byte[] getFileContent(String filename) {
        return Blob.readContent(getFileHashID(filename));
    }

    public String getFileContentAsString(String filename) {
        return Blob.readContentAsString(getFileHashID(filename));
    }

    /**
     * init commit的parent1和parent2都是空字符串
     * 其他大部分commit中parent1非空
     * 只有merge的commit的parent1和parent2都非空
     * @return
     */
    public boolean hasParentCommit() {
        return !parent1.isEmpty();
    }

    public boolean hasParent1Commit() {
        return !parent1.isEmpty();
    }

    public boolean hasParent2Commit() {
        return !parent2.isEmpty();
    }

    public Commit getParent1Commit() {
        return Commit.load(this.parent1);
    }

    public Commit getParent2Commit() {
        return Commit.load(this.parent2);
    }

    public String getLog() {
        if (hasParent2Commit()) {
            return String.format("""
                    ===
                    commit %s
                    Merge: %s %s
                    Date: %s
                    %s
                    """, hashID, parent1.substring(0, 7), parent2.substring(0, 7), timestamp, message);
        } else {
            return String.format("""
                    ===
                    commit %s
                    Date: %s
                    %s
                    """, hashID, timestamp, message);
        }
    }

    public String getMessage() {
        return message;
    }
}

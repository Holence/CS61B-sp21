package gitlet;

import static gitlet.Utils.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, String> tracked;

    Commit(String m, Date d, String parent1, Map<String, String> tracked) {
        message = m;
        timestamp = formatDate(d);
        this.parent1 = parent1;
        this.tracked = tracked;
        generateHashID();
    }

    public static Commit initCommit() {
        // 最初的commit的两个parent都是空字符串
        return new Commit("initial commit", new Date(0), "", new HashMap<>());
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
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(d);
    }

    private void generateHashID() {
        hashID = sha1(message, timestamp, parent1, parent2, tracked.toString());
    }

    public String getHashID() {
        return hashID;
    }

    public static Commit load(String commitHashID) {
        return Obj.readCommit(commitHashID);
    }

    public void save() {
        Obj.writeObj(serialize(this), hashID);
    }

    /**
     * Commit的tracked中是否含有和fileHashID相同的文件
     * @param fileHashID
     * @return
      */
    public boolean containsTracked(String fileHashID) {
        return tracked.values().contains(fileHashID);
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

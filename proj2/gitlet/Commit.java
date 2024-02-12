package gitlet;

import static gitlet.Repository.OBJECTS_DIR;
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
    private String message;
    private String timestamp;
    private String parent1 = "";
    private String parent2 = "";
    private Map<String, String> tracked = new HashMap<>();

    Commit(String m, Date d, String parent, Map<String, String> tracked) {
        message = m;
        timestamp = formatDate(d);
        parent1 = parent;
        this.tracked = tracked;
    }

    @Override
    public void dump() {
        System.out.println(message);
        System.out.println(timestamp);
        System.out.println(parent1);
        System.out.println(parent2);
        System.out.println(tracked);
    }

    private String formatDate(Date d) {
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(d);
    }

    public String getHashID() {
        return sha1(message, timestamp, parent1, parent2, tracked.toString());
    }

    public static Commit load(String commitHashID) {
        return readObject(join(OBJECTS_DIR, commitHashID), Commit.class);
    }

    public void save() {
        Blob.writeBlob(serialize(this), getHashID());
    }

    /**
     * Commit的tracked中是否含有和fileHashID相同的文件
     * @param fileHashID
     * @return
      */
    public boolean containsTracked(String fileHashID) {
        return tracked.values().contains(fileHashID);
    }
}

package gitlet;

import static gitlet.Repository.STAGE_FILE;
import static gitlet.Utils.*;

import java.util.HashMap;
import java.util.Map;

public class Stage implements Dumpable {
    // 从filename到hashID的映射
    private Map<String, String> unchanged = new HashMap<>();
    private Map<String, String> added = new HashMap<>();
    private Map<String, String> removed = new HashMap<>();

    public enum STATE {
        UNCHANGED, ADDED, REMOVED
    }

    @Override
    public void dump() {
        System.out.println(unchanged);
        System.out.println(added);
        System.out.println(removed);
    }

    public boolean hasStaged() {
        return added.size() > 0 || removed.size() > 0;
    }

    /**
     * Commit时，把stage中removed清空，把added移入unchanged;
    */
    public void performCommit() {
        removed = new HashMap<>();
        for (String key : added.keySet()) {
            unchanged.put(key, added.get(key));
        }
        added = new HashMap<>();
    }

    public Map<String, String> getUnchanged() {
        return unchanged;
    }

    public static Stage load() {
        return readObject(STAGE_FILE, Stage.class);
    }

    public void save() {
        writeObject(STAGE_FILE, this);
    }

    public boolean containsAdded(String fileHashID) {
        return added.values().contains(fileHashID);
    }

    public void addAdded(String filename, String fileHashID) {
        added.put(filename, fileHashID);
    }

    public void changeState(String filename, String fileHashID, STATE state) {
        switch (state) {

        // Commit时的特殊情况、rm的情况1
        case UNCHANGED:
            added.remove(filename);
            removed.remove(filename);
            unchanged.put(filename, fileHashID);
            break;
        case ADDED:
            removed.remove(filename);
            unchanged.remove(filename);
            added.put(filename, fileHashID);
            break;
        // rm的情况2
        case REMOVED:
            added.remove(filename);
            unchanged.remove(filename);
            removed.put(filename, fileHashID);
            break;
        }
    }
}

package gitlet;

import static gitlet.Repository.STAGE_FILE;
import static gitlet.Utils.*;

import java.util.SortedMap;
import java.util.TreeMap;

public class Stage implements Dumpable {
    // 从filename到hashID的映射
    private SortedMap<String, String> unchanged = new TreeMap<>();
    private SortedMap<String, String> added = new TreeMap<>();
    private SortedMap<String, String> removed = new TreeMap<>();

    public enum STATE {
        UNCHANGED, ADDED, REMOVED
    }

    @Override
    public void dump() {
        System.out.println(unchanged);
        System.out.println(added);
        System.out.println(removed);
    }

    public SortedMap<String, String> getUnchanged() {
        return unchanged;
    }

    public boolean containsUnchanged(String fileHashID) {
        return unchanged.values().contains(fileHashID);
    }

    public SortedMap<String, String> getAdded() {
        return added;
    }

    public boolean containsAdded(String fileHashID) {
        return added.values().contains(fileHashID);
    }

    public SortedMap<String, String> getRemoved() {
        return removed;
    }

    public boolean hasStaged() {
        return added.size() > 0 || removed.size() > 0;
    }

    public static Stage load() {
        return readObject(STAGE_FILE, Stage.class);
    }

    public void save() {
        writeObject(STAGE_FILE, this);
    }

    /**
     * Commit时，把stage中removed清空，把added移入unchanged;
    */
    public void performCommit() {
        removed = new TreeMap<>();
        for (String key : added.keySet()) {
            unchanged.put(key, added.get(key));
        }
        added = new TreeMap<>();
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

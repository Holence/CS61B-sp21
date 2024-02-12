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

    /**
     * Commit时的特殊情况
     * 如果added或removed中存在filename的记录，则删除掉
     * 另外在unchanged中记录filename到fileHashID的映射
     * @param filename
     * @param fileHashID
      */
    public void setBackToUnchanged(String filename, String fileHashID) {
        added.remove(filename);
        removed.remove(filename);
        unchanged.put(filename, fileHashID);
    }
}

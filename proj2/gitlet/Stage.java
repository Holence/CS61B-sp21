package gitlet;

import static gitlet.Repository.CWD;
import static gitlet.Repository.STAGE_FILE;
import static gitlet.Utils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class Stage implements Dumpable {
    private static final long serialVersionUID = -6830305721978036177L;

    // 从filename到hashID的映射
    private SortedMap<String, String> unchanged;
    private SortedMap<String, String> added;
    private SortedMap<String, String> removed;

    public enum STATE {
        UNCHANGED, ADDED, REMOVED
    }

    Stage() {
        unchanged = new TreeMap<>();
        added = new TreeMap<>();
        removed = new TreeMap<>();
    }

    Stage(SortedMap<String, String> unchanged) {
        this.unchanged = unchanged;
        added = new TreeMap<>();
        removed = new TreeMap<>();
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

    public boolean containsUnchanged(String filename, String fileHashID) {
        String s = unchanged.get(filename);
        return s != null && s.equals(fileHashID);
    }

    public SortedMap<String, String> getAdded() {
        return added;
    }

    public boolean containsAdded(String filename, String fileHashID) {
        String s = added.get(filename);
        return s != null && s.equals(fileHashID);
    }

    public void removeAdded(String filename) {
        added.remove(filename);
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
     * stage中不为REMOVED的文件名 且 现在not exist了;
     * @return
     */
    public SortedSet<String> getDeleted() {
        List<String> cwdFileList = plainFilenamesIn(CWD);
        SortedSet<String> sortedSet = new TreeSet<>();
        for (String filename : unchanged.keySet()) {
            if (!cwdFileList.contains(filename)) {
                sortedSet.add(filename);
            }
        }
        for (String filename : added.keySet()) {
            if (!cwdFileList.contains(filename)) {
                sortedSet.add(filename);
            }
        }
        return sortedSet;
    }

    /**
     * stage中不为REMOVED的文件名 且 hashID不一样了;
     * @return
     */
    public SortedSet<String> getModified() {
        List<String> cwdFileList = plainFilenamesIn(CWD);
        SortedSet<String> sortedSet = new TreeSet<>();
        for (String filename : unchanged.keySet()) {
            if (cwdFileList.contains(filename) && !Blob.getHashID(join(CWD, filename)).equals(unchanged.get(filename))) {
                sortedSet.add(filename);
            }
        }
        for (String filename : added.keySet()) {
            if (cwdFileList.contains(filename) && !Blob.getHashID(join(CWD, filename)).equals(added.get(filename))) {
                sortedSet.add(filename);
            }
        }
        return sortedSet;
    }

    public static List<String> getIgnoredFiles() {
        // ignore files
        File gitletignore = join(CWD, ".gitletignore");
        List<String> ignoredFiles = null;
        if (gitletignore.exists()) {
            ignoredFiles = Arrays.asList(readContentsAsString(gitletignore).split("\\r?\\n"));
        }
        return ignoredFiles;
    }

    public static List<String> getNotIgnoredFiles() {
        List<String> ignoredFiles = getIgnoredFiles();
        List<String> cwdFileList = new ArrayList<>(plainFilenamesIn(CWD));

        if (ignoredFiles != null) {
            for (String filename : ignoredFiles) {
                if (cwdFileList.contains(filename)) {
                    cwdFileList.remove(filename);
                }
            }
        }
        return cwdFileList;
    }

    /**
     * Untracked
     * exist但在stage中不是added或unchanged
     * exist但在stage中是removed
     * @return
     */
    public SortedSet<String> getUntracked() {
        List<String> cwdFileList = getNotIgnoredFiles();

        SortedSet<String> sortedSet = new TreeSet<>();
        for (String filename : cwdFileList) {
            if (unchanged.containsKey(filename) || added.containsKey(filename)) {
                continue;
            } else {
                // exist但在stage中不是added或unchanged
                // exist但在stage中是removed
                sortedSet.add(filename);
            }
        }
        return sortedSet;
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

package gitlet;

import static gitlet.Repository.OBJECTS_DIR;
import static gitlet.Utils.*;

import java.util.List;

public class Obj {
    public static void writeObj(Object o, String hashID) {
        // 要用前2位作文件夹，后38位作文件名
        // 但要写global-lob，要获取所有的Commit，遍历文件夹太麻烦了，懒得弄了
        writeContents(join(OBJECTS_DIR, hashID), o);
    }

    public static Commit readCommit(String hashID) {
        if (hashID.length() == 40) {
            return readObject(join(OBJECTS_DIR, hashID), Commit.class);
        } else {
            // 支持短链访问
            List<String> fileList = plainFilenamesIn(OBJECTS_DIR);
            for (String filename : fileList) {
                if (hashID.equals(filename.substring(0, hashID.length()))) {
                    return readObject(join(OBJECTS_DIR, filename), Commit.class);
                }
            }
            return null;
        }
    }
}

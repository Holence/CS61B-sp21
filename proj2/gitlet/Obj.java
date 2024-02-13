package gitlet;

import static gitlet.Repository.OBJECTS_DIR;
import static gitlet.Utils.*;

import java.io.File;
import java.util.List;

public class Obj {

    public static File getPath(String hashID) {
        // 要用前2位作文件夹，后38位作文件名
        // 但要写global-lob，要获取所有的Commit，遍历文件夹太麻烦了，懒得弄了
        return join(OBJECTS_DIR, hashID);
    }

    public static List<String> getAllObjHashID() {
        return plainFilenamesIn(OBJECTS_DIR);
    }
}

package gitlet;

import static gitlet.Repository.OBJECTS_DIR;
import static gitlet.Utils.*;

public class Blob {
    public static void writeBlob(Object o, String hashID) {
        // TODO: 要用前2位作文件夹，后38位作文件名
        writeContents(join(OBJECTS_DIR, hashID), o);
    }

    public static Object readBlob(String hashID) {
        // TODO: 要支持4位及以上的短链访问
        return null;
    }
}

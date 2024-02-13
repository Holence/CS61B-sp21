package gitlet;

import static gitlet.Repository.BLOB_DIR;
import static gitlet.Utils.join;
import static gitlet.Utils.plainFilenamesIn;
import static gitlet.Utils.readContents;
import static gitlet.Utils.sha1;
import static gitlet.Utils.writeContents;

import java.io.File;
import java.util.List;

public class Blob {

    /////////////////////////////////////////////
    // Commit和Blob的路径方式一模一样，应该都继承自Obj
    // 但subclass没法修改superclass的static类型
    // 没法指定ROOT_DIR啊？？？
    public static File getPath(String hashID) {
        // 要用前2位作文件夹，后38位作文件名
        // 但要写global-lob，要获取所有的Commit，遍历文件夹太麻烦了，懒得弄了
        return join(BLOB_DIR, hashID);
    }

    public static List<String> getAllObjHashID() {
        return plainFilenamesIn(BLOB_DIR);
    }
    /////////////////////////////////////////////

    public static String getHashID(File f) {
        return sha1(readContents(f));
    }

    public static void writeBlob(File f) {
        // 要用前2位作文件夹，后38位作文件名
        // 但要写global-lob，要获取所有的Commit，遍历文件夹太麻烦了，懒得弄了
        writeContents(getPath(getHashID(f)), readContents(f));
    }

    public static void writeBlob(File f, String hashID) {
        // 要用前2位作文件夹，后38位作文件名
        // 但要写global-lob，要获取所有的Commit，遍历文件夹太麻烦了，懒得弄了
        writeContents(getPath(hashID), readContents(f));
    }

    public static byte[] readBlob(String hashID) {
        return readContents(getPath(hashID));
    }
}

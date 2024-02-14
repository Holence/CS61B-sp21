package gitlet;

import static gitlet.Repository.BLOB_DIR;
import static gitlet.Utils.*;

import java.io.File;

public class Blob extends Obj {

    public static String getHashID(File f) {
        return sha1(readContents(f));
    }

    public static void writeBlob(File f) {
        // 要用前2位作文件夹，后38位作文件名
        // 但要写global-lob，要获取所有的Commit，遍历文件夹太麻烦了，懒得弄了
        writeContents(getPath(BLOB_DIR, getHashID(f)), readContents(f));
    }

    public static void writeBlob(File f, String hashID) {
        // 要用前2位作文件夹，后38位作文件名
        // 但要写global-lob，要获取所有的Commit，遍历文件夹太麻烦了，懒得弄了
        writeContents(getPath(BLOB_DIR, hashID), readContents(f));
    }

    public static byte[] readContent(String hashID) {
        return readContents(getPath(BLOB_DIR, hashID));
    }

    public static String readContentAsString(String hashID) {
        return readContentsAsString(getPath(BLOB_DIR, hashID));
    }
}

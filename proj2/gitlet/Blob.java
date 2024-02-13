package gitlet;

import static gitlet.Utils.readContents;
import static gitlet.Utils.sha1;
import static gitlet.Utils.writeContents;

import java.io.File;

public class Blob {
    public static String getHashID(File f) {
        return sha1(readContents(f));
    }

    public static void writeBlob(File f) {
        // 要用前2位作文件夹，后38位作文件名
        // 但要写global-lob，要获取所有的Commit，遍历文件夹太麻烦了，懒得弄了
        writeContents(Obj.getPath(Blob.getHashID(f)), readContents(f));
    }

    public static void writeBlob(File f, String hashID) {
        // 要用前2位作文件夹，后38位作文件名
        // 但要写global-lob，要获取所有的Commit，遍历文件夹太麻烦了，懒得弄了
        writeContents(Obj.getPath(hashID), readContents(f));
    }

    public static byte[] readBlob(String hashID) {
        return readContents(Obj.getPath(hashID));
    }
}

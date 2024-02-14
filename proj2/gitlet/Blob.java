package gitlet;

import static gitlet.Repository.BLOB_DIR;
import static gitlet.Utils.*;

import java.io.File;

public class Blob extends Obj {

    public static String getHashID(File f) {
        return sha1(readContents(f));
    }

    public static void writeBlob(File f) {
        writeContents(getPath(BLOB_DIR, getHashID(f)), readContents(f));
    }

    public static void writeBlob(File f, String hashID) {
        writeContents(getPath(BLOB_DIR, hashID), readContents(f));
    }

    public static byte[] readContent(String hashID) {
        return readContents(getPath(BLOB_DIR, hashID));
    }

    public static String readContentAsString(String hashID) {
        return readContentsAsString(getPath(BLOB_DIR, hashID));
    }
}

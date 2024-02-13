package gitlet;

import static gitlet.Utils.readContents;
import static gitlet.Utils.sha1;

import java.io.File;

public class Blob {
    public static String getHashID(File f) {
        return sha1(readContents(f));
    }
}

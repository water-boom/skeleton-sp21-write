package gitlet;

import static gitlet.Utils.*;
import static gitlet.Refs.*;
import static gitlet.Commit.*;
import static gitlet.Blob.*;

public class FuncUtils {
    public static void setupPersistence() {
        GITLET_DIR.mkdirs();
        COMMIT_FOLDER.mkdirs();
        BLOBS_FOLDER.mkdirs();
        REFS_DIR.mkdirs();
        HEAD_DIR.mkdirs();
        ADD_STAGE_DIR.mkdirs();
        REMOVE_STAGE_DIR.mkdirs();
    }
}

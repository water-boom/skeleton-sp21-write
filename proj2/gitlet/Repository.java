package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import static gitlet.Commit.*;
import static gitlet.Branch.*;
import static gitlet.Stage.*;

// TODO: any imports you need here
import java.util.Date;
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File COMMITS_DIR = join(OBJECTS_DIR, "commits");
    public static final File BLOBS_DIR = join(OBJECTS_DIR, "blobs");

    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(GITLET_DIR, "heads");

    public static final File HEAD = join(GITLET_DIR, "HEAD");

    public static final File addStage = join(GITLET_DIR, "addStage");
    public static final File rmStage = join(GITLET_DIR, "rmStage");
    /* TODO: fill in the rest of this class. */
    public static void createDir(){
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();
        addStage.mkdir();
        rmStage.mkdir();
    }

}

package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

// TODO: any imports you need here
import static gitlet.Refs.*;
import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static java.lang.System.exit;
import java.util.Date; // TODO: You'll likely use this in this class

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */

    private String message; // commit信息
    private String directParent; // 直接父节点
    private String otherParent; // 另一个父节点,用于merge
    private Date timestamp; // 时间戳
    private HashMap<String, String> blobMap = new HashMap<>(); //哈希表,存储文件名和对应的blob的hashname

    /* TODO: fill in the rest of this class. */

    public Commit(String message, Date timestamp, String directparent,
                  String blobFileName, String blobHashName) {
        this.message = message;
        this.timestamp = timestamp;
        this.directParent = directparent;
        if (blobFileName == null || blobFileName.isEmpty()) {
            this.blobMap = new HashMap<>();
        } else {
            this.blobMap.put(blobFileName, blobHashName);
        }
    }
    public Commit(Commit directparent) {
        this.message = directparent.message;
        this.timestamp = directparent.timestamp;
        this.directParent = directparent.directParent;
        this.blobMap = directparent.blobMap;
    }

    public String getHashName() {
        return sha1(this.message, dateToTimeStamp(this.timestamp), this.directParent);
    }
    public HashMap<String, String> getBlobMap() {
        return blobMap;
    }

    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

    public void setDirectParent(String directParent) {
        this.directParent = directParent;
    }
    public String getDirectParent() {
        return directParent;
    }
    public void setOtherParent(String otherParent) {
        this.otherParent = otherParent;
    }
    public String getOtherParent() {
        return otherParent;
    }

    /**
     * 将此commit对象存储到文件中
     */
    public void saveCommit() {
        // get the uid of this
        String hashname = this.getHashName();

        // write obj to files
        File commitFile = new File(COMMIT_FOLDER, hashname);
        writeObject(commitFile, this);
    }
    public static Commit getCommit(String hashName) {
        List<String> commitFiles = plainFilenamesIn(COMMIT_FOLDER);
        /* 如果在commit文件夹中不存在此文件 */
        if (!commitFiles.contains(hashName)) {
            return null;
        }
        File commitFile = join(COMMIT_FOLDER, hashName);
        Commit commit = readObject(commitFile, Commit.class);
        return commit;
    }
    /**
     * commit的blob操作
     *
     * @param fileName
     * @param blobName
     */
    public void addBlob(String fileName, String blobName) {
        this.blobMap.put(fileName, blobName);
    }
    public void removeBlob(String fileName) {
        this.blobMap.remove(fileName);
    }


    /**
     * 获取HEAD指向的最新的commit对象
     *
     * @return
     */
    public static Commit getHeadCommit() {
            /* 获取HEAD指针,这个指针指向目前最新的commit */
            String headContent = readContentsAsString(HEAD_POINT);
            String headHashName = headContent.split(":")[1];
            File commitFile = join(COMMIT_FOLDER, headHashName);
            /* 获取commit文件 */
            Commit commit = readObject(commitFile, Commit.class);

            return commit;
    }

    /**
     * 获取某个branch指向的最新的commit对象
     *
     * @param branchName
     * @param errorMsg
     * @return
     */
    public static Commit getBranchHeadCommit(String branchName, String errorMsg) {
        File brancheFile = join(HEAD_DIR, branchName);
        if (!brancheFile.exists()) {
            System.out.println(errorMsg);
            exit(0);
        }
        /* 获取HEAD指针,这个指针指向目前最新的commit */
        String headHashName = readContentsAsString(brancheFile);
        File commitFile = join(COMMIT_FOLDER, headHashName);
        /* 获取commit文件 */
        Commit commit = readObject(commitFile, Commit.class);
        return commit;
    }


    public static Commit getCommitFromId(String commitId) {
        Commit commit = null;
        /* 查找对应的commit */

        /*  直接从commit文件夹中依次寻找 */
        String resCommitId = null;
        List<String> commitFileNames = plainFilenamesIn(COMMIT_FOLDER);
        /* 用于应对前缀的情况 */
        for (String commitFileName : commitFileNames) {
            if (commitFileName.startsWith(commitId)) {
                resCommitId = commitFileName;
                break;
            }
        }

        if (resCommitId == null) {
            return null;
        } else {
            File commitFile = join(COMMIT_FOLDER, resCommitId);
            commit = readObject(commitFile, Commit.class);
        }

        return commit;
    }

    public static Commit getSplitCommit(Commit commitA, Commit commitB) {

        Commit p1 = commitA, p2 = commitB;
        /* 用于遍历提交链 */
        Deque<Commit> dequecommitA = new ArrayDeque<>();
        Deque<Commit> dequecommitB = new ArrayDeque<>();
        /* 用于保存访问过的节点 */
        HashSet<String> visitedInCommitA = new HashSet<>();
        HashSet<String> visitedInCommitB = new HashSet<>();

        dequecommitA.add(p1);
        dequecommitB.add(p2);

        while (!dequecommitA.isEmpty() || !dequecommitB.isEmpty()) {
            if (!dequecommitA.isEmpty()) {
                /* commitA 的队列中存在可遍历对象 */
                Commit currA = dequecommitA.poll();
                if (visitedInCommitB.contains(currA.getHashName())) {
                    return currA;
                }
                visitedInCommitA.add(currA.getHashName());
                addParentsToDeque(currA, dequecommitA);
            }

            if (!dequecommitB.isEmpty()) {
                Commit currB = dequecommitB.poll();
                if (visitedInCommitA.contains(currB.getHashName())) {
                    return currB;
                }
                visitedInCommitB.add(currB.getHashName());
                addParentsToDeque(currB, dequecommitB);
            }
        }
        return null;

    }

    /**
     * 将此节点的父节点（或者是两个父节点）放入队列中
     *
     * @param commit
     * @param dequeCommit
     */
    private static void addParentsToDeque(Commit commit, Queue<Commit> dequeCommit) {
        if (!commit.getDirectParent().isEmpty()) {
            dequeCommit.add(getCommitFromId(commit.getDirectParent()));
        }

        if (commit.getOtherParent() != null) {
            dequeCommit.add(getCommitFromId(commit.getOtherParent()));
        }
    }

}


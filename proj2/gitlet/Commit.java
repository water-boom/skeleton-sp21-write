package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;



import static gitlet.Repository.*;
import static gitlet.Utils.*;
import java.util.Date;

public class Commit implements Serializable {


    private String message; // commit信息
    private String parent; // 直接父节点
    private String otherParent; // 其他父节点
    private Date timestamp; // 时间戳
    private HashMap<String, String> blobMap = new HashMap<>();

    /* TODO: fill in the rest of this class. */

    public Commit(String message, Date timestamp, String parent,
                  String blobFileName, String blobHashName) {
        this.message = message;
        this.timestamp = timestamp;
        this.parent = parent;
        if (blobFileName == null || blobFileName.isEmpty()) {
            this.blobMap = new HashMap<>();
        } else {
            this.blobMap.put(blobFileName, blobHashName);
        }
    }
    public Commit(Commit parent) {
        this.message = parent.message;
        this.timestamp = parent.timestamp;
        this.parent = parent.parent;
        this.blobMap = parent.blobMap;
    }

    public String getHashName() {
        return sha1(this.message, dateToTimeStamp(this.timestamp), this.parent);
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

    public void setDirectParent(String parent) {
        this.parent = parent;
    }
    public String getDirectParent() {
        return parent;
    }

    public void setOtherParent(String targetParent) {
        this.otherParent = targetParent;
    }
    public String getOtherParent() {
        return otherParent;
    }

    public void saveCommit() {
        // get the uid of this
        String hashName = this.getHashName();

        // write obj to files
        File commitFile = new File(COMMIT_FOLDER, hashName);
        writeObject(commitFile, this);
    }

    public static Commit getCommit(String hashName) {
        List<String> commitFiles = plainFilenamesIn(COMMIT_FOLDER);
        if (commitFiles != null && !commitFiles.contains(hashName)) {
            return null;
        }
        File commitFile = join(COMMIT_FOLDER, hashName);
        return readObject(commitFile, Commit.class);
    }

    public void addBlob(String fileName, String blobName) {
        this.blobMap.put(fileName, blobName);
    }
    public void removeBlob(String fileName) {
        this.blobMap.remove(fileName);
    }

    public static Commit getHeadCommit() {
            String headContent = readContentsAsString(HEAD_POINT);
            String headHashName = headContent.split(":")[1];
            File commitFile = join(COMMIT_FOLDER, headHashName);
            return readObject(commitFile, Commit.class);
    }

    public static Commit getBranchHeadCommit(String branchName, String errorMsg) {
        File brancheFile = join(HEAD_DIR, branchName);
        if (!brancheFile.exists()) {
            throw new GitletException(errorMsg);
        }else {
            String headHashName = readContentsAsString(brancheFile);
            File commitFile = join(COMMIT_FOLDER, headHashName);
            return readObject(commitFile, Commit.class);
        }
    }

    public static Commit getCommitFromId(String commitId) {
        String resCommitId = null;
        List<String> commitFileNames = plainFilenamesIn(COMMIT_FOLDER);
        /* 如果仅为前缀*/
        if (commitFileNames != null) {
            for (String commitFileName : commitFileNames) {
                if (commitFileName.startsWith(commitId)) {
                    resCommitId = commitFileName;
                    break;
                }
            }
        }
        //not found
        if (resCommitId == null) {
            return null;
        }
        return readObject(join(COMMIT_FOLDER, resCommitId), Commit.class);
    }

    public static Commit getSplitCommit(Commit commitA, Commit commitB) {
        /* 用于遍历提交链 */
        Deque<Commit> dequeCommitA = new ArrayDeque<>();
        Deque<Commit> dequeCommitB = new ArrayDeque<>();
        /* 用于保存访问过的节点 */
        HashSet<String> visitedInCommitA = new HashSet<>();
        HashSet<String> visitedInCommitB = new HashSet<>();

        dequeCommitA.add(commitA);
        dequeCommitB.add(commitB);

        while (!dequeCommitA.isEmpty() || !dequeCommitB.isEmpty()) {
            if (!dequeCommitA.isEmpty()) {
                /* commitA 的队列中存在可遍历对象 */
                Commit curA = dequeCommitA.poll();
                if (visitedInCommitB.contains(curA.getHashName())) {
                    return curA;
                }
                visitedInCommitA.add(curA.getHashName());
                addParentsToDeque(curA, dequeCommitA);
            }else{
                Commit curB = dequeCommitB.poll();
                if (visitedInCommitA.contains(curB.getHashName())) {
                    return curB;
                }
                visitedInCommitB.add(curB.getHashName());
                addParentsToDeque(curB, dequeCommitB);
            }
        }
        return null;
    }

    private static void addParentsToDeque(Commit commit, Queue<Commit> dequeCommit) {
        if (!commit.getDirectParent().isEmpty()) {
            dequeCommit.add(getCommitFromId(commit.getDirectParent()));
        }

        if (commit.getOtherParent() != null) {
            dequeCommit.add(getCommitFromId(commit.getOtherParent()));
        }
    }

}


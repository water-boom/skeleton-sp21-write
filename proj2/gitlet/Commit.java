package gitlet;

// TODO: any imports you need here
import java.io.File;
import java.io.Serializable;
import java.util.*;
import static gitlet.Utils.*;
import static gitlet.Repository.*;
import static gitlet.Branch.*;
import static gitlet.Blob.*;
import static java.lang.System.exit;

import java.util.Date; // TODO: You'll likely use this in this class



public class Commit implements Serializable{
    /**
     * TODO: add instance variables here.
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date timestamp;
    private String parent;
    private String mergeParent;
    private HashMap<String, String> blobsMap = new HashMap<>();
    public String getMessage() {
        return message;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public String getParent() {
        return parent;
    }
    public String getMergeParent() {
        return mergeParent;
    }
    public HashMap<String,String> getHashMap() {
        return blobsMap;
    }
    public String getHashName() {
        return sha1(this.message, this.timestamp.toString(), this.parent);
    }
    public void setParent(String parent) {
        this.parent = parent;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public void setMergeParent(String mergeParent) {
        this.mergeParent = mergeParent;
    }
    /* TODO: fill in the rest of this class. */

    public Commit(String message, Date timestamp,
                  String parent,
                  String blobFileName, String blobHash
        ) {
        this.message = message;
        this.timestamp = timestamp;
        this.parent = parent;
        if(blobFileName == null || blobHash.isEmpty()) {
            this.blobsMap = new HashMap<>();
        }else {
            this.blobsMap.put(blobFileName, blobHash);
        }
    }

    public Commit(Commit parent) {
        this.message = parent.getMessage();
        this.timestamp = parent.getTimestamp();
        this.parent = parent.getParent();
        this.blobsMap = new HashMap<>(parent.blobsMap);
    }
    public void saveCommit() {
        String hashName = this.getHashName();

        File commitFile = join(Repository.COMMITS_DIR, hashName);
        writeObject(commitFile, this);
    }
    public static Commit getCommit(String hashName) {
        List<String> commitFiles = plainFilenamesIn(Repository.COMMITS_DIR);
        if(!commitFiles.contains(hashName)) {
             return null;
        }
        File commitFile = join(Repository.COMMITS_DIR, hashName);
        Commit commit = readObject(commitFile, Commit.class);
        return commit;
    }
    public static Commit getHeadCommit() {
        String headContent = readContentsAsString(Repository.HEAD).trim();
        String headHashName = headContent.split(":")[1];
        File commitFile = join(COMMITS_DIR, headHashName);
        Commit commit = readObject(commitFile, Commit.class);
        return commit;
    }
    public static Commit getBranchHeadCommit(String branchName, String errorMsg) {
        File brancheFile = join(HEADS_DIR, branchName);
        if (!brancheFile.exists()) {
            System.out.println(errorMsg);
            exit(0);
        }
        String headHashName = readContentsAsString(brancheFile);
        File commitFile = join(COMMITS_DIR, headHashName);
        Commit commit = readObject(commitFile, Commit.class);
        return commit;
    }

    public void addBlob(String filename, String blobName) {
        this.blobsMap.put(filename, blobName);
    }
    public void removeBlob(String fileName) {
        this.blobsMap.remove(fileName);
    }
    public static Commit findCommit(String commitID) {
        Commit target = null;

        List<String> commitFiles = plainFilenamesIn(Repository.COMMITS_DIR);
        for(String commitFileName: commitFiles){
            if(commitFileName.startsWith(commitID)){
                File commitFile = join(Repository.COMMITS_DIR, commitFileName);
                target = readObject(commitFile, Commit.class);
                break;
            }
        }

        return target;
    }
}


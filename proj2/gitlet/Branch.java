package gitlet;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static gitlet.Repository.*;
import static gitlet.Commit.*;
import static gitlet.Utils.*;
import static gitlet.Blob.*;
import static gitlet.Stage.*;
public class Branch {
    public static void saveBreanch(String branchName, String commitHashName) {
        File branchFile = join(HEADS_DIR, branchName);
        writeContents(branchFile, commitHashName);
    }

    public static void setHead(String branchName, String branchHeadCommitHash) {
        writeContents(HEAD, branchName + ":" + branchHeadCommitHash);
    }

    public static String getHeadBranchName() {
        String headContent = readContentsAsString(HEAD);
        String[] splitContent = headContent.split(":");
        String branchName = splitContent[0];
        return branchName;
    }

    public void checkOut(String[] args) {

    }

    public static void createBranch(String branchName) {
        Commit headCommit = getHeadCommit();
        List<String> branchFiles = plainFilenamesIn(HEADS_DIR);
        if (branchFiles.contains(branchName)) {
            throw new GitletException("A branch with that name already exists.");
        }
        String headCommitHashName = headCommit.getHashName();
        saveBreanch(branchName, headCommitHashName);
    }

    public static void removeBranch(String branchName) {
        File branchFile = join(HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            throw new GitletException("A branch with that name does not exist.");
        }
        String headBranchName = getHeadBranchName();
        if (headBranchName.equals(branchName)) {
            throw new GitletException("Cannot remove the current branch.");
        }
        branchFile.delete();
    }

    public static void reset(String commitID) {
        if (findCommit(commitID) == null) {
            throw new GitletException("No commit with that id exists.");
        }
        Commit headCommit = getHeadCommit();
        Commit targetCommit = findCommit(commitID);
        HashMap<String, String> headBlobsMap = headCommit.getHashMap();
        List<String> UnderFiles = plainFilenamesIn(CWD);
        if(ExistUnTrackFile(headCommit)) {
            Set<String> currTrackSet = headCommit.getHashMap().keySet();
            Set<String> resetTrackSet = targetCommit.getHashMap().keySet();
            boolean hasAllUntrack = false;

            for(String fileName: UnderFiles) {
                if(!currTrackSet.contains(fileName) && resetTrackSet.contains(fileName)) {
                    remove(fileName);
                    hasAllUntrack = true;
                    break;
                }
            }
            if(hasAllUntrack) {
                throw new GitletException("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }

        for(String fileName:UnderFiles){
            restrictedDelete(join(CWD,fileName));
        }
        for(String fileName: targetCommit.getHashMap().keySet()){
            File workFile = join(CWD, fileName);
            String blobName = targetCommit.getHashMap().get(fileName);
            String blobFile = BLOBS_DIR + blobName;
            String blobContent = getBlobContentFromName(blobFile);
            writeContents(workFile, blobContent);
        }
        saveBreanch(getHeadBranchName(),commitID);
        setHead(getHeadBranchName(),commitID);
    }
    public static boolean ExistUnTrackFile(Commit commit) {
        List<String> workFileNames = plainFilenamesIn(CWD);
        Set<String> currTrackSet = commit.getHashMap().keySet();
        for (String workFile : workFileNames) {
            if (!currTrackSet.contains(workFile)) {
                return true;
            }
        }
        return false;
    }

    public static void merge(String branchName) {

    }
}

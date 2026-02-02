package gitlet;
import java.io.File;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static gitlet.Repository.*;
import static gitlet.Branch.*;
import static gitlet.Blob.*;
import static gitlet.Commit.*;
import static gitlet.Utils.*;


public class Stage {

    public static void init() {
        if(GITLET_DIR.exists()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        createDir();
        Date timestamp = new Date(0);
        Commit initialCommit = new Commit("initial commit", timestamp,"",null ,null);
        initialCommit.saveCommit();
        //create master branch
        String commitHashName = initialCommit.getHashName();
        saveBreanch("master", commitHashName);
        setHead("master",commitHashName);
    }
    public static void add(String fileName){
        if(fileName == null || fileName.isEmpty()){
            throw new GitletException("Please enter a file name.");
        }
        File fileToAdd = join(CWD, fileName);

        if(!fileToAdd.exists()){
            throw new GitletException("File does not exist.");
        }
        String fileToAddContent = readContentsAsString(fileToAdd);
        Commit headCommit = getHeadCommit();
        HashMap<String,String> headCommitBlobMap = headCommit.getHashMap();
        if(headCommitBlobMap.containsKey(fileName)){
            String fileToAddHash = headCommit.getHashMap().get(fileName);
            String commitContent = getBlobContentFromName(fileToAddHash);
            if(commitContent.equals(fileToAddContent)){
                List<String> filesAdd = plainFilenamesIn(addStage);
                List<String> filesRm = plainFilenamesIn(rmStage);
                if (filesAdd != null && filesAdd.contains(fileName)) {
                    join(addStage, fileName).delete();
                }
                if (filesRm != null && filesRm.contains(fileName)) {
                    join(rmStage, fileName).delete();
                }

                return;
            }
        }
        String fileContent = readContentsAsString(fileToAdd);
        String blobName = sha1(fileContent);
        Blob newBlob = new Blob(fileContent, blobName);
        newBlob.saveBlob();
        File blobPoint = join(addStage, fileName);
        writeContents(blobPoint, newBlob.getFilePath().getName());

    }
    public static void commit(String message) {
        List<String> addStages = plainFilenamesIn(addStage);
        List<String> rmStages = plainFilenamesIn(rmStage);

        if ((addStages == null || addStages.isEmpty()) && (rmStages == null || rmStages.isEmpty())) {
            throw new GitletException("No changes added to the commit.");
        }
        if (message == null || message.isEmpty()) {
            throw new GitletException("Please enter a commit message.");
        }

        Commit headCommit = getHeadCommit();
        Commit newCommit = new Commit(headCommit);
        newCommit.setParent(headCommit.getHashName());
        newCommit.setTimestamp(new Date());
        newCommit.setMessage(message);

        if (addStages != null) {
            for (String fileName : addStages) {
                String tmpHashName = readContentsAsString(join(addStage, fileName));
                newCommit.addBlob(fileName, tmpHashName);
                join(addStage, fileName).delete();
            }
        }
        HashMap<String, String> blobMap = newCommit.getHashMap();

        if (rmStages != null) {
            for (String fileName : rmStages) {
                if (blobMap.containsKey(fileName)) {
                    newCommit.removeBlob(fileName);
                }
                join(rmStage, fileName).delete();
            }
        }
        newCommit.saveCommit();
        setHead(getHeadBranchName(), newCommit.getHashName());
        saveBreanch(getHeadBranchName(), newCommit.getHashName());
    }
    public static void remove(String fileName){
        if(fileName ==null || fileName.isEmpty()){
            throw new GitletException("Please enter a file name.");
        }
        Commit headCommit = getHeadCommit();
        HashMap<String,String> blobMap = headCommit.getHashMap();
        List<String> addStages = plainFilenamesIn(addStage);

        if((blobMap == null || !blobMap.containsKey(fileName)) && (addStages == null || !addStages.contains(fileName))){
            throw new GitletException("No reason to remove the file.");
        }
        if(addStages != null && addStages.contains(fileName)){
            join(addStage, fileName).delete();
        }
        if(blobMap != null && blobMap.containsKey(fileName)){
            File fileToRemove =new File(rmStage, fileName);
            writeContents(fileToRemove,"");
            File waitToDelete = join(CWD, fileName);
            restrictedDelete(waitToDelete);
        }
    }
    public static void log(){
        Commit headCommit = getHeadCommit();
        Commit currentCommit = headCommit;
        while(currentCommit != null){
            printCommitInfo(currentCommit);
            String parentHashName = currentCommit.getParent();
            if(parentHashName == null || parentHashName.isEmpty()){
                break;
            }
            currentCommit = getCommit(parentHashName);
        }
    }
    public static void globalLog(){
        List<String> commitFiles = plainFilenamesIn(Repository.COMMITS_DIR);
        for(String commitFileName: commitFiles){
            Commit commit = getCommit(commitFileName);
            printCommitInfo(commit);
        }
    }
    public static void printCommitInfo(Commit commit){
        System.out.println("===");
        System.out.println("commit " + commit.getHashName());
        System.out.println("Date: " + commit.getTimestamp().toString());
        System.out.println(commit.getMessage());
        System.out.println();
    }
    public static void find(String message) {
        boolean found = false;
        List<String> commitFiles = plainFilenamesIn(Repository.COMMITS_DIR);
        if(commitFiles == null){
            throw new GitletException("Found no commit with that message.");
        }
        for(String commitFile: commitFiles){
            Commit  tmpCommit = getCommit(commitFile);
            if(tmpCommit.getMessage().equals(message)) {
                message(tmpCommit.getHashName());
                found = true;
            }
        }
        if(!found){
            throw new GitletException("Found no commit with that message.");
        }
    }
    public static void status(){
        System.out.println("=== Branches ===");
        String headBranch = getHeadBranchName();
        List<String> branches = plainFilenamesIn(HEADS_DIR);
        if(branches != null) {
            for (String branch : branches) {
                if (branch.equals(headBranch)) {
                    System.out.print("*");
                }
                System.out.println(branch);
            }
        }
        System.out.print("\n");

        System.out.println("=== Staged Files ===");
        List<String> addStageFiles = plainFilenamesIn(addStage);
        if(addStageFiles != null){
            for (String fileName : addStageFiles) {
                System.out.println(fileName);
            }
        }
        System.out.print("\n");

        System.out.println("=== Removed Files ===");
        List<String> removeStageFiles = plainFilenamesIn(rmStage);
        if(removeStageFiles != null){
            for (String fileName : removeStageFiles) {
                System.out.println(fileName);
            }
        }
        System.out.print("\n");

        System.out.println("=== Modifications Not Staged For Commit ===");
        // compute modifications not staged and untracked files
        Commit headCommit = getHeadCommit();
        HashMap<String, String> headMap = headCommit.getHashMap();
        List<String> workFiles = plainFilenamesIn(CWD);
        if(workFiles != null){
            for(String f: workFiles){
                if(f.equals(".gitlet")){
                    continue;
                }
                File wf = join(CWD, f);
                String content = readContentsAsString(wf);
                String currHash = sha1(content);
                boolean tracked = headMap.containsKey(f);
                boolean staged = (addStageFiles != null && addStageFiles.contains(f));
                boolean removedStaged = (removeStageFiles != null && removeStageFiles.contains(f));
                if(tracked){
                    String headBlob = headMap.get(f);
                    String headContent = getBlobContentFromName(headBlob);
                    String headHash = sha1(headContent == null ? "" : headContent);
                    if(!headHash.equals(currHash) && !staged){
                        System.out.println(f + " (modified)");
                    }
                } else {
                    if(!staged && !removedStaged){
                        // untracked: will be printed in Untracked Files section
                    }
                }
            }
        }
        System.out.print("\n");
        System.out.println("=== Untracked Files ===");
        // print untracked files
        if(workFiles != null){
            for(String f: workFiles){
                if(f.equals(".gitlet")){
                    continue;
                }
                boolean inHead = headCommit.getHashMap().containsKey(f);
                boolean inAdd = addStageFiles != null && addStageFiles.contains(f);
                if(!inHead && !inAdd){
                    System.out.println(f);
                }
            }
        }
    }
    public static void checkOut(String[] args){
        if(args.length == 3 && args[1].equals("--")){
            String fileName = args[2];
            Commit headCommit = getHeadCommit();
            if(!headCommit.getHashMap().containsKey(fileName)){
                throw new GitletException("File does not exist in that commit.");
            }
            String blobName = headCommit.getHashMap().get(fileName);
            String blobContent = getBlobContentFromName(blobName);
            File target = join(CWD, fileName);
            writeContents(target, blobContent == null ? "" : blobContent);
            return;
        }
        if(args.length == 4 && args[2].equals("--")){
            String commitID = args[1];
            String fileName = args[3];
            Commit target = findCommit(commitID);
            if(target == null){
                throw new GitletException("No commit with that id exists.");
            }
            if(!target.getHashMap().containsKey(fileName)){
                throw new GitletException("File does not exist in that commit.");
            }
            String blobName = target.getHashMap().get(fileName);
            String blobContent = getBlobContentFromName(blobName);
            File targetFile = join(CWD, fileName);
            writeContents(targetFile, blobContent == null ? "" : blobContent);
            return;
        }
        if(args.length == 2){
            String branchName = args[1];
            List<String> branches = plainFilenamesIn(HEADS_DIR);
            if(branches == null || !branches.contains(branchName)){
                throw new GitletException("No such branch exists.");
            }
            String headBranch = getHeadBranchName();
            if(headBranch.equals(branchName)){
                throw new GitletException("No need to checkout the current branch.");
            }
            Commit targetCommit = getBranchHeadCommit(branchName, "A branch with that name does not exist.");
            Commit currentHead = getHeadCommit();
            // check for untracked files that would be overwritten
            List<String> workFiles = plainFilenamesIn(CWD);
            if(workFiles != null){
                Set<String> currTrackSet = currentHead.getHashMap().keySet();
                Set<String> targetTrackSet = targetCommit.getHashMap().keySet();
                for(String f: workFiles){
                    if(!currTrackSet.contains(f) && targetTrackSet.contains(f)){
                        throw new GitletException("There is an untracked file in the way; delete it, or add and commit it first.");
                    }
                }
            }
            // clear working directory (files) and write target commit files
            if(workFiles != null){
                for(String f: workFiles){
                    restrictedDelete(join(CWD, f));
                }
            }
            for(String f: targetCommit.getHashMap().keySet()){
                String blobName = targetCommit.getHashMap().get(f);
                String blobContent = getBlobContentFromName(blobName);
                writeContents(join(CWD, f), blobContent == null ? "" : blobContent);
            }
            // update HEAD
            setHead(branchName, readContentsAsString(join(HEADS_DIR, branchName)));
            return;
        }
        throw new GitletException("Incorrect operands.");
    }
    public static void reset(String commitID) {
        if (findCommit(commitID) == null) {
            throw new GitletException("No commit with that id exists.");
        }
        Commit headCommit = getHeadCommit();
        Commit targetCommit = findCommit(commitID);
        List<String> UnderFiles = plainFilenamesIn(CWD);
        if(ExistUnTrackFile(headCommit)) {
            Set<String> currTrackSet = headCommit.getHashMap().keySet();
            Set<String> resetTrackSet = targetCommit.getHashMap().keySet();
            boolean hasAllUntrack = false;

            if (UnderFiles != null) {
                for(String fileName: UnderFiles) {
                    if(!currTrackSet.contains(fileName) && resetTrackSet.contains(fileName)) {
                        remove(fileName);
                        hasAllUntrack = true;
                        break;
                    }
                }
            }
            if(hasAllUntrack) {
                throw new GitletException("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }

        if (UnderFiles != null) {
            for(String fileName:UnderFiles){
                restrictedDelete(join(CWD,fileName));
            }
        }
        for(String fileName: targetCommit.getHashMap().keySet()){
            File workFile = join(CWD, fileName);
            String blobName = targetCommit.getHashMap().get(fileName);
            String blobContent = getBlobContentFromName(blobName);
            writeContents(workFile, blobContent == null ? "" : blobContent);
        }
        saveBreanch(getHeadBranchName(),commitID);
        setHead(getHeadBranchName(),commitID);
    }
    public static void merge(String branchName) {
        Commit headCommit = getHeadCommit();
        Commit ToMergeCommit = getBranchHeadCommit(branchName,
                "A branch with that name does not exist.");
        Commit splitCommit = findSplitCommit(headCommit, ToMergeCommit);
        if (splitCommit == null) {
            throw new GitletException("Unable to find a split point for merge.");
        }
        if(splitCommit.getHashName().equals(ToMergeCommit.getHashName())) {
            throw new GitletException("Given branch is an ancestor of the current branch.");
        }
        processSplitCommit(headCommit, ToMergeCommit, splitCommit, branchName);


    }

    public static Commit findSplitCommit(Commit head, Commit other) {
        // collect all ancestor hashes of head
        java.util.Set<String> headAncestors = new java.util.HashSet<>();
        Commit cur = head;
        while (cur != null) {
            headAncestors.add(cur.getHashName());
            String p = cur.getParent();
            if (p == null || p.isEmpty()) {
                break;
            }
            cur = getCommit(p);
        }
        // walk other ancestors and return first that appears in headAncestors
        cur = other;
        while (cur != null) {
            if (headAncestors.contains(cur.getHashName())) {
                return cur;
            }
            String p = cur.getParent();
            if (p == null || p.isEmpty()) {
                break;
            }
            cur = getCommit(p);
        }
        return null;
    }

    private static void stageBlobForFile(String fileName, String content) {
        String blobName = sha1(content);
        Blob b = new Blob(content, blobName);
        b.saveBlob();
        writeContents(join(addStage, fileName), b.getFilePath().getName());
    }

    public static void processSplitCommit(Commit head, Commit other, Commit split, String branchName) {
        boolean conflict = false;
        Commit newCommit = new Commit(head);

        java.util.Set<String> allFiles = new java.util.HashSet<>();
        allFiles.addAll(head.getHashMap().keySet());
        allFiles.addAll(other.getHashMap().keySet());
        allFiles.addAll(split.getHashMap().keySet());

        for (String f : allFiles) {
            String headBlob = head.getHashMap().get(f);
            String otherBlob = other.getHashMap().get(f);
            String splitBlob = split.getHashMap().get(f);

            String headContent = headBlob == null ? null : getBlobContentFromName(headBlob);
            String otherContent = otherBlob == null ? null : getBlobContentFromName(otherBlob);
            String splitContent = splitBlob == null ? null : getBlobContentFromName(splitBlob);

            // file added in other branch
            if (splitBlob == null && headBlob == null && otherBlob != null) {
                writeContents(join(CWD, f), otherContent == null ? "" : otherContent);
                stageBlobForFile(f, otherContent == null ? "" : otherContent);
                newCommit.addBlob(f, sha1(otherContent == null ? "" : otherContent));
                continue;
            }
            // file removed in other branch and unchanged in head since split -> remove
            if (splitBlob != null && otherBlob == null && headBlob != null && headBlob.equals(splitBlob)) {
                // remove from working dir and stage for removal
                restrictedDelete(join(CWD, f));
                writeContents(join(rmStage, f), "");
                newCommit.removeBlob(f);
                continue;
            }
            // if file changed in other since split and unchanged in head -> take other
            if ((splitBlob == null && headBlob != null && otherBlob == null)) {
                // nothing to do (file existed in head but not in other and split missing) - keep head
                continue;
            }
            if (splitBlob != null) {
                boolean headChanged = (headBlob == null && splitBlob != null) || (headBlob != null && !headBlob.equals(splitBlob));
                boolean otherChanged = (otherBlob == null && splitBlob != null) || (otherBlob != null && !otherBlob.equals(splitBlob));

                if (!headChanged && otherChanged) {
                    // take other
                    writeContents(join(CWD, f), otherContent == null ? "" : otherContent);
                    stageBlobForFile(f, otherContent == null ? "" : otherContent);
                    newCommit.addBlob(f, sha1(otherContent == null ? "" : otherContent));
                } else if (headChanged && !otherChanged) {
                    // keep head (do nothing)
                } else if (headChanged && otherChanged) {
                    // both changed
                    if (headContent != null && otherContent != null && !headContent.equals(otherContent)) {
                        // conflict
                        String merged = "<<<<<<< HEAD\n" + (headContent == null ? "" : headContent)
                                + "\n=======\n" + (otherContent == null ? "" : otherContent)
                                + "\n>>>>>>>\n";
                        writeContents(join(CWD, f), merged);
                        stageBlobForFile(f, merged);
                        newCommit.addBlob(f, sha1(merged));
                        conflict = true;
                    } else if (headContent == null && otherContent != null) {
                        writeContents(join(CWD, f), otherContent);
                        stageBlobForFile(f, otherContent);
                        newCommit.addBlob(f, sha1(otherContent));
                    } else if (otherContent == null && headContent != null) {
                        // keep head
                    }
                }
            }
        }

        // create merge commit: set merge parent
        newCommit.setParent(head.getHashName());
        newCommit.setMergeParent(other.getHashName());
        newCommit.setTimestamp(new Date());
        newCommit.setMessage("Merged " + branchName + " into " + getHeadBranchName() + ".");
        newCommit.saveCommit();
        setHead(getHeadBranchName(), newCommit.getHashName());
        saveBreanch(getHeadBranchName(), newCommit.getHashName());

        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }


}

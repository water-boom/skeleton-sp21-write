package gitlet;
import java.io.File;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static gitlet.Repository.*;
import static gitlet.Branch.*;
import static gitlet.Blob.*;
import static gitlet.Commit.*;
import static gitlet.Utils.*;
import static java.lang.System.exit;


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
                if (filesAdd.contains(fileName)) {
                    join(addStage, fileName).delete();
                }
                if (filesRm.contains(fileName)) {
                    join(rmStage, fileName).delete();
                }

                return;
            }
        }
        String fileContent =readContentsAsString(fileToAdd);
        String blobName = sha1(fileContent);
        Blob newBlob = new Blob(fileName, blobName);
        newBlob.saveBlob();
        File blobPoint = join(addStage, fileName);
        writeContents(blobPoint, newBlob.getFilePath().getName());

    }
    public static void commit(String message) {
        List<String> addStages = plainFilenamesIn(addStage);
        List<String> rmStages = plainFilenamesIn(rmStage);

        if (addStages.isEmpty() && rmStages.isEmpty()) {
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

        for (String fileName : addStages) {
            String tmpHashName = readContentsAsString(join(addStage, fileName));
            newCommit.addBlob(fileName, tmpHashName);
            join(addStage, fileName).delete();
        }
        HashMap<String, String> blobMap = newCommit.getHashMap();

        for (String fileName : rmStages) {
            if (blobMap.containsKey(fileName)) {
                newCommit.removeBlob(fileName);
            }
            join(rmStage, fileName).delete();
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

        if(!blobMap.containsKey(fileName) || !addStages.contains(fileName)){
            throw new GitletException("No reason to remove the file.");
        }
        if(addStages.contains(fileName)){
            join(addStage, fileName).delete();
        }
        if(blobMap.containsKey(fileName)){
            File fileToRemove =new File(rmStage, fileName);
            writeContents(fileToRemove,"");
            File waitToDelete = join(CWD, fileName);
            restrictedDelete(waitToDelete);
        }
    }
    public static void log(){
        Commit headCommit = getHeadCommit();
        Commit currentCommit = headCommit;
        while(!currentCommit.getParent().isEmpty()){
            printCommitInfo(currentCommit);
            String parentHashName = currentCommit.getParent();
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
    public static void find(String meaasge) {
        Commit headCommit = getHeadCommit();
        boolean found = false;
        Commit currentCommit = headCommit;
        List<String> commitFiles = plainFilenamesIn(Repository.COMMITS_DIR);
        for(String commitFile: commitFiles){
            Commit  tmpCommit = getCommit(commitFile);
            if(tmpCommit.getMessage().equals(meaasge)) {
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
        for (String branch : branches) {
            if (branch.equals(headBranch)) {
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.print("\n");

        System.out.println("=== Staged Files ===");
        List<String> addStageFiles = plainFilenamesIn(addStage);
        for (String fileName : addStageFiles) {
            System.out.println(fileName);
        }
        System.out.print("\n");

        System.out.println("=== Removed Files ===");
        List<String> removeStageFiles = plainFilenamesIn(rmStage);
        for (String fileName : removeStageFiles) {
            System.out.println(fileName);
        }
        System.out.print("\n");

        System.out.println("=== Modifications Not Staged For Commit ===");
    }
    public static void checkOut(String[] args){

    }



}

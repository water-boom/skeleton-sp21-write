package gitlet;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Commit.*;
import static gitlet.Blob.*;
import static java.lang.System.exit;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    public static final File CWD = new File(System.getProperty("user.dir"));

    public static final File GITLET_DIR = join(CWD, ".gitlet");


    static final File OBJECTS_FOLDER = join(GITLET_DIR, "objects");
    static final File COMMIT_FOLDER = join(OBJECTS_FOLDER, "commits");
    static final File BLOBS_FOLDER = join(OBJECTS_FOLDER, "blobs");

    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEAD_DIR = join(REFS_DIR, "heads");

    public static final File HEAD_POINT = join(REFS_DIR, "HEAD");

    public static final File ADD_STAGE_DIR = join(GITLET_DIR, "addstage");
    public static final File REMOVE_STAGE_DIR = join(GITLET_DIR, "removestage");

    public static void setupPersistence() {
        GITLET_DIR.mkdirs();
        COMMIT_FOLDER.mkdirs();
        BLOBS_FOLDER.mkdirs();
        REFS_DIR.mkdirs();
        HEAD_DIR.mkdirs();
        ADD_STAGE_DIR.mkdirs();
        REMOVE_STAGE_DIR.mkdirs();
    }
    /* TODO: fill in the rest of this class. */
    public static void saveBranch(String branchName, String hashName) {

        File branchHead = join(HEAD_DIR, branchName);
        writeContents(branchHead, hashName);
    }

    public static void saveHEAD(String branchName, String branchHeadCommitHash) {
        writeContents(HEAD_POINT, branchName + ":" + branchHeadCommitHash);
    }

    public static String getHeadBranchName() {
        String headContent = readContentsAsString(HEAD_POINT);
        String[] splitContent = headContent.split(":");
        return splitContent[0];
    }

    public static void initPersistence() {
        if (GITLET_DIR.exists()) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        setupPersistence();
        Date timestampInit = new Date(0);
        Commit initialCommit = new Commit("initial commit", timestampInit, "", null, null);
        initialCommit.saveCommit();
        String commitHashName = initialCommit.getHashName();
        String branchName = "master";
        saveBranch(branchName, commitHashName);
        saveHEAD("master", commitHashName);
    }

    public static void addStage(String addFileName) {
        if (addFileName == null || addFileName.isEmpty()) {
            throw new GitletException("Please enter a file name.");
        }

        File fileAdded = join(CWD, addFileName);
        /* 如果在工作目录中不存在此文件 */

        if (!fileAdded.exists()) {
            throw new GitletException("File does not exist.");
        }
        String fileAddedContent = readContentsAsString(fileAdded);

        Commit headCommit = getHeadCommit();
        HashMap<String, String> headCommitBlobMap = headCommit.getBlobMap();

        /* 如果这个文件已经被track */
        if (headCommitBlobMap.containsKey(addFileName)) {
            String fileAddedInHash = headCommit.getBlobMap().get(addFileName);
            String commitContent = getBlobContentFromName(fileAddedInHash);

            /* 如果暂存内容和想要添加内容一致，则不将其纳入暂存区，
            同时将其从暂存区删除（如果存在）,同时将其从removal区移除 */
            if (commitContent.equals(fileAddedContent)) {
                List<String> filesAdd = plainFilenamesIn(ADD_STAGE_DIR);
                List<String> filesRm = plainFilenamesIn(REMOVE_STAGE_DIR);
                /* 如果在暂存区存在,从暂存区删除 */
                if (filesAdd != null && filesAdd.contains(addFileName)) {
                    join(ADD_STAGE_DIR, addFileName).delete();
                }
                /* 如果在removal area存在,从中删除 */
                if (filesRm != null && filesRm.contains(addFileName)) {
                    join(REMOVE_STAGE_DIR, addFileName).delete();
                }

                return; //直接退出
            }
        }
        /* 将文件放入暂存区，blob文件名是内容的hash值，内容是源文件内容 */
        String fileContent = readContentsAsString(fileAdded);
        String blobName = sha1(fileContent);

        Blob blobAdd = new Blob(fileContent, blobName); // 使用blob进行对象化管理
        blobAdd.saveBlob();

        /* 不管原先是否存在，都会执行写逻辑*/
        /* addStage中写入指针,文件名是addFileName, 内容是暂存区保存的路径 */
        File blobPoint = join(ADD_STAGE_DIR, addFileName);
        writeContents(blobPoint, blobAdd.getFilePath().getName());
    }

    public static void commitFile(String commitMsg) {
        /* 获取addstage中的filename和hashname */
        List<String> addStageFiles = plainFilenamesIn(ADD_STAGE_DIR);
        List<String> removeStageFiles = plainFilenamesIn(REMOVE_STAGE_DIR);
        /* 错误的情况，直接返回 */
        if (addStageFiles != null && addStageFiles.isEmpty() && removeStageFiles.isEmpty()) {
            throw new GitletException("No changes added to the commit.");
        }
        if (commitMsg == null || commitMsg.isEmpty()) {
            throw new GitletException("Please enter a commit message.");
        }


        /* 获取最新的commit*/
        Commit oldCommit = getHeadCommit();

        /* 创建新的commit，newCommit根据oldCommit进行调整*/
        Commit newCommit = new Commit(oldCommit);
        newCommit.setDirectParent(oldCommit.getHashName());  // 指定父节点
        newCommit.setTimestamp(new Date(System.currentTimeMillis())); // 修改新一次的commit的时间戳为目前时间
        newCommit.setMessage(commitMsg); // 修改新一次的commit的时间戳为目前时间
//        newCommit.setBranchName(oldCommit.getBranchName()); // 在log或者status中需要展示本次commit的分支


        /* 对每一个addstage中的fileName进行其路径的读取，保存进commit的blobMap */
        for (String stageFileName : addStageFiles) {
            String hashName = readContentsAsString(join(ADD_STAGE_DIR, stageFileName));
            newCommit.addBlob(stageFileName, hashName);     // 在newCommit中更新blob
            join(ADD_STAGE_DIR, stageFileName).delete();
        }

        HashMap<String, String> blobMap = newCommit.getBlobMap();

        /* 对每一个rmstage中的fileName进行其路径的读取，删除commit的blobMap中对应的值 */
        for (String stageFileName : removeStageFiles) {
            if (blobMap.containsKey(stageFileName)) {
                newCommit.removeBlob(stageFileName);   // 在newCommit中删除removeStage中的blob
            }
            join(REMOVE_STAGE_DIR, stageFileName).delete();
        }

        newCommit.saveCommit();

        /* 更新HEAD指针和当前branch head指针 */
        saveHEAD(getHeadBranchName(), newCommit.getHashName());
        saveBranch(getHeadBranchName(), newCommit.getHashName());
    }

    public static void removeStage(String removeFileName) {
        /* 如果文件名是空或者如果工作区没有这个文件 */
        if (removeFileName == null || removeFileName.isEmpty()) {
            System.out.println("Please enter a file name.");
            exit(0);
        }

        Commit headCommit = getHeadCommit();
        HashMap<String, String> blobMap = headCommit.getBlobMap();
        List<String> addStageFiles = plainFilenamesIn(ADD_STAGE_DIR);

        if (!blobMap.containsKey(removeFileName)) {
            if (!addStageFiles.contains(removeFileName)) {
                System.out.println("No reason to remove the file.");
                exit(0);
            }

        }
        /* 如果addStage中存在，则删除 */
        File addStageFile = join(ADD_STAGE_DIR, removeFileName);
        if (addStageFile.exists()) {
            addStageFile.delete();
        }
        /* 当此文件正被track中 */
        if (blobMap.containsKey(removeFileName)) {
            /* 添加进removeStage */
            File remoteFilePoint = new File(REMOVE_STAGE_DIR, removeFileName);
            writeContents(remoteFilePoint, "");

            /* 删除工作目录下文件,注意仅在这个文件被track的时候进行删除 */
            File fileDeleted = new File(CWD, removeFileName);
            restrictedDelete(fileDeleted);
        }
    }

    public static String dateToTimeStamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(date);
    }

    public static void printCommitLog(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getHashName());
        System.out.println("Date: " + dateToTimeStamp(commit.getTimestamp()));
        System.out.println(commit.getMessage());
        System.out.print("\n");
    }

    public static void printLog() {
        Commit headCommit = getHeadCommit();
        Commit commit = headCommit;

        while (!commit.getDirectParent().equals("")) {
            printCommitLog(commit);
            commit = getCommit(commit.getDirectParent());
        }
        /* 打印最开始的一项*/
        printCommitLog(commit);
    }

    public static void printGlobalLog() {

        List<String> commitFiles = plainFilenamesIn(COMMIT_FOLDER);
        for (String commitFileName : commitFiles) {
            Commit commit = getCommit(commitFileName);
            printCommitLog(commit);
        }
    }

    public static void findCommit(String commitMsg) {
        Commit headCommit = getHeadCommit();
        Commit commit = headCommit;
        boolean found = false;
        List<String> commitFiles = plainFilenamesIn(COMMIT_FOLDER);
        for (String commitFile : commitFiles) {
            Commit commit1 = getCommit(commitFile);
            if (commit1.getMessage().equals(commitMsg)) {
                message(commit1.getHashName());
                found = true;
            }
        }

        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void showStatus() {
        System.out.println("=== Branches ===");
        String headBranch = getHeadBranchName();
        List<String> branches = plainFilenamesIn(HEAD_DIR);
        for (String branch : branches) {
            if (branch.equals(headBranch)) {
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        List<String> addStageFiles = plainFilenamesIn(ADD_STAGE_DIR);
        for (String fileName : addStageFiles) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        List<String> removeStageFiles = plainFilenamesIn(REMOVE_STAGE_DIR);
        for (String fileName : removeStageFiles) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for( String fileName : plainFilenamesIn(CWD)) {
            Commit headCommit = getHeadCommit();
            HashMap<String, String> blobMap = headCommit.getBlobMap();
            List<String> addStageFileNames = plainFilenamesIn(ADD_STAGE_DIR);
            if (!blobMap.containsKey(fileName)
                    && !addStageFileNames.contains(fileName)) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    public static void checkOut(String[] args) {
        String fileName;
        if (args.length == 2) {
            //  git checkout branchName
            checkoutBranch(args[1]);
        } else if (args.length == 4) {
            //  git checkout [commit id] -- [file name]
            if (!args[2].equals("--")) {
                message("Incorrect operands.");
                exit(0);
            }
            /* 获取到Blob对象 */
            fileName = args[3];
            String commitId = args[1];
            Commit commit = getHeadCommit();

            /* 是否可以进行对objects文件夹的重构，实现hashMap结构
                使得时间效率上不是线性, 而不是依靠链表查找？ */
            if (getCommitFromId(commitId) == null) {
                System.out.println("No commit with that id exists.");
                exit(0);
            } else {
                commit = getCommitFromId(commitId);
            }

            if (!commit.getBlobMap().containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                exit(0);
            }
            String blobName = commit.getBlobMap().get(fileName);
            String targetBlobContent = getBlobContentFromName(blobName);

            /* 将Blob对象中的内容覆盖working directory中的内容 */
            File fileInWorkDir = join(CWD, fileName);
            overWriteFileWithBlob(fileInWorkDir, targetBlobContent);

        } else if (args.length == 3) {
            //  git checkout -- [file name]
            /* 获取到Blob对象中的内容 */
            fileName = args[2];
            Commit headCommit = getHeadCommit();
            if (!headCommit.getBlobMap().containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                exit(0);
            }
            String blobName = headCommit.getBlobMap().get(fileName);
            String targetBlobContent = getBlobContentFromName(blobName);

            /* 将Blob对象中的内容覆盖working directory中的内容 */
            File fileInWorkDir = join(CWD, fileName);
            overWriteFileWithBlob(fileInWorkDir, targetBlobContent);

        }
    }

    public static boolean untrackFileExists(Commit commit) {
        List<String> workFileNames = plainFilenamesIn(CWD);
        Set<String> currTrackSet = commit.getBlobMap().keySet();
        /* 先检测CWD中是否存在未被current branch跟踪的文件 */

        for (String workFile : workFileNames) {
            if (!currTrackSet.contains(workFile)) {
                return true;
            }
        }
        return false;
    }

    public static void checkoutBranch(String branchName) {
        Commit headCommit = getHeadCommit();

        if (branchName.equals(getHeadBranchName())) {
            System.out.println("No need to checkout the current branch.");
            exit(0);
        }
        // 获取branchName的head对应的commit
        Commit branchHeadCommit = getBranchHeadCommit(branchName, "No such branch exists");
        HashMap<String, String> branchHeadBlobMap = branchHeadCommit.getBlobMap();
        Set<String> fileNameSet = branchHeadBlobMap.keySet();

        List<String> workFileNames = plainFilenamesIn(CWD);

        if (untrackFileExists(headCommit)) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            exit(0);
        }

        /* 检测完后清空CWD文件夹 */
        for (String workFile : workFileNames) {
            restrictedDelete(join(CWD, workFile));
        }

        /* 将fileNameSet中每一个跟踪的文件重写入工作文件夹中 */
        for (var trackedfileName : fileNameSet) {
            // 每一个trackedfileName是一个commit中跟踪的fileName
            File workFile = join(CWD, trackedfileName);
            String blobHash = branchHeadBlobMap.get(trackedfileName);   // 文件对应的blobName
            String blobFromNameContent = getBlobContentFromName(blobHash);
            writeContents(workFile, blobFromNameContent);
        }

        /* 将目前给定的分支视作当前分支 */
        saveHEAD(branchName, branchHeadCommit.getHashName());
    }

    public static void createBranch(String branchName) {
        Commit headCommit = getHeadCommit();
        List<String> fileNameinHeadDir = plainFilenamesIn(HEAD_DIR);
        if (fileNameinHeadDir.contains(branchName)) {
            message("A branch with that name already exists.");
            exit(0);
        }

        saveBranch(branchName, headCommit.getHashName());

    }

    public static void removeBranch(String branchName) {
        /* 检测是否有相关Branch */
        File brancheFile = join(HEAD_DIR, branchName);
        if (!brancheFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            exit(0);
        }
        /* 检测Branch是否为curr branch */
        Commit headCommit = getHeadCommit();
        if (headCommit.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            exit(0);
        }
        /* 删除这个branch的指针文件 */
        File branchHeadPoint = join(HEAD_DIR, branchName);
        branchHeadPoint.delete();
    }

    public static void reset(String commitId) {

        if (getCommitFromId(commitId) == null) {
            System.out.println("No commit with that id exists.");
            exit(0);
        }
        Commit headCommit = getHeadCommit();
        Commit commit = getCommitFromId(commitId);  // 将要reset的commit
        HashMap<String, String> commitBlobMap = commit.getBlobMap();

        /* 先检测CWD中是否存在未被current branch跟踪的文件 */
        List<String> workFileNames = plainFilenamesIn(CWD);
        /* 先检测CWD中是否存在未被current branch跟踪的文件 */
        if (untrackFileExists(headCommit)) {
            Set<String> currTrackSet = headCommit.getBlobMap().keySet();
            Set<String> resetTrackSet = commit.getBlobMap().keySet();
            boolean isUntrackInBoth = false;

            /* workfile没有在headCommit中也没有在commit中，将其从addstage中剔除，但在CWD中保存 */
            for (String workFile : workFileNames) {
                if (!currTrackSet.contains(workFile) && !resetTrackSet.contains(workFile)) {
                    removeStage(workFile);
                    isUntrackInBoth = true;
                    break;
                }
            }
            if (!isUntrackInBoth) {
                throw new GitletException("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }

        }

        /* 检测完后清空CWD文件夹 */
        for (String workFile : workFileNames) {
            restrictedDelete(join(CWD, workFile));
        }

        /* 将fileNameSet中每一个跟踪的文件重写入工作文件夹中 */
        for (var trackedfileName : commit.getBlobMap().keySet()) {
            // 每一个trackedfileName是一个commit中跟踪的fileName
            File workFile = join(CWD, trackedfileName);
            String blobHash = commitBlobMap.get(trackedfileName);   // 文件对应的blobName
            String blobFromNameContent = getBlobContentFromName(blobHash);
            writeContents(workFile, blobFromNameContent);
        }

        /* 同时将其branchHEAD指向commit*/
        saveBranch(getHeadBranchName(), commitId);
        /* 将目前给定的HEAD指针指向这个commit */
        saveHEAD(getHeadBranchName(), commitId);
    }

    public static void commitFileForMerge(String commitMsg, String branchName) throws GitletException {
        /* 获取addstage中的filename和hashname */
        List<String> addStageFiles = plainFilenamesIn(ADD_STAGE_DIR);
        List<String> removeStageFiles = plainFilenamesIn(REMOVE_STAGE_DIR);
        /* 错误的情况，直接返回 */
        if (addStageFiles.isEmpty() && removeStageFiles.isEmpty()) {
            throw new GitletException("No changes added to the commit.");
        }

        if (commitMsg == null) {
            throw new GitletException("Please enter a commit message.");
        }

        /* 获取最新的commit*/
        Commit oldCommit = getHeadCommit();
        Commit branchHeadCommit = getBranchHeadCommit(branchName, null);

        /* 创建新的commit，newCommit根据oldCommit进行调整*/
        Commit newCommit = new Commit(oldCommit);
        newCommit.setDirectParent(oldCommit.getHashName());  // 指定父节点
        newCommit.setTimestamp(new Date(System.currentTimeMillis())); // 修改新一次的commit的时间戳为目前时间
        newCommit.setMessage(commitMsg); // 修改新一次的commit的时间戳为目前时间
        newCommit.setOtherParent(branchHeadCommit.getHashName());   // 指定另一个父节点

        /* 对每一个addstage中的fileName进行其路径的读取，保存进commit的blobMap */
        for (String stageFileName : addStageFiles) {
            String hashName = readContentsAsString(join(ADD_STAGE_DIR, stageFileName));
            newCommit.addBlob(stageFileName, hashName);     // 在newCommit中更新blob
            join(ADD_STAGE_DIR, stageFileName).delete();
        }

        HashMap<String, String> blobMap = newCommit.getBlobMap();

        /* 对每一个rmstage中的fileName进行其路径的读取，删除commit的blobMap中对应的值 */
        for (String stageFileName : removeStageFiles) {
            if (blobMap.containsKey(stageFileName)) {
                join(BLOBS_FOLDER, blobMap.get(stageFileName)).delete(); // 删除blobs中的文件
                newCommit.removeBlob(stageFileName);   // 在newCommit中删除removeStage中的blob
            }
            join(REMOVE_STAGE_DIR, stageFileName).delete();
        }

        newCommit.saveCommit();

        /* 更新HEAD指针和master指针 */
        saveHEAD(getHeadBranchName(), newCommit.getHashName());
        saveBranch(getHeadBranchName(), newCommit.getHashName());
    }
    
    
    public static void mergeBranch(String branchName) {
        checkSafetyInMerge(branchName);
        Commit headCommit = getHeadCommit();
        Commit otherHeadCommit = getBranchHeadCommit(branchName,
                "A branch with that name does not exist."); // 如果不存在这个branch，则报错
        /* 获取当前splitCommit对象 */
        Commit splitCommit = getSplitCommit(headCommit, otherHeadCommit);
        if (splitCommit.getHashName().equals(otherHeadCommit.getHashName())) {
            throw new GitletException("Given branch is an ancestor of the current branch.");
        }

        HashMap<String, String> splitCommitBolbMap = splitCommit.getBlobMap();
        Set<String> splitKeySet = splitCommitBolbMap.keySet();
        HashMap<String, String> headCommitBolbMap = headCommit.getBlobMap();
        Set<String> headKeySet = headCommitBolbMap.keySet();
        HashMap<String, String> otherHeadCommitBolbMap = otherHeadCommit.getBlobMap();
        Set<String> otherKeySet = otherHeadCommitBolbMap.keySet();

        processSplitCommit(splitCommit, headCommit, otherHeadCommit);
        /* 为解决被删除操作 */
        for (var headTrackName : headKeySet) {
            if (!otherHeadCommitBolbMap.containsKey(headTrackName)) {
                if (!splitCommitBolbMap.containsKey(headTrackName)) {
                    /* 情况4：如果在other和split中都没有这个文件 */
                    continue;
                } else {
                    /* split：存在  other：被删除 */
                    if (!headCommitBolbMap.get(headTrackName)
                            .equals(splitCommitBolbMap.get(headTrackName))) {
                        /* HEAD：被修改 */
                        /* 存在 conflict */
                        processConflict(headCommit, otherHeadCommit, headTrackName);
                    }
                    /* 其他情况是情况6 已处理过 */
                }
            } else if (otherHeadCommitBolbMap.containsKey(headTrackName)
                    && !splitCommitBolbMap.containsKey(headTrackName)) {
                /* 情况3b other中存在文件, split中不存在文件，即这是不一致的修改*/
                if (!otherHeadCommitBolbMap.get(headTrackName)
                        .equals(headCommitBolbMap.get(headTrackName))) {
                    /*如果是不一致的修改，进行conflict处理，如果是一致的就跳过*/
                    processConflict(headCommit, otherHeadCommit, headTrackName);
                }
            }
        }
        for (var otherTrackName : otherKeySet) {
            if (!headCommitBolbMap.containsKey(otherTrackName)
                    && !splitCommitBolbMap.containsKey(otherTrackName)) {
                /* 情况5：如果在head和split中都没有这个文件 */
                String[] checkOutArgs = {"checkout",
                        otherHeadCommit.getHashName(),
                        "--",
                        otherTrackName};
                checkOut(checkOutArgs);
                addStage(otherTrackName);
            }
        }
        if (splitCommit.getHashName().equals(headCommit.getHashName())) {
            message("Current branch fast-forwarded.");
        }
        /* 进行一次自动的commit */
        String commitMsg = String.format("Merged %s into %s.", branchName, getHeadBranchName());
        commitFileForMerge(commitMsg, branchName);
    }

    public static void checkSafetyInMerge(String branchName) {

        List<String> addStageFiles = plainFilenamesIn(ADD_STAGE_DIR);
        List<String> rmStageFiles = plainFilenamesIn(REMOVE_STAGE_DIR);
        /* 如果存在暂存，直接退出 */
        if (!addStageFiles.isEmpty() || !rmStageFiles.isEmpty()) {
            throw new GitletException("You have uncommitted changes.");
        }
        Commit headCommit = getHeadCommit();
        // 如果不存在这个branch，则报错
        String errMsg = "A branch with that name does not exist.";
        Commit otherHeadCommit = getBranchHeadCommit(branchName, errMsg);

        if (getHeadBranchName().equals(branchName)) {
            throw new GitletException("Cannot merge a branch with itself.");
        }
        /* 查看是否存在未被跟踪的文件 */
        if (untrackFileExists(headCommit)) {
            throw new GitletException("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        }
    }

    public static void processConflict(Commit headCommit, Commit otherHeadCommit,
                                       String splitTrackName) {
        String otherBlobFile = "";
        String otherBlobContent = "";

        String headBlobFile = "";
        String headBlobContent = "";

        HashMap<String, String> headCommitBolbMap = headCommit.getBlobMap();
        HashMap<String, String> otherHeadCommitBolbMap = otherHeadCommit.getBlobMap();

        /* 打印冲突 */
        message("Encountered a merge conflict.");
        /* 获取*/
        if (otherHeadCommitBolbMap.containsKey(splitTrackName)) {
            otherBlobFile = otherHeadCommitBolbMap.get(splitTrackName);
            otherBlobContent = getBlobContentFromName(otherBlobFile);
        }

        if (headCommitBolbMap.containsKey(splitTrackName)) {
            headBlobFile = headCommitBolbMap.get(splitTrackName);
            headBlobContent = getBlobContentFromName(headBlobFile);
        }

        /* 修改workFile中的内容*/
        StringBuilder resContent = new StringBuilder();
        resContent.append("<<<<<<< HEAD\n");
        resContent.append(headBlobContent);
        resContent.append("=======" + "\n");
        resContent.append(otherBlobContent);
        resContent.append(">>>>>>>" + "\n");

        String resContentString = resContent.toString();
        writeContents(join(CWD, splitTrackName), resContentString);
        addStage(splitTrackName);
    }
    public static void processSplitCommit(Commit splitCommit, Commit headCommit,
                                          Commit otherHeadCommit) {
        HashMap<String, String> splitCommitBolbMap = splitCommit.getBlobMap();
        Set<String> splitKeySet = splitCommitBolbMap.keySet();
        HashMap<String, String> headCommitBolbMap = headCommit.getBlobMap();
        Set<String> headKeySet = headCommitBolbMap.keySet();
        HashMap<String, String> otherHeadCommitBolbMap = otherHeadCommit.getBlobMap();
        Set<String> otherKeySet = otherHeadCommitBolbMap.keySet();

        /* 从split中的文件开始 */
        for (var splitTrackName : splitKeySet) {
            // 如果在HEAD中未被修改(包括未被删除）
            if (headCommitBolbMap.containsKey(splitTrackName)
                    && headCommitBolbMap.get(splitTrackName)
                    .equals(splitCommitBolbMap.get(splitTrackName))) {
                // 如果other中存在此文件
                if (otherHeadCommitBolbMap.containsKey(splitTrackName)) {
                    /* 情况1 HEAD中未被修改，other中被修改*/
                    if (!otherHeadCommitBolbMap.get(splitTrackName)
                            .equals(splitCommitBolbMap.get(splitTrackName))) {
                        // 使用checkout将other的文件覆盖进工作区，同时将其add进暂存区
                        String[] checkOutArgs = {"checkout",
                                otherHeadCommit.getHashName(),
                                "--",
                                splitTrackName};
                        checkOut(checkOutArgs);
                        addStage(splitTrackName);
                    }
                } else {
                    /* 情况6: 当HEAD未修改，other中被删除 */
                    removeStage(splitTrackName);
                }
            } else {
                // 在HEAD中被修改（包括被删除）
                if (otherHeadCommitBolbMap.containsKey(splitTrackName)
                        && otherHeadCommitBolbMap.get(splitTrackName)
                        .equals(splitCommitBolbMap.get(splitTrackName))) {
                    /* 情况2 other中未被修改，HEAD中被修改，则不修改任何事情
                       情况7 other中未被修改，HEAD中被删除，则不修改任何事情 */
                    continue;
                } else {
                    /* other中被修改 或者被删除 */
                    if (!otherHeadCommitBolbMap.containsKey(splitTrackName)
                            && !headCommitBolbMap.containsKey(splitTrackName)) {
                        /* 情况3a 一致的删除 */
                        continue;
                    } else if (!otherHeadCommitBolbMap.containsKey(splitTrackName)
                            || !headCommitBolbMap.containsKey(splitTrackName)) {
                        /* 只存在一方被删除，跳过，从后面单独对HEAD和other指针进行操作 */
                        continue;
                    } else {
                        if (otherHeadCommitBolbMap.get(splitTrackName).equals(headCommitBolbMap.get(splitTrackName))) {
                            /* 情况3a 一致的修改 */
                            continue;
                        } else {
                            /* 情况3b 不一致的修改，不包括删除操作 */
                            processConflict(headCommit, otherHeadCommit, splitTrackName);
                        }
                    }
                }
            }
        }
    }

}
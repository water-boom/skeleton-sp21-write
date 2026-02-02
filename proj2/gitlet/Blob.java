package gitlet;

import java.io.File;
import java.io.Serializable;


import static gitlet.Repository.*;
import static gitlet.Utils.*;

public class Blob implements Serializable {
    private String content;
    private File filePath;
    private String hashName;

    public Blob(String content, String hashName) {
        this.content = content;
        this.hashName = hashName;
        this.filePath = join(BLOBS_FOLDER, hashName);
    }

    public File getFilePath() {
        return filePath;
    }


    public void saveBlob() {
        if (!filePath.exists()) {
            // 如果这个blob原先不存在，则进行blob的储存
            writeContents(filePath, this.content);
        }

    }

    public static String getBlobContentFromName(String blobName) {
        /* 获取commit文件 */
        String blobContent = null;
        File blobFile = join(BLOBS_FOLDER, blobName);
        if (blobFile.isFile() && blobFile.exists()) {
            blobContent = readContentsAsString(blobFile);
        }
        return blobContent;
    }

    public static void overWriteFileWithBlob(File file, String content) {
        writeContents(file, content);
    }

}
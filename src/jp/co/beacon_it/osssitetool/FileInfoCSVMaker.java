package jp.co.beacon_it.osssitetool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class FileInfoCSVMaker {

    private final int FILE_NAME_INDEX = 0;
    private final int FILE_SIZE_INDEX = 1;
    private final int TIMESTAMP_INDEX = 2;
    private final int MD5_HASHV_INDEX = 3;

    private String dirPath;
    private String oldCSVName = "infoscoop.csv";
    private String newCSVName = "infoscoop.csv";
    private String[] orderPatterns;
    private boolean isUseCSVHeader = false;

    public FileInfoCSVMaker(String dirPath, String oldCSVName, String newCSVName, String[] orderPatterns, boolean isUseCSVHeader) {
        this.dirPath = dirPath;
        this.orderPatterns = orderPatterns;
        this.isUseCSVHeader = isUseCSVHeader;

        if (oldCSVName != null && oldCSVName.length() != 0) {
            this.oldCSVName = oldCSVName;
        }

        if (newCSVName != null && newCSVName.length() != 0) {
            this.newCSVName = newCSVName;
        }

    }

    public void exec()
            throws SecurityException, NoSuchAlgorithmException, FileNotFoundException, IOException, Exception {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new Exception("error: directory [" + dirPath + "] does not exist.");
        }

        // delete old csv
        deleteOldCSV();

        // set "file name" field
        String[] fileNameList = dir.list();
        String[][] fileInfoList = new String[fileNameList.length][];
        sortFileName(fileInfoList, new ArrayList<String>(Arrays.asList(fileNameList)));

        // set "file size", "timestamp", "md5 hash value" fields
        for (int i = 0, end = fileInfoList.length; i < end; i++) {
            String fileName = fileInfoList[i][FILE_NAME_INDEX];
            File file = new File(dirPath + "/" + fileName);

            if (!file.isFile()) {
                throw new Exception("error: [" + dirPath + "/" + fileName + "] is not file.");
            }

            fileInfoList[i][FILE_SIZE_INDEX] = String.valueOf(file.length());
            fileInfoList[i][TIMESTAMP_INDEX] = String.valueOf(file.lastModified());
            fileInfoList[i][MD5_HASHV_INDEX] = getMD5HashValue(file);
        }

        // output csv
        outputCSV(fileInfoList);
    }

    private void deleteOldCSV() throws SecurityException, Exception {
        File csv = new File(dirPath + "/" + oldCSVName);

        if (!csv.exists() || !csv.isFile()) {
            return;
        }

        if (!csv.delete()) {
            throw new Exception("error: fail deleting file [" + dirPath + "/" + oldCSVName + "], maybe.");
        }
    }

    private void sortFileName(String[][] fileInfoList, List<String> fileNameList) {
        int arrayI = 0;
        for (String pattern : orderPatterns) {
            Pattern p = Pattern.compile(pattern);
            for (int listI = 0, end = fileNameList.size(); listI < end; listI++) {
                String fileName = fileNameList.get(listI);
                if (p.matcher(fileName).matches()) {
                    fileInfoList[arrayI++] = new String[] { fileName, null, null, null };
                    fileNameList.remove(listI);
                }
            }
        }

        // leftovers
        for (String fileName : fileNameList) {
            fileInfoList[arrayI++][FILE_NAME_INDEX] = fileName;
        }
    }

    private String getMD5HashValue(File file)
            throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        StringBuffer hashValue = null;
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        DigestInputStream in = null;
        try {
            in = new DigestInputStream(new BufferedInputStream(new FileInputStream(file)), md5);
            while (in.read() != -1) {
            }
            
            byte[] b = md5.digest();
            hashValue = new StringBuffer();
            for (int i = 0, end = b.length; i < end; i++) {
                hashValue.append(String.format("%02x", b[i] & 0xff));
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return hashValue.toString();
    }

    private void outputCSV(String[][] fileInfoList) throws IOException {
        FileWriter out = null;
        try {
            out = new FileWriter(new File(dirPath + "/" + newCSVName));

            if (isUseCSVHeader) {
                out.write("#FILE_NAME,FILE_SIZE,TIMESTAMP,MD5_HASH_VALUE\n");
            }

            for (String[] fileInfo : fileInfoList) {
                StringBuffer line = new StringBuffer(fileInfo[FILE_NAME_INDEX]);
                line.append(",");
                line.append(fileInfo[FILE_SIZE_INDEX]);
                line.append(",");
                line.append(fileInfo[TIMESTAMP_INDEX]);
                line.append(",");
                line.append(fileInfo[MD5_HASHV_INDEX]);
                line.append("\n");
                out.write(line.toString());
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

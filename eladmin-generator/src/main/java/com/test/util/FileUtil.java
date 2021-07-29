package com.test.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * @author hangui_zhang
 * @date 2021-04-12 19:30
 */
public class FileUtil {
    protected static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static List<ZipEntry> getZipEntrys(File folder) {
        List<String> files = getFileAndFolders(folder, null);
        String rootFolder = FileTransfer.formatPath(folder.getAbsolutePath());
        List<ZipEntry> entrys = new ArrayList<ZipEntry>();
        for (String string : files) {
            File file = new File(string);
            if (file.isDirectory()) {
                String fmtFile = FileTransfer.formatPath(file.getAbsolutePath());
                String subFile = fmtFile.replace(rootFolder, "");
                if (!StringUtils.isEmpty(subFile)) {
                    ZipEntry entry = new ZipEntry(subFile);
                    entrys.add(entry);
                }
            } else {
                ZipEntry entry = new ZipEntry(file.getAbsolutePath());
                entrys.add(entry);
            }
        }
        return entrys;
    }

    public static List<String> getFileAndFolders(File folder) {
        return getFileAndFolders(folder, null);
    }

    public static List<String> getFileAndFolders(String folder, final String ext) {
        File folder1 = new File(folder);
        return getFileAndFolders(folder1, ext);
    }

    public static List<String> getFileAndFolders(File folder, final String ext) {
        File[] files = null;
        if (!StringUtils.isEmpty(ext)) {
            files = folder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.getAbsolutePath().endsWith(ext)) {
                        return true;
                    }
                    return false;
                }
            });
        } else {
            files = folder.listFiles();
        }
        List<String> list = new ArrayList<String>();
        if (null != files) {
            for (File f : files) {
                if (f.isDirectory()) {
                    list.add(f.getAbsolutePath());
                    list.addAll(getFiles(f, ext));
                } else {
                    list.add(f.getAbsolutePath());
                }
            }
        }
        return list;
    }

    public static List<String> getFiles(File folder) {
        return getFiles(folder, null);
    }

    public static List<String> getFiles(File folder, final String ext) {
        File[] files = null;
        if (!StringUtils.isEmpty(ext)) {
            files = folder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.getAbsolutePath().endsWith(ext)) {
                        return true;
                    }
                    return false;
                }
            });
        } else {
            files = folder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isFile()) {
                        return true;
                    }
                    return false;
                }
            });
        }
        List<String> list = new ArrayList<String>();
        if (null != files) {
            for (File f : files) {
                list.add(f.getAbsolutePath());
            }
        }

        // 查找子目录
        File[] subFolders = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }
                return false;
            }
        });
        if (null != subFolders) {
            for (File file : subFolders) {
                list.addAll(getFiles(file, ext));
            }
        }
        return list;
    }

    public static File makeBackupFile(File dest) {
        File backupFile = dest;
        if (dest.exists()) {
            String fileName = dest.getName();
            String name = fileName.substring(0, FileTransfer.formatPath(fileName).lastIndexOf("."));
            String ext = fileName.substring(FileTransfer.formatPath(fileName).lastIndexOf("."));
            String[] arr = name.split("-");
            int num = 1;
            String[] arr2 = arr;
            int two = 2;
            if (arr.length == two && NumberUtils.isDigits(arr[1])) {
                num = Integer.valueOf(arr[1]);
                num++;
                arr2 = new String[arr.length - 1];
                System.arraycopy(arr, 0, arr2, 0, arr2.length);
            } else {
                num = 1;
            }
            fileName = StringUtils.join(arr2) + "-" + num + ext;
            File file2 = new File(FileTransfer.formatPath(backupFile.getParent()) + "/" + fileName);
            backupFile = makeBackupFile(file2);
        }
        return backupFile;
    }

    /**
     * 定义GB的计算常量
     */
    private static final int GB = 1024 * 1024 * 1024;
    /**
     * 定义MB的计算常量
     */
    private static final int MB = 1024 * 1024;
    /**
     * 定义KB的计算常量
     */
    private static final int KB = 1024;

    /**
     * 临时文件夹 {当前运行目录}/TEMP
     */
    private static final String TEMP_DIR = FileTransfer.formatPath ((new File("")).getAbsolutePath()+ "/TEMP")+ "/";
    /**
     * 复制inputStream 输出到临时文件File
     */
    static File copyInputStreamToTempFile(InputStream ins, String name) throws Exception {
        File file = new File(FileUtil.TEMP_DIR + name);
        if (file.exists()) {
            return file;
        }else{
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            if(!file.exists()){
                file.createNewFile();
            }
        }
        OutputStream os = new FileOutputStream(file);
        int bytesRead;
        int len = 8192;
        byte[] buffer = new byte[len];
        while ((bytesRead = ins.read(buffer, 0, len)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        ins.close();
        return file;
    }
}

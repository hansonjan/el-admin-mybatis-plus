package com.test;

import com.test.util.FileTransfer;
import com.test.util.FileUtil;

import java.io.File;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        String filePath = "E:\\nannar\\el-admin-space\\el-admin-mybatis-plus";
        String oldDir = "me/zhengjie";
        String newDir = "com/admin";
        List<String> files = FileUtil.getFiles(new File(filePath));
        for (String file : files) {
            String path = FileTransfer.formatPath(file);
            if(path.contains(oldDir)){
                String newPath = path.replace(oldDir,newDir);
                cn.hutool.core.io.FileUtil.move(new File(path),new File(newPath),Boolean.TRUE);
            }
            System.out.println();
        }
        System.out.println("fileCount:"+files.size());

        String oldPkg = "me.zhengjie";
        String newPkg = "com.admin";
        files = FileUtil.getFiles(new File(filePath));
        int reWriteCount = 0;
        for (String file : files) {
            List<String> lines = cn.hutool.core.io.FileUtil.readLines(file, "UTF-8");
            boolean reWritable = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                boolean change = false;
                while(line.contains(oldPkg)){
                    line = line.replace(oldPkg,newPkg);
                    reWritable = true;
                    change = true;
                }
                if(change){
                    lines.set(i,line);
                }
            }
            if(reWritable){
                cn.hutool.core.io.FileUtil.writeLines(lines,file,"UTF-8");
                reWriteCount ++;
                System.out.println("rewrite:"+file);
            }
        }
        System.out.println("rewriteCount:"+reWriteCount);
    }
}

package com.test;

import com.test.util.FileTransfer;
import com.test.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FixColumnAlias {

    private static List<String> getPojoFiles(String path){
        //extends BaseEntity
        List<String> files = FileUtil.getFiles(new File(path));
        List<String> list = new ArrayList<>();
        for (int i = files.size()-1; i >=0; i--) {
            String fileName = files.get(i);
            List<String> lines = cn.hutool.core.io.FileUtil.readLines(fileName, "UTF-8");
            boolean isPojo = false;
            for (String line : lines) {
                if(line.contains(" extends ") && (line.contains("BaseEntity") || line.contains("BaseModel"))){
                    isPojo = true;
                    break;
                }
            }
            if(isPojo){
                list.add(fileName);
            }
        }
        return list;
    }

    public static void main(String[] args) {
        String filePath = "E:\\nannar\\el-admin-space\\el-admin-mybatis-plus";

        List<String> files = getPojoFiles(filePath);
        int rewriteCount = 0;
        for (String file : files) {
            String path = FileTransfer.formatPath(file);
            List<String> lines = cn.hutool.core.io.FileUtil.readLines(path, "UTF-8");
            boolean reWritable = false;
            for (int i = lines.size() - 1; i >= 0 ; i--) {
                String line = lines.get(i);
                boolean fieldLine = isFieldLine(line);
                if(fieldLine){
                    List<String> fieldLines = getFieldLines(lines, i);

                    boolean hasFieldMark = hasFieldMark(fieldLines);
                    if(!hasFieldMark){
                        reWritable = true;
                        String fieldName = getFieldName(line);
                        String columnName = toColumnName(fieldName);

                        String leftSpace = line.substring(0,line.indexOf("p"));
                        String fieldMarkLine = leftSpace + "@TableField(\""+columnName+"\")";
                        lines.add(i,fieldMarkLine);
                    }
                }
            }
            if(reWritable){
                cn.hutool.core.io.FileUtil.writeLines(lines,path,"UTF-8");
                System.out.println("rewrite:"+path);
                rewriteCount ++;
            }

            if(hasNotImportField(lines)){
                addImportFieldStatment(lines);
                cn.hutool.core.io.FileUtil.writeLines(lines,path,"UTF-8");
                System.out.println("rewrite:"+path);
                rewriteCount ++;
            }
            //import com.baomidou.mybatisplus.annotation.TableField;

        }
        System.out.println("fileCount:"+files.size()+",rewrite:"+rewriteCount);

    }

    private static boolean hasNotImportField(List<String> lines){
        boolean notImported = true;
        for (String line : lines) {
            if(line.contains("import ") && line.contains("com.baomidou.mybatisplus.annotation.TableField")){
                notImported = false;
                break;
            }
            if(line.contains(" class ")){
                break;
            }
        }
        return notImported;
    }

    private static void addImportFieldStatment(List<String> lines){
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if(line.startsWith("import")){
                lines.add(i,"import com.baomidou.mybatisplus.annotation.TableField;");
                break;
            }
        }
    }

    private static boolean isFieldLine(String line){
        line = line.replace("\t"," ").trim();
        return line.startsWith("private") && line.endsWith(";");
    }

    private static boolean hasFieldMark(List<String> lines){
        boolean hasFieldMark = false;
        for (String line : lines) {
            line = line.replace("\t"," ").trim();
            if(line.startsWith("@TableField") || line.startsWith("@TableId")){
                hasFieldMark = true;
                break;
            }
        }
        return hasFieldMark;
    }

    private static String getFieldName(String line){
        line = line.trim();
        if(line.contains("=")){
            line = line.substring(0,line.indexOf("=")).trim();
        }
        String str = line.substring(line.lastIndexOf(" ") + 1);
        while(str.endsWith(";")){
            str = str.substring(0,str.length()-1);
        }
        return str;
    }
    private static String toColumnName(String fieldName){
        StringBuffer buffer = new StringBuffer(fieldName);
        for (int i = buffer.length()-1; i >=1 ; i--) {
            if(Character.isUpperCase(buffer.charAt(i))){
                buffer.insert(i,'_');
            }
        }
        return buffer.toString().toLowerCase();
    }

    private static List<String> getFieldLines(List<String> lines,int index){
        List<String> list = new ArrayList<>();
        list.add(0,lines.get(index));
        for (int i = index-1; i >=0 ; i--) {
            String line = lines.get(i);
            line.replace("\t"," ").trim();
            if("".equals(line) || isFieldLine(line)){
                break;
            }
            list.add(0,lines.get(i));
        }
        return list;
    }
}

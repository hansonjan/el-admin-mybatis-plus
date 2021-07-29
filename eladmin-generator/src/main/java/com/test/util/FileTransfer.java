package com.test.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @typename      : FileTransfer
 * @description   : (用一段文字描述此类的作用)
 * @author        : hangui_zhang
 * @create by     : 2018年12月12日 下午2:12:19
 * @version       : V1.0  
 */
public class FileTransfer {
    /**
     * WINDOWS 文件分隔符\
     */
    public static final String WINDOWS_FILE_SPLITER1 = "\\";
    /**
     * WINDOWS 文件分隔符\\
     */
    public static final String WINDOWS_FILE_SPLITER11 = "\\\\";
    /**
     * WINDOWS 文件分隔符//
     */
    public static final String WINDOWS_FILE_SPLITER2 = "//";
    /**
     * Linux 文件分隔符/
     */
    public static final String LINUX_FILE_SPLITER = "/";
    public static final String LINUX_LINE_END = "\n";
    public static final String WINDOWS_LINE_END = "\r\n";
    /**
     * 空字符串
     */
    public static final String EMPTY = "";
    private static final String DOT = ".";
    private static final String DOLLER = "$";
    private static final String EXT_CLASS = ".class";
    
    /**
     * 格式化文件路径：Linux格式，以/分隔。
     * @author        : hangui_zhang
     * @create by     : 2019-01-14 14:14:01
     * @param path
     * @return
     */
    public static String formatPath(String path) {
    	path = repalcePath(path,FileTransfer.WINDOWS_FILE_SPLITER1,FileTransfer.LINUX_FILE_SPLITER);
    	path = repalcePath(path,FileTransfer.WINDOWS_FILE_SPLITER2,FileTransfer.LINUX_FILE_SPLITER);
    	path = repalcePath(path,FileTransfer.LINUX_FILE_SPLITER+FileTransfer.LINUX_FILE_SPLITER,FileTransfer.LINUX_FILE_SPLITER);
		return path;
	}
    
    /**
     * 格式化文件路径：windows单斜杠格式，以\分隔。
     * @author        : hangui_zhang
     * @create by     : 2019-01-14 14:14:01
     * @param path
     * @return
     */
    public static String formatPathWindowsSingleSplit(String path) {
    	path = repalceStr(path,FileTransfer.WINDOWS_FILE_SPLITER1,FileTransfer.LINUX_FILE_SPLITER);
    	path = repalceStr(path,FileTransfer.WINDOWS_FILE_SPLITER2,FileTransfer.LINUX_FILE_SPLITER);
    	path = repalceStr(path,FileTransfer.LINUX_FILE_SPLITER,FileTransfer.WINDOWS_FILE_SPLITER1);
		return path;
	}
    
    /**
     * 格式化文件路径：windows双斜杠格式，以\\分隔。
     * @author        : hangui_zhang
     * @create by     : 2019-01-14 14:14:01
     * @param path
     * @return
     */
    public static String formatPathWindowsDoubleSplit(String path) {
    	path = repalceStr(path,FileTransfer.WINDOWS_FILE_SPLITER1,FileTransfer.LINUX_FILE_SPLITER);
    	path = repalceStr(path,FileTransfer.WINDOWS_FILE_SPLITER2,FileTransfer.LINUX_FILE_SPLITER);
    	path = repalceStr(path,FileTransfer.LINUX_FILE_SPLITER,FileTransfer.WINDOWS_FILE_SPLITER11);
		return path;
	}
    
	public static String repalcePath(String source, String fromSubPathStr, String toSubPathStr) {
		String str = FileTransfer.toLinuxPath(source);
		String str1 = FileTransfer.toLinuxPath(fromSubPathStr);
		String str2 = FileTransfer.toLinuxPath(toSubPathStr);
		String result = str.replace(str1,str2);
		return result;
	}
	
	public static String repalceStr(String source, String fromStr, String toStr) {
		String result = source.replace(fromStr,toStr);
		return result;
	}

	/**
	 * <pre>
	 * 将文件路径转换为Linux格式，文件夹之间以/分隔 
	 * </pre>
	 * 
	 * @author        : hangui_zhang
	 * @create by     : 2018年12月12日 上午10:51:28
	 * @param source
	 * @return
	 */
	public static String toLinuxPath(String source) {
	    String str = source;
        str = replace(str,FileTransfer.WINDOWS_FILE_SPLITER1,FileTransfer.LINUX_FILE_SPLITER);
        str = replace(str,FileTransfer.WINDOWS_FILE_SPLITER2,FileTransfer.LINUX_FILE_SPLITER);
        str = str.trim();
		return str;
	}
	/**
     * <pre>
     * 将文件路径转换为Linux格式，文件夹之间以\\分隔 
     * </pre>
     * 
	 * @author        : hangui_zhang
	 * @create by     : 2018年12月12日 上午10:53:35
	 * @param source
	 * @return
	 */
	public static String toWindowsPath(String source) {
		String str = source;
		str = replace(str,FileTransfer.WINDOWS_FILE_SPLITER1,FileTransfer.LINUX_FILE_SPLITER);
		str = replace(str,FileTransfer.WINDOWS_FILE_SPLITER2,FileTransfer.LINUX_FILE_SPLITER);
		str = replace(str,FileTransfer.LINUX_FILE_SPLITER,FileTransfer.WINDOWS_FILE_SPLITER1);
		str = str.trim();
		return str;
	}
	/**
	 * 拼接文件路径字符串
	 * @author        : hangui_zhang
	 * @create by     : 2019-03-08 10:27:29
	 * @param folder
	 * @param subPath
	 * @return
	 */
	public static String pathConcat(String folder,String subPath) {
	    return pathConcat(folder, subPath, null);
	}
	/**
     * 拼接文件路径字符串
     * @author        : hangui_zhang
     * @create by     : 2019-03-08 10:27:29
     * @param folder
     * @param subPath
     * @param moreSubPath
     * @return
     */
    public static String pathConcat(String folder,String subPath,String... moreSubPath) {
        StringBuilder sb = new StringBuilder();
        sb.append(folder);
        sb.append(FileTransfer.LINUX_FILE_SPLITER);
        sb.append(subPath);
        if(null != moreSubPath) {
            for (String sub : moreSubPath) {
                sb.append(FileTransfer.LINUX_FILE_SPLITER);
                sb.append(sub);
            }
        }
        String path = formatPath(sb.toString());
        return path;
    }
	/**
	 * 替换新的后缀名
	 * @param file
	 * @param newExt
	 * @return
	 */
	public static File changeExt(File file,String newExt) {
		String newPath = FileTransfer.changeExt(file.getAbsolutePath(), newExt);
		return new File(newPath);
	}
    /**
     * 替换新的后缀名
     * @param fileFullName
     * @param newExt
     * @return
     */
    public static String changeExt(String fileFullName,String newExt) {
    	String result = fileFullName;
    	String dot = ".";
    	if(null != fileFullName && null != newExt) {
    		if(fileFullName.contains(dot)) {
    			String pre = fileFullName.substring(0,fileFullName.indexOf(dot));
    			if(!newExt.startsWith(dot)) {
    				newExt = dot+newExt;
    			}
    			result = pre + newExt;
    		}
    	}
    	return result;
    }
	/**
	 * 替换字符串模板
	 * @author        : hangui_zhang
	 * @create by     : 2018年12月12日 上午10:56:17
	 * @param str      要替换的字符串模板。
	 * @param src      要被替换的子字符串
	 * @param target   子字符串要替换成的新字符串。
	 * @return
	 */
	public static String replace(String str,String src,String target) {
	    while(str.contains(src)) {
	        str = str.replace(src, target);
	    }
	    return str;
	}
	/**
	 * 过滤出属于某个目录下的所有文件
	 * @param files   所有文件的清单
	 * @param folder  要过滤的文件夹
	 * @return
	 */
	public static List<String> filter(String[] files, String folder) {
		List<String> list = new ArrayList<String>();
		for(String file : files) {
			if(file.startsWith(folder)) {
				list.add(file);
			}
		}
		return list;
	}

	/**
	 * 过滤出属于某个目录下的所有文件
	 * @param files   所有文件的清单
	 * @param folders  要过滤的文件夹
	 * @return
	 */
	public static List<String> filter(String[] files, String[] folders) {
		List<String> list = new ArrayList<String>();
		for(String folder : folders) {
			for(String file : files) {
				if(file.startsWith(folder)) {
					list.add(file);
				}
			}
		}
		return list;
	}

	/**
	 * 获取java类编译后的classes文件,一个java类可能对应多个class文件。
	 * @param folder     class文件所在的目录
	 * @param fileName	 java文件的名称不包含路径和后缀名。
	 * @return
	 */
	public static List<String> getClassFiles(String folder, String fileName) {
		final String javaFileName = fileName;
		File fdir = new File(folder);
		File[] fs = fdir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if(pathname.getAbsolutePath().endsWith(FileTransfer.EXT_CLASS)) {
					if(pathname.getName().startsWith(javaFileName + FileTransfer.DOT)
							|| pathname.getName().startsWith(javaFileName + FileTransfer.DOLLER)) {
						return true;
					}
				}
				return false;
			}
		});
		List<String> classFileName = new ArrayList<String>();
		if(null != fs && fs.length > 0) {
			for(int i = 0;i < fs.length;i++) {
				classFileName.add(fs[i].getAbsolutePath());
			}
		}
		return classFileName;
	}

}

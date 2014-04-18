package com.simple.ipeer.bc.files;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 *
 * @author iPeer
 */
public class BackupFile {
    
    private final String path, extensions, backupPath;
    private final boolean isFile;

    public BackupFile(String path, boolean isFile, String extensions) {
	this.path = path;
	this.isFile = isFile;
	this.extensions = (extensions.equals("") && !isFile ? "*.*" : extensions);
	this.backupPath = generateDefaultPath(path);
    }
    
        public BackupFile(String path, boolean isFile, String extensions, String backupPath) {
	this.path = path;
	this.isFile = isFile;
	this.extensions = extensions;
	this.backupPath = backupPath;
    }
    
    private String generateDefaultPath(String p) {
//	Path path1 = Paths.get(p);	
//	String root = path1.getRoot().toString();
//	String parent = root;
//	try {
//	    parent = path1.getParent().normalize().toString();
//	} catch (NullPointerException e) { return ""; }
//	return parent.substring(root.length() - 1);
	return p.substring(p.lastIndexOf(System.getProperty("file.separator")));
    }
    
    public String saveString() {
	return String.format("%s|%s|%s", this.path, this.extensions, this.backupPath);
    }
    
    public String path() { return this.path; }
    public String extensions() { return this.extensions; }
    public boolean isFile() { return this.isFile; }
    public String backupPath() { return this.backupPath; }

    public Object[] asArray() {
	
	Object[] a = {this.path, this.backupPath, this.extensions};
	return a;
	
    }
    
    public static BackupFile createFromArray(Object[] in) {
	String path = in[0].toString();
	String extensions = in[1].toString();
	String backupPath = in[2].toString();
	BackupFile buf = new BackupFile(path, !(new File(path).isDirectory()), extensions, backupPath);
	return buf;
    }

}

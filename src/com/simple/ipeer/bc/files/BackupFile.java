package com.simple.ipeer.bc.files;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author iPeer
 */
public class BackupFile {
    
    private String path, extensions, backupPath;
    private boolean isFile;

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
	Path path1 = Paths.get(p);	
	String root = path1.getRoot().toString();
	String parent = root;
	try {
	    parent = path1.getParent().normalize().toString();
	} catch (NullPointerException e) { return "<backupdir>"+System.getProperty("file.separator"); }
	return "<backupdir>"+parent.substring(root.length() - 1);
    }
    
    public String path() { return this.path; }
    public String extensions() { return this.extensions; }
    public boolean isFile() { return this.isFile; }
    public String backupPath() { return this.backupPath; }

    public Object[] asArray() {
	
	Object[] a = {this.path, this.backupPath, this.extensions};
	return a;
	
    }

}

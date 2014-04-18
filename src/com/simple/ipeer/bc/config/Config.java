package com.simple.ipeer.bc.config;

import com.simple.ipeer.bc.files.BackupFile;
import java.awt.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

/**
 *
 * @author iPeer
 */
public class Config {
    
    public File BACKUP_TO;
    public int FAILURE_METHOD = 0;
    public int LOG_METHOD = 0;
    
    public String CONFIG_NAME;
    
    public ArrayList<BackupFile> fileList;
    

    public Config(String configName, File backupto, int failuremethod, int logmethod, ArrayList<BackupFile> files) {
	this.BACKUP_TO = backupto;
	this.FAILURE_METHOD = failuremethod;
	this.LOG_METHOD = logmethod;
	this.fileList = files;
	this.CONFIG_NAME = configName;
    }
    
    public void saveConfig(String name) throws IOException, ConfigExistsException {
	saveConfig(name, false);
    }
    
    public void saveConfig(String name, boolean overwrite) throws IOException {
	File f = new File("./configs/");
	f.mkdirs();
	f = new File(f, name+".bcc");
//	if (f.exists() && !overwrite)
//	    throw new ConfigExistsException("The specified config name already exists");
	FileWriter out = new FileWriter(f, false);
	out.append("CONFIG_NAME="+name+"\n");
	out.append("BACKUP_TO="+BACKUP_TO.getAbsolutePath()+"\n");
	out.append("FAILURE_METHOD="+Integer.toString(FAILURE_METHOD)+"\n");
	out.append("LOG_METHOD="+Integer.toString(LOG_METHOD)+"\n");
	out.append("FILES\n");
	for (Iterator<BackupFile> it = fileList.iterator(); it.hasNext();) {
	    out.append((it.next()).saveString()+"\n");
	}
	out.flush();
	out.close();
    }
    
    public boolean exists() {
	return new File("./configs/", this.CONFIG_NAME+".bcc").exists();
    }
    
    public static Config loadConfig(String name) throws IOException {
	File f = new File("./configs/", name+".bcc");
	if (!f.exists()) 
	    throw new IOException("Config doesn't exist.");
	Scanner s = new Scanner(f);
	String configName = "Generic Name";
	String backupPath = "./backups/";
	int fMethod = 0;
	int lMethod = 0;
	ArrayList<BackupFile> files = new ArrayList<BackupFile>();
	while (s.hasNext()) {
	    String line = s.nextLine();
	    if (line.startsWith("CONFIG_NAME"))
		configName = line.split("=")[1];
	    if (line.startsWith("BACKUP_TO"))
		backupPath = line.split("=")[1];
	    if (line.startsWith("FAILURE_METHOD"))
		fMethod = Integer.parseInt(line.split("=")[1]);
	    if (line.startsWith("LOG_METHOD"))
		lMethod = Integer.parseInt(line.split("=")[1]);
	    if (line.equals("FILES")) {
		while (s.hasNext())
		    files.add(BackupFile.createFromArray(s.nextLine().split("\\|")));
	    }
	}
	return new Config(configName, new File(backupPath), fMethod, lMethod, files);
    }
    
    public static Object[] loadConfigList() {
	File f = new File("configs/");
	if (!f.exists()) { return new String[0]; }
	ArrayList<String> configs = new ArrayList<String>();
	for (File f1 : f.listFiles()) {
	    if (f1.getAbsolutePath().equals(".") || f1.getAbsolutePath().equals("..")) { continue; }
	    String path = f1.getAbsoluteFile().toString();
	    int start = path.lastIndexOf(System.getProperty("file.separator")) + 1;
	    String name = path.substring(start, path.length() - 4);
	    configs.add(name);
	}
	return configs.toArray();
		
    }

}

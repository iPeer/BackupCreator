package com.simple.ipeer.bc.files;

import com.simple.ipeer.bc.config.Config;
import com.simple.ipeer.bc.gui.MainGUI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author iPeer
 */
public class FileFinder {
    
    private final ArrayList<BackupFile> fileList = new ArrayList<BackupFile>();
    
    private final MainGUI mGUI;
    private Config cfg;
    public volatile boolean CONTINUE_BACKUP = true;
    private int FILE_COUNT = 0;

    public FileFinder(MainGUI gui) {
	this.mGUI = gui;
    }
    
    public void startBackup(String configName) {
	try {
	    this.cfg = Config.loadConfig(configName);
	    MainGUI g = this.mGUI;
	    File f = new File(cfg.CONFIG_NAME+".flist");
	    if (f.exists())
		f.delete();
	    g.jProgressBar1.setIndeterminate(true);
	    g.jComboBox1.setEnabled(false);
	    g.stopButton.setEnabled(true);
	    g.createButton.setEnabled(false);
	    g.statusText.setText("Generating file list...");
	    generateFileList();
	    g.jProgressBar1.setIndeterminate(false);
	    if (!this.CONTINUE_BACKUP) {
		g.statusText.setText("Cancelled by user.");
	    	g.createButton.setEnabled(true);
		g.stopButton.setEnabled(false);
		g.jComboBox1.setEnabled(true);
	    }
	    else
		g.statusText.setText(this.FILE_COUNT+" files ready for backup.");
	} catch (IOException ex) {
	    Logger.getLogger(FileFinder.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    public void performBackup() {
	MainGUI g = this.mGUI;
	if (!this.CONTINUE_BACKUP) { g.statusText.setText("Cancelled by user."); return; }
	g.statusText.setText("Backing up files...");
	g.jProgressBar1.setMaximum(FILE_COUNT);
	g.jProgressBar1.setIndeterminate(false);
	g.jProgressBar1.setValue(0);
	g.jProgressBar1.setStringPainted(true);
	try {
	    Scanner s = new Scanner(new File(cfg.CONFIG_NAME+".flist"));
	    while (s.hasNext() && this.CONTINUE_BACKUP) {
		g.jProgressBar1.setString((g.jProgressBar1.getValue() + 1)+"/"+g.jProgressBar1.getMaximum());
		String[] line = s.nextLine().split("\\|");
		Path old = new File(line[0]).toPath();
		Path _new = new File(line[2]).toPath();
		new File(_new.toFile().getPath()).mkdirs();
		try {
		    Files.copy(old, _new, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) { if (cfg.FAILURE_METHOD == 1) { break; } }
		g.jProgressBar1.setValue(g.jProgressBar1.getValue() + 1);
	    }
	} catch (FileNotFoundException ex) {	}
	g.statusText.setText("Backup complete. Ready for next instruction.");
	g.createButton.setEnabled(true);
	g.stopButton.setEnabled(false);
	g.jComboBox1.setEnabled(true);
	File f = new File(cfg.CONFIG_NAME+".flist");
	if (!f.delete())
	    f.deleteOnExit();
    }
    
//    private void generateFileList() {
//	generateFileList(this.cfg.fileList.get(0));
//    }
    
    private void generateFileList() {
	ArrayList<BackupFile> files = new ArrayList<BackupFile>();
	String btPath = preparePath(this.cfg.BACKUP_TO).getAbsolutePath();
	for (Iterator<BackupFile> it = this.cfg.fileList.iterator(); it.hasNext();) {
	    
	    BackupFile bf = it.next();
	    if (new File(bf.path()).isFile())
		files.add(bf);
	    else {
		getFiles(bf.path(), new File(bf.path()), files, bf.backupPath(), bf.extensions(), btPath);
	    }      
	}
    }
    
    private void getFiles(String fPath, File in, ArrayList<BackupFile> files, String bPath, String extensions, String toPath) {
	if (!this.CONTINUE_BACKUP) { return; }
	for (File f : in.listFiles()) {
	    if (f.isFile()) {
		String ext = "";
		try {
		    ext = f.getAbsoluteFile().toString().substring(f.getAbsoluteFile().toString().lastIndexOf("."));
		}
		catch (StringIndexOutOfBoundsException e) { ext = "::NOEXT::"; }
		if (!(extensions.equals("*.*") || ext.equals("::NOEXT::")) && !Arrays.asList(extensions.split(";")).contains(ext)) { continue; }
		String sPath = toPath+bPath+f.getAbsolutePath().substring(f.getAbsolutePath().indexOf(fPath) + fPath.length());
		BackupFile bf2 = new BackupFile(f.getAbsolutePath(), true, "*.*", sPath);
		try { 
		    writeToFile(bf2);
		    this.FILE_COUNT++;
		} catch (IOException ex) {
		    Logger.getLogger(FileFinder.class.getName()).log(Level.SEVERE, null, ex);
		}
	    }
	    else
		getFiles(fPath, f, files, bPath, extensions, toPath);
	}
    }
    
    private File preparePath(File f) {
	String path = f.getAbsolutePath();
	/* I wish there was a better way to do all this */
	Date d = new Date();
	d.setTime(System.currentTimeMillis());
	path = path.replaceAll("%d", new SimpleDateFormat("dd-MM-yyyy").format(d))
		.replaceAll("%t", new SimpleDateFormat("HH-mm-ss").format(d));
	return new File(path);
    }
    
    private void writeToFile(BackupFile file) throws IOException {
	File f = new File(cfg.CONFIG_NAME+".flist");
	FileWriter fw = new FileWriter(f, true);
	fw.write(file.saveString()+"\n");
	fw.flush();
	fw.close();
    }

}

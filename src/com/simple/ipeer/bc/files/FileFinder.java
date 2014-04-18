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
import java.text.DecimalFormat;
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
    
    private MainGUI mGUI = null;
    private Config cfg;
    public volatile boolean CONTINUE_BACKUP = true;
    private int FILE_COUNT = 0;
    private long FILE_SIZE = 0L;
    
    private boolean nogui = false;
    
    public FileFinder(MainGUI gui) {
	this.mGUI = gui;
    }
    
    public FileFinder(boolean noGUI) {
	this.nogui = noGUI;
    }
    
    public void startBackup(String configName) {
	try {
	    this.cfg = Config.loadConfig(configName);
	    File f = new File(cfg.CONFIG_NAME+".flist");
	    if (f.exists())
		f.delete();
	    MainGUI g = null;
	    if (!this.nogui) {
		g = this.mGUI;
		g.jProgressBar1.setIndeterminate(true);
		g.jComboBox1.setEnabled(false);
		g.stopButton.setEnabled(true);
		g.createButton.setEnabled(false);
		g.statusText.setText("Generating file list...");
	    }
	    else { System.out.println("Generating file list..."); }
	    generateFileList();
	    if (!this.nogui)
		g.jProgressBar1.setIndeterminate(false);
	    if (!this.CONTINUE_BACKUP) {
		if (!this.nogui) {
		    g.statusText.setText("Cancelled by user.");
		    g.createButton.setEnabled(true);
		    g.stopButton.setEnabled(false);
		    g.jComboBox1.setEnabled(true);
		}
	    }
	    else
		if (!this.nogui)
		    g.statusText.setText(this.FILE_COUNT+" files ready for backup.");
		else
		    System.out.println(this.FILE_COUNT+" files to back up.");
	} catch (IOException ex) {
	    Logger.getLogger(FileFinder.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    public void performBackup() {
	MainGUI g = this.mGUI;
	if (!this.CONTINUE_BACKUP) { if (!this.nogui) { g.statusText.setText("Cancelled by user."); } return; }
	if (!this.nogui) {
	    g.statusText.setText("Backing up files...");
	    g.jProgressBar1.setMaximum(FILE_COUNT);
	    g.jProgressBar1.setIndeterminate(false);
	    g.jProgressBar1.setValue(0);
	    g.jProgressBar1.setStringPainted(true);
	}
	else
	    System.out.println("Backing up files...");
	try {
	    Scanner s = new Scanner(new File(cfg.CONFIG_NAME+".flist"));
	    int fileNum = 1;
	    long sizeCopied = 0L;
	    while (s.hasNext() && this.CONTINUE_BACKUP) {
		if (!this.nogui)
		    g.jProgressBar1.setString((g.jProgressBar1.getValue() + 1)+"/"+g.jProgressBar1.getMaximum());
		String[] line = s.nextLine().split("\\|");
		Path old = new File(line[0]).toPath();
		Path _new = new File(line[2]).toPath();
		File fs = old.toFile();
		sizeCopied += fs.length();
		File f = new File(_new.toFile().getPath());
		f.mkdirs();
		try {
		    Files.copy(old, _new, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) { if (cfg.FAILURE_METHOD == 1) { break; } }
		if (!this.nogui)
		    g.jProgressBar1.setValue(g.jProgressBar1.getValue() + 1);
		else
		    System.out.println("("+fileNum+++"/"+this.FILE_COUNT+", "+readableFileSize(sizeCopied)+" of "+readableFileSize(this.FILE_SIZE)+") "+f.getPath());
	    }
	} catch (FileNotFoundException ex) {	}
	if (!this.nogui) {
	    g.statusText.setText("Backup complete. Ready for next instruction.");
	    g.createButton.setEnabled(true);
	    g.stopButton.setEnabled(false);
	    g.jComboBox1.setEnabled(true);
	}
	else {
	    System.out.println("Backup Complate.");
	    System.exit(0);
	}
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
	    if (new File(bf.path()).isFile()) {
		files.add(bf);
	    }
	    else {
		getFiles(bf.path(), new File(bf.path()), files, bf.backupPath(), bf.extensions(), btPath);
	    }
	}
    }
    
    // Not my code
    public String readableFileSize(long size) {
	if(size <= 0) return "0";
	final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
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
		    this.FILE_SIZE += new File(bf2.path()).length();
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

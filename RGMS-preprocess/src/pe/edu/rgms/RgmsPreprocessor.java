package pe.edu.rgms;

import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import pe.edu.rgms.strategies.DefaultStrategy;
import pe.edu.rgms.strategies.GroovyStrategy;
import pe.edu.rgms.strategies.GspStrategy;
import pe.edu.rgms.strategies.ParseStrategy;

public class RgmsPreprocessor {

	private File rgmsRoot;

	private File newRgms;
	
	public static void main(String[] args) throws IOException {
		new RgmsPreprocessor().parse();
	}
	
	public void parse() {
		Properties prop = new Properties();

		try {
			File f = new File(".");
			File f2 = new File(f.getCanonicalPath()+"/conf.properties");
			prop.load(new FileInputStream(f2));

			String rgmsOriginPath = prop.getProperty("originPath");
			String rgmsNewPath = prop.getProperty("newPath");
			rgmsRoot = new File (rgmsOriginPath);
			newRgms = new File (rgmsNewPath);
			
			System.out.println("rgmsRoot="+rgmsRoot);
			System.out.println("rgmsNewPath="+rgmsNewPath);
			if (existFile (rgmsRoot) && existFile (newRgms)) {
				startProgressBar();
				createFolder(rgmsRoot);
				finishProgressBar();
			}

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Fatal Error: "+ ex.getMessage());
		}

	}
		
	private void finishProgressBar() {
		JOptionPane.showMessageDialog(null, "All Files copied.");
	}

	private void startProgressBar() {
		JOptionPane.showMessageDialog(null, "The system is going to start.");
	}

	public void createFolder (File orignalFile) throws IOException {
		if (!orignalFile.isDirectory())
			return;
		newFilePath(orignalFile).mkdirs();
		for (File innerFile : orignalFile.listFiles()) {
			if (innerFile.isFile()){
				if (innerFile.getName().endsWith(".groovy"))
					parseFile(innerFile, new GroovyStrategy());
				else if (innerFile.getName().endsWith(".gsp"))
					parseFile(innerFile, new GspStrategy());
				else parseFile(innerFile, new DefaultStrategy());
			} else {
				createFolder (innerFile);
			}
				
		}
		
	}
	
	public void parseFile (File orignalFile, ParseStrategy strategy) throws IOException {
		if (!existFile(orignalFile))
			return;
		File newFile = newFilePath(orignalFile);
		newFile.createNewFile();
		
		BufferedReader br = new BufferedReader(new FileReader(orignalFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
		String line;
		while ((line = br.readLine()) != null) {
			line = strategy.parseLine(line);
			bw.write(line);
			bw.newLine();
		}
		br.close();
		bw.close();
	}

	private File newFilePath(File orignalFile) {
		//String deltaPath = orignalFile.getAbsolutePath().replaceFirst(rgmsOriginPath, "");
		String deltaPath = rgmsRoot.toURI().relativize(orignalFile.toURI()).getPath();
		deltaPath = deltaPath.replaceAll("-", "");
		deltaPath = deltaPath.replaceAll("_", "");
		File newFile = new File (newRgms.toURI().getPath()+File.separator+ deltaPath);
		return newFile;
	}
	
	private boolean existFile(File f) throws HeadlessException, IOException {
		if (!f.exists()) {
			JOptionPane.showMessageDialog(null, "File does not exists: "+ f.getCanonicalPath());
			return false;
		}
		return true;
	}

}

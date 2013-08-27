package pe.edu.rgms;

import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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

	public void createFolder (File originalFile) throws IOException {
		if (!originalFile.isDirectory())
			return;
		newFilePath(originalFile).mkdirs();
		for (File innerFile : originalFile.listFiles()) {
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
	
	public void parseFile (File originalFile, ParseStrategy strategy) throws IOException {
		if (!existFile(originalFile))
			return;
		File newFile = newFilePath(originalFile);
		newFile.createNewFile();

        if (isBinary(originalFile)) {
            newFile.setExecutable(true);
            copyFileUsingStream(originalFile,newFile);
        } else {
            processFile(originalFile, strategy, newFile);
        }
	}

    private boolean isBinary(File originalFile) {
       String name = originalFile.getName();
       return name.endsWith(".exe") || name.endsWith(".out") || name.endsWith(".png");
    }

    private void processFile(File originalFile, ParseStrategy strategy, File newFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(originalFile));
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

    private File newFilePath(File originalFile) {
		//String deltaPath = originalFile.getAbsolutePath().replaceFirst(rgmsOriginPath, "");
		String deltaPath = rgmsRoot.toURI().relativize(originalFile.toURI()).getPath();
		if (!deltaPath.contains(".rng")) {
            deltaPath = deltaPath.replaceAll("-", "");
		    deltaPath = deltaPath.replaceAll("_", "");
        }
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

    // From http://www.journaldev.com/861/4-ways-to-copy-file-in-java
    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
}

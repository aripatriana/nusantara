package com.nusantara.automate.window;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;

import com.nusantara.automate.ContextLoader;
import com.nusantara.automate.DriverManager;
import com.nusantara.automate.io.FileImageIO;
import com.nusantara.automate.util.IDUtils;
import com.nusantara.automate.util.SimpleEntry;

public class WindowScreen {

	@Value("{tmp_dir}")
	private String tmpDir;
	
	@Value("{testcase_dir}")
	private String testCaseDir;
	
	@Value("generate.output.image")
	private String generateOutputImage;

	public final static int CAPTURE_CURRENT_WINDOW = 0;
	public final static int CAPTURE_FULL_WINDOW = 1;
	
	private final static int SNAPSHOT_TEMP = 0;
	private final static int SNAPSHOT_FINAL = 1;
	
	private Scrolling scrolling;
	private WebDriver webDriver;
	private String targetFolder;
	private String prefixFileName;
	
	public WindowScreen(WebDriver webDriver) {
		this.webDriver = webDriver;
		ContextLoader.setObject(this);
	}
	
	public String getTargetFolder() {
		return targetFolder;
	}
	
	public void setTargetFolder(String targetFolder) {
		this.targetFolder = targetFolder;
	}
	
	public String getPrefixFileName() {
		return prefixFileName;
	}
	
	public void setPrefixFileName(String prefixFileName) {
		this.prefixFileName = prefixFileName;
	}
	
	public boolean checkIsScrolling() {
		Integer scrollHeight = scrolling.getScrollHeight();
		Integer clientHeight = scrolling.getClientHeight();
		return !clientHeight.equals(scrollHeight);
	}
	
	public void setScrolling(Scrolling scrolling) {
		this.scrolling = scrolling;
	}
	
	private void capture(int captureType, Scrolling scrolling) throws IOException {
		if ("true".equalsIgnoreCase(generateOutputImage)) {
			setScrolling(scrolling);
			
			LinkedHashMap<String, SimpleEntry<PositionPixel, File>> images = new LinkedHashMap<String, SimpleEntry<PositionPixel, File>>();
			if (checkIsScrolling() && (captureType == 1)) {
				
				scrolling.scrollToDown();
				while (true) {
					int pixelRelative = getPixelRelative();
					File file = snapshot(WindowScreen.SNAPSHOT_TEMP);
					images.put(file.getName(), new SimpleEntry<PositionPixel, File>(scrolling.getPosition(), file));
					
					if (!scrolling.isPixelOrigin()) {
						scrolling.moveUp(pixelRelative);	
					} else {
						break;
					}				
				}
				
				putToFile(FileImageIO.combineImage(scrolling.getClientWidth(), scrolling.getScrollHeight(), scrolling.getClientHeight(), images));
			} else {
				snapshot(WindowScreen.SNAPSHOT_FINAL);
			}				
		}
	}
	
	public void capture(int captureType) throws IOException {
		capture(captureType, new WindowScrolling((JavascriptExecutor) webDriver));	
	}
	
	public void capture(int captureType, String elementId) throws IOException {
		capture(captureType, new ModalScrolling((JavascriptExecutor) webDriver, elementId));
	}

	public int getPixelRelative() {
		Integer clientHeight =  scrolling.getClientHeight();
		Integer scrollHeight = scrolling.getScrollHeight();
		PositionPixel position = scrolling.getPosition();
		int relative = scrollHeight/clientHeight;
		if (relative < 1) {
			relative = scrollHeight%clientHeight;
		} else {
			relative = position.getY()/clientHeight;
			if (relative < 1) {
				relative = position.getY()%clientHeight;
			} else {
				relative = 500;				
			}
		}
		return relative;
	}
	
	public File snapshot(int snapshotType) throws IOException {
		 TakesScreenshot scrShot =((TakesScreenshot)webDriver);
		 File sourceFile = scrShot.getScreenshotAs(OutputType.FILE);
		 if (snapshotType == WindowScreen.SNAPSHOT_TEMP) {
			 return putToTempFile(sourceFile);
		 } else {
			 return putToFile(sourceFile);	 
		 }
	}
	
	public File normalizeFile(File sourceFile) throws IOException {
		BufferedImage buf = FileImageIO.resizeImage(ImageIO.read(sourceFile), scrolling.getClientWidth(), scrolling.getClientHeight());
		ImageIO.write(buf, "png", sourceFile);
		return sourceFile;
	}
	
	public File putToTempFile(File sourceFile) throws IOException {
		sourceFile = normalizeFile(sourceFile);
		File destFile=new File(tmpDir + "\\" + IDUtils.getRandomId() + ".png");
		FileUtils.copyFile(sourceFile, destFile);
		return destFile;
	}
	
	public File putToFile(File sourceFile) throws IOException {
		sourceFile = normalizeFile(sourceFile);
		String targetFileName = testCaseDir + "\\";
		if (targetFolder != null)
			targetFileName += targetFolder + "\\";
		if (prefixFileName != null)
			targetFileName += prefixFileName + "_";
		targetFileName += IDUtils.getRandomId() + ".png";
		File destFile=new File(targetFileName);
		FileUtils.copyFile(sourceFile, destFile);
		return destFile;
	}
	
	public File putToFile(BufferedImage bufferedImg) throws IOException {
		String targetFileName = testCaseDir + "\\";
		if (targetFolder != null)
			targetFileName += targetFolder + "\\";
		if (prefixFileName != null)
			targetFileName += prefixFileName + "_";
		targetFileName += IDUtils.getRandomId() + ".png";
		File destFile=new File(targetFileName);
		ImageIO.write(bufferedImg, "png", destFile);
		return destFile;
	}
	
	public static void main(String[] args) throws IOException {
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put("{tmp_dir}", System.getProperty("user.dir") + "\\tmp");
		metadata.put("{log_dir}", System.getProperty("user.dir") + "\\log");
		metadata.put("{config_dir}", System.getProperty("user.dir") + "\\config");
		metadata.put("{keyfile_dir}", System.getProperty("user.dir") + "\\keyfile");
		metadata.put("{testcase_dir}", System.getProperty("user.dir") + "\\testcase");
		
		WebDriver wd = DriverManager.getDefaultDriver();
		wd.get("https://www.telkomsigma.co.id/");
		WindowScreen ws = new WindowScreen(wd);
		ContextLoader.setObjectWithCustom(ws, metadata);
		ws.capture(1);
	}
}

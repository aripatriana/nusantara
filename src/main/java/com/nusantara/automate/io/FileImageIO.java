package com.nusantara.automate.io;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.nusantara.automate.util.SimpleEntry;
import com.nusantara.automate.window.PositionPixel;

/**
 * The tools for customized image file
 * 
 * @author ari.patriana
 *
 */
public class FileImageIO {

	public static BufferedImage createBufferedImage(int width, int height) {
		BufferedImage result = new BufferedImage(
				width, height,
                BufferedImage.TYPE_INT_RGB);
		return result;
	}
	
	public static BufferedImage combineImage(int targetWidth, int targetHeight, int imageOrigHeight, LinkedHashMap<String, SimpleEntry<PositionPixel, File>> images) throws IOException {
		BufferedImage bufferedImg = createBufferedImage(targetWidth, targetHeight);
		Graphics graph = bufferedImg.getGraphics();
		int x = 0;
		for (Entry<String, SimpleEntry<PositionPixel, File>> image : images.entrySet()) {
			BufferedImage bi = javax.imageio.ImageIO.read(image.getValue().getValue());
			graph.drawImage(bi, x, image.getValue().getKey().getY(), null);
		}
		return bufferedImg;
	}
	
	public static BufferedImage resizeImage(BufferedImage image, int targetWidth, int targetHeight) {
		BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
	    Graphics2D graphics2D = resizedImage.createGraphics();
	    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	    graphics2D.drawImage(image, 0, 0, targetWidth, targetHeight, null);
	    graphics2D.dispose();
	    return resizedImage;		
	}
	
	public static void main(String[] args) throws IOException {
		BufferedImage im = javax.imageio.ImageIO.read(new File("D:\\result.png"));
		BufferedImage out = FileImageIO.resizeImage(im, 1000, 500);
		javax.imageio.ImageIO.write(out, "png", new File("D:\\result2.png"));
	}

}

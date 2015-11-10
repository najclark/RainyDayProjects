
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

public class NickUtils {
	public static Color getAvgColor(BufferedImage image) {
		int redSum = 0, greenSum = 0, blueSum = 0;
		int pixels = image.getHeight() * image.getWidth();
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				Color color = new Color(image.getRGB(x, y));
				redSum += color.getRed();
				greenSum += color.getGreen();
				blueSum += color.getBlue();
			}

		}
		redSum /= pixels;
		greenSum /= pixels;
		blueSum /= pixels;

		return new Color(redSum, greenSum, blueSum);
	}

	public static Image resizeImage(Image img, int w, int h) {
		BufferedImage resizedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = resizedImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, w, h, null);

		return resizedImage;
	}

	public static void saveImage(BufferedImage image, String fileName, String ext) {

		File file = new File(fileName + "." + ext);
		try {
			ImageIO.write(image, ext, file); // ignore returned boolean
		} catch (IOException e) {
			System.out.println("Write error for " + file.getPath() + ": " + e.getMessage());
		}
	}

	public static Image getImage(String path) {
		try {
			File file = new File(path);
			return ImageIO.read(file);
		} catch (IOException e) {
			System.out.println("Error getting image from: " + path);
			e.printStackTrace();
		}

		return null;
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		return bimage;
	}
	
	public static long etaMinutes(int cur, int total, long time){
		int eta = (int) (time * (total - cur));
		return TimeUnit.SECONDS.toMinutes(eta);
	}

}

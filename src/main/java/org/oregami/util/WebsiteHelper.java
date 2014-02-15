package org.oregami.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import com.mortennobel.imagescaling.ResampleOp;

public class WebsiteHelper {

	private String rasterizeLocation = null;//"/Users/sebastian/Downloads/phantomjs/rasterize.js";
	private String phantomjsLocation = null;//"/usr/local/bin/phantomjs";
	
	private static WebsiteHelper instance = null;
	
	public WebsiteHelper(String phantomjsLocation, String rasterizeLocation) {
		this.phantomjsLocation = phantomjsLocation;
		this.rasterizeLocation = rasterizeLocation;
	}
	
	public static void init(String phantomjsLocation, String rasterizeLocation) {
		instance = new WebsiteHelper(phantomjsLocation, rasterizeLocation);
	}
	
	public static WebsiteHelper instance() {
		if (instance==null) {
			throw new RuntimeException("WebsiteHelper must be initialized");
		}
		return instance;
	}
	
	public Map<String, String> createWebsite(String url, String size) throws IOException {
		
		if (!url.startsWith("http://www.oregami.org")) {
			throw new RuntimeException("url_not_allowed");
		}
		if (size==null) {
			size = " 1280px*1024px";
		}
		File temp = File.createTempFile("website-screenshot", ".png"); 
		
		String command = phantomjsLocation + " " + rasterizeLocation + " "
				+ url + " " + temp.getAbsolutePath() + size;
		
		System.out.println("Executing:\n" + command);

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		Map<String, String> ret = new TreeMap<String, String>();
		ret.put("output", output.toString());
//		ret.put("command", command);
//		ret.put("filename", temp.getAbsolutePath());
		
		return ret;

	}

	public byte[] resize(byte[] imageBytes, Integer targetWidth, Integer targetHeight)
			throws IOException {

		InputStream in = new ByteArrayInputStream(imageBytes);
		BufferedImage bImageFromConvert = ImageIO.read(in);
		
		if (targetHeight==null) {
			targetHeight = targetWidth * bImageFromConvert.getHeight() / bImageFromConvert.getWidth();
		}
		
		ResampleOp  resampleOp = new ResampleOp (targetWidth, targetHeight);
		BufferedImage rescaledImage = resampleOp.filter(bImageFromConvert, null);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(rescaledImage, "png", baos );
		baos.flush();
		byte[] rescaledImageInByte = baos.toByteArray();
		baos.close();
		
		return rescaledImageInByte;

	}
	

	public byte[] readFile(String filename) {

		File file = new File(filename);
		byte[] bFile = new byte[(int) file.length()];

		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			// convert file into array of bytes
			fileInputStream.read(bFile);
			fileInputStream.close();
			return bFile;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

}
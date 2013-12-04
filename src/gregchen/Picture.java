/*
 * Copyright 2013 Gregory M Chen
   This file is part of the project MosquitoSimulation.

    MosquitoSimulation is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MosquitoSimulation is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MosquitoSimulation.  If not, see <http://www.gnu.org/licenses/>.
 */


package gregchen;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

/**
 * Class for loading, resizing, tinting picture for backgrounds and spritesheets.
 * @author Greg
 *
 */
public class Picture
{
	/*
	private static HashMap<String, BufferedImage> bImageMap = new HashMap<String, BufferedImage>();
	private static HashMap<String, BufferedImage> bImageResizeMap = new HashMap<String, BufferedImage>();
	private static HashMap<String, BufferedImage> bImageTintMap = new HashMap<String, BufferedImage>();
	
	*/
	
	private static HashMap<String, BufferedImage> bImageMap = new HashMap<String, BufferedImage>();
	private static HashMap<String, Picture> picMap = new HashMap<String, Picture>();
	private static HashMap<String, Picture[]> spritesMap = new HashMap<String, Picture[]>();
	
	private BufferedImage pic;
	
	private String fileName;
	private Color color;
	private int spriteNumber = -1;
	private double imageHeight;
	
	/**
	 * Constructor for loading an image
	 * @param fileName
	 */
	public Picture(String fileName)
	{
		this.fileName = fileName;
		this.color = Color.WHITE;
		
		if(bImageMap.containsKey(fileName))
		{
			pic = bImageMap.get(fileName);
			this.imageHeight = pic.getHeight();
		}
		else
		{
			try
			{
				pic = ImageIO.read(this.getClass().getResourceAsStream(fileName));
				this.imageHeight = pic.getHeight();
				bImageMap.put(fileName, this.pic);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Constructor for loading an image and setting its fields. Does not actually alter the image.
	 * @param fileName
	 * @param color
	 * @param spriteNumber
	 * @param imageHeight
	 * @param pic
	 */
	public Picture(String fileName, Color color,  int spriteNumber, double imageHeight, BufferedImage pic)
	{
		this.fileName = fileName;
		this.pic = pic;
		this.spriteNumber = spriteNumber;
		this.color = color;
		this.imageHeight = imageHeight;
	}
	
	public int getWidth()
	{
		return pic.getWidth();
	}
	public int getHeight()
	{
		return pic.getHeight();
	}
	public void draw(Graphics g, Position p)
	{
		g.drawImage(pic, (int)p.x, (int)p.y, null);
	}
	/**
	 * Gets a reized image based on a given new height
	 * @param newHeight The new height
	 * @return The resized Picture
	 */
	public Picture getResized(double newHeight)
	{
		double scaleFactor = (double)newHeight/pic.getHeight();
		if(bImageMap.containsKey(fileName + color.getRGB() + spriteNumber + (int)(100*newHeight)))
		{
			return picMap.get(fileName + color.getRGB() + spriteNumber + (int)(100*newHeight));
		}
		
		AffineTransform tx = new AffineTransform();
		tx.scale(scaleFactor, scaleFactor);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		BufferedImage newPic = op.filter(pic, null);
		Picture pict = new Picture(fileName, color, spriteNumber, newHeight, newPic); 
		picMap.put(fileName + color.getRGB() + "" + imageHeight + "" + spriteNumber, pict);
	
		return pict;
	}
	/**
	 * Splits the image into sprite pieces using the width and the given number of sprites in the sheet
	 * (determined by the constructor)
	 * @return
	 */
	public Picture[] getPieces()
	{		
		if(spritesMap.containsKey(fileName + color.getRGB() + (int)(100 * imageHeight)))
		{
			return spritesMap.get(fileName + color.getRGB() + (int)(100 * imageHeight)).clone();
		}
		
		BufferedImage picCopy = getDeepCopy(pic);
		int numPieces = picCopy.getWidth()/picCopy.getHeight();
		Picture[] pieces = new Picture[numPieces];
		
		for(int i = 0; i < numPieces; i++)
		{
			pieces[i] = new Picture(this.fileName, this.color, i, this.imageHeight, picCopy.getSubimage(i * picCopy.getWidth()/numPieces, 0, picCopy.getWidth() / numPieces, picCopy.getHeight()));
		}
		spritesMap.put(fileName + color.getRGB() + (int)(100 * imageHeight), pieces);
		
		return pieces;
	}
	/**
	 * Returns a tinted image
	 * @param color The new color
	 * @return The tinted Picture
	 */
	public Picture getTinted(Color color) 
	{
		
		if(bImageMap.containsKey(fileName + color.getRGB() + spriteNumber + (int)(100 * imageHeight)))
		{
			return new Picture(fileName, color, spriteNumber, imageHeight, bImageMap.get(fileName + color.getRGB() + spriteNumber + (int)(100 * imageHeight)));
		}
		BufferedImage newPic = getDeepCopy(pic);
		for(int y = 0; y < this.getHeight(); y++)
		{
			for(int x = 0; x < this.getWidth(); x++)
			{
				int rgbColor = pic.getRGB(x, y);
				Color prevColor = new Color(rgbColor);
				
				int alpha = (rgbColor>>24) & 0xff;
				
				float[] hsbColor = Color.RGBtoHSB(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), null);
				float hue = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[0];
				int rgb = Color.HSBtoRGB(hue, hsbColor[1], hsbColor[2]);
				Color newColor = new Color(rgb);
				newColor = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), alpha);
				newPic.setRGB(x, y, newColor.getRGB());
			}
		}
		bImageMap.put(fileName + color.getRGB() + spriteNumber + (int)(100 * imageHeight), newPic);
		
		return new Picture(fileName, color, spriteNumber, this.imageHeight, newPic);
	}
	
	@Override
	public String toString()
	{
		return "FileName: " + fileName + " RGB Color: " + color.getRGB() + " Image Height: " + imageHeight + " Sprite Number: " + spriteNumber;
	}
	
	private BufferedImage getDeepCopy(BufferedImage image) //NOTE: does not yet implement alpha channel
	{
		BufferedImage newBImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		for(int y = 0; y < image.getHeight(); y++)
		{
			for(int x = 0; x < image.getWidth(); x++)
			{
				newBImage.setRGB(x, y, image.getRGB(x, y));
			}
		}
		return newBImage;
	}
}

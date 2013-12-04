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
import java.awt.Graphics2D;
import java.util.Random;


public class SpriteSheet 
{
	private Picture[] sprites;
	private final int SPRITE_CHANGE_DELAY = 3;
	private int currentSprite = 0;
	private int spriteCounter = 0;
	private int spriteCounter2 = 0;
	
	//assumes the picture is of size n by kn
	public SpriteSheet(String fileName, double radius)
	{
		Picture pic = new Picture(fileName);
		pic = pic.getResized(2 * radius);
		sprites = pic.getPieces();
		
		randomizeSpriteCounter();
	}
	
	protected void randomizeSpriteCounter()
	{
		Random random = new Random();
		currentSprite = random.nextInt(sprites.length);
		spriteCounter = random.nextInt(SPRITE_CHANGE_DELAY);
	}
	
	public void tint(Color color)
	{
		for(int i = 0; i < sprites.length; i++)
		{
			sprites[i] = sprites[i].getTinted(color);
		}
	}
	
	
	public void update()
	{
		spriteCounter++;
		if(spriteCounter > SPRITE_CHANGE_DELAY)
		{
			if( currentSprite != 3 || spriteCounter2 > -1)//SPRITE_CHANGE_DELAY)
			{
				currentSprite = (currentSprite + 1) % sprites.length;
				spriteCounter2 = 0;
				spriteCounter = 0;
			}
			else
			{
				spriteCounter2++;
			}
			
		}
	}
	
	public void drawCurrentSprite(Graphics g, Position p)
	{
		sprites[currentSprite].draw(g, p);
	}
}

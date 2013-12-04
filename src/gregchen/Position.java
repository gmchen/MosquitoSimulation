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
import java.awt.Point;
import java.util.Random;

/**
 * Class for custom Position object with a few helpful methods
 * @author Greg
 *
 */
public class Position 
{
	public double x;
	public double y;
	private static Random random = new Random();
	
	public Position() {}
	
	public Position(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	public double getDistance(Position otherPosition)
	{
		return Math.sqrt(
				(x - otherPosition.x) * (x - otherPosition.x)
				+ (y - otherPosition.y) * (y - otherPosition.y));
	}
	public double getDistance(double otherX, double otherY)
	{
		return Math.sqrt(
				(x - otherX) * (x - otherX)
				+ (y - otherY) * (y - otherY));
	}
	public Point toPoint()
	{
		return new Point((int)x, (int)y);
	}
	public double getDirectionTo(Position otherPos)
	{
		double deltaY = otherPos.y - y;
		if(x <= otherPos.x)
		{
			return wrapAngle(Math.asin(deltaY/getDistance(otherPos)));
		}
		else
		{
			return wrapAngle(Math.PI - Math.asin(deltaY/getDistance(otherPos)));
		}
	}
	private double wrapAngle(double angle)
	{
		if(angle >= 0 && angle <= 2*Math.PI)
		{
			return angle;
		}
		if(angle < 0)
		{
			return wrapAngle(angle + 2*Math.PI);
		}
		return wrapAngle(angle - 2*Math.PI);
	}
	
	public static Position getRandomPosition(double xMax, double yMax)
	{
		return getRandomPosition(0, 0, xMax, yMax);
	}
	public static Position getRandomPosition(double xMin, double yMin, double xMax, double yMax)
	{
		return new Position(xMin + random.nextDouble() * (xMax - xMin), yMin + random.nextDouble() * (yMax - yMin));
	}
}

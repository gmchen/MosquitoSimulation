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
import java.util.Random;

/**
 * 
 * A class containing static methods for some common calculations
 * @author Greg
 */
public class Calc 
{
	static Random random = new Random();
	
	/**
	 * Takes any angle in radians and returns a value between (inclusive) 0 and 2pi
	 * @param angle in radians
	 * @return angle between (inclusive) 0 and 2pi
	 */
	public static double wrapAngle(double angle)
	{
		if(angle >= 0 && angle <= 2*Math.PI)
		{
			return angle;
		}
		if(angle < 0)
		{
			//Unnecessary, but I like recursion
			return wrapAngle(angle + 2*Math.PI);
		}
		return wrapAngle(angle - 2*Math.PI);
	}
	
	/**
	 * Calculates the component of a given velocity projected onto a different direction
	 * @param direction	the given velocity angle in radians
	 * @param speed		the given velocity magnitude 
	 * @param angle		the new angle (being projected onto)
	 * @return the magnitude of the projection
	 */
	public static double getVelocityMagnitude(double direction, double speed, double angle)
	{
		angle = Calc.wrapAngle(angle);
		direction = Calc.wrapAngle(direction);
		double innerAngle;
		if(direction > angle)
		{
			innerAngle = direction - angle;
		}
		else
		{
			innerAngle = angle - direction;
		}
		if(innerAngle > 180)
		{
			innerAngle = 360 - innerAngle;
		}
		
		return speed * Math.cos(innerAngle);
	}
	
	/**
	 * Makes a random boolean decision given a probability
	 * @param probability probability between (inclusive) 0 and 1
	 * @return true or false based on this probability
	 */
	public static boolean randomOutcome(double probability)
	{
		if(random.nextDouble() < probability)
		{
			return true;
		}
		return false;
	}
}

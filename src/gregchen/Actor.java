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
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
/**
 * 
 * A class representing a generic circular actor in the simulation.
 * The actor is responsible for keeping track properties such as
 * its Picture, age, location, speed, colour, and whether or not it is alive.
 * The Simulation object should call each actor's update and draw methods each frame. 
 * It can be inherited to define mosquitoes, humans, etc.
 * 
 * @author Greg
 */
public class Actor 
{
	private static int tagIndex = 0;
	public final static String CIRCLE_FILE_LOCATION = "/images/Circles.png";
	public static final double DEFAULT_SPEED = 5;
	private Random random = new Random();
	private Position location;
	private double direction;
	private double speed = 5;
	private double radius;
	private int tag;
	private Color color;
	private boolean isAlive = true;
	private SpriteSheet sprites;
	private double age = 0; //Increments every 30 frames.
	private Position prevDrawPosition;
	private double boundRadius = 30;
	private Position birthLocation;
	
	/**
	 * The 'standard' constructor for the object
	 * @param radius Radius of the circle on screen
	 * @param birthLocation Initial location of the actor
	 * @param speed Number of pixel distances to move per update
	 * @param color Color to display
	 */
	public Actor(double radius, Position birthLocation, double speed, Color color)
	{
		this.radius = radius;
		this.birthLocation = birthLocation;
		Random random = new Random();
		double theta = 2 * Math.PI * random.nextDouble();
		double r = 30 * random.nextDouble();
		this.location = new Position(birthLocation.x + r * Math.cos(theta), birthLocation.y + r * Math.sin(theta));
		sprites = new SpriteSheet(CIRCLE_FILE_LOCATION, radius);
		this.setColor(color); 
		this.speed = speed;
		randomizeDirection();
		tag = tagIndex;
		tagIndex++;
	}
	/**
	 * Default constructor for the object (rarely used)
	 */
	public Actor()
	{
		tag = tagIndex;
		tagIndex++;
	}
	/**
	 * Sets a random direction
	 */
	private void randomizeDirection()
	{
		setDirection(2*Math.PI * random.nextDouble());
	}
	/**
	 * Moves the actor using its given speed and direction
	 */
	public void move()
	{
		move(speed, getDirection());
	}
	/**
	 * Moves the actor using a custom speed and direction
	 * @param speed Pixel lengths to move
	 * @param direction Angle in radians
	 */
	protected void move(double speed, double direction)
	{
		location.x += speed * Math.cos(direction);
		location.y += speed * Math.sin(direction);
	}
	
	/**
	 * Get the magnitude of this actor's velocity projected
	 * onto another angle
	 * @param angle That other angle
	 * @return
	 */
	private double getVelocityMagnitude(double angle)
	{
		return Calc.getVelocityMagnitude(direction, speed, angle);
	}
	
	/**
	 * Sets two actors' velocities after an elastic collision
	 * which conserves momentum
	 * @param a One actor
	 * @param b The other actor
	 */
	public static void bounce(Actor a, Actor b)
	{
		separate(a, b);
		double aContactAngle = a.getLocation().getDirectionTo(b.getLocation());
		double bContactAngle = b.getLocation().getDirectionTo(a.getLocation());
		double aMagnitude = a.getVelocityMagnitude(aContactAngle);
		double bMagnitude = b.getVelocityMagnitude(bContactAngle);
		
		double aXVelocity = aMagnitude * Math.cos(aContactAngle);
		double aYVelocity = aMagnitude * Math.sin(aContactAngle);
		double bXVelocity = bMagnitude * Math.cos(bContactAngle);
		double bYVelocity = bMagnitude * Math.sin(bContactAngle);
		
		double aMass = Math.pow(a.radius, 2); //set exponent to 3 for
		double bMass = Math.pow(b.radius, 2); //3D proportionality
		
		double aX = (aXVelocity*(aMass - bMass) + 2*bMass*bXVelocity)/(aMass + bMass);
		double aY = (aYVelocity*(aMass - bMass) + 2*bMass*bYVelocity)/(aMass + bMass);
		double bX = (bXVelocity*(bMass - aMass) + 2*aMass*aXVelocity)/(bMass + aMass);
		double bY = (bYVelocity*(bMass - aMass) + 2*aMass*aYVelocity)/(bMass + aMass);
		
		//calculate and add orthogonal component
		double aOrthAngle = Calc.wrapAngle(aContactAngle + Math.PI/2);
		double bOrthAngle = Calc.wrapAngle(bContactAngle + Math.PI/2);
		double aOrthMag = a.getVelocityMagnitude(aOrthAngle);
		double bOrthMag = b.getVelocityMagnitude(bOrthAngle);
		
		double aXOrthVelocity = aOrthMag * Math.cos(aOrthAngle);
		double aYOrthVelocity = aOrthMag * Math.sin(aOrthAngle);
		double bXOrthVelocity = bOrthMag * Math.cos(bOrthAngle);
		double bYOrthVelocity = bOrthMag * Math.sin(bOrthAngle);
		
		aX += aXOrthVelocity;
		aY += aYOrthVelocity;
		bX += bXOrthVelocity;
		bY += bYOrthVelocity;
	
		
		a.setDirection(Math.atan2(aY, aX));
		b.setDirection(Math.atan2(bY, bX));
		a.setSpeed(Math.sqrt(aX*aX + aY*aY));
		b.setSpeed(Math.sqrt(bX*bX + bY*bY));
	}
	/**
	 * Determines whether this actor is touching another
	 * @param otherActor The other actor
	 * @return True or false depending on whether they are touching
	 */
	public boolean isTouching(Actor otherActor)
	{
		if(this.location.getDistance(otherActor.getLocation()) < this.radius + otherActor.radius)
		{
			return true;
		}
		return false;
	}
	/**
	 * Sets this actor's age
	 * @param age The age
	 */
	public void setAge(double age)
	{
		this.age = age;
	}
	/**
	 * Moves the two actors apart if they are overlapping
	 * @param a One actor
	 * @param b The other actor
	 */
	public static void separate(Actor a, Actor b)
	{
		while(a.location.getDistance(b.location) < a.radius + b.radius)
		{
			a.move(0.5, Math.PI + a.location.getDirectionTo(b.location));
			b.move(0.5, Math.PI + b.location.getDirectionTo(a.location));
		}
	}
	/**
	 * Sets the isAlive field to false
	 */
	public void kill()
	{
		isAlive = false;
	}
	
	//getters and setters
	
	/**
	 * @return isAlive Whether or not the actor is alive
	 */
	public boolean isAlive()
	{
		return isAlive;
	}
	/**
	 * @param newColor The new color
	 */
	public void setColor(Color newColor)
	{
		if(!newColor.equals(color))
		{
			color = newColor;
			sprites.tint(color);
		}
	}
	/**
	 * @param newSprites The new sprites to set
	 */
	public void setSprites(SpriteSheet newSprites)
	{
		sprites = newSprites;
	}
	/**
	 * @return The current top-left corner to be drawn
	 */
	private Position getDrawLocation()
	{
		return new Position(location.x - radius, location.y - radius);
	}
	/**
	 * @return The previous top-left corner drawn
	 */
	public Position getPrevDrawPosition()
	{
		return prevDrawPosition;
	}
	/**
	 * @return The radius
	 */
	public double getRadius() {return radius;}
	/**
	 * @return The birth location
	 */
	public Position getBirthLocation() {return birthLocation;}
	/**
	 * @param p New location
	 */
	public void setLocation(Position p)
	{
		this.location = p;
	}
	/**
	 * @param p New birth location
	 */
	public void setBirthLocation(Position p)
	{
		this.birthLocation = p;
	}
	/**
	 * @param newRadius The new radius
	 */
	public void setRadius(double newRadius)
	{
		radius = newRadius;
	}
	/**
	 * @return The speed
	 */
	public double getSpeed() {return speed;}
	/**
	 * @param newSpeed The new speed
	 */
	public void setSpeed(double newSpeed)
	{
		speed = newSpeed;
	}
	/**
	 * @return The current centre location
	 */
	public Position getLocation() {return location;}
	/**
	 * @param direction The new direction
	 */
	public void setDirection(double direction) {
		this.direction = direction;
	}
	/**
	 * @return The current direction
	 */
	public double getDirection() {
		return direction;
	}
	/**
	 * Moves the actor away from a wall it is overlapping/passing, then
	 * sets its velocity to bounce elastically
	 * @param width The width of the simulation
	 * @param height The height of the simulation
	 */
	public void bounceWalls(int width, int height) 
	{
		if(getLocation().x - getRadius() < 0)
		{
			while(getLocation().x - getRadius() < 0)
			{
				move(1, 0);
			}
			setDirection(Math.PI - getDirection());
		}
		if(getLocation().x + getRadius() > width)
		{
			while(getLocation().x + getRadius() > width)
			{
				move(1, Math.PI);
			}
			setDirection(Math.PI - getDirection());
		}
		if(getLocation().y - getRadius() < 0)
		{
			while(getLocation().y - getRadius() < 0)
			{
				move(1, Math.PI/2);
			}
			setDirection(2*Math.PI - getDirection());
		}
		if(getLocation().y + getRadius() > height)
		{
			while(getLocation().y + getRadius() > height)
			{
				move(1, 3*Math.PI/2);
			}
			setDirection(2*Math.PI - getDirection());
		}
	}
	/**
	 * Bounces if the actor touches/passes the boundary of how far from its birth location it
	 * is permitted to travel
	 * @param boundRadius The radius of the max distance from its birth location it can travel
	 */
	public void bounceWalls(double boundRadius) 
	{
		/*
		if(getLocation().x < birthLocation.x - bounceRadius || getLocation().x > birthLocation.x + bounceRadius)
		{
			setDirection(Math.PI - getDirection());
			this.randomizeDirection();
		}
		if(getLocation().y < birthLocation.y - bounceRadius || getLocation().y > birthLocation.y + bounceRadius)
		{
			setDirection(2*Math.PI - getDirection());
			this.randomizeDirection();
		}
		*/
		if(this.location.getDistance(this.birthLocation) > boundRadius - 2*this.radius)
		{
			while(location.getDistance(this.birthLocation) > boundRadius - 2 * this.radius)
			{
				this.move(1, this.getLocation().getDirectionTo(birthLocation));
			}
			double angle = this.getLocation().getDirectionTo(this.birthLocation);
			Random random = new Random();
			angle +=  Math.PI/2 - random.nextDouble() * Math.PI;
			setDirection(angle);
			
			/*
			double contactAngle = birthLocation.getDirectionTo(this.getLocation());
			double aMagnitude = getVelocityMagnitude(contactAngle);
			
			double aXVelocity = aMagnitude * Math.cos(contactAngle);
			double aYVelocity = aMagnitude * Math.sin(contactAngle);
			
			//calculate and add orthogonal component
			double aOrthAngle = Calc.wrapAngle(contactAngle + Math.PI/2);
			double aOrthMag = getVelocityMagnitude(aOrthAngle);
			
			double aXOrthVelocity = aOrthMag * Math.cos(aOrthAngle);
			double aYOrthVelocity = aOrthMag * Math.sin(aOrthAngle);
			
			double aX = -aXVelocity + aXOrthVelocity;
			double aY = -aYVelocity + aYOrthVelocity;
			
			
			setDirection(Math.atan2(aY, aX));
			*/
			//setSpeed(Math.sqrt(aX*aX + aY*aY));
		}
	}
	/**
	 * Updates the actor based on a 30fps simulation
	 */
	public void update()
	{
		this.move();
		this.age += 1/30d; //each frame increment age by 1/30 of a day
		this.boundRadius = 33 * age + 30;
		bounceWalls(boundRadius);
		this.sprites.update();
		
	}
	/**
	 * Draws the circle representing the actor
	 * @param g The graphics object to draw on
	 * @param interpolation The linear interpolation value
	 * @param insets The 'margins' of the graphics object
	 */
	public void draw(Graphics g, double interpolation, Insets insets) 
	{
		Position drawPosition = new Position(this.getDrawLocation().x + speed* interpolation *Math.cos(this.direction) + insets.left,
				this.getDrawLocation().y + speed * interpolation * Math.sin(this.direction) + insets.top);
		
		sprites.drawCurrentSprite(g, drawPosition);
		prevDrawPosition = drawPosition;
		//g.drawOval((int)(birthLocation.x - boundRadius) + insets.left, (int)(birthLocation.y - boundRadius) + insets.top, (int)(2*boundRadius), (int)(2*boundRadius));
	}
	/**
	 * @return The age
	 */
	public double getAge()
	{
		return age;
	}
	/**
	 * @return
	 */
	public double getBoundRadius()
	{
		return this.boundRadius;
	}
	//returns an array of colliding pairs of actors, where second index of 0 and 1 are used for colliding actors.
	/**
	 * Returns a hashmap of the colliding actors using a quadtree
	 * @param actors Actors considered
	 * @param width Width of the area considered
	 * @param height Height of the area considered
	 * @return A hashmap of colliding actors
	 */
	public static HashMap<Actor, Actor> getCollidingActors(ArrayList<Actor> actors, int width, int height)
	{		
		HashMap<Actor, Actor> colliders = getCollidingActors(actors, width, height, 0, 0);
		return colliders;
	}
	
	//recursive helper method
	private static HashMap<Actor, Actor> getCollidingActors(ArrayList<Actor> actors, double width, double height, double x, double y)
	{
		//Node is a leaf if there are 2 or less actors, or if height is smaller than my arbitrary limit of 3* radius
		if(actors.size() <= 5 || height < 3 * actors.get(0).radius)
		{
			return getCollidingActors(actors);
		}
		
		//I define branch 1, 2, 3, 4 to be the top left, top right, bottom left, bottom right quadrants respectively
		ArrayList<Actor> branch1Actors = new ArrayList<Actor>();
		ArrayList<Actor> branch2Actors = new ArrayList<Actor>();
		ArrayList<Actor> branch3Actors = new ArrayList<Actor>();
		ArrayList<Actor> branch4Actors = new ArrayList<Actor>();
		
		int numActorsInRect = 0;
		
		//asign actors to branches
		for(Actor a:actors)
		{
			if(isInRect(a, width/2, height/2, x, y))
			{
				branch1Actors.add(a);
			}
			if(isInRect(a, width/2, height/2, x + width/2, y))
			{
				branch2Actors.add(a);
			}
			if(isInRect(a, width/2, height/2, x, y + height/2))
			{
				branch3Actors.add(a);
			}
			if(isInRect(a, width/2, height/2, x + width/2, y + height/2))
			{
				branch4Actors.add(a);
			}
		}
		
		HashMap<Actor, Actor> colliders = new HashMap();
		colliders.putAll(getCollidingActors(branch1Actors, width/2, height/2, x, y));
		colliders.putAll(getCollidingActors(branch2Actors, width/2, height/2, x + width/2, y));
		colliders.putAll(getCollidingActors(branch3Actors, width/2, height/2, x, y + height/2));
		colliders.putAll(getCollidingActors(branch4Actors, width/2, height/2, x + width/2, y + height/2));
		
		return colliders;
	}
	
	private static HashMap<Actor, Actor> getCollidingActors(ArrayList<Actor> actors)
	{
		HashMap<Actor, Actor> colliders = new HashMap();
		for(int i = 0; i < actors.size(); i++)
		{
			for(int j = 0; j < actors.size(); j++)
			{
				if(actors.get(i).getTag() > actors.get(j).getTag())
				{
					if(actors.get(i).isTouching(actors.get(j)))
					{
						colliders.put(actors.get(i), actors.get(j));
						
					}
				}
			}
		}
		return colliders;
	}
	/**
	 * Helper method to determine if actor is in a rectangle
	 * @param a The actor
	 * @param width Width of the rectangle
	 * @param height Height of the rectangle
	 * @param x x location of the rectangle
	 * @param y y location of the rectangle
	 * @return True if and only if the actor is inside the rectangle
	 */
	private static boolean isInRect(Actor a, double width, double height, double x, double y)
	{
		double r = a.getRadius();
		
		if(a.getLocation().x > x - r && a.getLocation().x < x + width + r
				&& a.getLocation().y > y - r && a.getLocation().y < y + height + r)
		{
			return true;
		}
		return false;
	}
	/**
	 * @return The actor's tag
	 */
	public int getTag()
	{
		return tag;
	}
}

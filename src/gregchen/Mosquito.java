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
import java.util.Random;
import java.util.Scanner;

/**
 * A class defining mosquito objects in the simulation which track
 * variables such as gender, genotype, number of times mated, time since
 * last mating (in order to simulate a refractory period), etc. An exponential decay
 * model of mortality has been shown in other mathematical models to be
 * sufficient to simulate death
 * @author Greg
 *
 */
public class Mosquito extends Actor
{
	public static double MORTALITY_PROB = 0.005402651;//assumes 0.85 //for 0.75, use 0.009543571;
	public static final double DAYS_UNTIL_FERTILE_AFTER_OVIPOS = 3.0;
	public static final double NUM_TIMES_MALE_CAN_MATE = 4.5;
	public static final double NUM_TIMES_FEMALE_CAN_MATE = 8.0;
	public static final double NUM_DAYS_UNTIL_EGG_DEPOSIT = 2;
	public static final int NUM_EGGS_PER_OVIPOS = 63; //Otero & Solari
	
	public static final double MALE_DAYS_UNTIL_FERTILE = 1.0;
	public static final double FEMALE_DAYS_UNTIL_FERTILE = 2.5; //Observations of...
	
	public static final String DOT_CIRCLE_FILE_LOCATION = "/images/DotCircles.png";
	
	private double daysSinceOvipos = 3.5;
	private int numTimesMated = 0;
	private Gender gender;
	private Genotype genotype;
	private Random random = new Random();
	private double numDaysCarryingEggs = 0;
	private boolean carryingEggs = false;
	private Genotype genotypeMate;
	
	/**
	 * Constructor given a saved state
	 * @param info The save state
	 */
	public Mosquito(String info)
	{
		super();
		int cursor = 0;
		double radius = Double.parseDouble(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		this.setRadius(radius);
		double speed = Double.parseDouble(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		this.setSpeed(speed);
		double x = Double.parseDouble(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		double y = Double.parseDouble(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		this.setLocation(new Position(x, y));
		double x2 = Double.parseDouble(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		double y2 = Double.parseDouble(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		this.setBirthLocation(new Position(x2, y2));
		double age = Double.parseDouble(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		this.setAge(age);
		int genderNumber = Integer.parseInt(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		int genotypeNumber = Integer.parseInt(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		double direction = Double.parseDouble(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		this.setDirection(direction);
		this.daysSinceOvipos = Double.parseDouble(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		this.numTimesMated = Integer.parseInt(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		this.numDaysCarryingEggs = Double.parseDouble(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		this.carryingEggs = Boolean.parseBoolean(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		int genotypeMateNumber = Integer.parseInt(info.substring(cursor));
		cursor = info.indexOf(' ', cursor) + 1;
		
		
		switch(genderNumber)
		{
		case 0:
			this.gender = Gender.MALE; break;
		case 1:
			this.gender = Gender.FEMALE; break;
		}
		switch(genotypeNumber)
		{
		case 0:
			this.genotype = Genotype.ff; break;
		case 1:
			this.genotype = Genotype.Ff; break;
		case 2:
			this.genotype = Genotype.FF; break;
		}
		switch(genotypeMateNumber)
		{
		case 0:
			this.genotypeMate = Genotype.ff; break;
		case 1:
			this.genotypeMate = Genotype.Ff; break;
		case 2:
			this.genotypeMate = Genotype.FF; break;
		}
		if(this.gender == Gender.MALE)
		{
			this.setSprites(new SpriteSheet(Actor.CIRCLE_FILE_LOCATION, this.getRadius()));
		}
		else
		{
			this.setSprites(new SpriteSheet(Mosquito.DOT_CIRCLE_FILE_LOCATION, this.getRadius()));
		}
		if(genotype == Genotype.FF)
		{
			this.setColor(Color.RED);
		}
		if(genotype == Genotype.ff)
		{
			this.setColor(Color.GREEN);
		}
		else if(genotype == Genotype.Ff)
		{
			this.setColor(Color.MAGENTA);
		}
	}
	
	/**
	 * The 'standard' Mosquito constructor
	 * @param radius
	 * @param birthLocation
	 * @param speed
	 * @param gender Either male or female enum value
	 * @param genotype FF, Ff, or ff enum value
	 */
	public Mosquito(double radius, Position birthLocation, double speed, Gender gender, Genotype genotype)
	{
		super(radius, birthLocation, speed, Color.WHITE);
		this.genotype = genotype;
		this.gender = gender;
		
		if(this.gender == Gender.FEMALE)
		{
			this.setSprites(new SpriteSheet(Mosquito.DOT_CIRCLE_FILE_LOCATION, this.getRadius()));
		}
		if(genotype == Genotype.ff)
		{
			this.setColor(Color.GREEN);
		}
		else if(genotype == Genotype.Ff)
		{
			this.setColor(Color.MAGENTA);
		}
		else if(genotype == Genotype.FF)
		{
			this.setColor(Color.RED);
		}
	}
	/**
	 * The 'standard' Mosquito constructor
	 * @param radius
	 * @param location
	 * @param birthLocation
	 * @param speed
	 * @param gender Either male or female enum value
	 * @param genotype FF, Ff, or ff enum value
	 */
	public Mosquito(double radius, Position location, Position birthLocation, double speed, Gender gender, Genotype genotype)
	{
		this(radius, birthLocation, speed, gender, genotype);
		
		this.setLocation(location);
	}
	
	@Override
	public void update()
	{
		super.update();
		if(random.nextDouble() < MORTALITY_PROB)
		{
			this.kill();
		}
		if(gender == Gender.FEMALE)
		{
			daysSinceOvipos += 1/30d;
			if(carryingEggs)
			{
				numDaysCarryingEggs += 1/30d;
			}
		}
	}
	/**
	 * Gets the save state
	 * @return a string representation of the save state
	 */
	public String getInfo()
	{
		int genderNumber = 0;
		int genotypeNumber = 0;
		int genotypeMateNumber = 0;
		if(this.gender == Gender.FEMALE)
		{
			genderNumber = 1;
		}
		if(genotype == Genotype.Ff)
		{
			genotypeNumber = 1;
		}
		else if(genotype == Genotype.FF)
		{
			genotypeNumber = 2;
		}
		if(genotype == Genotype.Ff)
		{
			genotypeMateNumber = 1;
		}
		else if(genotype == Genotype.FF)
		{
			genotypeMateNumber = 2;
		}
		return "" + this.getRadius() + " " + this.getSpeed() + " " + this.getLocation().x + " " 
				+ this.getLocation().y + " " +this.getBirthLocation().x 
				+ " " +this.getBirthLocation().y + " " + this.getAge()
				+ " " + genderNumber + " " + genotypeNumber + " " + this.getDirection()
				+ " " + daysSinceOvipos + " " + numTimesMated + " " + numDaysCarryingEggs
				+ " " + carryingEggs + " " + genotypeMateNumber;
	}
	public void removeEggs()
	{
		this.carryingEggs = false;
		numDaysCarryingEggs = 0;
		daysSinceOvipos = 0;
	}
	public boolean isFertile()
	{
		if(gender == Gender.FEMALE)
		{
			if(getDaysSinceOvipos() > Mosquito.DAYS_UNTIL_FERTILE_AFTER_OVIPOS
					&& !carryingEggs()
					&& this.getAge() > FEMALE_DAYS_UNTIL_FERTILE
					&& this.numTimesMated < Mosquito.NUM_TIMES_FEMALE_CAN_MATE)
			{
				return true;
			}
		}
		else
		{
			if(getNumTimesMated() < Mosquito.NUM_TIMES_MALE_CAN_MATE
					&& this.getAge() > MALE_DAYS_UNTIL_FERTILE)
			{
				return true;
			}
		}
		return false;
	}
	public double getNumDaysCarryingEggs()
	{
		return numDaysCarryingEggs;
	}
	public Genotype getGenotypeMate()
	{
		return genotypeMate;
	}
	public boolean carryingEggs()
	{
		return carryingEggs;
	}
	public double getDaysSinceOvipos()
	{
		return daysSinceOvipos;
	}
	public int numTimesMated()
	{
		return numTimesMated;
	}
	public Genotype getGenotype()
	{
		return genotype;
	}
	public Gender getGender()
	{
		return gender;
	}
	public int getNumTimesMated()
	{
		return numTimesMated;
	}
	public void mate(Mosquito other)
	{
		numTimesMated++;
		if(gender == Gender.FEMALE)
		{
			carryingEggs = true;
			numDaysCarryingEggs = 0;
			genotypeMate = other.getGenotype();
		}
	}
	/**
	 * Gets a random genotype according to Mendelian genetics given parental genotypes
	 * @param g1 Parent 1 genotype
	 * @param g2 Parent 2 genotpye
	 * @return
	 */
	public static Genotype getRandomGenotype(Genotype g1, Genotype g2)
	{
		Genotype theGenotype = null; 
		Random random = new Random();
		if(g1 == Genotype.ff && g2 == Genotype.ff)
		{
			theGenotype = Genotype.ff;
		}
		else if (g1 == Genotype.Ff && g2 == Genotype.ff ||
				g1 == Genotype.ff && g2 == Genotype.Ff)
		{
			if(random.nextBoolean())
			{
				theGenotype = Genotype.ff;
				
			}
			else
			{
				theGenotype = Genotype.Ff;
				
			}
		}
		else if (g1 == Genotype.FF && g2 == Genotype.ff ||
				g1 == Genotype.ff && g2 == Genotype.FF)
		{
				theGenotype = Genotype.Ff;
			
		}
		else if (g1 == Genotype.Ff && g2 == Genotype.FF ||
				g1 == Genotype.FF && g2 == Genotype.Ff)
		{
				if(random.nextBoolean())
				{
					theGenotype = Genotype.FF;
				}
				else
				{
					theGenotype = Genotype.Ff;
				}
		}
		else if (g1 == Genotype.FF && g2 == Genotype.FF)
		{
			
				theGenotype = Genotype.FF;

		}
		else if (g1 == Genotype.Ff && g2 == Genotype.Ff)
		{
			double val = random.nextDouble();
			if(val < 0.25)
			{
				theGenotype = Genotype.FF;
			}
			else if(val < 0.75)
			{
				theGenotype = Genotype.Ff;	
			}
			else
			{
				theGenotype = Genotype.ff;
			}
		}
		return theGenotype;
	}

}

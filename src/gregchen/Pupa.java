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
import java.util.Scanner;


public class Pupa 
{
	public static double DAYS_TO_ADULTHOOD = 9.7; //Tun-Lin & Burkot
	public static double DAYS_TO_STAGE_LATE = 3.3;
	public static double DAYS_TO_STAGE_FINAL = 7.2;
	
	private double age = 0;
	private boolean isAlive = true;
	private Genotype genotype;
	private Gender gender;
	private Position birthLocation;
	public enum Stage{EARLY, LATE, FINAL};
	private Stage stage;
	private double larvalTime;
	private double densityDependentMortalityProb = 0;
	Random random = new Random();
	
	public Pupa(Genotype genotype, Position birthLocation)
	{
		Random random = new Random();
		this.genotype = genotype;
		this.birthLocation = birthLocation;
		larvalTime = DAYS_TO_ADULTHOOD + 3.0 * random.nextDouble() - 1.5;
		if(random.nextBoolean())
		{
			this.gender = Gender.FEMALE;
		}
		else
		{
			this.gender = Gender.MALE;
		}	
	}
	public Pupa(Genotype genotype, Gender gender, Position birthLocation, double age)
	{
		this.genotype = genotype;
		this.birthLocation = birthLocation;
		larvalTime = DAYS_TO_ADULTHOOD + 3.0 * random.nextDouble() - 1.5;
		this.gender = gender;
		this.age = age;
	}
	public Pupa(String info)
	{
		int cursor = 0;
		age = Double.parseDouble(info.substring(0, info.indexOf(' ')));
		cursor = info.indexOf(' ') + 1;
		larvalTime = Double.parseDouble(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		int genderNumber = Integer.parseInt(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		int genotypeNumber = Integer.parseInt(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		
		double x = Double.parseDouble(info.substring(cursor, info.indexOf(' ', cursor)));
		cursor = info.indexOf(' ', cursor) + 1;
		double y = Double.parseDouble(info.substring(cursor));
		
		birthLocation = new Position(x, y);
		
		switch(genderNumber)
		{
		case 0:
			gender = Gender.MALE; break;
		case 1:
			gender = Gender.FEMALE; break;
		}
		switch(genotypeNumber)
		{
		case 0:
			genotype = Genotype.ff; break;
		case 1:
			genotype = Genotype.Ff; break;
		case 2:
			genotype = Genotype.FF; break;
		}
	
	}
	public Pupa(Genotype genotype, Position birthLocation, Boolean randomizeAge)
	{
		this(genotype, birthLocation);
		Random random = new Random();
		this.age = 9.6 * random.nextDouble();
	}
	
	public void update()
	{
		if(random.nextDouble() < this.densityDependentMortalityProb)
		{
			//System.out.println("Kill!");
			this.kill();
		}
		age += 1/30d;
		if(age < DAYS_TO_STAGE_LATE)
		{
			stage = Stage.EARLY;
		}
		else if(age < DAYS_TO_STAGE_FINAL)
		{
			stage = Stage.LATE;
		}
		else //therefore age >= DAYS_TO_STAGE_FINAL
		{
			stage = Stage.FINAL;
		}
	}
	public Stage getStage()
	{
		return this.stage;
	}
	public double getLarvalTime()
	{
		return larvalTime;
	}
	public void kill()
	{
		isAlive = false;
	}
	public void setDensityDependentMortalityProb(double prob)
	{
		this.densityDependentMortalityProb = prob;
	}
	public Position getBirthLocation()
	{
		return birthLocation;
	}
	public boolean isAlive()
	{
		return isAlive;
	}
	public Genotype getGenotype()
	{
		return genotype;
	}
	public Gender getGender()
	{
		return gender;
	}
	public double getAge()
	{
		return age;
	}
}

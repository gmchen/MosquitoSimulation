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
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Class that describes a 'breeding site', which (for Aedes aegypti) is almost
 * exclusively a human container (e.g. a used car tire) that holds standing water.
 * The BreedingSite is displayed as a black circle on the simulation and is
 * responsible for storing and killing pupae.
 * @author Greg
 *
 */
public class BreedingSite 
{
	public static final double DENSITY_INDEPENDENT_MORTALITY_PROB = 0.005515448;//0.000439193;
	
	//public static final double SUB_CC_SURVIVAL_RATIO = 15.0/230; //New Orleans
	
	private Position location;
	//private final int CC_EARLY = 143; //Focks & Sackett
	//private final int CC_LATE = 94; //Focks & Sackett
	private final int CC_FINAL = 25;//17; //Focks & Sackett
	
	private final int FLASH_DURATION = 5;
	private ArrayList<Pupa> pupae = new ArrayList<Pupa>();
	private boolean flashing = false;
	private int flashCount = 0;
	
	/**
	 * Main constructor
	 * @param p Poition of the site
	 */
	public BreedingSite(Position p)
	{
		location = p;
	}
	/**
	 * Constructor for a breeding site given save data
	 * @param info The save data
	 */
	public BreedingSite(String info)
	{
		Scanner scanner = new Scanner(info);
		this.location = new Position(scanner.nextDouble(), scanner.nextDouble());
		int n = scanner.nextInt();
	}
	/**
	 * Add a pupa to the breeding site
	 * @param pupa the pupa to add
	 */
	public void add(Pupa pupa)
	{
		pupae.add(pupa);
	}
	/**
	 * Begin the flash sequence
	 */
	public void flash()
	{
		flashing = true;
		flashCount = 0;
	}
	public boolean isFlashing()
	{
		return flashing;
	}
	/**
	 * Kill all of the pupae
	 */
	public void killAllPupae()
	{
		for(Pupa p:pupae)
		{
			p.kill();
		}
	}
	/**
	 * Get save data
	 * @return A string representation of the save data
	 */
	public String getInfo()
	{
		String toReturn = "";
		toReturn += location.x + " " + location.y + " ";
		toReturn += pupae.size() + "\n";
		
		for(Pupa p:pupae)
		{
			int genderNumber = 0;
			int genotypeNumber = 0;
			if(p.getGender() == Gender.FEMALE)
			{
				genderNumber = 1;
			}
			if(p.getGenotype() == Genotype.Ff)
			{
				genotypeNumber = 1;
			}
			else if(p.getGenotype() == Genotype.FF)
			{
				genotypeNumber = 2;
			}
			toReturn += p.getAge() + " " + p.getLarvalTime() +" " + genderNumber + " " + genotypeNumber + " " + p.getBirthLocation().x + " "
					+ p.getBirthLocation().y +  "\n";
		}
		
		return toReturn;
	}
	
	/**
	 * Update the breeding site assuming 30fps. Kill pupae if necessary
	 */
	public void update()
	{
		flashCount++;
		if(flashing)
		{
			if(flashCount > FLASH_DURATION)
			{
				flashing = false;
			}
		}
		Random random = new Random();
		//update pupae
		for(int i = 0; i < pupae.size(); i++)
		{
			//density-independent mortality
			if(random.nextDouble() < DENSITY_INDEPENDENT_MORTALITY_PROB)
			{
				pupae.get(i).kill();
			}
			pupae.get(i).update();
			
			if(!pupae.get(i).isAlive())
			{
				pupae.remove(i);
				i--;
			}
		}
		//Density-dependent deaths
			
		ArrayList<Pupa> earlies = new ArrayList<Pupa>();
		ArrayList<Pupa> lates = new ArrayList<Pupa>();
		ArrayList<Pupa> finals = new ArrayList<Pupa>();

		for(Pupa p:pupae)
		{
			if(p.getStage() == Pupa.Stage.EARLY)
			{
				earlies.add(p);
			}
			else if(p.getStage() == Pupa.Stage.LATE)
			{
				lates.add(p);
			}
			if(p.getStage() == Pupa.Stage.FINAL)
			{
				finals.add(p);
			}
		}
		//System.out.println(earlies.size());
		
		/*
		while(earlies.size() > CC_EARLY)
		{
			int index = random.nextInt(earlies.size());
			earlies.get(index).kill();
			earlies.remove(index);
		}
		while(lates.size() > CC_LATE)
		{
			int index = random.nextInt(lates.size());
			lates.get(index).kill();
			lates.remove(index);
		}
		*/
		

		
		while(finals.size() > CC_FINAL)
		{
			int index = random.nextInt(finals.size());
			finals.get(index).kill();
			finals.remove(index);
		}

		//System.out.println(finals.size());
		/*//old code. 
		for(Pupa p:pupae)
		{
			//p.setDensityDependentMortalityProb(SUB_CC_MORTALITY_PROB);
		}
		if(earlies.size() > CC_EARLY)
		{
			double prob = getMortalityProb(Pupa.DAYS_TO_STAGE_LATE * 30, (double)CC_EARLY / earlies.size());
			for(Pupa p:earlies)
			{
				p.setDensityDependentMortalityProb(prob);
			}
		}
		if(lates.size() > CC_LATE)
		{
			double prob = getMortalityProb((Pupa.DAYS_TO_STAGE_FINAL - Pupa.DAYS_TO_STAGE_LATE) * 30,
					(double)CC_LATE / lates.size());
			for(Pupa p:lates)
			{
				p.setDensityDependentMortalityProb(prob);
			}
		}
		if(finals.size() > CC_FINAL)
		{
			//System.out.println(finals.size());
			double prob = getMortalityProb((Pupa.DAYS_TO_ADULTHOOD - Pupa.DAYS_TO_STAGE_FINAL) * 30, 
					(double)CC_FINAL / finals.size());
			//System.out.println(prob);
			for(Pupa p:finals)
			{
				p.setDensityDependentMortalityProb(prob);
			}
		}
		*/
	}
	/**
	 * Get the density-dependent mortality probability
	 * @param duration
	 * @param fractionToSurvive
	 * @return
	 */
	private double getMortalityProb(double duration, double fractionToSurvive)
	{
		return 1.0 - Math.pow(fractionToSurvive, 1.0 / duration);
	}
	
	/**
	 * Get a list of the pupae which are ready to emerge as adults
	 * @return The mature pupae
	 */
	public ArrayList<Pupa> getMaturePupae()
	{
		ArrayList<Pupa> maturePupae = new ArrayList<Pupa>();
		for(int i = 0; i < pupae.size(); i++)
		{
			if(pupae.get(i).getAge() > pupae.get(i).getLarvalTime())
			{
				maturePupae.add(pupae.get(i));
				pupae.remove(i);
			}
		}
		return maturePupae;
	}
	
	/**
	 * Getter for the breeding site's location
	 * @return The location
	 */
	public Position getLocation()
	{
		return location;
	}
	
	/**
	 * Getter for the number of pupae
	 * @return The pupae
	 */
	public int getNumPupae()
	{
		return pupae.size();
	}
}

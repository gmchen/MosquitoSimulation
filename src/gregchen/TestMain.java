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
/**
 * A class that creates a simulation that demonstrates the mechanics of the simulation.
 * @author Greg
 *
 */
public class TestMain 
{
	/**
	 * Create and run a simulation with immortal mosquitoes to demonstrate breeding. 
	 * Initializes one female mosquito (with dot) and male mosquito (without dot).
	 * The female's genotype is ff (indicated by its green colour), and the male's is FF (indicated by its red colour).
	 * The they mate by colliding, and the female deposits her eggs in a breeding location (which flashes).
	 * By Mendelian genetics, all offspring have genotype Ff (indicated by magenta colour).
	 * This hypothetical gene is lethal to when homozygous to females.
	 * @param args
	 */
	public static void main(String[] args) 
	{
		Simulation sim = new Simulation(600, 600, Simulation.DEFAULT_RADIUS, Simulation.DEFAULT_SPEED, 
				true, true, 0, 0, 0, 0);
		Mosquito.MORTALITY_PROB = 0;
		Mosquito m1 = new Mosquito(Simulation.DEFAULT_RADIUS, new Position(349, 349), new Position(289, 300), Mosquito.DEFAULT_SPEED, Gender.MALE, Genotype.FF);
		Mosquito m2 = new Mosquito(Simulation.DEFAULT_RADIUS, new Position(250, 250), new Position(311, 300), Mosquito.DEFAULT_SPEED, Gender.FEMALE, Genotype.ff);
		m1.setAge(10);
		m2.setAge(10);
		m1.setDirection(5*Math.PI/4);
		m2.setDirection(1*Math.PI/4);
		sim.getMosquitoes().add(m1);
		sim.getMosquitoes().add(m2);
		for(BreedingSite b:sim.getBreedingSites())
		{
			b.killAllPupae();
		}
		sim.start();
	}

}

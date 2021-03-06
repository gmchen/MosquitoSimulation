﻿/*
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
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * A class describing the graphical mosquito population dynamic simulation
 * @author Greg
 *
 */
public class Simulation extends JFrame implements KeyListener, WindowListener
{
	public static final double EMERGENCE_SURVIVAL_PROB = 0.83; //Stochastic Population...
	public static final int MAX_DISTANCE_TO_BREEDING_SITE = 33;
	public static final double MATING_PROB = 1.0;//0.7;
	public static final int DEFAULT_RADIUS = 3;
	public static final int DEFAULT_SPEED = 6;
	
	public static final int SCALE = 1;
	
	static int resultCounter = 1;
	
	boolean fromSave = false;
	boolean saveState = false;
	boolean printNumbers = false;
	boolean saveData = false;
	
	//static double testInterval = 2.0;
	//static int testReleases = 5;
	
	private double releaseInterval = 2.0;
	private int numReleases = 5;
	
	
	private int releaseCounter;
	private double timeOfLastRelease;
	private double simulationDayCounter;
	private int numPerRelease = 0; //for testing. Return to zero later.
	
	private BufferedImage background;
	private final String BACKGROUND_FILE_NAME = "/images/Background.png";
	private ArrayList<Mosquito> mosquitoes;
	private int width, height;
	private final static int UPDATES_PER_SECOND = 30;
	private final static int TIME_BETWEEN_UPDATES = 1000000000 / UPDATES_PER_SECOND;
	private final static int MAX_UPDATES_BEFORE_RENDER = 5;
	private final static int TARGET_FPS = 60;
	private final static int TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;
	private Timer timer;
	private int screenWidth, screenHeight;
	private ArrayList<BreedingSite> breedingSites;
	private double defaultRadius, defaultSpeed;
	private boolean fsRIDL = false;
	private int numFemales = 0, numMales = 0;
	private boolean showBreedingSites = false;;
	private int initialNumber;
	private int numFF, numFf, numff;
	PrintWriter pw;
	//int trial = 0;
	//static int successCounter = 0;
	
	/**
	 * The main constructor for the simulation
	 * @param width
	 * @param height
	 * @param defaultRadius Default radius for Actors
	 * @param defaultSpeed Default speed for actors
	 * @param fsRIDL Whether or not the gene is lethal to females
	 * @param showBreedingSites Whether or not to display the circular breeding sites
	 * @param initialNumber The inital number of insects
	 * @param numReleases The number of release
	 * @param releaseInterval The release interval
	 * @param numPerRelease The number of insects released each time
	 */
	public Simulation (int width, int height, double defaultRadius,	double defaultSpeed, boolean fsRIDL, boolean showBreedingSites, int initialNumber, int numReleases, int releaseInterval, int numPerRelease)
	{
		super("Simulation");
		this.showBreedingSites = showBreedingSites;
		this.fsRIDL = fsRIDL;
		this.defaultRadius = defaultRadius;
		this.defaultSpeed = defaultSpeed;
		this.setLocation(new Point(10, 10));
		this.setIgnoreRepaint(true);
		this.setResizable(false);
		this.screenWidth = width;
		this.screenHeight = height;
		this.width = (int)(width * SCALE); //make the actual screen 4 times the size by area
		this.height = (int)(height * SCALE);
		this.addKeyListener(this);
		this.addWindowListener(this);
		this.initialNumber = initialNumber;
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if(initialNumber == 1000)
		{
			fromSave = true;
		}
		//this.numReleases = numReleases;
		//this.releaseInterval = releaseInterval;
		
		if(saveData)
		{
		try {
			pw = new PrintWriter(new File("Population" + resultCounter + ".txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		pw.println("Initial number: " + initialNumber + " Number per release: " + numPerRelease
				+ " Number Releases: " + numReleases + " Release Interval: " + releaseInterval);
		resultCounter++;
		}
		
		
		this.numPerRelease = numPerRelease;
		this.numReleases = numReleases;
		this.releaseInterval = releaseInterval;
		
		setVisible(true);
		this.createBufferStrategy(2);
		setSize(width + getInsets().left + getInsets().right, height + getInsets().top + getInsets().bottom);
		
		java.awt.Font f = new java.awt.Font("default", java.awt.Font.BOLD, 15);
		this.getGraphics().setFont(f);
		
		init(initialNumber);
		initializeExcelFile();
	}

	private static int fileCount = 0;
	Workbook wb;
	Sheet s;
	int currentRow = 0;
	private FileOutputStream excelOut;
	
	private void initializeExcelFile()
	{
		//short rownum;
		//Create a Data folder if it does not already exist
		File theDir = new File("Data");
		//if the directory does not exist, create it
		if(!theDir.exists())
		{
			theDir.mkdir();
		}
		// create a new file
		
		if(fileCount == 0)
		{
			File file = null;
			do
			{
				fileCount++;
				file = new File("Data/Data " + fileCount + ".xls");
			}while(file.exists());
		}
		
		try {
			excelOut = new FileOutputStream("Data/Data " + fileCount + ".xls");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fileCount++;
		
		// create a new workbook
		wb = new HSSFWorkbook();
		// create a new sheet
		s = wb.createSheet();
		// declare a row object reference
		Row r = null;
		// declare a cell object reference
		Cell c = null;
		// create 3 cell styles
		CellStyle cs = wb.createCellStyle();
		CellStyle cs2 = wb.createCellStyle();
		CellStyle cs3 = wb.createCellStyle();
		DataFormat df = wb.createDataFormat();
		// create 2 fonts objects
		Font f = wb.createFont();
		Font f2 = wb.createFont();

		//set font 1 to 12 point type
		f.setFontHeightInPoints((short) 12);
		//make it blue
		f.setColor( (short)0xc );
		// make it bold
		//arial is the default font
		f.setBoldweight(Font.BOLDWEIGHT_BOLD);

		//set font 2 to 10 point type
		f2.setFontHeightInPoints((short) 10);
		//make it red
		f2.setColor( (short)Font.COLOR_RED );
		//make it bold
		f2.setBoldweight(Font.BOLDWEIGHT_BOLD);

		f2.setStrikeout( true );

		//set cell stlye
		cs.setFont(f);
		//set the cell format 
		cs.setDataFormat(df.getFormat("#,##0.0"));

		//set a thin border
		cs2.setBorderBottom(cs2.BORDER_THIN);
		//fill w fg fill color
		cs2.setFillPattern((short) CellStyle.SOLID_FOREGROUND);
		//set the cell format to text see DataFormat for a full list
		cs2.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));

		// set the font
		cs2.setFont(f2);

		// set the sheet name in Unicode
		wb.setSheetName(0, "Greg Chen");
		// in case of plain ascii
		// wb.setSheetName(0, "HSSF Test");
		// Make header cells
		
		r = s.createRow(0);
		c = r.createCell(7);
		c.setCellStyle(cs3);
		
		c.setCellValue("Initial number: " + initialNumber + " Number per release: " + numPerRelease
				+ " Number Releases: " + numReleases + " Release Interval: " + releaseInterval
				+ " Female lethal gene: " + fsRIDL);
		
		currentRow = 2;
		for (short cellnum = (short) 0; cellnum < 7; cellnum ++)
	    {
	        // create a numeric cell
	        c = r.createCell(cellnum);
	        // do some goofy math to demonstrate decimals
	        String heading = null;
	        switch(cellnum)
	        {
	        case 0:
	        	heading = "Day"; break;
	        case 1:
	        	heading = "Total Population"; break;
	        case 2:
	        	heading = "Male Population"; break;
	        case 3:
	        	heading = "Female Population"; break;
	        case 4:
	        	heading = "FF"; break;
	        case 5:
	        	heading = "Ff"; break;
	        case 6:
	        	heading = "ff"; break;
	        }
	        
	        c.setCellValue(heading);

	       
            // set this cell to the first cell style we defined
            c.setCellStyle(cs);
            // set the cell's string value to "Test"

	        // make this column a bit wider
            if(cellnum > 0)
            {
            	s.setColumnWidth((short) (cellnum), (short) ((300) / ((double) 1 / 20)));
            }
	        r.setHeight((short) 800);
	    }
		/*
		int rownum;
		for (rownum = (short) 0; rownum < 30; rownum++)
		{
		    // create a row
		    r = s.createRow(rownum);
		
		    r.setHeight((short) 0x249);


		    //
		   
		}

		//draw a thick black border on the row at the bottom using BLANKS
		// advance 2 rows
		rownum++;
		rownum++;

		r = s.createRow(rownum);

		// define the third style to be the default
		// except with a thick black border at the bottom
		cs3.setBorderBottom(cs3.BORDER_THICK);

		*/
		
		// write the workbook to the output stream
		// close our file (don't blow out our file handles)
		    
	}
	
	/**
	 * Write to excel file
	 */
	private void writeDataToExcelFile()
	{
		Row r = s.createRow(currentRow);
		
		Cell c = r.createCell(0);
		c.setCellValue(this.simulationDayCounter);
		c = r.createCell(1);
		c.setCellValue(this.mosquitoes.size());
		c = r.createCell(2);
		c.setCellValue(this.numMales);
		c = r.createCell(3);
		c.setCellValue(this.numFemales);
		c = r.createCell(4);
		c.setCellValue(this.numFF);
		c = r.createCell(5);
		c.setCellValue(this.numFf);
		c = r.createCell(6);
		c.setCellValue(this.numff);
		
		currentRow++;
	}
	
	private void init(int initialNumber)
	{
		try {
			background = ImageIO.read(this.getClass().getResourceAsStream(BACKGROUND_FILE_NAME));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.timeOfLastRelease = -this.releaseInterval;
		this.simulationDayCounter = 0;
		this.releaseCounter = 0;
		breedingSites = new ArrayList<BreedingSite>();
		mosquitoes = new ArrayList<Mosquito>();
		
		/*
		this.numReleases = testReleases; //chenge this back
		this.releaseInterval = testInterval;
		testInterval++;
		if(testInterval >= 10)
		{
			testInterval = 1;
			testReleases++;
			if(testReleases>= 10)
			{
				System.out.println("All done!");
				this.dispose();
			}
		}
		*/
		if(fromSave)
		{
			load();
		}
		else
		{
			addInitialMosquitoes(initialNumber);
			Random random = new Random();
			for(Mosquito m:mosquitoes)
			{
				m.setAge(5 * random.nextDouble());
			}
			
			//make 107 * scalefactor breeding sites, filled each with 230 pupae
			for(int i = 0; i < 107 * SCALE * SCALE; i++)
			{
				BreedingSite b = new BreedingSite(Position.getRandomPosition(this.width, this.height));
				breedingSites.add(b);
				for(int j = 0; j < mosquitoes.size() / 107* SCALE * SCALE * 5; j++)
				{
					Pupa p = new Pupa(Genotype.ff, b.getLocation(), true);
					b.add(p);
				}
			}
		}
	}
	
	public ArrayList<BreedingSite> getBreedingSites()
	{
		return breedingSites;
	}
	
	public void save()
	{
		PrintWriter printer = null;
		try {
			printer = new PrintWriter("MosquitoInfo.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		printer.println(mosquitoes.size());
		for(Mosquito m:mosquitoes)
		{
			printer.println(m.getInfo());
		}
		printer.close();
		try {
			printer = new PrintWriter(new File("BreedingSiteInfo.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		printer.println(breedingSites.size());
		for(BreedingSite b:breedingSites)
		{
			printer.println(b.getInfo());
		}
		printer.close();
	}
	
	public void load()
	{
		Scanner scanner = null;
		
		scanner = new Scanner(this.getClass().getResourceAsStream("/saveData/BreedingSiteInfo.txt"));
		
		int numSites = scanner.nextInt();
		for(int i = 0; i < numSites; i++)
		{
			BreedingSite b = new BreedingSite(new Position(scanner.nextDouble(), scanner.nextDouble()));
			breedingSites.add(b);
			int numPupae = scanner.nextInt();
			scanner.nextLine();
			for(int j = 0; j < numPupae; j++)
			{
				String line = scanner.nextLine();
				Pupa p = new Pupa(line);
				b.add(p);
			}
		}
		

		scanner = new Scanner(this.getClass().getResourceAsStream("/saveData/MosquitoInfo.txt"));
		
		int numMosquitoes = scanner.nextInt();
		scanner.nextLine();
		for(int i = 0; i < numMosquitoes; i++)
		{
			Mosquito m = new Mosquito(scanner.nextLine());
			mosquitoes.add(m);
		}
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Mosquito> addInitialMosquitoes(int number)
	{
		for(int i = 0; i < number; i++)
		{
			Position location = Position.getRandomPosition(width, height);
			
			Genotype genotype;
			
			genotype = Genotype.ff;
			
			Gender gender;

			Random random = new Random();
			if(random.nextBoolean())
			{
				gender = Gender.MALE;
			}
			else
			{
				gender = Gender.FEMALE;
			}
			
			Mosquito m = new Mosquito(defaultRadius, location, defaultSpeed, gender, genotype);
			mosquitoes.add(m);
		}
		return (ArrayList<Mosquito>) mosquitoes.clone();
	}
	
	public void setNumPerRelease(int numPerRelease)
	{
		this.numPerRelease = numPerRelease;
	}
	
	public void addFSRIDLMosquitoes(int number)
	{
		timeOfLastRelease = simulationDayCounter;
		Random random = new Random();
		for(int i = 0; i < number; i++)
		{
			Position location = new Position(300 + 100 * random.nextDouble() - 50,300 + 100 * random.nextDouble() - 50);
			Mosquito m = new Mosquito(defaultRadius, location, defaultSpeed, Gender.MALE, Genotype.FF);
			mosquitoes.add(m);
		}
	}
	
	public void addMosquito(Pupa pupa)
	{
		Mosquito m = new Mosquito(defaultRadius, pupa.getBirthLocation(), defaultSpeed, pupa.getGender(), pupa.getGenotype());
		this.mosquitoes.add(m);
	}
	
	public void update()
	{
		writeDataToExcelFile();
		
		if(printNumbers)
		{
			System.out.println("Number of mosquitoes:" + mosquitoes.size());
		}
		
		simulationDayCounter+= 1/30d;
		
		if(saveData)
		{
			if(this.simulationDayCounter > 300)
			{
				pw.close();
				this.dispose();
			}
		}
			/*
			if(mosquitoes.size() == 0 || this.simulationDayCounter >  300)
			{
				System.out.println("Release interval: " + releaseInterval);
				System.out.println("Number of Releases: " + numReleases);
				
				if(mosquitoes.size() == 0 || this.simulationDayCounter > 300 && mosquitoes.size() < 100)
				{
					PrintWriter pw = null;
					try {
						pw = new PrintWriter(new File("Success" + successCounter + ".txt"));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Success!");
					pw.println("Success!");
					pw.println("Days elapsed: " + this.simulationDayCounter);
					pw.println("Number of Mosquitoes: " + this.mosquitoes.size());
					pw.println("Release interval: " + releaseInterval);
					pw.println("Number of releases: " + numReleases);
					pw.close();
					successCounter++;
				}
				
				init(1000);
			}
		
		*/
		//Save state to text file
		if(saveState)
		{
			if(simulationDayCounter > 300)
			{
				save();
				this.dispose();
			}
		}
		//System.out.println(this.simulationDayCounter);
		if(this.releaseCounter < this.numReleases && this.simulationDayCounter >= this.timeOfLastRelease + this.releaseInterval)
		{
			this.addFSRIDLMosquitoes(numPerRelease);
			this.timeOfLastRelease = this.simulationDayCounter;
			this.releaseCounter++;
		}
		
		for(BreedingSite b: breedingSites)
		{
			b.update();
			ArrayList<Pupa> maturePupae = b.getMaturePupae();
			
			for(Pupa p: maturePupae)
			{
				Random random =new Random();
				if(random.nextInt() < EMERGENCE_SURVIVAL_PROB)
				{
					this.addMosquito(p);
				}
			}
		}
		
		//Update hosts
		int numMales = 0;
		int numFemales = 0;
		numFF = 0;
		numFf = 0;
		numff = 0;
		for(int i = 0; i < mosquitoes.size(); i++)
		{
			Mosquito m = mosquitoes.get(i);
			if(m.getGender() == Gender.MALE)
			{
				numMales++;
			}
			else
			{
				numFemales++;
			}
			if(m.getGenotype() == Genotype.FF)
			{
				numFF++;
			}
			else if(m.getGenotype() == Genotype.Ff)
			{
				numFf++;
			}
			if(m.getGenotype() == Genotype.ff)
			{
				numff++;
			}
			if(this.fsRIDL && m.getGenotype() != Genotype.ff && m.getGender() == Gender.FEMALE)
			{
				m.kill();
			}
			
			m.update();
			
			m.bounceWalls(width, height);
			
			if(m.getGender() == Gender.FEMALE && m.carryingEggs() && m.getNumDaysCarryingEggs() > Mosquito.NUM_DAYS_UNTIL_EGG_DEPOSIT)
			{
				//deposit eggs, i.e. add pupae to nearest breeding site
				m.removeEggs();
				
				BreedingSite closest = breedingSites.get(0);
				int min = Integer.MAX_VALUE;
				for(int j = 0; j < breedingSites.size(); j++)
				{
					BreedingSite b = breedingSites.get(j);
					int poss = (int)((m.getLocation().x - b.getLocation().x) * (m.getLocation().x - b.getLocation().x)
							+ (m.getLocation().y - b.getLocation().y) * (m.getLocation().y - b.getLocation().y));
					if(poss < min)
					{
						min = poss;
						closest = b;
					}
				}
				if(Math.sqrt(min) < MAX_DISTANCE_TO_BREEDING_SITE)
				{
					
					closest.flash();
					for(int j = 0; j < Mosquito.NUM_EGGS_PER_OVIPOS; j++)
					{
						closest.add(new Pupa(Mosquito.getRandomGenotype(m.getGenotype(), m.getGenotypeMate()), closest.getLocation()));
					}
					
				}
			}
		
			
			if(!mosquitoes.get(i).isAlive())
			{
				mosquitoes.remove(i);
				i--;
			}
		}
		if(printNumbers)
		{
			System.out.println(numMales + " males, " + numFemales + " females.");
		}
		
		if(saveData)
		{
			pw.println(this.simulationDayCounter + "\t" + mosquitoes.size() + "\t" + numMales + "\t" + numFemales);
			//pw.println(this.simulationDayCounter + "\t" + mosquitoes.size() + "\t" + numFF + "\t" + numFf + "\t" + numff);
		}
		
		this.numFemales = numFemales;
		this.numMales = numMales;
		
		HashMap<Actor, Actor> colliders = Actor.getCollidingActors(new ArrayList<Actor>(mosquitoes), width, height);
		
		for(Actor a:colliders.keySet())
		{
			Actor b = colliders.get(a);
			if(a instanceof Mosquito && b instanceof Mosquito)
			{
				Mosquito m1 = (Mosquito) a;
				Mosquito m2 = (Mosquito) b;
				Mosquito female = null;
				Mosquito male = null;
				if(m2.getGender() == Gender.FEMALE && m1.getGender() == Gender.MALE)
				{
					female = m2;
					male = m1;
				}
				else if(m1.getGender() == Gender.FEMALE && m2.getGender() == Gender.MALE)
				{
					female = m1;
					male = m2;
				}
				Random random = new Random();
				if(female != null)
				{
					if(female.isFertile() && male.isFertile() && random.nextDouble() < Simulation.MATING_PROB)
					{
						female.mate(male);
						male.mate(female);
					}
				}
			}
		}
	}

	public void start()
	{
		timer = new Timer(0, new ActionListener() 
		{
			  
	          public void actionPerformed(ActionEvent e) 
	          { 
	          		
	        	  Thread loop = new Thread()
	        	  {
		             public void run()
		              {
			              runLoop();
		              }
		           };
		           loop.start();
	          }
	          
	    });
		timer.setRepeats(false);

		timer.start();
	}
	
	public void runLoop()
	{
		long lastUpdate = System.nanoTime();
		int loops;
		double interpolation;
		long lastRenderTime;
		
		while(this.isVisible())
		{
			loops = 0;
			while(this.isVisible() && System.nanoTime() - lastUpdate > TIME_BETWEEN_UPDATES && loops < MAX_UPDATES_BEFORE_RENDER)
			{
				lastUpdate = System.nanoTime();
				
				this.update();

				loops++;
				//System.out.println("Update");
			
			}
			//System.out.println("Draw");
			interpolation = Math.min(1.0d, (double) (System.nanoTime() - lastUpdate) / TIME_BETWEEN_UPDATES);
			
			drawSim(interpolation);
			
			lastRenderTime = System.nanoTime();
			
			while(System.nanoTime() - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && System.nanoTime() - lastUpdate < TIME_BETWEEN_UPDATES)
			{
				Thread.yield();
				try{Thread.sleep(1);} catch(Exception e){}
			}
		}
	}

	private void drawSim(double interpolation) 
	{
		BufferStrategy bf = this.getBufferStrategy();
		//long start = System.nanoTime();
		Graphics g= null;
		try
		{
			g = bf.getDrawGraphics();
			
			this.draw(g, interpolation, this.getInsets());
			
		} finally
		{
			g.dispose();
		}
		bf.show();
		Toolkit.getDefaultToolkit().sync();
		//System.out.println((System.nanoTime()-start)/1000000);
	}
	
	public void draw(Graphics g, double interpolation, Insets insets)
	{
		g.drawImage(background, insets.left, insets.top, null);
		// For drawing breeding sites
		if(showBreedingSites)
		{
			for(BreedingSite b:breedingSites)
			{
				if(b.isFlashing())
				{
					g.setColor(Color.ORANGE);
				}
				else
				{
					g.setColor(Color.BLACK);
				}
				g.drawOval((int)(b.getLocation().x - MAX_DISTANCE_TO_BREEDING_SITE) + insets.left, (int)(b.getLocation().y - MAX_DISTANCE_TO_BREEDING_SITE) + insets.top, (int)(2*MAX_DISTANCE_TO_BREEDING_SITE), (int)(2*MAX_DISTANCE_TO_BREEDING_SITE));
			}
		}
		for(int i = 0; i < mosquitoes.size(); i++)
		{
			if(mosquitoes.get(i).getLocation().x < screenWidth + 2*mosquitoes.get(i).getBoundRadius()
				&& mosquitoes.get(i).getLocation().y < screenHeight + 2*mosquitoes.get(i).getBoundRadius())
				{
					mosquitoes.get(i).draw(g, interpolation, insets);
				}
		}
		g.setColor(Color.WHITE);
		g.drawString("Total Mosquitoes: " + this.mosquitoes.size(), 10, 50);
		g.drawString(this.numMales + " Males, " + this.numFemales + " Females.", 10, 70);
		g.drawString(this.numFF + " FF, " + this.numFf + " Ff, " + this.numff + " ff.", 10, 90);
	}
	
	public ArrayList<Mosquito> getMosquitoes() 
	{
		return mosquitoes;
	}
	
	private boolean keyDown = false;
	@Override
	public void keyTyped (KeyEvent e )
	{  
		
		if(fsRIDL && !keyDown && (int)e.getKeyChar() == KeyEvent.VK_SPACE)
		{
			this.addFSRIDLMosquitoes(this.numPerRelease);
			keyDown = true;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{
		
	}

	@Override
	public void keyReleased(KeyEvent e) 
	{
		keyDown = false;
		
	}

	
	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) 
	{
		this.setVisible(false);
		try {
			wb.write(excelOut);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			excelOut.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}  
}

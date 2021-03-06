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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * 
 * A class describing a control panel with spinners which
 * allow the user to manually modify the number of insects,
 * insects per release, etc. The panel contains a button which
 * instantiates and begins a simulation based on the parameters
 * (a single control panel may create multiple simulations at once)
 * @author Greg
 */
public class ControlPanel extends JPanel implements ActionListener
{
	final static int PANEL_WIDTH = 300;
	final static int PANEL_HEIGHT = 600;
	private final int JLABEL_X_POSITION = 20;
	private final int JSPINNER_X_POSITION = 200;
	private final int Y_INCREMENT = 40;
	private int itemYPosition = 20;
	private ArrayList<JLabel> labels;
	private JButton startButton;
	private JCheckBox fsRIDLCheckBox;
	private JCheckBox showBreedingSitesBox;
	
	private JSpinner initialNumberSpinner = null;
	//private JSpinner radiusSpinner = null;
	//private JSpinner speedSpinner = null;
	private JSpinner numPerReleaseSpinner = null;
	private JSpinner numReleasesSpinner = null;
	private JSpinner releaseIntervalSpinner = null;
	
	/**
	 * Constructor for a control panel
	 */
	public ControlPanel()
	{
		super(null);
		setSize(PANEL_WIDTH, PANEL_HEIGHT);
		setVisible(true);
		
		labels = new ArrayList<JLabel>();
		
		JLabel label = new JLabel("Simulation Control Panel.");
		label.setLocation(JLABEL_X_POSITION, itemYPosition);
		label.setSize(400, 30);
		this.add(label);
		itemYPosition += Y_INCREMENT;
		
		//add JLabels and JSpinners
		SpinnerNumberModel numberModel = new SpinnerNumberModel(1000 * Simulation.SCALE * Simulation.SCALE, 1, Integer.MAX_VALUE, 1);
		//SpinnerNumberModel radiusModel = new SpinnerNumberModel(3, 1, Integer.MAX_VALUE, 1);
		//SpinnerNumberModel speedModel = new SpinnerNumberModel(6, 1, Integer.MAX_VALUE, 1);
		SpinnerNumberModel numPerReleaseModel = new SpinnerNumberModel(1000, 0, Integer.MAX_VALUE, 1);
		SpinnerNumberModel numReleasesModel = new SpinnerNumberModel(7, 0, Integer.MAX_VALUE, 1);
		SpinnerNumberModel releaseIntervalModel = new SpinnerNumberModel(7, 0, Integer.MAX_VALUE, 1);
		
		initialNumberSpinner = new JSpinner(numberModel);
		//radiusSpinner = new JSpinner(radiusModel);
		//speedSpinner = new JSpinner(speedModel);
		numPerReleaseSpinner = new JSpinner(numPerReleaseModel);
		numReleasesSpinner = new JSpinner(numReleasesModel);
		releaseIntervalSpinner = new JSpinner(releaseIntervalModel);
		
		addJLabelAndSpinner("Number of Insects", initialNumberSpinner);
		//addJLabelAndSpinner("Radius", radiusSpinner);
		//addJLabelAndSpinner("Speed", speedSpinner);
		addJLabelAndSpinner("Insects Per Release", numPerReleaseSpinner);
		addJLabelAndSpinner("Number of Releases", numReleasesSpinner);
		addJLabelAndSpinner("Release Interval", releaseIntervalSpinner);
		
		JLabel newLabel = new JLabel("Female-Lethal Gene");
		newLabel.setLocation(JLABEL_X_POSITION, itemYPosition);
		newLabel.setSize(200, 30);
		fsRIDLCheckBox = new JCheckBox("", true);
		fsRIDLCheckBox.setSize(30, 30);
		fsRIDLCheckBox.setLocation(JSPINNER_X_POSITION, itemYPosition);
		itemYPosition += Y_INCREMENT;
		this.add(newLabel);
		this.add(fsRIDLCheckBox);
		
		JLabel newLabel2 = new JLabel("Show Breeding Sites");
		newLabel2.setLocation(JLABEL_X_POSITION, itemYPosition);
		newLabel2.setSize(200, 30);
		showBreedingSitesBox = new JCheckBox("", true);
		showBreedingSitesBox.setSize(30, 30);
		showBreedingSitesBox.setLocation(JSPINNER_X_POSITION, itemYPosition);
		itemYPosition += Y_INCREMENT;
		this.add(newLabel2);
		this.add(showBreedingSitesBox);
		
		//add button
		
		startButton = new JButton();
		startButton.setSize(75, 30);
		startButton.setText("Begin");
		startButton.setLocation(JSPINNER_X_POSITION, itemYPosition);
		this.add(startButton);
		startButton.addActionListener(this);
		
	}
	
	public void addJLabelAndSpinner(String text, JSpinner spinner)
	{
		JLabel label = new JLabel(text);
		label.setLocation(JLABEL_X_POSITION, itemYPosition);
		
		label.setSize(200, 30);
		
		spinner.setLocation(JSPINNER_X_POSITION, itemYPosition);
		JFormattedTextField jtf = ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
		jtf.setHorizontalAlignment(JTextField.LEFT);
		
		spinner.setSize(75, 30);
		
		labels.add(label);
		
		this.add(label);
		this.add(spinner);
		
		itemYPosition += Y_INCREMENT;
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		int radius = Simulation.DEFAULT_RADIUS;//(Integer) this.radiusSpinner.getValue();
		int speed = Simulation.DEFAULT_SPEED;//(Integer) this.speedSpinner.getValue();
		boolean fsRIDL = fsRIDLCheckBox.isSelected();
		boolean showBreedingSites = showBreedingSitesBox.isSelected();
		int initialNumber = (Integer) this.initialNumberSpinner.getValue();
		int numReleases = (Integer)this.numReleasesSpinner.getValue();
		int releaseInterval = (Integer)this.releaseIntervalSpinner.getValue();
		int numPerRelease = (Integer)this.numPerReleaseSpinner.getValue();
		
		Simulation sim = new Simulation(600, 600, radius, speed, fsRIDL, showBreedingSites, initialNumber, numReleases, releaseInterval, numPerRelease);
		
		//sim.setNumPerRelease((Integer)this.numPerReleaseSpinner.getValue());
	
		sim.start();
	}
}

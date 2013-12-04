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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;

/**
 * The main class. Instantiates and displays the main control panel
 *  @author Greg
 */
public class Main
{
	public static void main(String[] args)
	{
		
		Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		
		JFrame mainFrame = new JFrame("Control Panel");
		mainFrame.setSize(ControlPanel.PANEL_WIDTH + mainFrame.getInsets().left + mainFrame.getInsets().right,
				ControlPanel.PANEL_HEIGHT + mainFrame.getInsets().top + mainFrame.getInsets().bottom);
		mainFrame.add(new ControlPanel());
		mainFrame.setResizable(false);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setLocation(10,  10);
		mainFrame.setLocation(10, maxBounds.height / 2 - mainFrame.getHeight()/2);
		mainFrame.setVisible(true);
				
	}
	
}

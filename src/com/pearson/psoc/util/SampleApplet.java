package com.pearson.psoc.util;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.Box;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SampleApplet extends JApplet implements Runnable {
		
	private JComboBox featureBox = new JComboBox();
	private JComboBox gradeBox = new JComboBox();
	private JButton executeButton = new JButton("Execute");
	private JButton abort = new JButton("Abort");
	private int count = 0;
	private static String selectedFeature = "";
	private static String selectedGrade = "";
	private ExecuteTestSetUtil executeTestSet = null;
	private CheckboxGroup radioGroup = null;
	private CheckboxGroup installationGroup = null;
	private static String testCaseType = null;
	private static String installType = null;
	private static Container myContainer = null; 
	
	public void run() 
    { 
		executeTestSet.executeTestCases(selectedFeature, testCaseType, installType, selectedGrade);
    }
	
	public void init() {
		executeTestSet = new ExecuteTestSetUtil();
		executeButton.setPreferredSize(new Dimension(100, 20));
		abort.setPreferredSize(new Dimension(100, 20));
		featureBox.setPreferredSize(new Dimension(200, 20));
		gradeBox.setPreferredSize(new Dimension(200, 20));
		JLabel label = new JLabel("PSC Automation Executor: "+executeTestSet.getConfiguration().getTrackName());
		Font headerFont = new Font("Tahoma", Font.BOLD, 24);
		Font labelFont = new Font("Tahoma", Font.PLAIN, 12);
		
		List<String> grades = executeTestSet.getAvailableGrades(executeTestSet.getConfiguration());
       
		for (int i = 0; i < grades.size(); i++) {
			gradeBox.addItem(grades.get(i));
		}
		executeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Checkbox selectedInstallCheckbox = installationGroup.getSelectedCheckbox();
				Checkbox selectedTestCaseTypeCheckbox = radioGroup.getSelectedCheckbox();
				if(null != selectedTestCaseTypeCheckbox) {
					if(null != selectedInstallCheckbox) {
						if(selectedFeature.isEmpty() || selectedFeature.equalsIgnoreCase(" --Select-- ")) {
							JOptionPane.showMessageDialog(null, "Please select a feature or All for execution", "Error", JOptionPane.ERROR_MESSAGE);
						} else {
							if(selectedGrade.isEmpty() || selectedGrade.equalsIgnoreCase(" --Select-- ")) {
								JOptionPane.showMessageDialog(null, "Please select a Grade for execution", "Error", JOptionPane.ERROR_MESSAGE);
							} else {
								executeButton.setEnabled(false);
								abort.setEnabled(true);
								testCaseType = selectedTestCaseTypeCheckbox.getLabel();
								installType = selectedInstallCheckbox.getLabel();
								callThread();
							}
						}
					} else {
						JOptionPane.showMessageDialog(null, "Please select a installation mode", "Error", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(null, "Please select a Critical or All for execution", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		abort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		featureBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(null != ((JComboBox) e.getSource()).getSelectedItem()) {
					selectedFeature = ((JComboBox) e.getSource()).getSelectedItem().toString();	
				}
			}
		});
		
		myContainer = getContentPane();
		myContainer.setLayout(new FlowLayout());
		
		gradeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedGrade = ((JComboBox) e.getSource()).getSelectedItem().toString();
				executeTestSet.generateInputFileForSelectedGrade(executeTestSet.getConfiguration().getMasterFile(), selectedGrade);
				List<String> features = executeTestSet.getTestCaseFeatures();
				featureBox.removeAllItems();
				for (int i = 0; i < features.size(); i++) {
					featureBox.addItem(features.get(i));
				}
			}
		});
		
		radioGroup = new CheckboxGroup();
        Checkbox criticalOnly = new Checkbox("Critical", radioGroup, false);
        Checkbox all = new Checkbox("All", radioGroup, false);
        JLabel testCaseLabel = new JLabel("Want to execute Critical or All? ");
        
        installationGroup = new CheckboxGroup();
        Checkbox freshInstall = new Checkbox("Fresh", installationGroup, false);
        Checkbox upgradeInstall = new Checkbox("Upgrade", installationGroup, false);
        JLabel installLabel = new JLabel("How you want to install app? ");
        
        
		myContainer.setBackground(new Color(255, 255, 204));
		label.setFont(headerFont);
		myContainer.add(label);
		myContainer.add(Box.createHorizontalGlue());
		myContainer.add(Box.createRigidArea(new Dimension(400, 10)));
		
		/*Image img = getImage(getCodeBase(), "processing.gif");
		DrawingPanel drawing_panel =  new DrawingPanel(img);
	    myContainer.add(drawing_panel);
	    myContainer.add(Box.createHorizontalGlue());
		myContainer.add(Box.createRigidArea(new Dimension(400, 10)));*/
			
		testCaseLabel.setFont(labelFont);
		myContainer.add(testCaseLabel);
		criticalOnly.setFont(labelFont);
		myContainer.add(criticalOnly);
		all.setFont(labelFont);
		myContainer.add(all);
		myContainer.add(Box.createHorizontalGlue());
		myContainer.add(Box.createRigidArea(new Dimension(400, 10)));
		myContainer.add(installLabel);
		installLabel.setFont(labelFont);
		myContainer.add(freshInstall);
		freshInstall.setFont(labelFont);
		myContainer.add(upgradeInstall);
		upgradeInstall.setFont(labelFont);
		if(grades.size() > 2) {
			myContainer.add(Box.createHorizontalGlue());
			myContainer.add(Box.createRigidArea(new Dimension(400, 10)));
			myContainer.add(gradeBox);
		}
		myContainer.add(Box.createHorizontalGlue());
		myContainer.add(Box.createRigidArea(new Dimension(400, 10)));
		myContainer.add(featureBox);
		myContainer.add(Box.createHorizontalGlue());
		myContainer.add(Box.createRigidArea(new Dimension(400, 10)));
		myContainer.add(executeButton);
		myContainer.add(abort);
		abort.setEnabled(false);
	}
	
	private void callThread() {
		Thread executionThread = new Thread(this);
		executionThread.start();
	}

	public static void main(String[] args) {
		run(new SampleApplet(), 500, 300);
	}

	public static void run(JApplet applet, int width, int height) {
		JFrame frame = new JFrame();
		setWindowPosition(frame, 0);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.getContentPane().add(applet);
		frame.setSize(width, height);
		applet.init();
		applet.start();
		frame.setVisible(true);
	}
	
	private static void setWindowPosition(JFrame window, int screen)
	{        
	    window.setLocation(200, 200);
	}
} 

class DrawingPanel extends JPanel { 
	Image img;
	DrawingPanel (Image img)
	{ 
		this.img = img; 
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	    g.drawImage(img, 0, 0, this);
    }
}

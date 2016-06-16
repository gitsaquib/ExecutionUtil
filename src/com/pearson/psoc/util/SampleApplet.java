package com.pearson.psoc.util;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class SampleApplet extends JApplet implements Runnable {
		
	private JComboBox featureBox = new JComboBox();
	private JComboBox gradeBox = new JComboBox();
	private JButton executeButton = new JButton("Execute");
	private JButton close = new JButton("Close");
	private int count = 0;
	private static String selectedFeature = "";
	private static String selectedGrade = "";
	private ExecuteTestSetUtil executeTestSet = null;
	private CheckboxGroup radioGroup = null;
	private CheckboxGroup installationGroup = null;
	private static String testCaseType = null;
	private static String installType = null;
	
	public void run() 
    { 
		executeTestSet.executeTestCases(selectedFeature, testCaseType, installType, selectedGrade);
    }
	
	public void init() {
		executeTestSet = new ExecuteTestSetUtil();
		executeButton.setPreferredSize(new Dimension(100, 20));
		close.setPreferredSize(new Dimension(100, 20));
		featureBox.setPreferredSize(new Dimension(200, 20));
		gradeBox.setPreferredSize(new Dimension(200, 20));
		JLabel label = new JLabel("PSC Automation Executor: "+executeTestSet.getConfiguration().getTrackName());
		Font headerFont = new Font("Tahoma", Font.BOLD, 24);
		Font labelFont = new Font("Tahoma", Font.PLAIN, 12);
		
		List<String> features = executeTestSet.getTestCaseFeatures();
		List<String> grades = executeTestSet.getAvailableGrades(executeTestSet.getConfiguration());
       
		for (int i = 0; i < features.size(); i++) {
			featureBox.addItem(features.get(i));
		}
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
								close.setEnabled(true);
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
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		featureBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedFeature = ((JComboBox) e.getSource()).getSelectedItem().toString();
			}
		});
		
		gradeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedGrade = ((JComboBox) e.getSource()).getSelectedItem().toString();
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
        
        Container cp = getContentPane();
		cp.setLayout(new FlowLayout());
		cp.setBackground(new Color(255, 255, 204));
		label.setFont(headerFont);
		cp.add(label);
		cp.add(Box.createHorizontalGlue());
		cp.add(Box.createRigidArea(new Dimension(400, 10)));
		testCaseLabel.setFont(labelFont);
		cp.add(testCaseLabel);
		criticalOnly.setFont(labelFont);
		cp.add(criticalOnly);
		all.setFont(labelFont);
		cp.add(all);
		cp.add(Box.createHorizontalGlue());
		cp.add(Box.createRigidArea(new Dimension(400, 10)));
		cp.add(installLabel);
		cp.add(freshInstall);
		cp.add(upgradeInstall);
		cp.add(Box.createHorizontalGlue());
		cp.add(Box.createRigidArea(new Dimension(400, 10)));
		cp.add(featureBox);
		cp.add(Box.createHorizontalGlue());
		cp.add(Box.createRigidArea(new Dimension(400, 10)));
		if(grades.size() > 2) {
			cp.add(gradeBox);
			cp.add(Box.createHorizontalGlue());
			cp.add(Box.createRigidArea(new Dimension(400, 10)));
		}
		cp.add(executeButton);
		cp.add(close);
		close.setEnabled(false);
		
	}
	
	private void callThread() {
		Thread executionThread = new Thread(this);
		executionThread.start();
	}

	public static void main(String[] args) {
		run(new SampleApplet(), 450, 300);
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
	    GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsDevice[] allDevices = env.getScreenDevices();
	    int topLeftX, topLeftY, screenX, screenY, windowPosX, windowPosY;

	    if (screen < allDevices.length && screen > -1)
	    {
	        topLeftX = allDevices[screen].getDefaultConfiguration().getBounds().x;
	        topLeftY = allDevices[screen].getDefaultConfiguration().getBounds().y;

	        screenX  = allDevices[screen].getDefaultConfiguration().getBounds().width;
	        screenY  = allDevices[screen].getDefaultConfiguration().getBounds().height;
	    }
	    else
	    {
	        topLeftX = allDevices[0].getDefaultConfiguration().getBounds().x;
	        topLeftY = allDevices[0].getDefaultConfiguration().getBounds().y;

	        screenX  = allDevices[0].getDefaultConfiguration().getBounds().width;
	        screenY  = allDevices[0].getDefaultConfiguration().getBounds().height;
	    }

	    windowPosX = ((screenX - window.getWidth())  / 2) + topLeftX;
	    windowPosY = ((screenY - window.getHeight()) / 2) + topLeftY;

	    window.setLocation(windowPosX, windowPosY);
	}
} 

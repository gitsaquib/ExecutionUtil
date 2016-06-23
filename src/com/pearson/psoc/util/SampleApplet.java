package com.pearson.psoc.util;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class SampleApplet extends JApplet implements Runnable {
		
	private JComboBox featureBox = new JComboBox();
	private JComboBox gradeBox = new JComboBox();
	private JButton executeButton = new JButton("Execute");
	private JButton dllHomeFolderButton = new JButton("Browse");
	private JButton settingsFolderButton = new JButton("Browse");
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
	private static List<String> grades = null;
	private static Result result = null;
	private static JTextField dllHomeField = null;
	private static JTextField settingField = null;
	private static JLabel gradeLabel = null;
	
	public void run() 
    { 
		result = executeTestSet.executeTestCases(selectedFeature, testCaseType, installType, selectedGrade);
		if(result.isError()) {
			JOptionPane.showMessageDialog(null, result.getErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "Pass: " + result.getPassCount() + "\nFail: " + result.getFailCount() + "\nInconclusive: " + result.getInconclusiveCount(), "Status", JOptionPane.INFORMATION_MESSAGE);
		}
		executeButton.setEnabled(true);
		abort.setLabel("Report");
    }
	
	public void init() {
		executeTestSet = new ExecuteTestSetUtil();
		executeButton.setPreferredSize(new Dimension(100, 20));
		dllHomeFolderButton.setPreferredSize(new Dimension(80, 20));
		settingsFolderButton.setPreferredSize(new Dimension(80, 20));
		abort.setPreferredSize(new Dimension(100, 20));
		featureBox.setPreferredSize(new Dimension(200, 20));
		gradeBox.setPreferredSize(new Dimension(200, 20));
		JLabel label = new JLabel("Automation Run: "+executeTestSet.getConfiguration().getTrackName());
		Font headerFont = new Font("Tahoma", Font.BOLD, 24);
		Font labelFont = new Font("Tahoma", Font.PLAIN, 12);
		
		radioGroup = new CheckboxGroup();
        Checkbox criticalOnly = new Checkbox("Critical", radioGroup, false);
        Checkbox all = new Checkbox("All                           ", radioGroup, false);
        JLabel testCaseLabel = new JLabel("Execute Test Cases: ");
        installationGroup = new CheckboxGroup();
        Checkbox freshInstall = new Checkbox("Fresh", installationGroup, false);
        Checkbox upgradeInstall = new Checkbox("Upgrade", installationGroup, false);
        Checkbox alreadyInstall = new Checkbox("Already Installed", installationGroup, false);
        JLabel installLabel = new JLabel("Install:     ");
        dllHomeField = new JTextField(executeTestSet.getConfiguration().getDllHome());
        dllHomeField.setPreferredSize(new Dimension(121, 20));
        dllHomeField.setToolTipText(executeTestSet.getConfiguration().getDllHome());
        
        settingField = new JTextField(executeTestSet.getConfiguration().getTestSettings());
        settingField.setPreferredSize(new Dimension(121, 20));
        settingField.setToolTipText(executeTestSet.getConfiguration().getTestSettings());
        
        grades = executeTestSet.getAvailableGrades(executeTestSet.getConfiguration());
	    if(null == grades || grades.size() > 2) {
	    	for (int i = 0; i < grades.size(); i++) {
				gradeBox.addItem(grades.get(i));
			}
		} else {
			gradeBox.setVisible(false);
			gradeLabel.setVisible(false);
			executeTestSet.generateInputFileForSelectedGrade(executeTestSet.getConfiguration().getMasterFile(), "Grade 1");
			List<String> features = executeTestSet.getTestCaseFeatures();
			featureBox.removeAllItems();
			for (int i = 0; i < features.size(); i++) {
				featureBox.addItem(features.get(i));
			}
		}
        
        JLabel browseExecutables = new JLabel("Debug Folder:  ");
        JLabel browseSettings = new JLabel("Test Settings:  ");
        gradeLabel = new JLabel("Select Grade:    ");
        JLabel componentLabel = new JLabel("Select Feature:  ");
        
        settingsFolderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			    	String path = chooser.getSelectedFile().getAbsolutePath();
			    	settingField.setText(path);
			    	settingField.setPreferredSize(new Dimension(121, 20));
			    	settingField.setToolTipText(path);
			        executeTestSet.getConfiguration().setTestSettings(chooser.getSelectedFile().getAbsolutePath());
			    }
			}
        });
        
		dllHomeFolderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("Browse the folder to process");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			    	String path = chooser.getSelectedFile().getAbsolutePath();
			    	dllHomeField.setText(path);
			    	dllHomeField.setPreferredSize(new Dimension(121, 20));
			        dllHomeField.setToolTipText(path);
			        executeTestSet.getConfiguration().setDllHome(chooser.getSelectedFile().getAbsolutePath());
				    grades = executeTestSet.getAvailableGrades(executeTestSet.getConfiguration());
				    if(null == grades || grades.size() > 2) {
				    	for (int i = 0; i < grades.size(); i++) {
							gradeBox.addItem(grades.get(i));
						}
					} else {
						gradeBox.setVisible(false);
						gradeLabel.setVisible(false);
						executeTestSet.generateInputFileForSelectedGrade(executeTestSet.getConfiguration().getMasterFile(), "Grade 1");
						List<String> features = executeTestSet.getTestCaseFeatures();
						featureBox.removeAllItems();
						for (int i = 0; i < features.size(); i++) {
							featureBox.addItem(features.get(i));
						}
					}
			    }
			}
		});
		
		executeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abort.setLabel("Abort");
				Checkbox selectedInstallCheckbox = installationGroup.getSelectedCheckbox();
				Checkbox selectedTestCaseTypeCheckbox = radioGroup.getSelectedCheckbox();
				if(null != selectedTestCaseTypeCheckbox) {
					if(null != selectedInstallCheckbox) {
						if(selectedFeature.isEmpty() || selectedFeature.equalsIgnoreCase(" --Select-- ")) {
							JOptionPane.showMessageDialog(null, "Please select a feature or All for execution", "Error", JOptionPane.ERROR_MESSAGE);
						} else {
							if(grades.size() > 2 && (selectedGrade.isEmpty() || selectedGrade.equalsIgnoreCase(" --Select-- "))) {
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
				if(abort.getLabel().equalsIgnoreCase("Abort")){
					int response = JOptionPane.showConfirmDialog(null, "Do you realy want to Abort execution?. Execution will stop after finishing currently running test method", 
							"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(response == 0) {
						System.exit(0);
					}
				} else if(abort.getLabel().equalsIgnoreCase("Report")){
					try {
						Desktop.getDesktop().open(new File(result.getOutFile()));
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "Unable to open report file", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
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
		
		myContainer.setBackground(new Color(255, 255, 255));
		myContainer.add(Box.createHorizontalGlue());
		myContainer.add(Box.createRigidArea(new Dimension(500, 1)));
		label.setFont(headerFont);
		label.setForeground(new Color(0, 76, 153));
		ImageComponent image = new ImageComponent("images//AppIcon.PNG");
		myContainer.add(image);
		myContainer.add(Box.createHorizontalGlue());
		myContainer.add(Box.createRigidArea(new Dimension(500, 20)));
		myContainer.add(label);
		myContainer.add(Box.createHorizontalGlue());
		myContainer.add(Box.createRigidArea(new Dimension(500, 20)));
		myContainer.add(browseExecutables);
		browseExecutables.setFont(labelFont);
		myContainer.add(dllHomeField);
		dllHomeField.setEditable(false);
		myContainer.add(dllHomeFolderButton);
		myContainer.add(Box.createHorizontalGlue());
		myContainer.add(Box.createRigidArea(new Dimension(500, 1)));
		myContainer.add(browseSettings);
		browseSettings.setFont(labelFont);
		myContainer.add(settingField);
		settingField.setEditable(false);
		myContainer.add(settingsFolderButton);
		myContainer.add(Box.createHorizontalGlue());
		myContainer.add(Box.createRigidArea(new Dimension(500, 1)));
		testCaseLabel.setFont(labelFont);
		myContainer.add(testCaseLabel);
		criticalOnly.setFont(labelFont);
		myContainer.add(criticalOnly);
		all.setFont(labelFont);
		myContainer.add(all);
		myContainer.add(Box.createHorizontalGlue());
		myContainer.add(Box.createRigidArea(new Dimension(500, 1)));
		myContainer.add(installLabel);
		installLabel.setFont(labelFont);
		myContainer.add(freshInstall);
		freshInstall.setFont(labelFont);
		myContainer.add(upgradeInstall);
		upgradeInstall.setFont(labelFont);
		myContainer.add(alreadyInstall);
		alreadyInstall.setFont(labelFont);
		if(null == grades || grades.size() > 2) {
			myContainer.add(Box.createHorizontalGlue());
			myContainer.add(Box.createRigidArea(new Dimension(500, 1)));
			myContainer.add(gradeLabel);
			gradeLabel.setFont(labelFont);
			myContainer.add(gradeBox);
		} else {
			executeTestSet.generateInputFileForSelectedGrade(executeTestSet.getConfiguration().getMasterFile(), "Grade 1");
			List<String> features = executeTestSet.getTestCaseFeatures();
			featureBox.removeAllItems();
			for (int i = 0; i < features.size(); i++) {
				featureBox.addItem(features.get(i));
			}
		}
		myContainer.add(Box.createHorizontalGlue());
		myContainer.add(Box.createRigidArea(new Dimension(500, 1)));
		myContainer.add(componentLabel);
		componentLabel.setFont(labelFont);
		myContainer.add(featureBox);
		myContainer.add(Box.createHorizontalGlue());
		myContainer.add(Box.createRigidArea(new Dimension(500, 10)));
		myContainer.add(executeButton);
		myContainer.add(abort);
		abort.setEnabled(false);
	}
	
	private void callThread() {
		Thread executionThread = new Thread(this);
		executionThread.start();
	}

	public static void main(String[] args) {
		run(new SampleApplet(), 500, 520);
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

class ImageComponent extends Component {
    BufferedImage img;
    public void paint(Graphics g) {
       g.drawImage(img, 0, 0, null);
    }

    public ImageComponent(String path) {
       try {
          img = ImageIO.read(new File(path));
       } catch (IOException e) {
          e.printStackTrace();
       }
    }

    public Dimension getPreferredSize() {
       if (img == null) {
          return new Dimension(100,100);
       } else {
          return new Dimension(img.getWidth(), img.getHeight());
       }
    }
}

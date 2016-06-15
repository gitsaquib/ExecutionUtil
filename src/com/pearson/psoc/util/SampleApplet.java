package com.pearson.psoc.util;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

public class SampleApplet extends JApplet {
		
	private JComboBox cBox = new JComboBox();
	private JButton b = new JButton("Execute");
	private int count = 0;
	private String selectedFeature = "";
	private ExecuteTestSetUtil executeTestSet = null;
	private CheckboxGroup radioGroup = null;
	
	public void init() {
		executeTestSet = new ExecuteTestSetUtil();
		b.setPreferredSize(new Dimension(100, 20));
		cBox.setPreferredSize(new Dimension(200, 20));
		JLabel label = new JLabel("PSC Automation Executor: "+executeTestSet.getConfiguration().getTrackName());
		Font myFont = new Font("Serif",Font.BOLD,24);
		label.setFont(myFont);
		List<String> features = executeTestSet.getTestCaseFeatures();
       
		for (int i = 0; i < features.size(); i++)
			cBox.addItem(features.get(i));
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Checkbox selectedCheckbox = radioGroup.getSelectedCheckbox();
					if(null != selectedCheckbox) {
						if(selectedFeature.isEmpty() || selectedFeature.equalsIgnoreCase(" --Select-- ")) {
							JOptionPane.showMessageDialog(null, "Please select a feature or All for execution", "Error", JOptionPane.ERROR_MESSAGE);
						} else {
							executeTestSet.executeTestCases(selectedFeature, selectedCheckbox.getLabel());
						}
					} else {
						JOptionPane.showMessageDialog(null, "Please select a Critical or All for execution", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			cBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					selectedFeature = ((JComboBox) e.getSource()).getSelectedItem().toString();
				}
			});
			radioGroup = new CheckboxGroup();
	        Checkbox criticalOnly = new Checkbox("Critical", radioGroup, false);
	        Checkbox all = new Checkbox("All", radioGroup, false);
	        JLabel testCaseLabel = new JLabel("Want to execute only Critical or All?");
	        
	        Container cp = getContentPane();
			cp.setLayout(new FlowLayout());
			cp.add(label);
			cp.add(Box.createHorizontalGlue());
			cp.add(Box.createRigidArea(new Dimension(400, 10)));
			cp.add(testCaseLabel);
			cp.add(criticalOnly);
			cp.add(all);
			cp.add(Box.createHorizontalGlue());
			cp.add(Box.createRigidArea(new Dimension(400, 10)));
			cp.add(cBox);
			cp.add(Box.createHorizontalGlue());
			cp.add(Box.createRigidArea(new Dimension(400, 10)));
			cp.add(b);
	}

	public static void main(String[] args) {
		run(new SampleApplet(), 400, 300);
	}

	public static void run(JApplet applet, int width, int height) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.getContentPane().add(applet);
		frame.setSize(width, height);
		applet.init();
		applet.start();
		frame.setVisible(true);
	}
} 

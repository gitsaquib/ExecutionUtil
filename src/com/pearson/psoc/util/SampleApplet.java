package com.pearson.psoc.util;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SampleApplet extends JApplet {
		
	private JComboBox c = new JComboBox();
	private JButton b = new JButton("Execute");
	private int count = 0;
	private String selectedFeature = "";
	private Label label = new Label("PSC Automation Executor");
	
	public void init() {
		ExecuteTestSet executeTestSet = new ExecuteTestSet();
		List<String> features = executeTestSet.getTestCaseFeatures();
		for (int i = 0; i < features.size(); i++)
			c.addItem(features.get(i));
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(selectedFeature.isEmpty() || selectedFeature.equalsIgnoreCase(" --Select-- ")) {
						JOptionPane.showMessageDialog(null, "Please select a feature or All for execution", "Error", JOptionPane.ERROR_MESSAGE);
					} else {
						System.out.println(selectedFeature);
					}
				}
			});
			c.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					selectedFeature = ((JComboBox) e.getSource()).getSelectedItem().toString();
				}
			});
			Container cp = getContentPane();
			cp.setLayout(new FlowLayout());
			cp.add(label);
			cp.add(c);
			cp.add(b);
			
	}

	public static void main(String[] args) {
		run(new SampleApplet(), 800, 600);
	}

	public static void run(JApplet applet, int width, int height) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(applet);
		frame.setSize(width, height);
		applet.init();
		applet.start();
		frame.setVisible(true);
	}
} 

package com.pearson.psoc.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExecuteTestSet {
	private static Configuration configuration;
	private static Map<String, TestCase> testCases = new LinkedHashMap<String, TestCase>();
	
	public ExecuteTestSet() {
		configuration = ExecuteTestSetUtil.readConfigFile();
		if (null != configuration) {
			try {
				if(configuration.getInputFile().contains(".xls") || configuration.getInputFile().contains(".XLS")) {
					testCases = ExecuteTestSetUtil.readXlsInputFile(configuration
							.getInputFile());	
				} else {
					testCases = ExecuteTestSetUtil.readTabDelimitedInputFile(configuration
							.getInputFile());
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public List<String> getTestCaseFeatures() {
		List<String> features = new ArrayList<String>();
		features.add(" --Select-- ");
		if (null != configuration) {
			Set<String> testCaseIds = testCases.keySet();
			for (String testCaseId : testCaseIds) {
				TestCase testCase = testCases.get(testCaseId);
				if(!features.contains(testCase.getTestCaseFeature())) {
					features.add(testCase.getTestCaseFeature());
				}
			}
		}
		features.add("All");
		return features;
	}
	
	public void executeTestCases() {
		if (null != configuration) {
			Set<String> testCaseIds = testCases.keySet();
			List<String> failedCases = new LinkedList<String>();
			int countOfRun = 0;
			for (String testCaseId : testCaseIds) {
				TestCase testCase = testCases.get(testCaseId);
				countOfRun++;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				String status = executeCommand(testCase.getTestCaseName(), configuration);
				if (!status.equals("Passed")) {
					failedCases.add(testCaseId);
				} else {
					System.out.println(testCaseId + "\t" + testCase.getTestCaseName() +"\t"+ testCase.getTestCaseFeature() + "\tPass");
				}

				if (configuration.getRestartSeetest().equalsIgnoreCase("true")) {
					if (countOfRun == Integer.parseInt(configuration
							.getRunCount())) {
						try {
							ExecuteTestSetUtil.restartSeetest(configuration
									.getSeeTest());
							countOfRun = 0;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			countOfRun = 0;
			for (String testCaseId : failedCases) {
				try {
					countOfRun = reRunFailedCases(configuration, testCases, countOfRun, testCaseId);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private int reRunFailedCases(Configuration configuration,
			Map<String, TestCase> testCases, int countOfRun, String testCaseId)
			throws InterruptedException {
		TestCase testCase = testCases.get(testCaseId);
		countOfRun++;
		Thread.sleep(3000);
		String status = executeCommand(testCase.getTestCaseName(), configuration);
		if(configuration.getRetryCount() == "1") {
			if(status.equalsIgnoreCase("Passed")) {
				System.out.println(testCaseId + "\t" + testCase.getTestCaseName() +"\t"+ testCase.getTestCaseFeature() + "\tPass");
			} else {
				System.out.println(testCaseId + "\t" + testCase.getTestCaseName() +"\t"+ testCase.getTestCaseFeature() + "\t"+ status);
			}
		} else {
			if (!status.equals("Passed")) {
				countOfRun++;
				status = executeCommand(testCase.getTestCaseName(), configuration);
				if(status.equalsIgnoreCase("Passed")) {
					System.out.println(testCaseId + "\t" + testCase.getTestCaseName() +"\t"+ testCase.getTestCaseFeature() + "\tPass");
				} else {
					System.out.println(testCaseId + "\t" + testCase.getTestCaseName() +"\t"+ testCase.getTestCaseFeature() + "\t"+ status);
				}
			} else {
				System.out.println(testCaseId + "\t" + testCase.getTestCaseName() +"\t"+ testCase.getTestCaseFeature() + "\tPass");
			}
		}
		if (configuration.getRestartSeetest().equalsIgnoreCase("true")) {
			if (countOfRun == Integer.parseInt(configuration
					.getRunCount())) {
				try {
					ExecuteTestSetUtil.restartSeetest(configuration
							.getSeeTest());
					countOfRun = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return countOfRun;
	}

	private String executeCommand(String testMethodName,
			Configuration configuration) {
		String status = ExecuteTestSetUtil.callCommandPrompt(
				configuration.getDllHome(), configuration.getMsTest(),
				configuration.getTestSettings(), testMethodName,
				configuration.getDllName());
		return status;
	}
}

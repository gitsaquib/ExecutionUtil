package com.pearson.psoc.util;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExecuteTestSet {

	public static void main(String args[]) throws Exception {
		Configuration configuration = ExecuteTestSetUtil.readConfigFile();
		if (null != configuration) {

			Map<String, String> testCases = new LinkedHashMap<String, String>();
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
			Set<String> testCaseIds = testCases.keySet();
			List<String> failedCases = new LinkedList<String>();
			int countOfRun = 0;
			for (String testCaseId : testCaseIds) {
				String testMethodName = testCases.get(testCaseId);
				countOfRun++;
				Thread.sleep(5000);
				String status = executeCommand(testMethodName, configuration);
				if (!status.equals("Passed")) {
					failedCases.add(testCaseId);
				} else {
					System.out
							.println(testCaseId + "\tPass\t" + testMethodName);
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
				countOfRun = reRunFailedCases(configuration, testCases, countOfRun, testCaseId);
			}
		}
		/*
		 * try { ExecuteTestSetUtil.getTestCasesDetails(
		 * "D:\\Project\\PSoC\\code\\Win\\WinPSCAutomation\\Pearson.PSCWinAutomation.212App"
		 * ); } catch (IOException e) { e.printStackTrace(); }
		 */
		/*
		 * try { ExecuteTestSetUtil.getLoginDetails(
		 * "D:\\Project\\PSoC\\code\\SeeTestAutomation\\Pearson.PSCAutomation.212App"
		 * ); } catch (IOException e) { e.printStackTrace(); }
		 */
	}

	private static int reRunFailedCases(Configuration configuration,
			Map<String, String> testCases, int countOfRun, String testCaseId)
			throws InterruptedException {
		String testMethodName = testCases.get(testCaseId);
		countOfRun++;
		Thread.sleep(10000);
		String status = executeCommand(testMethodName, configuration);
		if (!status.equals("Passed")) {
			countOfRun++;
			status = executeCommand(testMethodName, configuration);
			
			System.out.println(testCaseId + "\t" + status + "\t"
					+ testMethodName);
			/*if (!status.equals("Passed")) {
				countOfRun++;
				status = executeCommand(testMethodName, configuration);
				System.out.println(testCaseId + "\t" + status + "\t"
						+ testMethodName);
			} else {
				System.out.println(testCaseId + "\t" + status + "\t"
					+ testMethodName);
			}*/
		} else {
			System.out
					.println(testCaseId + "\tPass\t" + testMethodName);
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

	private static String executeCommand(String testMethodName,
			Configuration configuration) {
		String status = ExecuteTestSetUtil.callCommandPrompt(
				configuration.getDllHome(), configuration.getMsTest(),
				configuration.getTestSettings(), testMethodName,
				configuration.getDllName());
		return status;
	}
}

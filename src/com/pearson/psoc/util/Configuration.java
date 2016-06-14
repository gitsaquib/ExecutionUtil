package com.pearson.psoc.util;

public class Configuration {
	
	private String dllName;
	private String testSettings;
	private String msTest;
	private String dllHome;
	private String seeTest;
	private String runCount;
	private String inputFile;
	private String restartSeetest;
	
	public String getDllName() {
		return dllName;
	}
	public void setDllName(String dllName) {
		this.dllName = dllName;
	}
	public String getTestSettings() {
		return testSettings;
	}
	public void setTestSettings(String testSettings) {
		this.testSettings = testSettings;
	}
	public String getMsTest() {
		return msTest;
	}
	public void setMsTest(String msTest) {
		this.msTest = msTest;
	}
	public String getDllHome() {
		return dllHome;
	}
	public void setDllHome(String dllHome) {
		this.dllHome = dllHome;
	}
	public String getSeeTest() {
		return seeTest;
	}
	public void setSeeTest(String seeTest) {
		this.seeTest = seeTest;
	}
	public String getRunCount() {
		return runCount;
	}
	public void setRunCount(String runCount) {
		this.runCount = runCount;
	}
	public String getInputFile() {
		return inputFile;
	}
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
	public String getRestartSeetest() {
		return restartSeetest;
	}
	public void setRestartSeetest(String restartSeetest) {
		this.restartSeetest = restartSeetest;
	}
}

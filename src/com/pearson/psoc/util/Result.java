package com.pearson.psoc.util;

public class Result {
	
	private boolean error;
	private String errorMessage;
	private int passCount;
	private	int failCount;
	private	int inconclusiveCount;
	private String outFile;
	
	public boolean isError() {
		return error;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public int getPassCount() {
		return passCount;
	}
	public void setPassCount(int passCount) {
		this.passCount = passCount;
	}
	public int getFailCount() {
		return failCount;
	}
	public void setFailCount(int failCount) {
		this.failCount = failCount;
	}
	public int getInconclusiveCount() {
		return inconclusiveCount;
	}
	public void setInconclusiveCount(int inconclusiveCount) {
		this.inconclusiveCount = inconclusiveCount;
	}
	public String getOutFile() {
		return outFile;
	}
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
}

package com.pearson.psoc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ExecuteTestSetUtil {
	
	private static final String TASKLIST = "tasklist";
	private static final String KILL = "taskkill /F /IM ";
	private Configuration configuration;
	private static Map<String, TestCase> testCases = new LinkedHashMap<String, TestCase>();
	private PrintWriter writer = null;
	private String filePath = null;
	private Result result = null;
	
	public ExecuteTestSetUtil() {
		configuration = readConfigFile();
	}
	
	public List<String> getTestCaseFeatures() {
		List<String> features = new ArrayList<String>();
		features.add(" --Select-- ");
		if (null != configuration) {
			try {
				if(configuration.getInputFile().contains(".xls") || configuration.getInputFile().contains(".XLS")) {
					testCases = readXlsInputFile(configuration.getInputFile());	
				} else {
					testCases = readTabDelimitedInputFile(configuration.getInputFile());
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
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
	
	public Result executeTestCases(String feature, String testCaseType, String installType, String selectedGrade) {
		result = new Result();
		if (null != configuration) {
			if(!copyGradeSpecificXmlFiles(configuration, selectedGrade)) {
				result.setError(true);
				result.setErrorMessage("Unable to copy login details file");
			}
			try {
		        final Calendar c = Calendar.getInstance();
		        c.setTime(new Date());
				File folder = new File("Results"+File.separator+c.get(Calendar.YEAR)+File.separator+(new SimpleDateFormat("MMM").format(c.getTime()))+File.separator+configuration.getTrackName()+File.separator+c.get(Calendar.DAY_OF_MONTH));
				folder.mkdirs();
				filePath = folder + File.separator + "Output-"+selectedGrade+"_"+c.getTime().getHours()+"-"+c.getTime().getMinutes()+"-"+c.getTime().getSeconds()+".txt";
				result.setOutFile(filePath);
				writer =  new PrintWriter(filePath, "UTF-8");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Set<String> testCaseIds = testCases.keySet();
			List<String> failedCases = new LinkedList<String>();
			int countOfRun = 0;
			if(installApp(installType)) {
				for (String testCaseId : testCaseIds) {
					TestCase testCase = testCases.get(testCaseId);
					if((feature.equalsIgnoreCase("All") || testCase.getTestCaseFeature().equalsIgnoreCase(feature))
							&& (testCaseType.trim().equalsIgnoreCase("All") || testCase.getTestCasePriority().equalsIgnoreCase(testCaseType))
							) {
						countOfRun++;
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						String status = executeCommand(testCase.getTestCaseName(), configuration);
						if (!status.startsWith("Pass")) {
							failedCases.add(testCaseId);
						} else {
							writeOutputFile(testCaseId + "\t" + testCase.getTestCaseName() +"\t"+ testCase.getTestCaseFeature() + "\tPass");
						}
					}
					if (configuration.getRestartSeetest().equalsIgnoreCase("true")) {
						if (countOfRun == Integer.parseInt(configuration
								.getRunCount())) {
							try {
								restartSeetest(configuration.getSeeTest());
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
			} else {
				result.setError(true);
				result.setErrorMessage("Unable to install app by using selected method.");
			}
		}
		writer.close();
		return result;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	private int reRunFailedCases(Configuration configuration,
			Map<String, TestCase> testCases, int countOfRun, String testCaseId)
			throws InterruptedException {
		TestCase testCase = testCases.get(testCaseId);
		countOfRun++;
		Thread.sleep(3000);
		String status = executeCommand(testCase.getTestCaseName(), configuration);
		if(configuration.getRetryCount() == "1") {
			if(status.startsWith("Pass")) {
				writeOutputFile(testCaseId + "\t" + testCase.getTestCaseName() +"\t"+ testCase.getTestCaseFeature() + "\tPass");
			} else {
				writeOutputFile(testCaseId + "\t" + testCase.getTestCaseName() +"\t"+ testCase.getTestCaseFeature() + "\t"+ status);
			}
		} else {
			if (!status.startsWith("Pass")) {
				countOfRun++;
				status = executeCommand(testCase.getTestCaseName(), configuration);
				if(status.startsWith("Pass")) {
					writeOutputFile(testCaseId + "\t" + testCase.getTestCaseName() +"\t"+ testCase.getTestCaseFeature() + "\tPass");
				} else {
					writeOutputFile(testCaseId + "\t" + testCase.getTestCaseName() +"\t"+ testCase.getTestCaseFeature() + "\t"+ status);
				}
			} else {
				writeOutputFile(testCaseId + "\t" + testCase.getTestCaseName() +"\t"+ testCase.getTestCaseFeature() + "\tPass");
			}
		}
		if (configuration.getRestartSeetest().equalsIgnoreCase("true")) {
			if (countOfRun == Integer.parseInt(configuration
					.getRunCount())) {
				try {
					restartSeetest(configuration.getSeeTest());
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
		String status = callCommandPrompt(
				configuration.getDllHome(), configuration.getMsTest(),
				configuration.getTestSettings(), testMethodName,
				configuration.getDllName());
		return status;
	}
	
	public static String callCommandPrompt(String debugFolderPath, String msTestExePath, String testSettingsPath, String testMethodName, String dllName) {
		String status = "Inconclusive";
		Runtime rt = Runtime.getRuntime();
		String[] commands = {"test.bat", debugFolderPath, msTestExePath, testSettingsPath, testMethodName, dllName};
		Process proc = null;
		try {
			proc = rt.exec(commands);
		} catch (IOException e) {
			System.out.println("1. Error while executing test: "+e.getMessage());
		}
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String s = null;
		int lineNum = 1;
		try {
			while ((s = stdInput.readLine()) != null) {
				if(lineNum == 14) {
					break;
				}
				lineNum++;
			}
		} catch (IOException e) {
			System.out.println("2. Error while executing test: "+e.getMessage());
		}
		if(s != null) {
			if(s.startsWith("Pass")) {
				status = "Pass";
			} else if(s.startsWith("Fail")) {
				status = "Fail";
				//System.out.println(testMethodName+"\t"+s);
			}
		}
		return status;
	}
	
	public static void getTestCasesDetails(String rootFolder) throws IOException {
		
		File classFilesFolder = new File(rootFolder);
    	
    	FilenameFilter fileNameFilter = new FilenameFilter() {
    		   
            @Override
            public boolean accept(File dir, String name) {
               if(name.lastIndexOf('.')>0)
               {
                  int lastIndex = name.lastIndexOf('.');
                  String str = name.substring(lastIndex);
                  if(str.equals(".cs"))
                  {
                     return true;
                  }
               }
               return false;
            }
        };
        
    	File classFiles[] = classFilesFolder.listFiles(fileNameFilter);
		
    	for (File classFile:classFiles) {
		    LineNumberReader rdr = new LineNumberReader(new FileReader(classFile));
		    try {
		        String line = rdr.readLine();
		        while(line != null) {
			        if (line.indexOf("WorkItem") >= 0) {
			        	String testCase = getTestCaseId(line);
			        	String functionName = getFunctionName(rdr);
			        	if(testCase.contains(",")) {
			        		String testCases[] = testCase.split(",");
			        		for(String caseId:testCases) {
			        			System.out.println(classFile.getName() + "\t" + caseId + "\t" + functionName);
			        		}
			        	} else {
			        		System.out.println(classFile.getName() + "\t" + testCase + "\t" + functionName);
			        	}
			        }
			        line = rdr.readLine();
		        }
		    } finally {
		        rdr.close();
		    }
    	}
	}
	
	public static void getLoginDetails(String rootFolder) throws IOException {
		
		File classFilesFolder = new File(rootFolder);
    	
    	FilenameFilter fileNameFilter = new FilenameFilter() {
    		   
            @Override
            public boolean accept(File dir, String name) {
               if(name.lastIndexOf('.')>0)
               {
                  int lastIndex = name.lastIndexOf('.');
                  String str = name.substring(lastIndex);
                  if(str.equals(".cs"))
                  {
                     return true;
                  }
               }
               return false;
            }
        };
        
    	File classFiles[] = classFilesFolder.listFiles(fileNameFilter);
		
    	for (File classFile:classFiles) {
		    LineNumberReader rdr = new LineNumberReader(new FileReader(classFile));
		    try {
		        String line = rdr.readLine();
		        while(line != null) {
			        if (line.indexOf("Login.GetLogin") >= 0) {
			        	String testCase = getTestCaseId(getTestCaseId(classFile, rdr.getLineNumber()));
			        	if(testCase.contains(",")) {
			        		String testCases[] = testCase.split(",");
			        		for(String caseId:testCases) {
			        			System.out.println(classFile.getName() + "\t" + caseId + "\t" + line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
			        		}
			        	} else {
			        		System.out.println(classFile.getName() + "\t" + testCase + "\t" + line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
			        	}
			        }
			        line = rdr.readLine();
		        }
		    } finally {
		        rdr.close();
		    }
    	}
	}
	
	private static String getTestCaseId(File classFile, int lineNumberOfLogin) throws IOException {
		LineNumberReader rdr = new LineNumberReader(new FileReader(classFile));
	    try {
	    	Map<Integer, String> lines = new HashMap<Integer, String>();
	    	String line = rdr.readLine();
	        while(line != null && rdr.getLineNumber() < lineNumberOfLogin) {
	        	lines.put(rdr.getLineNumber(), line);
		        line = rdr.readLine();
	        }
	        boolean testIdNotFound = true;
	        while(testIdNotFound) {
	        	String lineFromMap = lines.get(lineNumberOfLogin);
	        	if(null != lineFromMap && lineFromMap.contains("WorkItem")) {
	        		testIdNotFound = false;
	        		return lineFromMap;
	        	} else {
	        		lineNumberOfLogin--;
	        	}
	        }
	    } finally {
	        rdr.close();
	    }
		return "";
	}
	
	public static String getFunctionName(LineNumberReader rdr) throws IOException {
		String line = "";
	    try {
	        line = rdr.readLine();
	        while(null != line && !line.trim().startsWith("public")) {
	        	line = rdr.readLine();
	        } 
	        if(null != line) {
	        	line = line.replace("public void", "").trim().replace("()", "");
	        }
	    } finally {

	    }
	    return line;
	}
	
	public static String getTestCaseId(String line) throws IOException {
	    try {
	        if(null != line) {
	        	line = line.trim().replace("[", "").replaceAll("WorkItem\\(", "TC").replaceAll("\\)", "").replace("]", "");
	        }
	    } finally {
	        
	    }
	    return line;
	}
	
	public static boolean isProcessRunning(String serviceName) throws Exception {
		Process p = Runtime.getRuntime().exec(TASKLIST);
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			if (line.contains(serviceName)) {
				return true;
			}
		}
		return false;
	}
	
	public static void restartSeetest(String serviceName) throws Exception {
		killProcess(serviceName.substring(serviceName.lastIndexOf("\\")+1));
		Thread.sleep(90000);
		if(isProcessRunning(serviceName)) {
			Thread.sleep(60000);	
		}
		startProcess(serviceName);
		if(!isProcessRunning(serviceName)) {
			Thread.sleep(30000);
		}
	}
	

	public static void killProcess(String serviceName) throws Exception {
		Runtime.getRuntime().exec(KILL + serviceName);
	}
	
	public static void startProcess(String serviceName) throws IOException {
		Process process = new ProcessBuilder(serviceName).start();
		InputStream is = process.getInputStream();
		/*InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}*/
	}
	
	public static Map<String, TestCase> readXlsInputFile(String inputSheet) throws IOException {
		Map<String, TestCase> testCases = new LinkedHashMap<String, TestCase>();
		File myFile = new File(inputSheet);
        FileInputStream fis = new FileInputStream(myFile);
        HSSFWorkbook myWorkBook = new HSSFWorkbook (fis);
        HSSFSheet mySheet = myWorkBook.getSheetAt(0);
        Iterator<HSSFRow> rowIterator = mySheet.rowIterator();
        while (rowIterator.hasNext()) {
        	HSSFRow row = rowIterator.next();
        	TestCase testCase = new TestCase();
        	testCase.setTestCaseId(row.getCell(Short.parseShort("0")).getNumericCellValue()+"");
        	testCase.setTestCaseName(row.getCell(Short.parseShort("1")).getStringCellValue());
        	testCase.setTestCaseFeature(row.getCell(Short.parseShort("2")).getStringCellValue());
        	testCase.setTestCasePriority(row.getCell(Short.parseShort("3")).getStringCellValue());
        	testCases.put(""+(row.getCell(Short.parseShort("0")).getNumericCellValue()), testCase);
        }
        return testCases;
	}
	
	public static Map<String, TestCase> readTabDelimitedInputFile(String inputSheet) throws IOException {
		Map<String, TestCase> testCases = new LinkedHashMap<String, TestCase>();
		File myFile = new File(inputSheet);
        Scanner scan = new Scanner(myFile);
        String line="";
        while (scan.hasNextLine()) {
            line = scan.nextLine();
            String[] split=line.split("\t");
            TestCase testCase = new TestCase();
            testCase.setTestCaseId(split[0]);
            testCase.setTestCaseName(split[1]);
            testCase.setTestCaseFeature(split[2]);
            testCase.setTestCasePriority(split[3]);
            testCases.put(split[0], testCase);
        } 
        return testCases;
	}

	public static Configuration readConfigFile(){
    	Properties prop = new Properties();
    	File file = new File("config.properties");
    	InputStream stream = null;
		try {
			stream = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			System.out.println("1. Unable to load config properties");
		}
    	try {
    		Configuration configuration = new Configuration();
			prop.load(stream);
			final String dir = System.getProperty("user.dir");
	        System.out.println("current dir = " + dir);
			configuration.setDllHome(dir+File.separator+prop.getProperty("DLLHOME"));
			configuration.setDllName(prop.getProperty("DLLNAME"));
			configuration.setMsTest(prop.getProperty("MSTEST"));
			configuration.setRunCount(prop.getProperty("RUNCOUNT"));
			configuration.setSeeTest(prop.getProperty("SEETEST"));
			configuration.setTestSettings(dir+File.separator+prop.getProperty("TESTSETTINGS"));
			configuration.setInputFile(dir+File.separator+prop.getProperty("INPUTFILE"));
			configuration.setRestartSeetest(prop.getProperty("RESTARTSEETEST"));
			configuration.setRetryCount(prop.getProperty("RETRYCOUNT"));
			configuration.setTrackName(prop.getProperty("TRACKNAME"));
			configuration.setMasterFile(dir+File.separator+prop.getProperty("MASTERFILE"));
			return configuration;
    	} catch(Exception e) {
    		System.out.println("2. Unable to load config properties");
    	}
    	return null;
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}
	
	public void writeOutputFile(String message) {
		if(message.contains("Pass")) {
			result.setPassCount(result.getPassCount()+1);
		} else if(message.contains("Fail")) {
			result.setFailCount(result.getFailCount()+1);
		} else if(message.contains("Inconclusive")) {
			result.setInconclusiveCount(result.getInconclusiveCount()+1);
		}
		writer.println(message);
	}
	
	public List<String> getAvailableGrades(Configuration configuration) {
		List<String> availableGrades = new ArrayList<String>();
		String dllHome = configuration.getDllHome();
		File loginXmlsFolder = new File(dllHome + File.separator + "LoginXmls");
		if(loginXmlsFolder.exists()) {
			File xmlFiles[] = loginXmlsFolder.listFiles();
			if(xmlFiles.length > 0) {
				availableGrades.add(" --Select-- ");
			}
			File file = null;
			for(int i = 2; i <= 12; i++) {
				file = new File(loginXmlsFolder, "Logins - Grade "+i+".xml");
				if(file.exists()) {
					availableGrades.add("Grade "+i);
				}
			}
		}
		return availableGrades;
	}
	
	public boolean copyGradeSpecificXmlFiles(Configuration configuration, String selectedGrade) {
		if(selectedGrade.equalsIgnoreCase("") || selectedGrade.equalsIgnoreCase("Grade 1")) {
			return true;
		}
		String dllHome = configuration.getDllHome();
		Path source = new File(dllHome + File.separator + "LoginXmls" + File.separator + "Logins - " + selectedGrade + ".xml").toPath();
		Path target = new File(dllHome + File.separator + "Xml" + File.separator + "Logins.xml").toPath();
		try {
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public boolean generateInputFileForSelectedGrade(String inputSheet, String selectedGrade) {
		File myFile = new File(inputSheet);
        FileInputStream fis;
        HSSFWorkbook myWorkBook;
		try {
			fis = new FileInputStream(myFile);
			myWorkBook = new HSSFWorkbook (fis);
			HSSFSheet mySheet = myWorkBook.getSheetAt(0);
	        Iterator<HSSFRow> rowIterator = mySheet.rowIterator();
	        short indexOfSelectedGrade = 0;
	        HSSFRow headerRow = rowIterator.next();
	        for(short i = 0; i < 11; i++) {
	        	if(null != headerRow.getCell(i)) {
		        	String headerName = headerRow.getCell(i).getStringCellValue();
		        	if(headerName != null && headerName.equalsIgnoreCase(selectedGrade)) {
		        		indexOfSelectedGrade = i;
		        		break;
		        	}
	        	}
	        }
	        File inputFile = new File(configuration.getInputFile());
	        if(inputFile.exists()) {
	        	inputFile.delete();
	        }
	        PrintWriter inputFileWriter =  new PrintWriter(configuration.getInputFile(), "UTF-8");
	        while (rowIterator.hasNext()) {
	        	HSSFRow row = rowIterator.next();
	        	boolean isTestCaseSelectedForSelectedGarde = row.getCell(indexOfSelectedGrade)!= null 
	        				? (row.getCell(indexOfSelectedGrade).getStringCellValue().equalsIgnoreCase("Yes") ? true : false ) : false;
	        	if(isTestCaseSelectedForSelectedGarde) {
		        	String rowStr = (row.getCell(Short.parseShort("0")).getNumericCellValue()+"").replace(".0", "") 
		        			+ "\t" + row.getCell(Short.parseShort("1")).getStringCellValue() 
		        			+ "\t" + row.getCell(Short.parseShort("2")).getStringCellValue() 
		        			+ "\t" + row.getCell(Short.parseShort("3")).getStringCellValue();
		        	inputFileWriter.println(rowStr);
	        	}
	        }
	        inputFileWriter.close();
	        return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean installApp(String installOption) {
		/*String status = "";
		if(installOption.equalsIgnoreCase("Fresh")) {
			status = executeCommand("InstallFreshApp", configuration);
		} else {
			status = executeCommand("InstallUpgradeApp", configuration);
		}
		return status.startsWith("Pass");*/
		return true;
	}
}

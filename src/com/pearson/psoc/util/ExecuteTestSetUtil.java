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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ExecuteTestSetUtil {
	
	private static final String TASKLIST = "tasklist";
	private static final String KILL = "taskkill /F /IM ";
	
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
				status = "Passed";
			} else if(s.startsWith("Fail")) {
				status = "Failed";
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
	
	public static Map<String, String> readXlsInputFile(String inputSheet) throws IOException {
		Map<String, String> testCases = new LinkedHashMap<String, String>();
		File myFile = new File(inputSheet);
        FileInputStream fis = new FileInputStream(myFile);
        HSSFWorkbook myWorkBook = new HSSFWorkbook (fis);
        HSSFSheet mySheet = myWorkBook.getSheetAt(0);
        Iterator<HSSFRow> rowIterator = mySheet.rowIterator();
        while (rowIterator.hasNext()) {
        	HSSFRow row = rowIterator.next();
        	testCases.put(""+(row.getCell(Short.parseShort("0")).getNumericCellValue()), row.getCell(Short.parseShort("1")).getStringCellValue());
        }
        return testCases;
	}
	
	public static Map<String, String> readTabDelimitedInputFile(String inputSheet) throws IOException {
		Map<String, String> testCases = new LinkedHashMap<String, String>();
		File myFile = new File(inputSheet);
        Scanner scan = new Scanner(myFile);
        String line="";
        while (scan.hasNextLine()) {
            line = scan.nextLine();
            String[] split=line.split("\t");
            testCases.put(split[0], split[1]);
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
			configuration.setDllHome(prop.getProperty("DLLHOME"));
			configuration.setDllName(prop.getProperty("DLLNAME"));
			configuration.setMsTest(prop.getProperty("MSTEST"));
			configuration.setRunCount(prop.getProperty("RUNCOUNT"));
			configuration.setSeeTest(prop.getProperty("SEETEST"));
			configuration.setTestSettings(prop.getProperty("TESTSETTINGS"));
			configuration.setInputFile(prop.getProperty("INPUTFILE"));
			configuration.setRestartSeetest(prop.getProperty("RESTARTSEETEST"));
			return configuration;
    	} catch(Exception e) {
    		System.out.println("2. Unable to load config properties");
    	}
    	return null;
	}
}

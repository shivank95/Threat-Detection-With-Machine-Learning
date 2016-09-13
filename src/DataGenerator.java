
import com.csvreader.CsvWriter;

import java.io.*;
import java.sql.Time;
import java.util.*;

import java.util.Random;

public class DataGenerator {
	
	String filePath = "";
	int recordCount = 0;
	String filePath2 = "";
	String filePath3 = "";
	String filePath4 = "";
	String holdingPath = "";
	int userCount = 0;
	int record_type = 0;
	
	
	public DataGenerator (String filePath, int recordCnt, String filePath2, String filePath3, String filePath4, int userCnt, String filePath5) {
		
		this.filePath = filePath;
		deleteFileIfExists(filePath);
		
		this.recordCount = recordCnt;
		
		this.filePath2 = filePath2;
		deleteFileIfExists(filePath2);
		
		this.filePath3 = filePath3;
		deleteFileIfExists(filePath3);
		
		this.filePath4 = filePath4;
		deleteFileIfExists(filePath4);
		
		this.holdingPath = filePath5;
		deleteFileIfExists(filePath5);
		
		//this.button_type = button;
		this.userCount = userCnt;
		this.record_type = 1;
	}
	
	private void generateRecord(int count, int oneUser) throws IOException {
		
		int flag = count;
		
		Random random = new Random();
		
		//String[] contents = new String[10];
		ArrayList<String> fullContents = new ArrayList<String>();
		ArrayList<String> contents = new ArrayList<String>();
		int offset = 80;
		
		if (oneUser == 0) {
			int userID = (random.nextInt(this.userCount) + 1);
			fullContents.add("" + userID);
		}
		else {
			fullContents.add("" + oneUser);
		}
		
		
		//Expected Number of Sensitive Records
		int sensitiveDataExpected = (random.nextInt(50) + offset);
		contents.add("" + sensitiveDataExpected);
		fullContents.add("" + sensitiveDataExpected);
		
		//Actual Number of Sensitive Records
		int sensitiveDataCount = generateSensitiveDataRecordsCount(sensitiveDataExpected, random, flag);
		
		
		contents.add("" + sensitiveDataCount);
		fullContents.add("" + sensitiveDataCount);
		
		//Number of ExpectedDataStores
		int expectedDataStores = random.nextInt(5) + 1;
		contents.add("" + expectedDataStores);
		fullContents.add("" + expectedDataStores);
		
		//Number of DataStores
		int NoOfDataStores = generateDataStoreCount(sensitiveDataCount, expectedDataStores, random);
		contents.add("" + NoOfDataStores);
		fullContents.add("" + NoOfDataStores);
		
		//Number of ExpectedRequests
		int expectedRequests = random.nextInt(9) + 1;
		contents.add("" + expectedRequests);
		fullContents.add("" + expectedRequests);
		
		//Number of ActualRequests
		int sensitiveRequests = generateSensitiveRequests(sensitiveDataCount, expectedRequests, random);
		contents.add("" + sensitiveRequests);
		fullContents.add("" + sensitiveRequests);
		
		//AnomalyDegree 1 - Medium, 2 - Excessive, 3 - Highly Excessive
		int diffSDR = relativeDifference(sensitiveDataCount, sensitiveDataExpected);
		int diffStores = relativeDifference(NoOfDataStores, expectedDataStores);
		int diffRequests = relativeDifference(sensitiveRequests, expectedRequests);
		
		int anomalyDegree = anomalyDegree(diffSDR, diffStores, diffRequests);
		contents.add("" + anomalyDegree);
		fullContents.add("" + anomalyDegree);
		
		//AnomalyType 1 - Excessive Sensitive Records Retrieved
		// 2 - Excessive number of stores accessed
		// 3 - Excessive number of requests
		// 4 - relocation anomaly
		int anomalyType = anomalyType(diffSDR, diffStores, diffRequests, random);
		contents.add("" + anomalyType);
		fullContents.add("" + anomalyType);
		
		String threat = isThreat(sensitiveDataCount, anomalyType, anomalyDegree, NoOfDataStores, sensitiveRequests, flag);
		contents.add("" + threat);
		fullContents.add(threat);
		
		//Write to Csv File
		//Training
		if (count == 2) {
			writeToCSV(this.filePath, contents);
			writeToCSV(this.filePath2, fullContents);
		}
		//Testing
		else if (count == 1) {
			writeToCSV(this.filePath3, contents);
			writeToCSV(this.filePath4, fullContents);
		}
		//Hold out
		else if (count == 3) {
			writeToCSV(this.holdingPath, fullContents);
		}
		
		
	}
	
	public void generateData(int count, int noOfRecords, int recordType, int oneUser) throws IOException {
		
		this.recordCount = noOfRecords;
		this.record_type = recordType;
		
		for (int i = 0; i < this.recordCount; i++) {
			
			generateRecord(count, oneUser);
		}
	}
	
	/*private int dayOfWeek(int anomalyDegree, int sensitiveDataCount ,Random random) {
		
		int bound = random.nextInt(100);
		
		int day = 1; // Monday = 1 and Sunday = 7
		//Highly Excessive Anomaly
		if (sensitiveDataCount > 50000 && bound > 40) {
			day = random.nextInt(2) + 6;
		}
		else if (anomalyDegree == 3 && bound > 70) {
			
			day = random.nextInt(2) + 6; // One of the weekends
		}
		else {
			day = random.nextInt(5) + 1;  //One of the weekdays
		}
		return day;
	}
	
	private int timeOfDay(int anomalyDegree, int sensitiveDataCount, Random random) {
		
		int randomBound = random.nextInt(100);
		
		int hour;
		
		if (sensitiveDataCount > 50000 && randomBound > 40) {
			
			hour = random.nextInt(10) + 20;   //20 - 29 = Malicious (8pm - 5am)
		}
		else if (anomalyDegree == 3 && randomBound > 80) {
			hour = random.nextInt(10) + 20;
		}
		else {
			hour = random.nextInt(14) + 6;
		}
		
		return hour;
		
	}*/
	
	private String isThreat(int sensitiveDataCount, int anomalyType, int anomalyDegree, int actualDS, int actualReq, int flag) {
		
		//Training Data, Testing Data
		
		if (anomalyType == 4 && anomalyDegree == 2) {
			return "Spoofing Threat";
		}
		else if (sensitiveDataCount > 9000 && anomalyType == 1 && anomalyDegree == 3) {
			return "Hoarding Threat";
		}
		else if (actualDS > 60 && anomalyType == 3 && anomalyDegree == 3) {
			return "Widespread Hoarding Threat";
		}
		else if (actualReq > 60 && anomalyType == 2 && anomalyDegree == 3) {
			return "Hoarding Threat";
		}
		
		return "Benign";
	}
	
	
	//AnomalyType 1 - Excessive Sensitive Records Retrieved
	// 2 - Excessive number of stores accessed
	// 3 - Excessive number of requests
	private int anomalyType (int diffSDR, int diffStores, int diffRequests, Random random) {
		
		//1 in 100 chance of a relocation anomaly
		if (random.nextInt(100) > 98) {
			return 4;
		}
		
		if (diffSDR >= diffStores && diffSDR >= diffRequests) {
			return 1;
		}
		else if (diffRequests >= diffSDR && diffRequests >= diffStores) { 
			return 2;
		}
		return 3;
	}
	
	private int relativeDifference (int a, int b) {
		
		double relDiff = ((a - b) / ((double) (a + b) / 2.0)) * 100;
		
		return (int) relDiff;
		
		
	}
	
	//Calculates the anomaly degree based on the relative differences between all 3 factors
	private int anomalyDegree(int diffSDR, int diffStores, int diffRequests) {
		
		int value = 1;
		
		//If any of them are greater than 100, it should be highly excessive
		if (diffSDR > 140 || diffStores > 100 || diffRequests > 100) {
			value = 3;
		}
		else if (diffSDR > 100 || diffStores > 75 || diffRequests > 80) {
			value = 2;
		}
		
		return value;
	}
	
	private int generateSensitiveRequests(int sensitiveDataCount, int expectedRequests, Random random) {
		
		int value = expectedRequests + 1;
		
		if (sensitiveDataCount > 90000) {
			value = expectedRequests + 5 +  random.nextInt(100);
		}
		else if (sensitiveDataCount < 2000 && random.nextInt(100) > 98) {
			value = expectedRequests + 5 + random.nextInt(2000);
		}
		else {
			value = expectedRequests + 1 + random.nextInt(5);
		}
		return value;
	}
	
	
	private int generateDataStoreCount(int sensitiveDataAccessed, int expectedDataStores, Random random) {
		
		int value = expectedDataStores + 1;
		
		if (sensitiveDataAccessed > 50000) {
			value = expectedDataStores + 5 +  random.nextInt(45);
		}
		else if (sensitiveDataAccessed < 1000 && random.nextInt(100) > 95) {
			value = expectedDataStores + 5 + random.nextInt(100);
		}
		else {
			value = expectedDataStores + 1 + random.nextInt(5);
		}
		return value;
	}
	
	private int generateSensitiveDataRecordsCount(int expectedRecordCnt, Random random, int flag) {
		
		if (this.record_type == 3) {
			int value = expectedRecordCnt + random.nextInt(1000);
			return value;
		}
		int spikeBound = random.nextInt(30);
		
		int value = 0;
		
		if (spikeBound > 27 || this.record_type == 2) {
			value = expectedRecordCnt + random.nextInt(1000000);
			
		}
		else {
			value = expectedRecordCnt + random.nextInt(1000);
		}
		
		return value;
	}
	
	private void writeToCSV(String outputFile, String[] contents) throws IOException {
		
		CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
		
		for (int i = 0; i < contents.length; i++) {
			csvOutput.write(contents[i]);
		}
		csvOutput.endRecord();
		
		csvOutput.close();
	}
	
	private void deleteFileIfExists(String filePath) {
		
		File f = new File(filePath);
		
		if (f.exists()) {
			
			f.delete();
		}
	}
	
	private void writeToCSV(String outputFile, ArrayList<String> contents) throws IOException {
		
		CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
		
		for (int i = 0; i < contents.size(); i++) {
			csvOutput.write(contents.get(i));
		}
		csvOutput.endRecord();
		
		csvOutput.close();
		contents.clear();
	}

}

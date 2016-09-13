

import java.io.FileReader;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import java.io.File;

import com.csvreader.CsvWriter;

import com.opencsv.CSVReader;

public class Aggregator {

	private String AGG_FILE_PATH;
	private String READ_FILE_PATH;
	private String[][] contents;
	private int recordCount;
	private int attrCount;
	
	
	public Aggregator (String filePath, String readFilePath, int recordCount, int attrCnt) throws IOException {
		AGG_FILE_PATH = filePath;
		deleteFileIfExists(AGG_FILE_PATH);
		READ_FILE_PATH = readFilePath;
		contents = new String[recordCount][attrCnt];
		this.recordCount = recordCount;
		this.attrCount = attrCount;
	}
	
	public void startAggregator() throws IOException {
		
		readFromCSV(READ_FILE_PATH, 0);
		
		generateAggregates();
	}
	
	private void deleteFileIfExists(String filePath) {
		
		File f = new File(filePath);
		
		if (f.exists()) {
			
			f.delete();
		}
	}
	
	private ArrayList<String> sortArrayList(ArrayList<String> uniqueUsers) {
		
		
		for (int t = 0; t < uniqueUsers.size() - 1; t++) {
			
            for (int i= 0; i < uniqueUsers.size() - t -1; i++) {
                
            	if(Integer.parseInt(uniqueUsers.get(i+1)) < Integer.parseInt((uniqueUsers.get(i)))) {
                    String tempStr = uniqueUsers.get(i);
                    uniqueUsers.set(i, uniqueUsers.get(i+1)); 
                    uniqueUsers.set(i+1, tempStr);
            	}
            }
        }
		
		return uniqueUsers;
		
	}
	
	private void generateAggregates() throws IOException {
		
		
		ArrayList<String> uniqueUsers = new ArrayList<String>();
		
		for (int i = 0; i < contents.length; i++) {
			
			String currentUser =  contents[i][0];
			
			if (currentUser != null) {
				
				if (!uniqueUsers.contains(currentUser)) {
					uniqueUsers.add(currentUser);
				}	
			}
		}
		
		uniqueUsers = sortArrayList(uniqueUsers);
		
		//printContents();
		
		for (int i = 0; i < uniqueUsers.size(); i++) {
			
			String currentUserID = uniqueUsers.get(i);
			
			int[] timeAggregates = new int[4];
			int bound3 = contents.length;
			int bound2 = (int)contents.length/2;
			int bound1 = (int)contents.length/4;
			int bound0 = (int)contents.length/12;
			int[] timeBounds = {bound0, bound1, bound2, bound3};
			int index = 0;
			
			while (index < 4) {
				
				int total = 0;
				for (int j = contents.length - 1; j >= contents.length - timeBounds[index]; j--) {
					
					
					if (contents[j][0] != null && currentUserID.equals(contents[j][0])) {	//Match a user
						
						
						total += Integer.parseInt(contents[j][2]);	
					}
				}
				timeAggregates[index] = total;
				index++;
			}
				
			String[] aggs = new String[6];
			aggs[0] = currentUserID;
			aggs[1] = "" + timeAggregates[0];
			aggs[2] = "" + timeAggregates[1];
			aggs[3] = "" + timeAggregates[2];
			aggs[4] = "" + timeAggregates[3];
			aggs[5] = checkThreat(timeAggregates);
			
			
			writeToCSV(AGG_FILE_PATH, aggs);
			
		}
		
	}
	
	private String checkThreat(int[] timeAggs) {
		
		if (timeAggs[0] > 5000) {
			return "Snooping Threat";
		}
		else if (timeAggs[1] > 7000) {
			return "Snooping Threat";
		}
		else if (timeAggs[2] > 11000) {
			return "Snooping Threat";
		}
		else if (timeAggs[3] > 15000) {
			return "Snooping Threat";
		}
		
		return "Benign";
	}
	
	
	private void writeToCSV(String outputFile, String[] contents) throws IOException {
		
		CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
		
		for (int i = 0; i < contents.length; i++) {
			csvOutput.write(contents[i]);
		}
		csvOutput.endRecord();
		
		csvOutput.close();
	}
	
	private void printContents() {
		
		for (int i = 0; i < contents.length; i++) {
	    	 
	    	 //if (contents[i][0] != null) {
	    		 
	    		 System.out.print(i + "    ");
	    		 for (int j = 0; j < contents[0].length; j++) {
	    			 System.out.print(contents[i][j] + ", ");
	    		 }
	    		 System.out.println();
	    	 //}
	    		 
	     }
	}
	
	
	
	//Parse the file and store in 2d array
	
	private String[] readFromCSV(String inputFile, int index) throws IOException {
		
		CSVReader csvInput = new CSVReader(new FileReader(inputFile));
		
		String [] nextLine = new String[this.attrCount];
		for (int i = 0; i < nextLine.length; i++) {
			nextLine[i] = "";
		}
	     while ((nextLine = csvInput.readNext()) != null && index < this.recordCount) {
	        // nextLine[] is an array of values from the line
	        //System.out.println(nextLine[0] + nextLine[1] + "etc...");
	    	 if (nextLine[0] != null && !nextLine[0].contains("null")) {
	    		 //System.out.println(nextLine[0].toString());
	    		 contents[index++] = nextLine;
	    	 }
	    	 
	     }
	     //Debugg
	     //printContents();
	     
		return null;
	}
	
	
	
	
}

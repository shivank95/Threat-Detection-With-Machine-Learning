import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.csvreader.CsvWriter;
import com.opencsv.CSVReader;

public class Sorter {
	
	private String READ_FILE_PATH;
	private String WRITE_FILE_PATH;
	private String[][] contents;
	private int recordCount;
	private int attrCount;
	private int userCount;
	
	public Sorter (String writefilePath, String readFilePath, int recordCount, int attrCount,int userCount)  throws IOException {
		
		WRITE_FILE_PATH = writefilePath;
		deleteFileIfExists(WRITE_FILE_PATH);
		READ_FILE_PATH = readFilePath;
		contents = new String[recordCount][attrCount];
		this.recordCount = recordCount;
		this.attrCount = attrCount;
		this.userCount = userCount;
	
	}
	
	private void deleteFileIfExists(String filePath) {
		
		File f = new File(filePath);
		
		if (f.exists()) {
			
			f.delete();
		}
	}
	
	public void startSorter() throws IOException {
		
		readFromCSV(READ_FILE_PATH, 0);
		sort();
		
	}
	
	private void sort() throws IOException {
		
		
		int index = 1;
		
		while (index <= this.userCount) {
			
		
			for (int i = 0; i < contents.length; i++) {
				
				if (contents[i][0].equals(Integer.toString(index))) {
					
					String[] line = contents[i];
					writeToCSV(WRITE_FILE_PATH, line);
				}
			}
			index++;
		}
		
		
	}
	
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
	
	private void writeToCSV(String outputFile, String[] contents) throws IOException {
		
		CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
		
		for (int i = 0; i < contents.length; i++) {
			csvOutput.write(contents[i]);
		}
		csvOutput.endRecord();
		
		csvOutput.close();
	}
}

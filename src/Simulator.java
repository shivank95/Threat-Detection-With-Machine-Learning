import java.io.IOException;

public class Simulator {
	
	
	private static String TRAINING_FILE_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\trainData20.csv";
	private static String TESTING_FILE_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\testData20.csv";
	private static String GENERAL_FILE_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\trainAllData20.csv";
	private static String GENERALTESTING_FILE_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\testAllData20.csv";
	private static String AGG_FILE_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\trainAnomAggs.csv";
	private static String AGGTEST_FILE_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\testAnomAggs.csv";
	private static String SORTED_FILE_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\trainSortedData20.csv";
	private static String TESTSORTED_FILE_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\testSortedData20.csv";
	private static String HOLDING_SET_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\holdingSet.csv";
	
	private static String PREDICTION_FILE_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\predictions.csv";
	
	private static int TRAINING_RECORD_COUNT = 200;
	private static int TESTING_RECORD_COUNT = 500;
	private static int HOLDING_RECORD_COUNT = 200;
	
	private static int button_type = 1;
	
	private static int USER_COUNT = 300;
	private static int ATTR_COUNT = 10;
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		
		String filePath = TRAINING_FILE_PATH;
		String filePath2 = GENERAL_FILE_PATH;
		String filePath3 = TESTING_FILE_PATH;
		String filePath4 = GENERALTESTING_FILE_PATH;
		String filePath5 = HOLDING_SET_PATH;
		
		int NoOfRecords = TRAINING_RECORD_COUNT;
		
		
		
		DataGenerator newData = new DataGenerator (filePath, NoOfRecords, filePath2, filePath3, filePath4,USER_COUNT, filePath5);
		//Generate training Data
		
		//Random data
		int recordType = 1;
		int oneUser = 0;
		
		newData.generateData(2,TRAINING_RECORD_COUNT,recordType,oneUser);
		
		//Generate Test Data
		newData.generateData(1,TESTING_RECORD_COUNT,recordType,oneUser);
		
		//create hacker
		recordType = 2;
		newData.generateData(3, 1,recordType,oneUser);
		
		//Create benign
		recordType = 3;
		newData.generateData(3, 5,recordType,oneUser);
		
		//hacker with 1 user id
		recordType = 2;
		oneUser = 15;
		newData.generateData(3, 20,recordType,oneUser);
		
		//Create Training Aggregates
		Aggregator createAggs = new Aggregator(AGG_FILE_PATH, GENERAL_FILE_PATH, TRAINING_RECORD_COUNT, ATTR_COUNT);
		createAggs.startAggregator();
		
		//Create Testing Aggregates
		Aggregator createTestAggs = new Aggregator(AGGTEST_FILE_PATH, GENERALTESTING_FILE_PATH, TESTING_RECORD_COUNT, ATTR_COUNT);
		createTestAggs.startAggregator();
		
		//Sort Training Data
		Sorter sorter = new Sorter(SORTED_FILE_PATH, GENERAL_FILE_PATH, TRAINING_RECORD_COUNT,ATTR_COUNT, USER_COUNT);
		sorter.startSorter();
		//Sort Testing Data
		Sorter sorterTest = new Sorter(TESTSORTED_FILE_PATH, GENERALTESTING_FILE_PATH, TESTING_RECORD_COUNT,ATTR_COUNT, USER_COUNT);
		sorterTest.startSorter();
		
		
		//Start and Train the Threat Detector
		ThreatDetector newDetector = new ThreatDetector(TRAINING_FILE_PATH, TESTING_FILE_PATH, AGG_FILE_PATH, AGGTEST_FILE_PATH,HOLDING_SET_PATH,HOLDING_RECORD_COUNT,ATTR_COUNT - 1,PREDICTION_FILE_PATH);
		newDetector.startDetector();
		
	}

}

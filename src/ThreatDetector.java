
import org.jfree.data.time.Millisecond;

import org.jfree.ui.RefineryUtilities;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

import libsvm.LibSVM;
import libsvm.SelfOptimizingLinearLibSVM;
import net.sf.javaml.tools.data.FileHandler;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.classification.evaluation.EvaluateDataset;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.sf.javaml.classification.bayes.NaiveBayesClassifier;

import com.csvreader.CsvWriter;
import com.opencsv.CSVReader;



public class ThreatDetector {
	
	/*private int classLabel;
	private String trainingFilePath;
	private String testingFilePath;*/
	
	private static int CLASS_LABEL_INDEX = 8;
	private static int CLASS_LABEL_AGGS = 5;
	
	private static String TRAINING_FILE_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\Data20.csv";
	private static String TESTING_FILE_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\testData20.csv";
	private static String AGG_FILE_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\anomAggs.csv";
	private static String AGGTEST_FILE_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\testAnomAggs.csv";
	private static String HOLDING_SET_PATH = "C:\\Users\\stibrewa\\Desktop\\Data\\holdingSet.csv";
	private static String PREDICTION_FILE_PATH = "";
	
	private int attrCount = 0;
	private int holdingSetCount = 100;
	
	SelfOptimizingLinearLibSVM knn;
	SelfOptimizingLinearLibSVM aggClassifier;
	
	public ThreatDetector(String trainPath, String testPath, String aggPath, String aggTestPath, String holdingSetPath, int holdingSetCount, int attrCount, String predictionPath) throws IOException {
		
		TRAINING_FILE_PATH = trainPath;
		TESTING_FILE_PATH = testPath;
		AGG_FILE_PATH = aggPath;
		AGGTEST_FILE_PATH = aggTestPath;
		HOLDING_SET_PATH = holdingSetPath;
		PREDICTION_FILE_PATH = predictionPath;
		this.holdingSetCount = holdingSetCount;
		this.attrCount = attrCount;
	}
	
	/*Function to parse dataset */
	private Dataset loadData(String filepath, int classLabel) throws IOException  {
		
		Dataset data = FileHandler.loadDataset(new File(filepath),classLabel,",");
		return data;
	}
	
	public String predictSpike(double[] attributes) {
		
		DenseInstance newInstance = new DenseInstance(attributes);
        
        Object predictedClassValue = this.knn.classify(newInstance);
        
        System.out.println("Predicted Value: " + predictedClassValue.toString());
        
        return predictedClassValue.toString();
        
	}
	
	public String predictAgg(double[] attributes) {
		
		DenseInstance newInstance = new DenseInstance(attributes);
        
        Object predictedClassValue = this.aggClassifier.classify(newInstance);
        
        System.out.println("Predicted Value: " + predictedClassValue.toString());
        
        return predictedClassValue.toString();
        
	}
	
	/*Function to print metrics*/
	private void printMetrics(Dataset data, String heading) {
		
		System.out.println(heading);
        System.out.println(" Instance 1: " + data.instance(0)); 
        
        System.out.println(" Number of attributes in each Record: " + data.noAttributes());
        System.out.println(" Number of records: " + data.size() + "\n");
        
	}
	
	/*Function for classification, prediction, user inputs and reoptimizing */
	public void startDetector() throws IOException, InterruptedException {
		
		//Load Training Data
		String path = TRAINING_FILE_PATH;
		
		Dataset data = loadData(path, CLASS_LABEL_INDEX);
		
		

		printMetrics(data,"Training Data..");
        
        /*
         * Contruct a KNN classifier that uses 5 neighbors to make a decision.
         */
        //this.knn = new SelfOptimizingLinearLibSVM(0,1);
		this.knn = new SelfOptimizingLinearLibSVM();
        //NaiveBayesClassifier knn = new NaiveBayesClassifier(true,true,true);
		knn.buildClassifier(data);
		
		path = AGG_FILE_PATH;
		Dataset aggData = loadData(path, CLASS_LABEL_AGGS);
		
		this.aggClassifier = new SelfOptimizingLinearLibSVM();
        //NaiveBayesClassifier knn = new NaiveBayesClassifier(true,true,true);
		aggClassifier.buildClassifier(aggData);
		
		int[] results = new int[2];
        
        
        //Load Testing Data
        Dataset data2 = FileHandler.loadDataset(new File(TESTING_FILE_PATH),CLASS_LABEL_INDEX,",");
        
        Dataset testAggData = FileHandler.loadDataset(new File(AGGTEST_FILE_PATH),CLASS_LABEL_AGGS,",");
        
        printMetrics(data2, "\nTesting Data..");
       
        results = getResults(data2);
        int correct = results[0];
        int wrong = results[1];
        
        System.out.println("Correct predictions  " + correct);
        System.out.println("Wrong predictions " + wrong + "\n");
        
        double[] graphPlot = {correct,wrong};
        String[] classLabels = {"Correct Predictions", "Wrong Predictions"};
        
        Grapher graph = new Grapher(graphPlot, "Initial Prediction Chart", classLabels);
        graph.setSize(560, 367);
        RefineryUtilities.centerFrameOnScreen( graph );    
        graph.setVisible( true );
        
        //Aggregate Data
        System.out.println("\nTesting Aggregate Data: ");
        results = getResultsAgg(testAggData);
        correct = results[0];
        wrong = results[1];
        System.out.println("Correct predictions  " + correct);
        System.out.println("Wrong predictions " + wrong + "\n");
        
        double[] graphPlot2 = {correct,wrong};
        
        Grapher graph2 = new Grapher(graphPlot2, "Aggregate Prediction Chart", classLabels);
        graph2.setSize(560, 367);
        RefineryUtilities.centerFrameOnScreen( graph2 );    
        graph2.setVisible( true );
        
        Map<Object, PerformanceMeasure> pm = EvaluateDataset.testDataset(knn, data2);
        printScores(pm);
	            
        //Read from hold out set randomly and re optimize
        
        Random random = new Random();
        
        //final DynamicDataDemo demo = new DynamicDataDemo("Wrong Predictions vs Time");
        //demo.pack();
        //RefineryUtilities.centerFrameOnScreen(demo);
        //demo.setVisible(true);
        
        //TimeUnit.SECONDS.sleep(5);
        
        /*for (int i = 0; i < this.holdingSetCount && demo.flag == 0; i++) {
        	
        	//Randomly get data from holding set and keep adding it
        	String[] nextRecord = readFromCSV(this.HOLDING_SET_PATH, random);
        	//System.out.println(Arrays.toString(nextRecord));
        	
        	//Add to training set
        	writeToCSV(this.TRAINING_FILE_PATH,nextRecord);
        	
        	//Reclassifiy
        	Dataset modifiedData = FileHandler.loadDataset(new File(TRAINING_FILE_PATH),CLASS_LABEL_INDEX,",");
    		//this.knn = new SelfOptimizingLinearLibSVM(-1,0);
            this.knn.buildClassifier(modifiedData);
            //System.out.println("\nOptimized.\n");
            
            //RerRun to tester
            results = getResults(data2);
            correct = results[0];
            wrong = results[1];
            //System.out.println("\nCorrect predictions  " + correct);
            final Millisecond now = new Millisecond();
            System.out.println("Time = " + now.toString());
            System.out.println("Wrong predictions " + wrong + " \n");
            
            //Regraph
            //double[] newPlot = {correct,wrong};
            //graph.add
            
            
            TimeUnit.MILLISECONDS.sleep(2);
            demo.addYValue(wrong);
            
        }*/
        
        //if (demo.flag == 1) {
        	
        System.out.println("Training finished..");
        
        ///}
        
        printThreatsToFile(data2);
        
        
        
        /*
			    //Create Attributes
	            double wac[] = new double[3];
	            wac[0]= Double.parseDouble(aCount);
	            wac[1] = Double.parseDouble(sdrCount);
	            wac[2] = Double.parseDouble(dspCount);
	            
	            DenseInstance newInstance = new DenseInstance(wac);
	            
	            Object predictedClassValue = knn.classify(newInstance);
	            
	            System.out.println("Predicted Value: " + predictedClassValue.toString());
	           
	            String actValue = prompt("Actual Value? (M/B):" );
	            if (actValue.contains("M") || actValue.contains("m")) {
	            	
	            	newInstance.setClassValue("Malicious");
	            }
	            else {
	            	newInstance.setClassValue("Benign");
	            }
	            
	            //Now that we have the correct answer, we need to save it back to the file
	            String outputFile = TRAINING_FILE_PATH;
	            String[] contents = {"" + wac[0], "" + wac[1], "" + wac[2], newInstance.classValue().toString()};
	            writeToCSV(outputFile,contents);
	            
	            
	            System.out.println("\nOptimizing...");
	            
	            //Reoptimize
	            //Load Training Data
	    		Dataset modifiedData = FileHandler.loadDataset(new File(TRAINING_FILE_PATH),CLASS_LABEL_INDEX,",");
	    		knn = new SelfOptimizingLinearLibSVM(0,1);
	            knn.buildClassifier(modifiedData);
	            System.out.println("\nOptimized.\n");
	    		
        	}

        }   
        catch (NumberFormatException e1) {
        	System.out.println("Program Ended..");
        }
        catch(Exception e) {
        	
            e.printStackTrace();
        }*/
        
        
	}
	
	/*
	 * Prints accuracy scores, F measures, recall and precision.
	 */
private String[] readFromCSV(String inputFile, Random random) throws IOException {
		
		int skipLines = random.nextInt(this.holdingSetCount - 1);
		CSVReader csvInput = new CSVReader(new FileReader(inputFile),',','\'', skipLines);
		
		String [] nextLine = new String[this.attrCount];
		
		if ((nextLine = csvInput.readNext()) != null) {
			return nextLine;
		}
	     
		return null;
	}
	
	
	private void printScores(Map<Object, PerformanceMeasure> pm) {
		
		System.out.println("Performace Measures: ");
		for(Object o:pm.keySet())
            System.out.println(o+": "+pm.get(o).getAccuracy() + "\n Recall: " + pm.get(o).getRecall() +" Precision: " +  pm.get(o).getPrecision()
		+ "\n F Measure: " + pm.get(o).getFMeasure() + " \n");
	}
	
	//Prompts user for data
	private String prompt(String prompt) throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	System.out.print(prompt);
    	String response = br.readLine();
    	
    	return response;
	}
	
	/*
	 * Method to write one record to a csv file
	 */
	private void writeToCSV(String outputFile, String[] contents) throws IOException {
		
		CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
		
		for (int i = 0; i < contents.length; i++) {
			csvOutput.write(contents[i]);
		}
		csvOutput.endRecord();
		
		csvOutput.close();
	}
	
	private int[] getResults(Dataset data) {
		
		int[] results = new int[2];
		
		
		//Counters for correct and wrong predictions. 
        int correct = 0, wrong = 0;
         //Classify all instances and check with the correct class values 
        for (Instance inst : data) {
            Object predictedClassValue = knn.classify(inst);
            Object realClassValue = inst.classValue();
            if (predictedClassValue.equals(realClassValue))
                correct++;
            else {
                wrong++;
               
            }
        }
        
        results[0] = correct;
        results[1] = wrong;
        
        return results;
	}
	
	private int[] getResultsAgg(Dataset data) {
		
		int[] results = new int[2];
		
		
		//Counters for correct and wrong predictions. 
        int correct = 0, wrong = 0;
         //Classify all instances and check with the correct class values 
        for (Instance inst : data) {
            Object predictedClassValue = aggClassifier.classify(inst);
            Object realClassValue = inst.classValue();
            if (predictedClassValue.equals(realClassValue))
                correct++;
            else {
                wrong++;
               
            }
        }
        
        results[0] = correct;
        results[1] = wrong;
        
        return results;
	}
	
	private void printThreatsToFile(Dataset data) throws IOException {
		
		//Classify all instances and check with the correct class values 
        for (Instance inst : data) {
            String predictedClassValue = (String) knn.classify(inst);
            
            //Object realClassValue = inst.classValue();
            
            if (!predictedClassValue.equals("Benign")) {
            	//System.out.print(inst.toString());
            	//System.out.println(" " + predictedClassValue);
            	String[] contents = {inst.toString()};
            	writeToCSV(PREDICTION_FILE_PATH,contents);
            }
            
        }
		
	}
	
	/*private double[] StringToDouble(String[] attributes) {
		
		double[] attributes = 
		
	}*/

}


import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class Grapher extends ApplicationFrame {
	
	private double[] classes;
	private String[] classLabels;
	public Grapher(double[] pieClasses, String title, String[] classLabels) {
		
		super(title);
		classes = pieClasses;
		this.classLabels = classLabels;
		setContentPane(createDemoPanel( ));
	}
	public JPanel createDemoPanel( )
	{
	      JFreeChart chart = createChart(createDataset( ) );  
	      return new ChartPanel( chart ); 
	}
	private PieDataset createDataset( ) 
	{
	      DefaultPieDataset dataset = new DefaultPieDataset( );
	      for (int i = classLabels.length - 1; i >= 0; i--) {
	    	  dataset.setValue( classLabels[i] + " (" + classes[i] + ")" , new Double( classes[i] ) );  
	      }
	      //dataset.setValue( "Wrong Predictions (" + classes[1] + ")" , new Double( classes[1] ) );  
	      //dataset.setValue( "Correct Predictions (" + classes[0] + ")" , new Double( classes[0] ) ); 
	      return dataset;         
	}
	private JFreeChart createChart( PieDataset dataset )
	   {
	      JFreeChart chart = ChartFactory.createPieChart(      
	         getTitle(),  // chart title 
	         dataset,        // data    
	         true,           // include legend   
	         true, 
	         false);

	      return chart;
	   }
	
}

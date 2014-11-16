package mainWindow;

import java.awt.EventQueue;
public class Main {
	
	public static void main(String [] args)
	  { 
		 EventQueue.invokeLater(new Runnable() {
	            public void run() {
	                try
	                {
	                	SurveyWindow startWindow = new SurveyWindow();
	                	startWindow.setVisible(true);
	                } catch (Exception e)
	                {
	                    e.printStackTrace();
	                }
	            }
	        });
		
	  }
}

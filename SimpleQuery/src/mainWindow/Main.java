package mainWindow;

import java.awt.EventQueue;
public class Main {
	
	public static void main(String [] args)
	  { 
		 EventQueue.invokeLater(new Runnable() {
	            public void run() {
	                try
	                {
	                	UserWindow mainWindow = new UserWindow();
	            		mainWindow.setVisible(true);
	                } catch (Exception e)
	                {
	                    e.printStackTrace();
	                }
	            }
	        });
		
	  }
}

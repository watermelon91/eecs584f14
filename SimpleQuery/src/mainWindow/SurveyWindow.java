/*
 * Created by JFormDesigner on Sun Nov 16 14:04:02 EST 2014
 */

package mainWindow;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import mainWindow.LoggingUtilities.LOG_TYPES;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

/**
 * @author abc def
 */
public class SurveyWindow extends JFrame {
	
	private int pageCount = 0;
	private int TOTAL_PAGE = 3;
	private UserWindow mainWindow = null;
	private LoggingUtilities logger = new LoggingUtilities();
	private String[] buggyQueries = new String[] {
			"SELECT o1.orderid, o2.orderid, o3.orderid \nFROM orderlines o1, orderlines o2, orderlines o3 \nWHERE o1.prod_id = o2.prod_id AND o1.prod_id = o3.prod_id AND date_part('month', o1.orderdate) = date_part('month', o2.orderdate) + 1 AND date_part('month', o1.orderdate) = date_part('month', o3.orderdate) + 1 AND o1.prod_id = 8032;", 
			"SELECT c.customerid, c.prod_id, o.orderdate \nFROM cust_hist c, orderlines o \nWHERE c.customerid = 5090 and c.prod_id = 5450 and o.orderid = c.prod_id;", 
			"SELECT c1.customerid, c2.customerid, o1.totalamount, o2.totalamount, o1.orderdate,  o2.orderdate \nFROM cust_hist c1, cust_hist c2, orders o1, orders o2 \nWHERE c1.customerid > c2.customerid AND c1.prod_id = c2.prod_id AND o1.orderid = c1.orderid AND o2.orderid = c2.orderid AND o1.totalamount - o2.totalamount > 500 AND o1.orderdate - o2.orderdate = 0;"};
	private String[] expectedResult = new String[] {
			"The query below has a bug in it which caused it to return a different set of order_ids than expected. \n\nExpected behavior: find all orders for the product with prod_id = 8032 that were placed in three consecutive months. (e.g. if order 500 was placed in Jan, 547 was placed in Feb, 578 was placed in Mar, you should find (500, 574, 578) in the returned result).", 
			"The query below has a bug in it which caused it to return a different set of output than expected. \n"
			+ "Expected output: \ncustomerid | prod_id | orderdate \n"
			+ "5090 |    5450 | 2009-03-10\n "
			+ "5090 |    5450 | 2009-09-18\n"
			+ "5090 |    5450 | 2009-09-13\n"
			+ "5090 |    5450 | 2009-12-17\n",
			"A manager is looking at whether there exists a pair of customers that placed orders on the same day for the same product, but their order total amount differed by 500. The query below returns no result, so please help to validate whether it is true that there's no such pair of customers exist, or it's a caused by a bug in the query."};
		
	public SurveyWindow() {
		super();
		initComponents();
		myInit();
	}
	
	private void myInit()
	{
		descTextArea.setText(
				"INSTRUCTIONS: \n"
				+ "1. click Start button below to start\n"
				+ "2. use the newly opened window (after clicking \"Start\") to \n  query the database");
		descLabel.setText("Expected behavior");
		buggyLabel.setText("Buggy query");
		solutionLabel.setText("Your solution");
		button.setText("Start");
		countLabel.setText(pageCount + "/" + TOTAL_PAGE);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
	}

	private void buttonMouseClicked(MouseEvent e) {
		if(button.getText().equals("Start"))
		{
			mainWindow = new UserWindow(logger);
			mainWindow.setVisible(true);
			
			descTextArea.setEnabled(true);
			descTextArea.setWrapStyleWord(true);
			descTextArea.setLineWrap(true);
			buggyQueryTextArea.setEnabled(true);
			buggyQueryTextArea.setWrapStyleWord(true);
			buggyQueryTextArea.setLineWrap(true);
			solutionQueryTextArea.setEnabled(true);
			solutionQueryTextArea.setEditable(true);
			
			buggyQueryTextArea.setText(buggyQueries[pageCount]);
			descTextArea.setText(expectedResult[pageCount]);
			
			button.setText("Next");
			pageCount++;
			countLabel.setText(pageCount + "/" + TOTAL_PAGE);
			
			logger.log(LOG_TYPES.START,"");
		}
		else if(button.getText().equals("Next"))
		{
			if(pageCount < 3)
			{
				buggyQueryTextArea.setText(buggyQueries[pageCount]);
				descTextArea.setText(expectedResult[pageCount]);
				
				if(pageCount == 2)
				{
					button.setText("Submit");
				}
				
				pageCount++;
				countLabel.setText(pageCount + "/" + TOTAL_PAGE);
			}

			logger.log(LOG_TYPES.INPUT_SOLUTION, solutionQueryTextArea.getText());
			solutionQueryTextArea.setText("");
		}
		else if(button.getText().equals("Submit"))
		{
			logger.log(LOG_TYPES.INPUT_SOLUTION, solutionQueryTextArea.getText());

        	JOptionPane.showMessageDialog(null, 
        			"Log being uploaded... This might take a minute. \nClick OK to dismiss message. \nThe application will exit upon upload completion.",
        			"Upload Log", 
        			JOptionPane.INFORMATION_MESSAGE);
        	logger.log(LOG_TYPES.END, "");
        	logger.sendLog();
        	
        	mainWindow.dispatchEvent(new WindowEvent(mainWindow, WindowEvent.WINDOW_CLOSING));
        	this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
	}

	private void thisWindowClosing(WindowEvent e) {
		// TODO add your code here
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - abc def
		descLabel = new JLabel();
		scrollPane3 = new JScrollPane();
		descTextArea = new JTextArea();
		buggyLabel = new JLabel();
		scrollPane1 = new JScrollPane();
		buggyQueryTextArea = new JTextArea();
		solutionLabel = new JLabel();
		scrollPane2 = new JScrollPane();
		solutionQueryTextArea = new JTextArea();
		countLabel = new JLabel();
		button = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new FormLayout(
			"3dlu, $lcgap, 60dlu, $lcgap, 226dlu",
			"79dlu, $lgap, 94dlu, $lgap, 98dlu, $lgap, default"));

		//---- descLabel ----
		descLabel.setText("text");
		contentPane.add(descLabel, CC.xy(3, 1));

		//======== scrollPane3 ========
		{

			//---- descTextArea ----
			descTextArea.setEditable(false);
			descTextArea.setEnabled(false);
			scrollPane3.setViewportView(descTextArea);
		}
		contentPane.add(scrollPane3, CC.xywh(5, 1, 1, 2));

		//---- buggyLabel ----
		buggyLabel.setText("text");
		contentPane.add(buggyLabel, CC.xy(3, 3));

		//======== scrollPane1 ========
		{

			//---- buggyQueryTextArea ----
			buggyQueryTextArea.setEditable(false);
			buggyQueryTextArea.setEnabled(false);
			scrollPane1.setViewportView(buggyQueryTextArea);
		}
		contentPane.add(scrollPane1, CC.xywh(5, 3, 1, 2));

		//---- solutionLabel ----
		solutionLabel.setText("text");
		contentPane.add(solutionLabel, CC.xy(3, 5));

		//======== scrollPane2 ========
		{

			//---- solutionQueryTextArea ----
			solutionQueryTextArea.setEditable(false);
			solutionQueryTextArea.setEnabled(false);
			scrollPane2.setViewportView(solutionQueryTextArea);
		}
		contentPane.add(scrollPane2, CC.xywh(5, 5, 1, 2));

		//---- countLabel ----
		countLabel.setText("text");
		contentPane.add(countLabel, CC.xy(3, 7));

		//---- button ----
		button.setText("text");
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				buttonMouseClicked(e);
			}
		});
		contentPane.add(button, CC.xy(5, 7));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - abc def
	private JLabel descLabel;
	private JScrollPane scrollPane3;
	private JTextArea descTextArea;
	private JLabel buggyLabel;
	private JScrollPane scrollPane1;
	private JTextArea buggyQueryTextArea;
	private JLabel solutionLabel;
	private JScrollPane scrollPane2;
	private JTextArea solutionQueryTextArea;
	private JLabel countLabel;
	private JButton button;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}

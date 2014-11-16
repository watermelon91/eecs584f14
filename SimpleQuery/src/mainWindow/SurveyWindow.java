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
	private String[] buggyQueries = new String[] {"query1", "query2", "query3"};
	
	public SurveyWindow() {
		super();
		initComponents();
		myInit();
	}
	
	private void myInit()
	{
		buggyQueryTextArea.setText(
				"INSTRUCTIONS: \n"
				+ "1. click Start button below to start\n"
				+ "2. use the newly opened window after clicking \"Start\" to \n  query the database");
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
			
			buggyQueryTextArea.setEnabled(true);
			solutionQueryTextArea.setEnabled(true);
			solutionQueryTextArea.setEditable(true);
			
			buggyQueryTextArea.setText(buggyQueries[pageCount]);
			
			button.setText("Next");
			pageCount++;
			countLabel.setText(pageCount + "/" + TOTAL_PAGE);
			
			logger.log(LOG_TYPES.START,"");
		}
		else if(button.getText().equals("Next"))
		{
			solutionQueryTextArea.setText("");
			
			if(pageCount < 3)
			{
				buggyQueryTextArea.setText(buggyQueries[pageCount]);
				
				if(pageCount == 2)
				{
					button.setText("Submit");
				}
				
				pageCount++;
				countLabel.setText(pageCount + "/" + TOTAL_PAGE);
			}

			logger.log(LOG_TYPES.INPUT_SOLUTION, solutionQueryTextArea.getText());
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
			"6dlu, $lcgap, 54dlu, $lcgap, 213dlu",
			"94dlu, $lgap, 98dlu, $lgap, default"));

		//---- buggyLabel ----
		buggyLabel.setText("text");
		contentPane.add(buggyLabel, CC.xy(3, 1));

		//======== scrollPane1 ========
		{

			//---- buggyQueryTextArea ----
			buggyQueryTextArea.setEditable(false);
			buggyQueryTextArea.setEnabled(false);
			scrollPane1.setViewportView(buggyQueryTextArea);
		}
		contentPane.add(scrollPane1, CC.xywh(5, 1, 1, 2));

		//---- solutionLabel ----
		solutionLabel.setText("text");
		contentPane.add(solutionLabel, CC.xy(3, 3));

		//======== scrollPane2 ========
		{

			//---- solutionQueryTextArea ----
			solutionQueryTextArea.setEditable(false);
			solutionQueryTextArea.setEnabled(false);
			scrollPane2.setViewportView(solutionQueryTextArea);
		}
		contentPane.add(scrollPane2, CC.xywh(5, 3, 1, 2));

		//---- countLabel ----
		countLabel.setText("text");
		contentPane.add(countLabel, CC.xy(3, 5));

		//---- button ----
		button.setText("text");
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				buttonMouseClicked(e);
			}
		});
		contentPane.add(button, CC.xy(5, 5));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - abc def
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

/*
 * Created by JFormDesigner on Sat Nov 15 14:27:22 EST 2014
 */

package mainWindow;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import mainWindow.LoggingUtilities.LOG_TYPES;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import databaseConnector.PostgresDBConnector.Pair;
import frontEndConnector.FrontEndConnector;

/**
 * @author abc def
 */
public class UserWindow extends JFrame {
	
	private FrontEndConnector connector = new FrontEndConnector("yijiadanajie.cta5xgwtrfyv.us-west-2.rds.amazonaws.com", "mydb", "yijia", "eecs58414");
	private DefaultTableModel tableModel = new DefaultTableModel();
	LoggingUtilities logger = new LoggingUtilities();

	public UserWindow() {
		super();
		initComponents();
		connector.initializeSQLConnection();
	}

	private void submitButtonMouseClicked(MouseEvent e) {
		// TODO add your code here
		System.out.println("Submit clicked");
		
		String query = queryEntertextPanel.getText();;
		Pair result;
		try {
			result = connector.executeQuerySeparateResult(query, Integer.MAX_VALUE);

			tableModel.setRowCount(0);
			tableModel.setColumnIdentifiers(result.attributes);
			for(int i = 0; i < result.data.size(); i++)
			{
				//System.out.println(Arrays.asList(result.data.get(i)));
				tableModel.addRow(result.data.get(i));
			}	
			
			tableModel.fireTableDataChanged();
			executionFeedbackPane.setText("Execution succeeded.");
			executionFeedbackPane.setForeground(new Color(0, 204, 0));
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			executionFeedbackPane.setText(e1.getMessage());
			executionFeedbackPane.setForeground(Color.red);
		}
		finally
		{
			
			logger.log(LOG_TYPES.BUTTON_CLICK, query);
		}
	}

	private void initComponents() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		Container myContainer = null;
		
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - abc def
		window = new JFrame();
		queryEnterLabel = new JLabel();
		resultDisplayLabel = new JLabel();
		scrollPane1 = new JScrollPane();
		queryEntertextPanel = new JTextPane();
		scrollPane2 = new JScrollPane();
		resultTable = new JTable();
		submitButton = new JButton();
		statusLabel = new JLabel();
		scrollPane3 = new JScrollPane();
		executionFeedbackPane = new JEditorPane();

		//======== window ========
		{
			Container windowContentPane = window.getContentPane();
			windowContentPane.setLayout(new FormLayout(
				"default, $lcgap, 150dlu, $lcgap, 345dlu",
				"default, $lgap, 94dlu, $lgap, 18dlu, $lgap, default, $lgap, 169dlu"));

			//---- queryEnterLabel ----
			queryEnterLabel.setText("text");
			windowContentPane.add(queryEnterLabel, CC.xy(3, 1));

			//---- resultDisplayLabel ----
			resultDisplayLabel.setText("text");
			windowContentPane.add(resultDisplayLabel, CC.xy(5, 1));

			//======== scrollPane1 ========
			{
				scrollPane1.setViewportView(queryEntertextPanel);
			}
			windowContentPane.add(scrollPane1, CC.xywh(3, 2, 1, 2));

			//======== scrollPane2 ========
			{
				scrollPane2.setViewportView(resultTable);
			}
			windowContentPane.add(scrollPane2, CC.xywh(5, 2, 1, 8));

			//---- submitButton ----
			submitButton.setText("text");
			submitButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					submitButtonMouseClicked(e);
				}
			});
			windowContentPane.add(submitButton, CC.xy(3, 5));

			//---- statusLabel ----
			statusLabel.setText("text");
			windowContentPane.add(statusLabel, CC.xy(3, 7));

			//======== scrollPane3 ========
			{

				//---- executionFeedbackPane ----
				executionFeedbackPane.setEditable(false);
				scrollPane3.setViewportView(executionFeedbackPane);
			}
			windowContentPane.add(scrollPane3, CC.xywh(3, 8, 1, 2));
			window.pack();
			window.setLocationRelativeTo(window.getOwner());
			
			myContainer = windowContentPane;
		}
		// JFormDesigner - End of component initialization  //GEN-END:initComponents

		//myContainer = windowContentPane;
		queryEnterLabel.setText("Query to send");
		resultDisplayLabel.setText("Query result");
		statusLabel.setText("Query execution status");
		submitButton.setText("Submit");
		setSize(myContainer.getSize());
		setMinimumSize(myContainer.getSize());
		setMaximumSize(myContainer.getSize());
		resultTable.setModel(tableModel);
		add(myContainer);
		
		addWindowListener(new WindowListener() {
	        @Override
	        public void windowClosing(WindowEvent e) {
	        	connector.closeDBConnection();
	        	System.out.println("SQL Connection closed.");
	        	JOptionPane.showMessageDialog(null, 
	        			"Log being uploaded... This might take a minute. \nClick OK to dismiss message. \nThe application will exit upon upload completion.",
	        			"Upload Log", 
	        			JOptionPane.INFORMATION_MESSAGE);
	        	logger.sendLog();
	            System.exit(0);
	        }

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
	    });
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - abc def
	private JFrame window;
	private JLabel queryEnterLabel;
	private JLabel resultDisplayLabel;
	private JScrollPane scrollPane1;
	private JTextPane queryEntertextPanel;
	private JScrollPane scrollPane2;
	private JTable resultTable;
	private JButton submitButton;
	private JLabel statusLabel;
	private JScrollPane scrollPane3;
	private JEditorPane executionFeedbackPane;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}

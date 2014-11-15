/*
 * Created by JFormDesigner on Sat Nov 15 14:27:22 EST 2014
 */

package mainWindow;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

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
	
	public UserWindow() {
		super();
		initComponents();
		connector.initializeSQLConnection();
	}

	private void submitButtonMouseClicked(MouseEvent e) {
		// TODO add your code here
		System.out.println("Submit clicked");
		
		String query = queryEntertextPanel.getText();;
		Pair result = connector.executeQuerySeparateResult(query, Integer.MAX_VALUE);
		
		tableModel.setRowCount(0);
		tableModel.setColumnIdentifiers(result.attributes);
		for(int i = 0; i < result.data.size(); i++)
		{
			System.out.println(Arrays.asList(result.data.get(i)));
			tableModel.addRow(result.data.get(i));
		}	
		
		tableModel.fireTableDataChanged();
	}

	private void windowWindowClosing(WindowEvent e) {
		// TODO add your code here
		connector.closeDBConnection();
		System.out.println("SQL connection closed.");
	}

	private void queryEntertextPanelKeyTyped(KeyEvent e) {
		// TODO add your code here
		
	}

	private void windowWindowClosed(WindowEvent e) {
		// TODO add your code here
		connector.closeDBConnection();
		System.out.println("SQL connection closed.");
	}


	private void initComponents() {

		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container myContainer = null;
		// assign myContainer
		// add table model
		
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - abc def
		window = new JFrame();
		queryEnterLabel = new JLabel();
		resultDisplayLabel = new JLabel();
		scrollPane1 = new JScrollPane();
		queryEntertextPanel = new JTextPane();
		scrollPane2 = new JScrollPane();
		resultTable = new JTable(tableModel);
		submitButton = new JButton();

		//======== window ========
		{
			window.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					windowWindowClosed(e);
				}
				@Override
				public void windowClosing(WindowEvent e) {
					windowWindowClosing(e);
				}
			});
			Container windowContentPane = window.getContentPane();
			windowContentPane.setLayout(new FormLayout(
				"default, $lcgap, 150dlu, $lcgap, 284dlu",
				"default, $lgap, 94dlu, $lgap, 18dlu, $lgap, 169dlu"));
			myContainer = windowContentPane;

			//---- queryEnterLabel ----
			queryEnterLabel.setText("text");
			windowContentPane.add(queryEnterLabel, CC.xy(3, 1));

			//---- resultDisplayLabel ----
			resultDisplayLabel.setText("text");
			windowContentPane.add(resultDisplayLabel, CC.xy(5, 1));

			//======== scrollPane1 ========
			{

				//---- queryEntertextPanel ----
				queryEntertextPanel.addKeyListener(new KeyAdapter() {
					@Override
					public void keyTyped(KeyEvent e) {
						queryEntertextPanelKeyTyped(e);
					}
				});
				scrollPane1.setViewportView(queryEntertextPanel);
			}
			windowContentPane.add(scrollPane1, CC.xywh(3, 2, 1, 2));

			//======== scrollPane2 ========
			{
				scrollPane2.setViewportView(resultTable);
			}
			windowContentPane.add(scrollPane2, CC.xywh(5, 2, 1, 6));

			//---- submitButton ----
			submitButton.setText("text");
			submitButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					submitButtonMouseClicked(e);
				}
			});
			windowContentPane.add(submitButton, CC.xy(3, 5));
			window.pack();
			window.setLocationRelativeTo(window.getOwner());
		}
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		queryEnterLabel.setText("Query to send");
		resultDisplayLabel.setText("Query result");
		submitButton.setText("Submit");
		setSize(myContainer.getSize());
		add(myContainer);
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
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}

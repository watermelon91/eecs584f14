/*
 * Created by JFormDesigner on Sat Nov 15 14:27:22 EST 2014
 */

package mainWindow;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import javax.swing.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import frontEndConnector.FrontEndConnector;
import frontEndConnector.FrontEndConnector.Pair;

/**
 * @author abc def
 */
public class UserWindow extends JFrame {
	
	private FrontEndConnector connector = new FrontEndConnector("yijiadanajie.cta5xgwtrfyv.us-west-2.rds.amazonaws.com", "mydb", "yijia", "eecs58414");
	
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
		
		for(int i = 0; i < result.data.size(); i++)
		{
			System.out.println(Arrays.asList(result.data.get(i)));
		}
	}

	private void windowWindowClosing(WindowEvent e) {
		// TODO add your code here
		connector.closeDBConnection();
	}

	private void queryEntertextPanelKeyTyped(KeyEvent e) {
		// TODO add your code here
		
	}


	private void initComponents() {

		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
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

		//======== window ========
		{
			window.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					windowWindowClosing(e);
				}
			});
			Container windowContentPane = window.getContentPane();
			windowContentPane.setLayout(new FormLayout(
				"default, $lcgap, 150dlu, $lcgap, 284dlu",
				"default, $lgap, 94dlu, $lgap, 18dlu, $lgap, 169dlu"));

			//---- queryEnterLabel ----
			queryEnterLabel.setText("Query to send");
			windowContentPane.add(queryEnterLabel, CC.xy(3, 1));

			//---- resultDisplayLabel ----
			resultDisplayLabel.setText("Query result");
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
			submitButton.setText("Submit");
			submitButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					submitButtonMouseClicked(e);
				}
			});
			windowContentPane.add(submitButton, CC.xy(3, 5));
			window.pack();
			window.setLocationRelativeTo(window.getOwner());
			
			setSize(windowContentPane.getSize());
			add(windowContentPane);
		}
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
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

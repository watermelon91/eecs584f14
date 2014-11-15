/*
 * Created by JFormDesigner on Sat Nov 15 14:27:22 EST 2014
 */

package mainWindow;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

/**
 * @author abc def
 */
public class UserWindow extends JFrame {
	
	public UserWindow() {
		super();
		initComponents();
	}

	private void submitButtonMouseClicked(MouseEvent e) {
		// TODO add your code here
		System.out.println("Submit clicked");
	}


	private void initComponents() {

		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - abc def
		frame1 = new JFrame();
		queryEnterLabel = new JLabel();
		resultDisplayLabel = new JLabel();
		scrollPane1 = new JScrollPane();
		queryEntertextPanel = new JTextPane();
		scrollPane2 = new JScrollPane();
		resultTable = new JTable();
		submitButton = new JButton();

		//======== frame1 ========
		{
			Container frame1ContentPane = frame1.getContentPane();
			frame1ContentPane.setLayout(new FormLayout(
				"default, $lcgap, 150dlu, $lcgap, 284dlu",
				"default, $lgap, 94dlu, $lgap, 18dlu, $lgap, 169dlu"));

			//---- queryEnterLabel ----
			queryEnterLabel.setText("text");
			frame1ContentPane.add(queryEnterLabel, CC.xy(3, 1));

			//---- resultDisplayLabel ----
			resultDisplayLabel.setText("text");
			frame1ContentPane.add(resultDisplayLabel, CC.xy(5, 1));

			//======== scrollPane1 ========
			{
				scrollPane1.setViewportView(queryEntertextPanel);
			}
			frame1ContentPane.add(scrollPane1, CC.xywh(3, 2, 1, 2));

			//======== scrollPane2 ========
			{
				scrollPane2.setViewportView(resultTable);
			}
			frame1ContentPane.add(scrollPane2, CC.xywh(5, 2, 1, 6));

			//---- submitButton ----
			submitButton.setText("text");
			submitButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					submitButtonMouseClicked(e);
				}
			});
			frame1ContentPane.add(submitButton, CC.xy(3, 5));
			frame1.pack();
			frame1.setLocationRelativeTo(frame1.getOwner());
			
			setBounds(frame1ContentPane.getBounds());
			add(frame1ContentPane);
		}
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - abc def
	private JFrame frame1;
	private JLabel queryEnterLabel;
	private JLabel resultDisplayLabel;
	private JScrollPane scrollPane1;
	private JTextPane queryEntertextPanel;
	private JScrollPane scrollPane2;
	private JTable resultTable;
	private JButton submitButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}

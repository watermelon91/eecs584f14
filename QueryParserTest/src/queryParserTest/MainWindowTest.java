package queryParserTest;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.io.*;

public class MainWindowTest {

	 public static void main(String [] args)
	  {
	    /*
		 JFrame frame=new JFrame();
		frame.setSize(100,100);
	    
	    
	    JButton button = new JButton();
	    button.setSize(100, 100);
	    button.setVisible(true);
	    
	    frame.add(button);
	    */
		 
		 MyFrame frame = new MyFrame();
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setVisible(true);
	  
	  }
}

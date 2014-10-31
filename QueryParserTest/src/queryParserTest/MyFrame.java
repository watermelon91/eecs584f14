package queryParserTest;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.io.*;

public class MyFrame extends JFrame {
	
	JButton button;

	MyFrame()
	{
		super();
		setSize(100, 100);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		
		button = new JButton("LOL");
		button.setSize(50, 50);
		
		add(button);
	}
}

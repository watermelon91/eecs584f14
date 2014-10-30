package binaryTree;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;

public class SwingWindow {

    private JFrame frame;
    private BinaryTreePanel panel;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try
                {
                    SwingWindow window = new SwingWindow(); 
                    window.frame.setVisible(true);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public SwingWindow()
        {
            initialize();
        }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame("frame");
        frame.setBounds(100, 100, 700, 800);
        frame.setPreferredSize(new Dimension(700,800));
        frame.setMinimumSize(new Dimension(700,800));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        LinkedBinaryTreeNode<Integer> root = new LinkedBinaryTreeNode<Integer>(5);
        root.setLeft(new LinkedBinaryTreeNode<Integer>(10));
        root.setRight(new LinkedBinaryTreeNode<Integer>(20));
        root.getLeft().setLeft(new LinkedBinaryTreeNode<Integer>(30));
        root.getRight().setLeft(new LinkedBinaryTreeNode<Integer>(40));
        
        panel = new BinaryTreePanel(root, 100, 100);
       panel.setLocation(10, 10);
       panel.setSize(1000, 1000);
       panel.setOpaque(true);
       
       frame.getContentPane().add(panel);
       frame.pack();

    }

}

package binaryTree;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;

import frontEndConnector.FrontEndConnector;
import frontEndConnector.QueryPlanTreeNode;

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
        
        /*
        LinkedBinaryTreeNode<Integer> root = new LinkedBinaryTreeNode<Integer>(5);
        root.setLeft(new LinkedBinaryTreeNode<Integer>(10));
        root.setRight(new LinkedBinaryTreeNode<Integer>(20));
        root.getLeft().setLeft(new LinkedBinaryTreeNode<Integer>(30));
        root.getRight().setLeft(new LinkedBinaryTreeNode<Integer>(40));
        */
        
        // create a connector to backend
        FrontEndConnector UIConnector = new FrontEndConnector("127.0.0.1", "eecs584", "postgres", "1academic");
        // start SQL connection
		String rst = UIConnector.initializeSQLConnection();
		if(rst.isEmpty())
		{
			System.out.println("Connection to db established...");
		}
		else
		{
			System.out.println(rst);
		}
		
        LinkedBinaryTreeNode<QueryPlanTreeNode> root;
		try {
			// get parsed query plan returned in the root of LinkedBinaryTree
			System.out.println("\nTEST: generate query plan tree & tmp tables...");
			root = UIConnector.debugQuery("select * from hrecords h, users u where h.user_id = u.user_id;");
			
			// draw the query plan
	        panel = new BinaryTreePanel(root, 100, 100);
	        panel.setLocation(10, 10);
	        panel.setSize(1000, 1000);
	        panel.setOpaque(true);
	       
	        frame.getContentPane().add(panel);
	        frame.pack();
	        
	        // get sample data
	        System.out.println("\nTEST: get sample data...");
	        String sampleDataRst = UIConnector.getSampleData("tmp1");
			String[] sampleDataRstArray = sampleDataRst.split(",");
			for(int i = 0; i < sampleDataRstArray.length; i++)
			{
				System.out.println(sampleDataRstArray[i]);
			}
	        
			// execute test query
			System.out.println("\nTEST: test query...");
			String testQueryRst = UIConnector.executeTestQuery("SELECT * FROM tmp1 WHERE user_id < 5;");
			String[] testQueryRstArray = testQueryRst.split(",");
			for(int i = 0; i < testQueryRstArray.length; i++)
			{
				System.out.println(testQueryRstArray[i]);
			}
	        
	        
	        // close db connection
			UIConnector.closeDBConnection();
			if(rst.isEmpty())
			{
				System.out.println("Connection to db closed.");
			}
			else
			{
				System.out.println(rst);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }

}

package mainWindow;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JButton;
import javax.swing.border.TitledBorder;
import javax.swing.JTable;
import javax.swing.JPasswordField;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import binaryTree.BinaryTreeNode;
import binaryTree.LinkedBinaryTreeNode;
import frontEndConnector.FrontEndConnector;
import frontEndConnector.QueryPlanTreeNode;

public class QueryDebuggerMainWindowSwing extends JFrame{
    private JTextField textUsername;
    private JPasswordField textPassword;
    private JButton btnDbSubmit;
    private JButton btnDbCancel;
    private JTable table;
    
    private JTextPane queryPane;
    private JButton btnQuerySubmit;
    private JButton btnQueryCancel;
    
    private mxGraph graph;

    private FrontEndConnector connector;
    
    private LinkedBinaryTreeNode<QueryPlanTreeNode> tree;
    
    final int gridwidth = 40, gridheight = 40;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try
                {
                    QueryDebuggerMainWindowSwing window = new QueryDebuggerMainWindowSwing();
                    window.setVisible(true);
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
    public QueryDebuggerMainWindowSwing()
        {
            super();
            initialize();
        }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setBounds(100, 100, 960, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Database Log In", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        JLabel lblUsername = new JLabel("Username: ");
        
        JLabel lblPassword = new JLabel("Password:");
        
        btnDbCancel = new JButton("Cancel");
        btnDbCancel.addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent arg0) {
                textUsername.setText("");
                textPassword.setText("");  
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }
        });
        
        btnDbSubmit = new JButton("Submit");
        btnDbSubmit.addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                if (btnDbSubmit.getText() == "Submit"){
                    connector = new FrontEndConnector("127.0.0.1","K" , "K", "5432");
                    //connector = new FrontEndConnector("127.0.0.1","K" , textUsername.getText(), textPassword.getText());
                    String rst = connector.initializeSQLConnection();
                    if(rst.isEmpty())
                        System.out.println("postgres connection established");
                    else System.out.println("postgres connection failed");
                    
                    textUsername.setEnabled(false);
                    textPassword.setEnabled(false);
                    btnDbCancel.setEnabled(false);
                    btnDbSubmit.setText("Disconnect");
                    btnDbSubmit.setSize(30, btnDbSubmit.getHeight());
                } else if (btnDbSubmit.getText() == "Disconnect") {
                    connector.closeDBConnection();
                    
                    textUsername.setEnabled(true);
                    textPassword.setEnabled(true);
                    btnDbCancel.setEnabled(true);
                    btnDbSubmit.setText("Submit");
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }
            
        });
        
        textUsername = new JTextField();
        
        textPassword = new JPasswordField();

        GroupLayout gl_panel = new GroupLayout(panel);
        gl_panel.setHorizontalGroup(
            gl_panel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel.createSequentialGroup()
                    .addGap(12)
                    .addComponent(lblUsername)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addComponent(textUsername, GroupLayout.PREFERRED_SIZE, 272, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblPassword, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(textPassword, GroupLayout.PREFERRED_SIZE, 278, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(btnDbCancel, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(btnDbSubmit, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
                    .addGap(42))
        );
        gl_panel.setVerticalGroup(
            gl_panel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblPassword)
                        .addComponent(lblUsername)
                        .addComponent(textUsername, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(textPassword, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnDbCancel)
                        .addComponent(btnDbSubmit))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel.setLayout(gl_panel);
        
        JPanel panel_1 = new JPanel();
        panel_1.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Query Box", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        
        JLabel lblPleaseEbterA = new JLabel("Please enter a query to execute:");
        queryPane = new JTextPane();
        JScrollPane queryscrollPane = new JScrollPane(queryPane);
        
        btnQuerySubmit = new JButton("Submit");
        btnQuerySubmit.addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (btnQuerySubmit.getText() == "Submit"){
                    // TODO submit query to connector and receive tree
                    queryPane.setEnabled(false);
                    btnQuerySubmit.setText("Edit"); 
                    btnQueryCancel.setEnabled(false);
                    
                    // TODO delete tree tree below
                    try
                    {
                        tree = connector.debugQuery("select * from hrecords h, users u where h.user_id = u.user_id;");
                    } catch (Exception e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    if (tree!= null) {
                        drawPlanTree();
                    }
                } else if (btnQuerySubmit.getText() == "Edit") {
                    queryPane.setEnabled(true);
                    btnQuerySubmit.setText("Submit");
                    btnQueryCancel.setEnabled(true);
                }
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                
            }
            
        });
        
        btnQueryCancel = new JButton("Cancel");
        btnQueryCancel.addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                queryPane.setText("");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }
            
        });
        GroupLayout gl_panel_1 = new GroupLayout(panel_1);
        gl_panel_1.setHorizontalGroup(
            gl_panel_1.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_1.createSequentialGroup()
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel_1.createSequentialGroup()
                            .addGap(6)
                            .addComponent(queryscrollPane, GroupLayout.PREFERRED_SIZE, 381, GroupLayout.PREFERRED_SIZE))
                        .addComponent(lblPleaseEbterA))
                    .addGap(377))
                .addGroup(gl_panel_1.createSequentialGroup()
                    .addGap(204)
                    .addComponent(btnQuerySubmit)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(btnQueryCancel, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(384, Short.MAX_VALUE))
        );
        gl_panel_1.setVerticalGroup(
            gl_panel_1.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_1.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblPleaseEbterA)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(queryscrollPane, GroupLayout.PREFERRED_SIZE, 244, GroupLayout.PREFERRED_SIZE)
                    .addGap(12)
                    .addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
                        .addComponent(btnQuerySubmit)
                        .addComponent(btnQueryCancel))
                    .addGap(51))
        );
        panel_1.setLayout(gl_panel_1);
        
        JPanel panel_2 = new JPanel();
        panel_2.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Subquery Box", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        JPanel panel_3 = new JPanel();
        panel_3.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Plan Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        graph = new mxGraph();
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graphComponent.getViewport().setOpaque(true);
        graphComponent.getViewport().setBackground(panel_3.getBackground());
        graphComponent.setBorder(null);
        panel_3.add(graphComponent);
        
        JPanel panel_4 = new JPanel();
        panel_4.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Subquery Partial Result", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                        .addComponent(panel, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 943, Short.MAX_VALUE)
                        .addGroup(groupLayout.createSequentialGroup()
                            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                .addGroup(groupLayout.createSequentialGroup()
                                    .addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                                    .addGap(18))
                                .addGroup(groupLayout.createSequentialGroup()
                                    .addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 414, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(ComponentPlacement.RELATED)))
                            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                .addComponent(panel_4, GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                                .addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 511, GroupLayout.PREFERRED_SIZE))))
                    .addGap(51))
        );
        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addComponent(panel, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 340, GroupLayout.PREFERRED_SIZE)
                        .addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 340, Short.MAX_VALUE))
                    .addGap(18)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                        .addComponent(panel_4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE))
                    .addGap(58))
        );
        
        table = new JTable();
        panel_4.add(table);
        
        JLabel lblPleaseEnter = new JLabel("Please enter a query for the node selected in the plan tree:");
        
        JTextPane textPane_1 = new JTextPane();
        
        JButton button = new JButton("Submit");
        
        JButton button_1 = new JButton("Cancel");
        GroupLayout gl_panel_2 = new GroupLayout(panel_2);
        gl_panel_2.setHorizontalGroup(
            gl_panel_2.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_panel_2.createSequentialGroup()
                    .addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel_2.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(lblPleaseEnter))
                        .addGroup(gl_panel_2.createSequentialGroup()
                            .addGap(12)
                            .addComponent(textPane_1, GroupLayout.PREFERRED_SIZE, 381, GroupLayout.PREFERRED_SIZE))
                        .addGroup(gl_panel_2.createSequentialGroup()
                            .addGap(214)
                            .addComponent(button, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(button_1, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap(374, Short.MAX_VALUE))
        );
        gl_panel_2.setVerticalGroup(
            gl_panel_2.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_2.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblPleaseEnter)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(textPane_1, GroupLayout.PREFERRED_SIZE, 234, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
                        .addComponent(button)
                        .addComponent(button_1))
                    .addContainerGap(8, Short.MAX_VALUE))
        );
        panel_2.setLayout(gl_panel_2);
        getContentPane().setLayout(groupLayout);
    }
    
    private class PlanTreeNode{
        Point point;
        Object obj;
        
        public PlanTreeNode(Point p, Object o){
            point = p;
            obj = o;
        }
    };
    
    void traverse(BinaryTreeNode root, int x, Map<BinaryTreeNode<?>, PlanTreeNode> coordinates){
        if (root == null) return;
        coordinates.put(root, new PlanTreeNode(new Point(x, gridheight * (root.depth()+1)), null));

        if (root.getRight() == null)
            traverse(root.getLeft(), x, coordinates);
        else {
            traverse(root.getLeft(), x-gridwidth, coordinates);
            traverse(root.getRight(), x+gridwidth, coordinates);
        }
    }
    private void drawPlanTree(){
        final Object parent = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        
        final Map<BinaryTreeNode<?>, PlanTreeNode> coordinates = new HashMap<BinaryTreeNode<?>, PlanTreeNode>();
        //traverse(tree, 0, coordinates);
        
        tree.traverseInorder(new BinaryTreeNode.Visitor() {
            private int x = gridwidth;
            public void visit(BinaryTreeNode node) {
                x += gridwidth;
                coordinates.put(node, new PlanTreeNode(new Point(x, gridheight * (node.depth()+1)), null));
            }
        });
        

        tree.traversePreorder(new BinaryTreeNode.Visitor() {
            public void visit(BinaryTreeNode node) {
                String data = node.getData().toString();
                PlanTreeNode planTreeNode = coordinates.get(node);
                planTreeNode.obj = graph.insertVertex(parent, null, data, planTreeNode.point.x, planTreeNode.point.y, 20, 20);
                if (node.getParent() != null) {
                    PlanTreeNode parentPlanTreeNode = coordinates.get(node.getParent());
                    graph.insertEdge(parent, null, "", parentPlanTreeNode.obj, planTreeNode.obj);
                }
            }
        });
        
        graph.getModel().endUpdate();
    }
}
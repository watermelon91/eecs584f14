package mainWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Struct;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;
import javax.swing.JPasswordField;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import binaryTree.BinaryTreeNode;
import binaryTree.LinkedBinaryTreeNode;
import frontEndConnector.DataPlanTreeNode;
import frontEndConnector.FrontEndConnector;
import frontEndConnector.FrontEndConnector.Pair;
import frontEndConnector.QueryPlanTreeNode;

import java.awt.event.ActionEvent;
import javax.swing.JTabbedPane;
import java.awt.Component;
import javax.swing.ScrollPaneConstants;
import java.awt.FlowLayout;

public class QueryDebuggerMainWindowSwing extends JFrame{
    private JTextField textUsername;
    private JPasswordField textPassword;
    private JButton btnDbSubmit;
    private JButton btnDbCancel;
    
    private JTextPane queryPane;
    private JButton btnQuerySubmit;
    private JButton btnQueryCancel;
    
    private JTextPane subQueryPane;
    private JButton btnSubQuerySubmit;
    private JButton btnSubQueryCancel;
    
    private mxGraph graph_sampleData;
    private mxGraph graph_trackTuple;


    private FrontEndConnector connector;
    
    private LinkedBinaryTreeNode<QueryPlanTreeNode> tree_sampleData;
    
    private Map<Object, QueryPlanTreeNode> treeObjects;
    
    private DefaultTableModel model_sampleData;
    private DefaultTableModel model_trackTuple;

    
    final int gridwidth = 120, gridheight = 150;
    private JTextField queryFrom_sampleData;
    
    private LoggingUtilities logger;
    
    private Pair samplePair;
    private JLabel lblPleaseEnter;
    private JButton btnExpandAll_sampleData;
    private JTextField queryFrom_trackTuple;

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
        logger = new LoggingUtilities();
        
        setBounds(100, 100, 1278, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // DB Login
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
                
                logger.log(LoggingUtilities.LOG_TYPES.BUTTON_CLICK, "log in cancel");
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
                    
                    logger.log(LoggingUtilities.LOG_TYPES.BUTTON_CLICK, "log in submit");
                } else if (btnDbSubmit.getText() == "Disconnect") {
                    connector.closeDBConnection();
                    
                    textUsername.setEnabled(true);
                    textPassword.setEnabled(true);
                    btnDbCancel.setEnabled(true);
                    btnDbSubmit.setText("Submit");
                    
                    logger.log(LoggingUtilities.LOG_TYPES.BUTTON_CLICK, "log in disconnect");
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
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(lblPassword, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(textPassword, GroupLayout.PREFERRED_SIZE, 278, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(btnDbCancel, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(btnDbSubmit, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(42, Short.MAX_VALUE))
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
                   
        
        JPanel panel_5 = new JPanel();
        {
            // Query Box   
            JPanel panel_1 = new JPanel();
            panel_5.add(panel_1);
            panel_1.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Query Box", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            
                    
                    JLabel lblPleaseEbterA = new JLabel("Please enter a query to execute:");
                    queryPane = new JTextPane();
                    JScrollPane queryscrollPane = new JScrollPane(queryPane);
                    
                    btnQuerySubmit = new JButton("Submit");
                    btnQuerySubmit.addMouseListener(new MouseListener(){
    
                        @Override
                        public void mouseClicked(MouseEvent arg0) {
                            if (btnQuerySubmit.getText() == "Submit"){
                                // submit query to connector and receive tree
                                queryPane.setEnabled(false);
                                btnQuerySubmit.setText("Edit"); 
                                btnQueryCancel.setEnabled(false);
                                
                                // delete tree below
                                try
                                {
                                    connector.dropAllTmpTables();
                                    tree_sampleData = connector.debugQuery("select * from hrecords h, users u where h.user_id = u.user_id;");
                                } catch (Exception e)
                                {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                
                                if (tree_sampleData!= null) {
                                    drawPlanTree();
                                    
                                    logger.log(LoggingUtilities.LOG_TYPES.BUTTON_CLICK, "query submit");
                                }
                            } else if (btnQuerySubmit.getText() == "Edit") {
                                queryPane.setEnabled(true);
                                btnQuerySubmit.setText("Submit");
                                btnQueryCancel.setEnabled(true);
                                
                                logger.log(LoggingUtilities.LOG_TYPES.BUTTON_CLICK, "query edit");
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
                            
                            logger.log(LoggingUtilities.LOG_TYPES.BUTTON_CLICK, "query cancel");
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
                                .addGap(6)
                                .addComponent(lblPleaseEbterA))
                            .addGroup(gl_panel_1.createSequentialGroup()
                                .addGap(85)
                                .addComponent(btnQuerySubmit)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(btnQueryCancel))
                            .addGroup(gl_panel_1.createSequentialGroup()
                                .addGap(12)
                                .addComponent(queryscrollPane, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                                .addContainerGap())
                    );
                    gl_panel_1.setVerticalGroup(
                        gl_panel_1.createParallelGroup(Alignment.LEADING)
                            .addGroup(gl_panel_1.createSequentialGroup()
                                .addGap(6)
                                .addComponent(lblPleaseEbterA)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(queryscrollPane, GroupLayout.PREFERRED_SIZE, 244, GroupLayout.PREFERRED_SIZE)
                                .addGap(12)
                                .addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
                                    .addComponent(btnQueryCancel)
                                    .addComponent(btnQuerySubmit))
                                .addContainerGap())
                    );
                    panel_1.setLayout(gl_panel_1);
        }
        // SubQuery Box
        JPanel panel_2 = new JPanel();
        panel_5.add(panel_2);
        panel_2.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Subquery Box", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        lblPleaseEnter = new JLabel("Please enter a query for plan tree node:");
        
        subQueryPane = new JTextPane();
        
        btnSubQuerySubmit = new JButton("Submit");
        btnSubQuerySubmit.addMouseListener(new MouseListener(){
    
            @Override
            public void mouseClicked(MouseEvent e) {
                if (btnSubQuerySubmit.getText() == "Submit"){
                    // submit query to connector and receive tree
                    btnSubQuerySubmit.setText("Edit"); 
                    btnSubQueryCancel.setEnabled(false);
                    btnExpandAll_sampleData.setText("Expand All");
                    queryFrom_sampleData.setText("Subquery");
              
                    samplePair = connector.executeTestQuery("select * from tmp0 order by h_user_id");  
    
                    model_sampleData.setColumnIdentifiers(samplePair.attributes);
                    model_sampleData.setRowCount(0);
                    for (String[] row: samplePair.data){
                        model_sampleData.addRow(row);
                    }                              
    
                    model_sampleData.fireTableDataChanged();
                    
                } else if (btnSubQuerySubmit.getText() == "Edit") {
                    subQueryPane.setEnabled(true);
                    btnSubQuerySubmit.setText("Submit");
                    btnSubQueryCancel.setEnabled(true);
                }                
            }
    
            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }
    
            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }
    
            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }
    
            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }
            
        });
        
        btnSubQueryCancel = new JButton("Cancel");
        btnSubQueryCancel.addMouseListener(new MouseListener(){

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
        GroupLayout gl_panel_2 = new GroupLayout(panel_2);
        gl_panel_2.setHorizontalGroup(
            gl_panel_2.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_2.createSequentialGroup()
                    .addGap(6)
                    .addComponent(lblPleaseEnter))
                .addGroup(gl_panel_2.createSequentialGroup()
                    .addGap(87)
                    .addComponent(btnSubQuerySubmit)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(btnSubQueryCancel, GroupLayout.PREFERRED_SIZE, 82, Short.MAX_VALUE)
                    .addContainerGap())
                .addGroup(gl_panel_2.createSequentialGroup()
                    .addGap(12)
                    .addComponent(subQueryPane, GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                    .addContainerGap())
        );
        gl_panel_2.setVerticalGroup(
            gl_panel_2.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_2.createSequentialGroup()
                    .addGap(6)
                    .addComponent(lblPleaseEnter)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(subQueryPane, GroupLayout.PREFERRED_SIZE, 234, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
                        .addComponent(btnSubQueryCancel)
                        .addComponent(btnSubQuerySubmit)))
        );
        gl_panel_2.setAutoCreateGaps(true);
        gl_panel_2.setAutoCreateContainerGaps(true);
        panel_2.setLayout(gl_panel_2);
                
        //Tabbed Pane
        final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        
        // Sample Data Tab
        JPanel tabSampleData = new JPanel();
        tabbedPane.addTab("Sample Data", null, tabSampleData, null);
        tabbedPane.setEnabledAt(0, true);
        

        // Sample Data Plan Tree
        JPanel panel_sampleDataPlanTree = new JPanel();
        panel_sampleDataPlanTree.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Plan Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        treeObjects = new HashMap<Object, QueryPlanTreeNode>();
        
        graph_sampleData = new mxGraph();
        graph_sampleData.setCellsEditable(false);
        graph_sampleData.setAllowDanglingEdges(false);
        
        final mxGraphComponent graphComponent_sampleData = new mxGraphComponent(graph_sampleData);
        graphComponent_sampleData.setPreferredSize(new Dimension(450, 300));
        graphComponent_sampleData.setAutoExtend(true);
        graphComponent_sampleData.getViewport().setOpaque(true);
        graphComponent_sampleData.setBorder(null);
        graphComponent_sampleData.setConnectable(false);
        graphComponent_sampleData.getViewport().setBackground(panel_sampleDataPlanTree.getBackground());
        graphComponent_sampleData.getGraphControl().addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                Object cell = graphComponent_sampleData.getCellAt(e.getX(), e.getY());
                if (treeObjects.containsKey(cell)){
                    btnExpandAll_sampleData.setText("Expand All");
                    queryFrom_sampleData.setText("Plan Tree Node");
                    
                    QueryPlanTreeNode node = treeObjects.get(cell);
                    samplePair = connector.getSampleData(node.getNewTableName());
                    
                    if (tabbedPane.getSelectedIndex() == 0){
                        System.out.println("here");
                        model_sampleData.setColumnIdentifiers(samplePair.attributes);
                        model_sampleData.setRowCount(0);
                        for (String[] row: samplePair.data){
                            model_sampleData.addRow(row);
                        }
                                     
                        model_sampleData.fireTableDataChanged(); 
                    }
                } 
                
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }
            
        });                
        panel_sampleDataPlanTree.add(graphComponent_sampleData);
        
        
        //Sample Data Pane
        JPanel panel_sampleDataTable = new JPanel();
        panel_sampleDataTable.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Record Table", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        JLabel lblQueryFor_sampleData = new JLabel("Query For:");
        
        queryFrom_sampleData = new JTextField();
        queryFrom_sampleData.setEditable(false);
        
        model_sampleData = new DefaultTableModel() {
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
              }
            };
            
        final JTable table_sampleData = new JTable(model_sampleData);
        table_sampleData.setFocusable(false);
        table_sampleData.addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {
                // Double Click to track tuple
                if (e.getClickCount() == 2){
                    tabbedPane.setSelectedIndex(1);
                }
                
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }
            
        });
        JScrollPane pane_sampleData = new JScrollPane(table_sampleData, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                           JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        btnExpandAll_sampleData = new JButton("Expand All");
        btnExpandAll_sampleData.addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {
                
                samplePair = null;
                if (btnExpandAll_sampleData.getText() == "Expand All"){
                    //TODO samplePair = connector.executeTestQuery(subQueryPane.getText());  
                    if (queryFrom_sampleData.getText().equals("Subquery"))
                        samplePair = connector.executeTestQueryAll("select * from tmp0 order by h_user_id");  
                    else if (queryFrom_sampleData.getText().equals("Plan Tree Node"))
                        samplePair = connector.getAllSampleData(treeObjects.get(graph_sampleData.getSelectionCell()).getNewTableName());
                    
                    btnExpandAll_sampleData.setText("Collapse sample");
                } else {
                    //TODO samplePair = connector.executeTestQuery(subQueryPane.getText());  
                    if (queryFrom_sampleData.getText().equals("Subquery"))
                        samplePair = connector.executeTestQuery("select * from tmp0 order by h_user_id");  
                    else if (queryFrom_sampleData.getText().equals("Plan Tree Node"))
                        samplePair = connector.getSampleData(treeObjects.get(graph_sampleData.getSelectionCell()).getNewTableName());               
                    btnExpandAll_sampleData.setText("Expand All");
                }

                model_sampleData.setRowCount(0);
                if (samplePair != null) {
                    model_sampleData.setColumnIdentifiers(samplePair.attributes);
                    for (String[] row: samplePair.data){
                        model_sampleData.addRow(row);
                    }             
                }

                model_sampleData.fireTableDataChanged();  
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub
                
            }
            
        });
        
        GroupLayout gl_panel_sampleDataTable = new GroupLayout(panel_sampleDataTable);
        gl_panel_sampleDataTable.setHorizontalGroup(
            gl_panel_sampleDataTable.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_panel_sampleDataTable.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_panel_sampleDataTable.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel_sampleDataTable.createSequentialGroup()
                            .addComponent(lblQueryFor_sampleData)
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addComponent(queryFrom_sampleData, GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE))
                        .addGroup(gl_panel_sampleDataTable.createSequentialGroup()
                            .addGap(6)
                            .addComponent(pane_sampleData, GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE))
                        .addComponent(btnExpandAll_sampleData, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE))
                    .addGap(15))
        );
        gl_panel_sampleDataTable.setVerticalGroup(
            gl_panel_sampleDataTable.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_sampleDataTable.createSequentialGroup()
                    .addGap(5)
                    .addGroup(gl_panel_sampleDataTable.createParallelGroup(Alignment.BASELINE)
                        .addComponent(queryFrom_sampleData, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblQueryFor_sampleData))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(pane_sampleData, GroupLayout.PREFERRED_SIZE, 543, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addComponent(btnExpandAll_sampleData)
                    .addGap(12))
        );
        panel_sampleDataTable.setLayout(gl_panel_sampleDataTable);
        GroupLayout gl_tabSampleData = new GroupLayout(tabSampleData);
        gl_tabSampleData.setHorizontalGroup(
            gl_tabSampleData.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_tabSampleData.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panel_sampleDataPlanTree, GroupLayout.PREFERRED_SIZE, 480, GroupLayout.PREFERRED_SIZE)
                    .addGap(15)
                    .addComponent(panel_sampleDataTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
        );
        gl_tabSampleData.setVerticalGroup(
            gl_tabSampleData.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_tabSampleData.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_tabSampleData.createParallelGroup(Alignment.LEADING)
                        .addComponent(panel_sampleDataPlanTree, GroupLayout.PREFERRED_SIZE, 640, GroupLayout.PREFERRED_SIZE)
                        .addComponent(panel_sampleDataTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
        );
        panel_sampleDataPlanTree.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        tabSampleData.setLayout(gl_tabSampleData);
        
        
        // Track Tuple Tab
        JPanel tabTrackTuple = new JPanel();
        tabbedPane.addTab("Track Tuple", null, tabTrackTuple, null);
        
        JPanel panel_trackTuplePlanTree = new JPanel();
        panel_trackTuplePlanTree.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Plan Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GroupLayout gl_panel_trackTuplePlanTree = new GroupLayout(panel_trackTuplePlanTree);
        gl_panel_trackTuplePlanTree.setHorizontalGroup(
            gl_panel_trackTuplePlanTree.createParallelGroup(Alignment.LEADING)
                .addGap(0, 480, Short.MAX_VALUE)
                .addGap(0, 468, Short.MAX_VALUE)
        );
        gl_panel_trackTuplePlanTree.setVerticalGroup(
            gl_panel_trackTuplePlanTree.createParallelGroup(Alignment.LEADING)
                .addGap(0, 640, Short.MAX_VALUE)
                .addGap(0, 612, Short.MAX_VALUE)
        );
        panel_trackTuplePlanTree.setLayout(gl_panel_trackTuplePlanTree);
        
        JPanel panel_trackTupleTable = new JPanel();
        panel_trackTupleTable.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Record Table", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        JLabel lblQueryFor_trackTuple = new JLabel("Tracking Down:");
        
        queryFrom_trackTuple = new JTextField();
        queryFrom_trackTuple.setEditable(false);
        
        JScrollPane pane_trackTuple = new JScrollPane((Component) null, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        JButton btnExpandAll_trackTuple = new JButton("Expand All");
        GroupLayout gl_panel_trackTupleTable = new GroupLayout(panel_trackTupleTable);
        gl_panel_trackTupleTable.setHorizontalGroup(
            gl_panel_trackTupleTable.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_panel_trackTupleTable.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_panel_trackTupleTable.createParallelGroup(Alignment.LEADING)
                        .addComponent(btnExpandAll_trackTuple, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
                        .addGroup(gl_panel_trackTupleTable.createParallelGroup(Alignment.TRAILING, false)
                            .addGroup(gl_panel_trackTupleTable.createSequentialGroup()
                                .addComponent(lblQueryFor_trackTuple)
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addComponent(queryFrom_trackTuple))
                            .addGroup(Alignment.LEADING, gl_panel_trackTupleTable.createSequentialGroup()
                                .addGap(6)
                                .addComponent(pane_trackTuple, GroupLayout.PREFERRED_SIZE, 396, GroupLayout.PREFERRED_SIZE))))
                    .addGap(15))
        );
        gl_panel_trackTupleTable.setVerticalGroup(
            gl_panel_trackTupleTable.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_trackTupleTable.createSequentialGroup()
                    .addGap(5)
                    .addGroup(gl_panel_trackTupleTable.createParallelGroup(Alignment.BASELINE)
                        .addComponent(queryFrom_trackTuple, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblQueryFor_trackTuple))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(pane_trackTuple, GroupLayout.PREFERRED_SIZE, 543, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addComponent(btnExpandAll_trackTuple)
                    .addGap(12))
        );
        panel_trackTupleTable.setLayout(gl_panel_trackTupleTable);
        GroupLayout gl_tabTrackTuple = new GroupLayout(tabTrackTuple);
        gl_tabTrackTuple.setHorizontalGroup(
            gl_tabTrackTuple.createParallelGroup(Alignment.LEADING)
                .addGap(0, 924, Short.MAX_VALUE)
                .addGroup(gl_tabTrackTuple.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panel_trackTuplePlanTree, GroupLayout.PREFERRED_SIZE, 480, GroupLayout.PREFERRED_SIZE)
                    .addGap(15)
                    .addComponent(panel_trackTupleTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
        );
        gl_tabTrackTuple.setVerticalGroup(
            gl_tabTrackTuple.createParallelGroup(Alignment.LEADING)
                .addGap(0, 659, Short.MAX_VALUE)
                .addGroup(gl_tabTrackTuple.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_tabTrackTuple.createParallelGroup(Alignment.LEADING)
                        .addComponent(panel_trackTuplePlanTree, GroupLayout.PREFERRED_SIZE, 640, GroupLayout.PREFERRED_SIZE)
                        .addComponent(panel_trackTupleTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
        );
        tabTrackTuple.setLayout(gl_tabTrackTuple);
      
        
        model_trackTuple = new DefaultTableModel() {
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
              }
            };
            
            
            
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(panel, GroupLayout.PREFERRED_SIZE, 2004, Short.MAX_VALUE)
                        .addGroup(groupLayout.createSequentialGroup()
                            .addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 308, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 945, GroupLayout.PREFERRED_SIZE)))
                    .addGap(11))
        );
        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addComponent(panel, GroupLayout.PREFERRED_SIZE, 67, Short.MAX_VALUE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 691, GroupLayout.PREFERRED_SIZE)
                        .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 705, GroupLayout.PREFERRED_SIZE)))
        );
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
    
    int traverse(BinaryTreeNode root, int x, Map<BinaryTreeNode<?>, PlanTreeNode> coordinates){
        if (root == null) return x;
        
        coordinates.put(root, new PlanTreeNode(new Point(x, gridheight * root.depth()), null));

        int maxDepth;
        if (root.getRight() == null){
            return traverse(root.getLeft(), x, coordinates);
        }
        else {
            traverse(root.getRight(), x+gridwidth, coordinates);
            return traverse(root.getLeft(), x-gridwidth, coordinates);
        }
    }
    private void drawPlanTree(){
        final Object parent = graph_sampleData.getDefaultParent();

        graph_sampleData.getModel().beginUpdate();
        
        final Map<BinaryTreeNode<?>, PlanTreeNode> coordinates = new HashMap<BinaryTreeNode<?>, PlanTreeNode>();
        final int maxshift = traverse(tree_sampleData, 0, coordinates);

        tree_sampleData.traversePreorder(new BinaryTreeNode.Visitor() {
            public void visit(BinaryTreeNode node) {
                QueryPlanTreeNode treeNode = (QueryPlanTreeNode) node.getData();
                PlanTreeNode planTreeNode = coordinates.get(node);
                planTreeNode.obj = graph_sampleData.insertVertex(parent, null, treeNode.getAbbreviatedTreeNode().getLargeFontStr()+"\n"+treeNode.getAbbreviatedTreeNode().getSmallFontStr(),planTreeNode.point.x-maxshift, planTreeNode.point.y, 200, 50);
                treeObjects.put(planTreeNode.obj, (QueryPlanTreeNode) node.getData());
                if (node.getParent() != null) {
                    PlanTreeNode parentPlanTreeNode = coordinates.get(node.getParent());
                    graph_sampleData.insertEdge(parent, null, "", planTreeNode.obj, parentPlanTreeNode.obj);
                }
            }
        });      
        
        graph_sampleData.getModel().endUpdate();
    }
}

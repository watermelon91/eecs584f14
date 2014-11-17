package mainWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import binaryTree.BinaryTreeNode;
import binaryTree.LinkedBinaryTreeNode;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.sun.xml.internal.ws.util.StringUtils;

import frontEndConnector.FrontEndConnector;
import frontEndConnector.FrontEndConnector.Pair;
import frontEndConnector.QueryPlanTreeNode;

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
    private mxGraph graph_findMissing;


    private FrontEndConnector connector;
    
    private LinkedBinaryTreeNode<QueryPlanTreeNode> tree_sampleData;
    private LinkedBinaryTreeNode<QueryPlanTreeNode> tree_trackTuple;
    private LinkedBinaryTreeNode<QueryPlanTreeNode> tree_findMissing;

    
    private Map<Object, LinkedBinaryTreeNode<QueryPlanTreeNode>> treeObjects_sampleData;
    private Map<Object, LinkedBinaryTreeNode<QueryPlanTreeNode>> treeObjects_trackTuple;
    private Map<Object, LinkedBinaryTreeNode<QueryPlanTreeNode>> treeObjects_findMissing;
    
    private DefaultTableModel model_sampleData;
    private DefaultTableModel model_trackTuple;
    private DefaultTableModel model_findMissing;
    private DefaultTableModel model_searchMissing;
    
    private JTable table_searchMissing;
    private DefaultTableCellRenderer renderer;
    
    final int gridwidth = 110, gridheight = 150;
    private JTextField queryFrom_sampleData;
    
    private LoggingUtilities logger;
    
    private Pair samplePair;
    private JLabel lblPleaseEnter;
    private JButton btnExpandAll_sampleData;
    private JButton btnExpandAll_trackTuple;
    private JButton btnExpandAll_findMissing;

    private JTextField queryFrom_trackTuple;
    private JTextField queryFrom_findMissing;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try
                {
                    QueryDebuggerMainWindowSwing window = new QueryDebuggerMainWindowSwing(new LoggingUtilities());
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
    public QueryDebuggerMainWindowSwing(LoggingUtilities slogger)
        {
            super();
            
            logger = slogger;
                
           
            initialize();
            
            
        }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        
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
                    //connector = new FrontEndConnector("yijiadanajie.cta5xgwtrfyv.us-west-2.rds.amazonaws.com", "mydb", "yijia" , "eecs58414");
                    //connector = new FrontEndConnector("127.0.0.1","K" , textUsername.getText(), textPassword.getText());
                    connector = new FrontEndConnector("127.0.0.1","postgres" , "K", "5432");

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
                                    //tree_sampleData = connector.debugQuery("SELECT c1.customerid, c2.customerid, o1.totalamount, o2.totalamount, o1.orderdate,  o2.orderdate FROM cust_hist c1, cust_hist c2, orders o1, orders o2 WHERE c1.customerid > c2.customerid AND c1.prod_id = c2.prod_id AND o1.orderid = c1.orderid AND o2.orderid = c2.orderid AND o1.totalamount - o2.totalamount > 500 AND o1.orderdate - o2.orderdate = 0;");
                                    tree_sampleData = connector.debugQuery("select * from hrecords h, users u, (select user_id from users u2) as t where h.user_id = u.user_id and u.user_id = t.user_id;");
                                } catch (Exception e)
                                {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                
                                if (tree_sampleData!= null) {
                                    drawPlanTree(graph_sampleData, tree_sampleData, treeObjects_sampleData);
                                    
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
        tabbedPane.addTab("Sample Intermediate Data", null, tabSampleData, null);
        tabbedPane.setEnabledAt(0, true);
        

        // Sample Data Plan Tree
        JPanel panel_sampleDataPlanTree = new JPanel();
        panel_sampleDataPlanTree.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Plan Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        treeObjects_sampleData = new HashMap<Object, LinkedBinaryTreeNode<QueryPlanTreeNode>>();
        
        graph_sampleData = new mxGraph();
        graph_sampleData.setCellsEditable(false);
        graph_sampleData.setAllowDanglingEdges(false);
        graph_sampleData.setCellsResizable(true);
        graph_sampleData.setAutoSizeCells(true);
        
        final mxGraphComponent graphComponent_sampleData = new mxGraphComponent(graph_sampleData);
        graphComponent_sampleData.setPreferredSize(new Dimension(450, 600));
        graphComponent_sampleData.setAutoExtend(false);
        graphComponent_sampleData.getViewport().setOpaque(true);
        graphComponent_sampleData.setBorder(null);
        graphComponent_sampleData.setConnectable(false);
        graphComponent_sampleData.getViewport().setBackground(panel_sampleDataPlanTree.getBackground());
        graphComponent_sampleData.getGraphControl().addMouseListener(new MouseListener(){
            private Object insertedVertex = null;

            @Override
            public void mouseClicked(MouseEvent e) {
                Object cell = graphComponent_sampleData.getCellAt(e.getX(), e.getY());
                if (treeObjects_sampleData.containsKey(cell)){
                    btnExpandAll_sampleData.setText("Expand All");
                    queryFrom_sampleData.setText("Plan Tree Node");
                    
                    QueryPlanTreeNode node = treeObjects_sampleData.get(cell).getData();
                    samplePair = connector.getSampleData(node.getNewTableName());
                    
                    model_sampleData.setColumnIdentifiers(samplePair.attributes);
                    model_searchMissing.setColumnIdentifiers(samplePair.attributes);

                    model_sampleData.setRowCount(0);
                    model_searchMissing.setRowCount(0);
                    model_searchMissing.setRowCount(1);
                    
                    for (String[] row: samplePair.data){
                        model_sampleData.addRow(row);
                    }
                                 
                    model_sampleData.fireTableDataChanged(); 
                    model_searchMissing.fireTableDataChanged(); 
                    
                    //detailed node info
                    graphComponent_sampleData.getGraph().getModel().beginUpdate();
                    if (insertedVertex != null)
                        graphComponent_sampleData.getGraph().removeCells(new Object[]{insertedVertex});

                    LinkedBinaryTreeNode<QueryPlanTreeNode> treeNode = treeObjects_sampleData.get(cell);
                    mxGeometry geo = graphComponent_sampleData.getGraph().getCellGeometry(cell);
                    insertedVertex = graphComponent_sampleData.getGraph().insertVertex(graphComponent_sampleData.getGraph().getDefaultParent(), null, treeNode.getData().toString(),geo.getX(), geo.getY()+geo.getHeight(), 200, 200);
                    graphComponent_sampleData.getGraph().setCellStyles(mxConstants.STYLE_ALIGN, "left", new Object[]{insertedVertex});
                    graphComponent_sampleData.getGraph().setCellStyles(mxConstants.STYLE_AUTOSIZE, "true", new Object[]{insertedVertex});
                    graphComponent_sampleData.getGraph().setCellStyles(mxConstants.STYLE_OPACITY, "1", new Object[]{insertedVertex});
                    graphComponent_sampleData.getGraph().setCellStyles(mxConstants.STYLE_OPACITY, "1", new Object[]{insertedVertex});
                    graphComponent_sampleData.getGraph().updateCellSize(insertedVertex);
                    graphComponent_sampleData.refresh();
                    graphComponent_sampleData.getGraph().getModel().endUpdate();
                   
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

                    String[] row = new String[table_sampleData.getColumnCount()];
                    for (int i = 0; i < table_sampleData.getColumnCount(); i++)
                        row[i] = table_sampleData.getModel().getValueAt(table_sampleData.getSelectedRow(), i).toString();           
                    
                    tree_trackTuple = connector.updateTreeWhyIsHere(tree_sampleData, 
                                                                    treeObjects_sampleData.get(graph_sampleData.getSelectionCell()), 
                                                                    row);
                    
                    drawPlanTree(graph_trackTuple, tree_trackTuple, treeObjects_trackTuple);
                    
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
                        samplePair = connector.getAllSampleData(treeObjects_sampleData.get(graph_sampleData.getSelectionCell()).getData().getNewTableName());
                    
                    btnExpandAll_sampleData.setText("Collapse sample");
                } else {
                    //TODO samplePair = connector.executeTestQuery(subQueryPane.getText());  
                    if (queryFrom_sampleData.getText().equals("Subquery"))
                        samplePair = connector.executeTestQuery("select * from tmp0 order by h_user_id");  
                    else if (queryFrom_sampleData.getText().equals("Plan Tree Node"))
                        samplePair = connector.getSampleData(treeObjects_sampleData.get(graph_sampleData.getSelectionCell()).getData().getNewTableName());               
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
        
        // Search Missing 
        model_searchMissing = new DefaultTableModel();
        
        renderer = new DefaultTableCellRenderer();
        renderer.setBorder(BorderFactory.createLineBorder(Color.black));
            
        table_searchMissing = new JTable(model_searchMissing);
        table_searchMissing.setRowSelectionAllowed(false);

        JScrollPane pane_searchTuple = new JScrollPane((Component) table_searchMissing, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        JLabel lblSearchForMissing = new JLabel("Search for missing tuple in the node selected:");
        
        JButton btnCancelSearchForMissing = new JButton("Cancel");
        
        JButton btnSearchForMissing = new JButton("Search");
        btnSearchForMissing.addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {               
                String[] row = new String[table_searchMissing.getColumnCount()];
                for (int i = 0; i < table_searchMissing.getColumnCount(); i++){
                    table_searchMissing.getCellEditor(0, i).stopCellEditing();
                    row[i] = "";
                    Object temp = table_searchMissing.getModel().getValueAt(0, i);
                    if (temp != null)
                        row[i] = temp.toString(); 
                    
                }
                
                try
                {
                    tree_findMissing = connector.updateTreeWhyNotHere(tree_sampleData, 
                                                                    treeObjects_sampleData.get(graph_sampleData.getSelectionCell()), 
                                                                    row);
                    drawPlanTree(graph_findMissing, tree_findMissing, treeObjects_findMissing);
                    
                    tabbedPane.setSelectedIndex(2);
                } catch (SQLException e1)
                {
                    JOptionPane.showMessageDialog(getParent(),                                                
                                                  "Searching input is not valid. \nPlease check input data and format\n(e.g. whether you have single quotes surrounding strings).",
                                                  "Input error",
                                                  JOptionPane.ERROR_MESSAGE);

                    e1.printStackTrace();
                    
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
        
        
        GroupLayout gl_panel_sampleDataTable = new GroupLayout(panel_sampleDataTable);
        gl_panel_sampleDataTable.setHorizontalGroup(
            gl_panel_sampleDataTable.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_panel_sampleDataTable.createSequentialGroup()
                    .addGroup(gl_panel_sampleDataTable.createParallelGroup(Alignment.TRAILING)
                        .addGroup(Alignment.LEADING, gl_panel_sampleDataTable.createSequentialGroup()
                            .addGap(12)
                            .addComponent(pane_sampleData, GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE))
                        .addGroup(gl_panel_sampleDataTable.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(btnSearchForMissing)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnCancelSearchForMissing))
                        .addGroup(gl_panel_sampleDataTable.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(lblQueryFor_sampleData)
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addComponent(queryFrom_sampleData, GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE))
                        .addGroup(gl_panel_sampleDataTable.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(gl_panel_sampleDataTable.createParallelGroup(Alignment.LEADING)
                                .addComponent(lblSearchForMissing, GroupLayout.PREFERRED_SIZE, 308, GroupLayout.PREFERRED_SIZE)
                                .addGroup(gl_panel_sampleDataTable.createParallelGroup(Alignment.TRAILING)
                                    .addComponent(btnExpandAll_sampleData, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(pane_searchTuple, GroupLayout.PREFERRED_SIZE, 374, GroupLayout.PREFERRED_SIZE)))))
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
                    .addComponent(pane_sampleData, GroupLayout.PREFERRED_SIZE, 384, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addComponent(btnExpandAll_sampleData)
                    .addGap(18)
                    .addComponent(lblSearchForMissing)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addComponent(pane_searchTuple, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addGroup(gl_panel_sampleDataTable.createParallelGroup(Alignment.BASELINE)
                        .addComponent(btnCancelSearchForMissing)
                        .addComponent(btnSearchForMissing))
                    .addContainerGap())
        );
        panel_sampleDataTable.setLayout(gl_panel_sampleDataTable);
        GroupLayout gl_tabSampleData = new GroupLayout(tabSampleData);
        gl_tabSampleData.setHorizontalGroup(
            gl_tabSampleData.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_tabSampleData.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panel_sampleDataPlanTree, GroupLayout.PREFERRED_SIZE, 480, GroupLayout.PREFERRED_SIZE)
                    .addGap(15)
                    .addComponent(panel_sampleDataTable, GroupLayout.PREFERRED_SIZE, 413, GroupLayout.PREFERRED_SIZE)
                    .addGap(10))
        );
        gl_tabSampleData.setVerticalGroup(
            gl_tabSampleData.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_tabSampleData.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_tabSampleData.createParallelGroup(Alignment.TRAILING, false)
                        .addComponent(panel_sampleDataTable, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(panel_sampleDataPlanTree, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE))
                    .addGap(23))
        );
        panel_sampleDataPlanTree.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        tabSampleData.setLayout(gl_tabSampleData);
        
        
        // Track Tuple Tab
        JPanel tabTrackTuple = new JPanel();
        tabbedPane.addTab("Track Existing Tuple", null, tabTrackTuple, null);
        
        JPanel panel_trackTuplePlanTree = new JPanel();
        panel_trackTuplePlanTree.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Plan Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        treeObjects_trackTuple = new HashMap<Object, LinkedBinaryTreeNode<QueryPlanTreeNode>>();
        
        graph_trackTuple = new mxGraph();
        graph_trackTuple.setCellsEditable(false);
        graph_trackTuple.setAllowDanglingEdges(false);
        
        final mxGraphComponent graphComponent_trackTuple = new mxGraphComponent(graph_trackTuple);
        graphComponent_trackTuple.setPreferredSize(new Dimension(450, 600));
        graphComponent_trackTuple.setAutoExtend(true);
        graphComponent_trackTuple.getViewport().setOpaque(true);
        graphComponent_trackTuple.setBorder(null);
        graphComponent_trackTuple.setConnectable(false);
        graphComponent_trackTuple.getViewport().setBackground(panel_trackTuplePlanTree.getBackground());
        graphComponent_trackTuple.getGraphControl().addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                Object cell = graphComponent_trackTuple.getCellAt(e.getX(), e.getY());
                if (treeObjects_trackTuple.containsKey(cell)){
                    btnExpandAll_trackTuple.setText("Expand All");
                    queryFrom_trackTuple.setText("Plan Tree Node");
                    
                    QueryPlanTreeNode node = treeObjects_trackTuple.get(cell).getData();
                    
                    model_trackTuple.setRowCount(0);
                    model_trackTuple.setColumnIdentifiers(new Vector());

                    if (node.getDataNode() != null) {
                        model_trackTuple.setColumnIdentifiers(node.getDataNode().getAttributes());
                        for (String[] row: node.getDataNode().getValues()){
                            model_trackTuple.addRow(row);
                        }                                     
                    } 
                    
                    model_trackTuple.fireTableDataChanged();
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
        panel_trackTuplePlanTree.add(graphComponent_trackTuple, BorderLayout.CENTER);
        
        
        JPanel panel_trackTupleTable = new JPanel();
        panel_trackTupleTable.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Record Table", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        JLabel lblQueryFor_trackTuple = new JLabel("Tracking Down:");
        
        queryFrom_trackTuple = new JTextField();
        queryFrom_trackTuple.setEditable(false);
        
        model_trackTuple = new DefaultTableModel() {
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
              }
            };
        final JTable table_trackTuple = new JTable(model_trackTuple);
        table_trackTuple.setFocusable(false);
        JScrollPane pane_trackTuple = new JScrollPane((Component) table_trackTuple, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);      
       
        btnExpandAll_trackTuple = new JButton("Expand All");
        GroupLayout gl_panel_trackTupleTable = new GroupLayout(panel_trackTupleTable);
        gl_panel_trackTupleTable.setHorizontalGroup(
            gl_panel_trackTupleTable.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_panel_trackTupleTable.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_panel_trackTupleTable.createParallelGroup(Alignment.LEADING)
                        .addComponent(btnExpandAll_trackTuple, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
                        .addGroup(gl_panel_trackTupleTable.createParallelGroup(Alignment.TRAILING, false)
                            .addGroup(gl_panel_trackTupleTable.createSequentialGroup()
                                .addGap(6)
                                .addComponent(pane_trackTuple, 0, 0, Short.MAX_VALUE))
                            .addGroup(Alignment.LEADING, gl_panel_trackTupleTable.createSequentialGroup()
                                .addComponent(lblQueryFor_trackTuple)
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addComponent(queryFrom_trackTuple, GroupLayout.PREFERRED_SIZE, 281, GroupLayout.PREFERRED_SIZE))))
                    .addContainerGap())
        );
        gl_panel_trackTupleTable.setVerticalGroup(
            gl_panel_trackTupleTable.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_trackTupleTable.createSequentialGroup()
                    .addGap(5)
                    .addGroup(gl_panel_trackTupleTable.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblQueryFor_trackTuple)
                        .addComponent(queryFrom_trackTuple, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
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
        panel_trackTuplePlanTree.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        tabTrackTuple.setLayout(gl_tabTrackTuple);
      
        
        //Find missing tab
        JPanel tabFindMissing = new JPanel();
        tabbedPane.addTab("Find Missing Tuple", null, tabFindMissing, null);
        
        JPanel panel_findMissingPlanTree = new JPanel();
        panel_findMissingPlanTree.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Plan Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_findMissingPlanTree.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        treeObjects_findMissing = new HashMap<Object, LinkedBinaryTreeNode<QueryPlanTreeNode>>();
        
        graph_findMissing = new mxGraph();
        graph_findMissing.setCellsEditable(false);
        graph_findMissing.setAllowDanglingEdges(false);
        
        final mxGraphComponent graphComponent_findMissing = new mxGraphComponent(graph_findMissing);
        graphComponent_findMissing.setPreferredSize(new Dimension(450, 600));
        graphComponent_findMissing.setAutoExtend(true);
        graphComponent_findMissing.getViewport().setOpaque(true);
        graphComponent_findMissing.setBorder(null);
        graphComponent_findMissing.setConnectable(false);
        graphComponent_findMissing.getViewport().setBackground(panel_findMissingPlanTree.getBackground());
        graphComponent_findMissing.getGraphControl().addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                Object cell = graphComponent_findMissing.getCellAt(e.getX(), e.getY());
                if (treeObjects_findMissing.containsKey(cell)){
                    btnExpandAll_findMissing.setText("Expand All");
                    queryFrom_findMissing.setText("Plan Tree Node");
                    
                    QueryPlanTreeNode node = treeObjects_findMissing.get(cell).getData();
                    
                    model_findMissing.setRowCount(0);
                    model_findMissing.setColumnIdentifiers(new Vector());

                    if (node.getDataNode() != null) {
                        model_findMissing.setColumnIdentifiers(node.getDataNode().getAttributes());
                        for (String[] row: node.getDataNode().getValues()){
                            model_findMissing.addRow(row);
                        }                                     
                    } 
                    
                    model_findMissing.fireTableDataChanged();
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
        panel_findMissingPlanTree.add(graphComponent_findMissing, BorderLayout.CENTER);
  
        
        JPanel panel_findMissingTable = new JPanel();
        panel_findMissingTable.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Record Table", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        
        JButton btnExpandAll_findMissing = new JButton("Expand All");
        
        JLabel lblQueryFor_findMissing = new JLabel("Tracking Down:");
        
        queryFrom_findMissing = new JTextField();
        queryFrom_findMissing.setEditable(false);
        
        model_trackTuple = new DefaultTableModel() {
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
              }
            };
        final JTable table_findMissing = new JTable(model_trackTuple);
        table_findMissing.setFocusable(false);
        JScrollPane pane_findMissing = new JScrollPane((Component) table_findMissing, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        GroupLayout gl_panel_findMissingTable = new GroupLayout(panel_findMissingTable);
        gl_panel_findMissingTable.setHorizontalGroup(
            gl_panel_findMissingTable.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_panel_findMissingTable.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_panel_findMissingTable.createParallelGroup(Alignment.TRAILING, false)
                        .addComponent(btnExpandAll_findMissing, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
                        .addGroup(Alignment.LEADING, gl_panel_findMissingTable.createSequentialGroup()
                            .addComponent(lblQueryFor_findMissing)
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addComponent(queryFrom_findMissing)
                            .addPreferredGap(ComponentPlacement.RELATED))
                        .addGroup(Alignment.LEADING, gl_panel_findMissingTable.createSequentialGroup()
                            .addGap(6)
                            .addComponent(pane_findMissing, GroupLayout.PREFERRED_SIZE, 383, GroupLayout.PREFERRED_SIZE)))
                    .addGap(17))
        );
        gl_panel_findMissingTable.setVerticalGroup(
            gl_panel_findMissingTable.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_findMissingTable.createSequentialGroup()
                    .addGap(5)
                    .addGroup(gl_panel_findMissingTable.createParallelGroup(Alignment.BASELINE)
                        .addComponent(queryFrom_findMissing, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblQueryFor_findMissing))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(pane_findMissing, GroupLayout.PREFERRED_SIZE, 543, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addComponent(btnExpandAll_findMissing)
                    .addGap(12))
        );
        panel_findMissingTable.setLayout(gl_panel_findMissingTable);
        GroupLayout gl_tabFindMissing = new GroupLayout(tabFindMissing);
        gl_tabFindMissing.setHorizontalGroup(
            gl_tabFindMissing.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_tabFindMissing.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panel_findMissingPlanTree, GroupLayout.PREFERRED_SIZE, 480, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(panel_findMissingTable, GroupLayout.PREFERRED_SIZE, 415, GroupLayout.PREFERRED_SIZE)
                    .addGap(33))
        );
        gl_tabFindMissing.setVerticalGroup(
            gl_tabFindMissing.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_tabFindMissing.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_tabFindMissing.createParallelGroup(Alignment.BASELINE)
                        .addComponent(panel_findMissingPlanTree, GroupLayout.PREFERRED_SIZE, 640, GroupLayout.PREFERRED_SIZE)
                        .addComponent(panel_findMissingTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGap(23))
        );
        tabFindMissing.setLayout(gl_tabFindMissing);
        
        //Window Layout
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
     
        addWindowListener(new WindowListener(){

            @Override
            public void windowOpened(WindowEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void windowClosing(WindowEvent e) {
                // TODO Auto-generated method stub
                connector.closeDBConnection();
                System.out.println("close on exit");
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
    
    private void drawPlanTree(final mxGraph graph, LinkedBinaryTreeNode<QueryPlanTreeNode> tree, final Map<Object, LinkedBinaryTreeNode<QueryPlanTreeNode>> treeObjects){
        final Object parent = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        
        final Map<BinaryTreeNode<?>, PlanTreeNode> coordinates = new HashMap<BinaryTreeNode<?>, PlanTreeNode>();
        final int maxshift = traverse(tree, 0, coordinates);

        tree.traversePreorder(new BinaryTreeNode.Visitor() {
            public void visit(BinaryTreeNode node) {
                QueryPlanTreeNode treeNode = (QueryPlanTreeNode) node.getData();
                PlanTreeNode planTreeNode = coordinates.get(node);
                String label = treeNode.getAbbreviatedTreeNode().getLargeFontStr()+"\n"+treeNode.getAbbreviatedTreeNode().getSmallFontStr()+"\n Node table name: "+treeNode.getAbbreviatedTreeNode().getTmpTableStr();               
                planTreeNode.obj = graph.insertVertex(parent, null, label, planTreeNode.point.x-maxshift, planTreeNode.point.y, 200, label.split("\n").length*15);

                if (treeNode.getDataNode() != null) {
                    graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, "yellow", new Object[]{planTreeNode.obj});
                }
                treeObjects.put(planTreeNode.obj,  (LinkedBinaryTreeNode<QueryPlanTreeNode>) node);
                if (node.getParent() != null) {
                    PlanTreeNode parentPlanTreeNode = coordinates.get(node.getParent());
                    graph.insertEdge(parent, null, "", planTreeNode.obj, parentPlanTreeNode.obj);
                }
            }
        });      
        
        graph.getModel().endUpdate();
    }
    

}

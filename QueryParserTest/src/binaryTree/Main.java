package binaryTree;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;


public class Main {

    /**
     * @param args
     */
    protected Shell shlDatabaseLogIn;
    private BinaryTreePanel panel;

    public static void main(String[] args) {



Main window = new Main();
window.open();
    }
    
    public void open() {
        Display display = Display.getDefault();
        
        LinkedBinaryTreeNode<Integer> root = new LinkedBinaryTreeNode<Integer>(5);
        root.setLeft(new LinkedBinaryTreeNode<Integer>(10));
        root.setRight(new LinkedBinaryTreeNode<Integer>(20));
        root.getLeft().setLeft(new LinkedBinaryTreeNode<Integer>(30));
        root.getRight().setLeft(new LinkedBinaryTreeNode<Integer>(40));
        
        
        shlDatabaseLogIn = new Shell();
        shlDatabaseLogIn.setToolTipText("");
        shlDatabaseLogIn.setSize(1000, 1000);
        shlDatabaseLogIn.setLayout(null);
        
         panel = new BinaryTreePanel(root, 30, 30);
        panel.setLocation(10, 10);
        panel.setSize(100, 100);
        panel.setOpaque(true);
        System.out.println(root.getRight().getData());
        
        shlDatabaseLogIn.open();
        shlDatabaseLogIn.layout();
        while (!shlDatabaseLogIn.isDisposed())
        {
            if (!display.readAndDispatch())
            {
                display.sleep();
            }
        }

    }

}

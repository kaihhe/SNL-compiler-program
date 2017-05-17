package cn.hekind.client;

import java.awt.EventQueue;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import cn.hekind.analyzer.SNLAnalyzer;
import cn.hekind.analyzer.TreeNode;
import cn.heknd.scanner.SNLScanner;
import cn.heknd.scanner.Token;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import java.awt.Dimension;

public class Client {
	//需要操作的成员变量
	JFileChooser fc = new JFileChooser();
	File file = null;
	JTextArea messageTextArea;
	JTextArea codeTextArea;
	String fileContext = null;
	SNLScanner scanner = new SNLScanner();
	SNLAnalyzer analyzer = null;
	TreeNode treeRoot;
	JTextArea treeTextArea;
	
	String helpMessage ="1.打开文件：文件->打开\n"
			+ "2.词法分析：编译->词法分析\n"
			+ "3.语法分析：编译->语法分析\n";
	String aboutMessage ="编译原理课程设计\n"
			+ "SNLCompiler\n"
			+ "SNL语言的词法分析程序+词法分析程序";
	
	private JFrame frmSnlcompiler;
	private JTable tokenTable;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client window = new Client();
					window.frmSnlcompiler.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Client() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmSnlcompiler = new JFrame();
		frmSnlcompiler.setTitle("SNLCompiler");
		frmSnlcompiler.setBounds(100, 100, 491, 337);
		frmSnlcompiler.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSnlcompiler.getContentPane().setLayout(new BorderLayout(0, 0));
		frmSnlcompiler.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		JMenuBar menuBar = new JMenuBar();
		frmSnlcompiler.getContentPane().add(menuBar, BorderLayout.NORTH);
		
		JMenu fileMenu = new JMenu("\u6587\u4EF6");
		menuBar.add(fileMenu);
		
		JMenuItem fileOpen = new JMenuItem("\u6253\u5F00");
		fileOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.setDialogTitle("打开文件");
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.showOpenDialog(fileOpen);
				file = fc.getSelectedFile();
				FileReader fr;
				StringBuilder context = new StringBuilder();
				try {
					fr = new FileReader(file);
					int c = 0;
					while((c = fr.read()) != -1)
					{
						context.append((char)c);
					}
				}catch (IOException ex) {
					System.out.println("文件读取错误");
				}
				fileContext = new String(context);
				codeTextArea.setText(fileContext);
			}
		});
		fileMenu.add(fileOpen);
		
		JMenuItem fileSave = new JMenuItem("\u4FDD\u5B58");
		fileSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(file == null)
					return;
				FileWriter fw;
				fileContext = codeTextArea.getText();
				try {
				fw = new FileWriter(file);
				fw.write(fileContext);
				fw.flush();
				fw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		fileMenu.add(fileSave);
		
		JMenu compiler = new JMenu("\u7F16\u8BD1");
		menuBar.add(compiler);
		
		JMenuItem scannerBtn = new JMenuItem("\u8BCD\u6CD5\u5206\u6790");
		scannerBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//调用词法分析程序
				fileContext = codeTextArea.getText();
				Vector<Token> token = (Vector<Token>) scanner.getToken(fileContext);
			    AbstractTableModel dataModel = new AbstractTableModel() {
			        public int getColumnCount() { return 3; }
			        public int getRowCount() { return token.size();}
			        public Object getValueAt(int row, int col) 
			        { 
			        	if(col==0)
			        		return token.get(row).type.toString()+":"+token.get(row).type.ordinal();
			        	else if(col==1)
			        		return  token.get(row).context;
			        	else
			        		return Integer.toString(token.get(row).line);
			        }
			        public String getColumnName(int col)
			        {
			        	if(col==0)
			        		return "type";
			        	else if(col==1)
			        		return "context";
			        	return "line";
			        }
			    };
			    tokenTable.setModel(dataModel);
			    tokenTable.updateUI();
			}
		});
		compiler.add(scannerBtn);
		
		JMenuItem analyzerBtn = new JMenuItem("\u8BED\u6CD5\u5206\u6790");
		analyzerBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileContext = codeTextArea.getText();
				//调用语法分析程序
				analyzer = new SNLAnalyzer((Vector<Token>) scanner.getToken(fileContext));
				treeRoot = analyzer.parse();
				String result =	analyzer.getMessage();
				if(result.equals(""))
					messageTextArea.setText("语法分析成功，没有错误");
				else
					messageTextArea.setText(analyzer.getMessage());
				treeTextArea.setText(treeRoot.getTree(treeRoot, 0));
				messageTextArea.updateUI();
			}
		});
		compiler.add(analyzerBtn);
		
		JMenu help = new JMenu("\u5E2E\u52A9");
		menuBar.add(help);
		
		JMenuItem helpHelp = new JMenuItem("\u5E2E\u52A9");
		helpHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JOptionPane.showMessageDialog(helpHelp,helpMessage ,"帮助",JOptionPane.INFORMATION_MESSAGE);
			}
		});
		help.add(helpHelp);
		
		JMenuItem helpAbout = new JMenuItem("\u5173\u4E8E");
		helpAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(helpHelp, aboutMessage,"关于",JOptionPane.INFORMATION_MESSAGE);
			}
		});
		help.add(helpAbout);
		
		JInternalFrame codeFrame = new JInternalFrame("code");
		codeFrame.setMaximizable(true);
		frmSnlcompiler.getContentPane().add(codeFrame, BorderLayout.CENTER);
		
		JScrollPane codeScrollPane = new JScrollPane();
		codeFrame.getContentPane().add(codeScrollPane, BorderLayout.CENTER);
		
		codeTextArea = new JTextArea();
		codeScrollPane.setViewportView(codeTextArea);
		
		JInternalFrame tokenFrame = new JInternalFrame("tokenList");
		tokenFrame.setPreferredSize(new Dimension(300, 34));
		frmSnlcompiler.getContentPane().add(tokenFrame, BorderLayout.WEST);
		
		JScrollPane tokenScrollPane = new JScrollPane();
		tokenFrame.getContentPane().add(tokenScrollPane, BorderLayout.CENTER);
		
		tokenTable = new JTable();
		tokenTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		tokenTable.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"type", "context","line"
			}
		));
		tokenScrollPane.setViewportView(tokenTable);
		
		JInternalFrame messageFrame = new JInternalFrame("message");
		messageFrame.setPreferredSize(new Dimension(55, 150));
		frmSnlcompiler.getContentPane().add(messageFrame, BorderLayout.SOUTH);
		
		JScrollPane messageScrollPane = new JScrollPane();
		messageFrame.getContentPane().add(messageScrollPane, BorderLayout.CENTER);
		
		messageTextArea = new JTextArea("\n\n\n\n");
		messageTextArea.setColumns(10);
		messageTextArea.setEditable(false);
		messageScrollPane.setViewportView(messageTextArea);
		
		JInternalFrame treeFrame = new JInternalFrame("tree");
		treeFrame.setPreferredSize(new Dimension(400, 34));
		treeFrame.setMinimumSize(new Dimension(300, 34));

		frmSnlcompiler.getContentPane().add(treeFrame, BorderLayout.EAST);
		treeFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JScrollPane treeScrollPane = new JScrollPane();
		treeFrame.getContentPane().add(treeScrollPane, BorderLayout.CENTER);
		
		treeTextArea = new JTextArea();
		treeTextArea.setEditable(false);
		treeScrollPane.setViewportView(treeTextArea);

		
		
		treeFrame.setVisible(true);
		messageFrame.setVisible(true);
		tokenFrame.setVisible(true);
		codeFrame.setVisible(true);
	}

}

package com.filemanage;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;

public class FileFrame extends JFrame implements TreeSelectionListener{
    //右邊準備顯示metadata的畫面
    private JDesktopPane mdiPane;
    //在下方顯示目前選取哪個檔案
    private JLabel statusLabel;
    //
    File fileRoot;
    //目前tree的節點
    private DefaultMutableTreeNode rootNode;
    //current被點在哪裡
    private DefaultMutableTreeNode currentNode;

    JMenuItem uploadItem;
    JMenuItem downloadItem;
    JMenuItem exitItem;
    JMenuItem newItem;
    JMenuItem saveItem;

    String filePath="./";


    //目前使用者開了幾個新檔案
    int	newFileIndex = 0;

    //表示目前
    private DefaultTreeModel treeModel;
    private JTree tree;
    private JScrollPane treePane;
    private JSplitPane splitPane;


    public static void main(String[] args) {
        new FileFrame();
    }
    FileFrame(){
        super("File Management");
        createMenu();
        createMDI();

        setLocation(100, 100);
        setSize(800, 400);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    //繪製樹狀圖架構
    private void createMDI() {
        this.mdiPane = new JDesktopPane();
        this.mdiPane.setBackground(Color.LIGHT_GRAY);

        //Frame底下要顯示的文字:
        this.statusLabel = new JLabel();

        this.renderTree();



        getContentPane().setLayout(new BorderLayout(5, 5));
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setRightComponent(mdiPane);
        splitPane.setOneTouchExpandable(true);
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(this.statusLabel, BorderLayout.SOUTH);

        getContentPane().add(this.mdiPane, BorderLayout.CENTER);
        getContentPane().add(this.treePane, BorderLayout.WEST);
    }

    private void renderTree(){
        //
        fileRoot= new File(filePath);
        this.rootNode = new DefaultMutableTreeNode(new FileNode("專題檔案", fileRoot));
        currentNode = rootNode; //先把目前指標指向root 如果要新增檔案就在root新增 因為user還沒有點任何檔案

        //建立其他子節點
        CreateChildNode ccn = new CreateChildNode(fileRoot, this.rootNode);
        new Thread(ccn).start();

        this.treeModel = new DefaultTreeModel(rootNode);
        this.tree = new JTree(this.treeModel);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        //會去呼叫valueChanged方法表示有Tree的節點被觸發了
        this.tree.addTreeSelectionListener(this);

        //顯示把柄提示還有檔案可以展開
        this.tree.setShowsRootHandles(true);

        //左邊: 樹狀圖式
        this.treePane = new JScrollPane(this.tree);
        getContentPane().add(this.treePane, BorderLayout.WEST);
    }

    private void createMenu() {
        //create Menu Bar
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("檔案");
        fileMenu.setMnemonic('F');
        setJMenuBar(bar);
        bar.add(fileMenu);

        uploadItem = new JMenuItem("上傳檔案");
        downloadItem = new JMenuItem("下載檔案");
        exitItem = new JMenuItem("離開系統...");
        newItem = new JMenuItem("建立新檔...");
        saveItem = new JMenuItem("儲存新檔...");

        uploadItem.setMnemonic('U');
        downloadItem.setMnemonic('D');
        exitItem.setMnemonic('E');
        newItem.setMnemonic('N');
        saveItem.setMnemonic('S');

        //function implementation
        uploadItem.addActionListener(new uploadAction());
        downloadItem.addActionListener(new downloadAction());
        newItem.addActionListener(new newItemAction());
        saveItem.addActionListener(new newItemAction());
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        fileMenu.add(uploadItem);
        fileMenu.add(downloadItem);
        fileMenu.add(newItem);
        fileMenu.add(saveItem);
        fileMenu.add(exitItem);


    }


    class uploadAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(filePath);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if(fileChooser.showSaveDialog(FileFrame.this) == JFileChooser.CANCEL_OPTION) return;
            File selectedFile = fileChooser.getSelectedFile();
            filePath = selectedFile.getPath();
            System.out.println("test=>"+filePath);
            Path source = Paths.get(filePath);

            FileNode targetFile = (FileNode)currentNode.getUserObject();
            Path target = Paths.get(targetFile.getFile().getAbsolutePath()+"\\"+source.getFileName());
            System.out.println("this is source =>" + source.toString() + "\n This is target =>" + target.toString());
            try {
                Files.copy( source, target,StandardCopyOption.REPLACE_EXISTING);
                SwingUtilities.invokeLater(new RenderTree());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
    class RenderTree implements Runnable{
        @Override
        public void run() {
            renderTree();
        }
    }

    class downloadAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            renderTree();
        }
    }

    //建立新檔案所需要執行的動作:
    class newItemAction implements  ActionListener{
        public void actionPerformed(ActionEvent e) {
            String fileName = "開新檔案-" + (++newFileIndex);
            //(1)產生編輯內容視窗
            createDocFrame(fileName, null);
            //(2)改變底下狀態欄位
            statusLabel.setText("建立新檔案");
            //(3)在tree新增節點,需要parent node, filename for new node, visible or not
            addObject(checkDocOrFile(currentNode), fileName, true);
//            splitPane.setLeftComponent(treePane);
//            splitPane.setOneTouchExpandable(true);
        }
    }

    class saveItemAction implements ActionListener{
        public void actionPerformed(ActionEvent e) {

        }
    }

    public DefaultMutableTreeNode checkDocOrFile(DefaultMutableTreeNode node){
        if(node.getChildCount()>0){
            System.out.println("This is Directory!!" + node.toString());
            return node;
        }else{
            System.out.println("This is File, and the parent of file is =>"+node.getParent().toString());
            return (DefaultMutableTreeNode) node.getParent();
        }

    }

    //TODO 在原本的Tree在建立新節點
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean visible){
        //先將檔案寫入系統中
        File newfile = new File(child.toString());


        //(1) 先建立出一個new file node
        DefaultMutableTreeNode childeNode = new DefaultMutableTreeNode(child);
        if(parent == null) parent = rootNode;
        treeModel.insertNodeInto(childeNode, parent, parent.getChildCount());


        //make sure user can see the node we created
        if(visible){
            TreePath path = new TreePath(childeNode.getPath());
            //將tree展開到新建立的點
            tree.scrollPathToVisible(path);
            //將游標跳到最新的點
            tree.setSelectionPath(path);
        }
        return childeNode;
    }

    //TODO 可以將檔案真正儲存至電腦中
    public void saveDocFile(JInternalFrame fr, File docFile){
        Component components[] = fr.getContentPane().getComponents();
        JTextArea txtArea = (JTextArea) ((JScrollPane) components[0]).getViewport().getView();
        FileWriter output;
        try {
            output = new FileWriter(docFile);
            output.write(txtArea.getText());
            output.close();
        }
        catch(IOException e) {}
    }


    //TODO　建立右邊可以編輯內容的視窗
    public JInternalFrame createDocFrame(String title, File docFile){
        JTextArea textArea = new JTextArea(10, 30);
        textArea.setFont(new Font("Monospace", Font.PLAIN, 12));
        textArea.setMargin(new Insets(5, 5, 5, 5));
        textArea.setTabSize(3);
        textArea.setCaretPosition(textArea.getDocument().getLength());

        //裡面可以關閉的視窗
        JInternalFrame frame = new JInternalFrame(title, true, true, true, false);
        BasicInternalFrameUI ui = (BasicInternalFrameUI) frame.getUI();
        System.out.println("east " + ui.getEastPane() + " north " + ui.getNorthPane());
        System.out.println("west " + ui.getWestPane() + " south " + ui.getSouthPane());

        //在視窗塞可以編輯內容的視窗
        frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        return null;


    }

    //TODO 當Tree目前被觸發在哪裡
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        showStatusLabel(node);
        showDocFrame(node.toString(),(FileNode) node.getUserObject());
        currentNode = node;
        System.out.println(checkDocOrFile(currentNode));
    }

    //TODO 在右邊視窗展示檔案內容
    public JInternalFrame showDocFrame(String title,
                                       FileNode docFile)
    {
        //為了不要讓Frame多個塞在mdiPane裡面
        JInternalFrame[] selectedFrame = mdiPane.getAllFrames();
        if(selectedFrame.length > 0){
            for(JInternalFrame frame : selectedFrame){
                frame.dispose();
            }
        }

        JTextArea txtArea =  new JTextArea(10, 30);
        txtArea.setFont(new Font("Monospace", Font.PLAIN, 12));
        txtArea.setMargin(new Insets(5, 5, 5, 5));
        txtArea.setTabSize(3);
        txtArea.setCaretPosition(txtArea.getDocument().getLength());

        JInternalFrame frame = new JInternalFrame(title, true, true, true, false);
        BasicInternalFrameUI ui = (BasicInternalFrameUI) frame.getUI();
        System.out.println("east " + ui.getEastPane() + " north " + ui.getNorthPane());
        System.out.println("west " + ui.getWestPane() + " south " + ui.getSouthPane());
        //MetalInternalFrameUI ui = new MetalInternalFrameUI(frame);
        //frame.setUI(ui);
        frame.getContentPane().add(new JScrollPane(txtArea), BorderLayout.NORTH);

        if(docFile != null) {
            txtArea.setText(loadTextFromFile(docFile.getFile()));
        }
        frame.pack();
        this.mdiPane.add(frame);
        try { frame.setMaximum(true); }
        catch(java.beans.PropertyVetoException e){}
        frame.setVisible(true);
        frame.addInternalFrameListener(new InternalFrameAdapter(){
            public void internalFrameClosed(InternalFrameEvent e) {
                String key = e.getInternalFrame().getTitle();
                statusLabel.setText(key + "已關閉");
            }
            public void internalFrameActivated(InternalFrameEvent e) {
                String key = e.getInternalFrame().getTitle();
                statusLabel.setText("編輯" + key);
            }
        });

        //如果要關閉frame
        frame.addVetoableChangeListener(new VetoableChangeListener(){
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException
            {
                if(e.getPropertyName().equals(JInternalFrame.IS_CLOSED_PROPERTY)) {
                    boolean changed = ((Boolean)e.getNewValue()).booleanValue();
                    System.out.println("changed=" + changed);
                    if(!changed) return;
                    JInternalFrame frame = (JInternalFrame)e.getSource();
                    int confirm = JOptionPane.showOptionDialog(frame,
                            "關閉" + frame.getTitle() + "?",
                            "確認關閉",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, null, null);
                    if(confirm != 0) {
                        statusLabel.setText("取消關閉");
                        throw new java.beans.PropertyVetoException("Cancelled", null);
                    }
                }
            }
        });

        return frame;
    }
    public String loadTextFromFile(File docFile)
    {
        String str;
        StringBuffer buffer = new StringBuffer();
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(docFile));
            while((str = input.readLine()) != null) {
                buffer.append(str + "\n");
            }
            input.close();
        }
        catch(IOException e) {}
        return buffer.toString();
    }

    private void showStatusLabel(DefaultMutableTreeNode node){
        if(node==null) return;
        Object nodeInfo = node.getUserObject();
        System.out.println("showStatusLable=>"+nodeInfo.getClass());

        if(node.isLeaf()){
            FileNode fileNode =(FileNode) nodeInfo;
            displayURL(fileNode.getFile().getAbsolutePath());
        }else{
            displayURL("Folder: "+node.toString());
        }
    }


    private void displayURL(String url) {
        if (url != null) {
            statusLabel.setText(url);
        } else { //null url
            statusLabel.setText("File Not Found");
        }
    }

}

package com.filemanage;

import javax.swing.*;
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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

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
    JMenuItem deleteItem;

    String filePath="C:\\Users\\micky\\OneDrive\\桌面";


    //目前使用者開了幾個新檔案
    int	newFileIndex = 0;

    //表示目前
    private DefaultTreeModel treeModel;
    private JTree tree;
    private JScrollPane treePane;
    private JSplitPane splitPane;

    //外觀
    public static int LOOK_AND_FEEL_METAL = 0;
    public static int LOOK_AND_FEEL_MOTIF = 1;
    public static int LOOK_AND_FEEL_WINDOWS = 2;
    public static int LOOK_AND_FEEL_WINDOWS_CLASSIC = 3;


    public static void main(String[] args) {
        new FileFrame();
    }
    FileFrame(){
        super("File Management");
        createMenu();
        createMDI();

        setLocation(100, 100);
        setSize(800, 400);
        changeLookAndFeel(LOOK_AND_FEEL_WINDOWS_CLASSIC);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    //繪製樹狀圖架構
    private void createMDI() {
        this.mdiPane = new JDesktopPane();
        this.mdiPane.setBackground(Color.LIGHT_GRAY);

        //Frame底下要顯示的文字:
        this.statusLabel = new JLabel();

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

        getContentPane().setLayout(new BorderLayout(5, 5));
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setRightComponent(mdiPane); //右邊顯示資料視窗 會擴展到最大
        splitPane.setLeftComponent(treePane);
        splitPane.setOneTouchExpandable(true);

        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(this.statusLabel, BorderLayout.SOUTH);
//        getContentPane().add(this.mdiPane, BorderLayout.CENTER);
//        getContentPane().add(this.treePane, BorderLayout.WEST);
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
        deleteItem = new JMenuItem("刪除檔案");

        uploadItem.setMnemonic('U');
        downloadItem.setMnemonic('D');
        exitItem.setMnemonic('E');
        deleteItem.setMnemonic('D');

        //function implementation
        uploadItem.addActionListener(new uploadAction());
        downloadItem.addActionListener(new downloadAction());
        deleteItem.addActionListener(new deleteItemAction());
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        fileMenu.add(uploadItem);
        fileMenu.add(downloadItem);
        fileMenu.add(deleteItem);
        fileMenu.add(exitItem);
    }

    //TODO 上傳檔案:
    class uploadAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(filePath);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if(fileChooser.showSaveDialog(FileFrame.this) == JFileChooser.CANCEL_OPTION) return;
            File selectedFile = fileChooser.getSelectedFile();

            //目前選取準備上傳的檔案
            filePath = selectedFile.getPath();
            System.out.println("File which is going to upload =>"+filePath);
            Path source = Paths.get(filePath);

            //將選取的檔案上傳的位置
            FileNode targetFile = (FileNode)checkDocOrFile(currentNode).getUserObject();

            //存放目標的位置: folder location\filename.png
            Path target = Paths.get(targetFile.getFile().getAbsolutePath()+"\\"+source.getFileName());
            System.out.println("this is source =>" + source.toString() + "\n This is target =>" + target.toString());

            try {
                Files.copy( source, target,StandardCopyOption.REPLACE_EXISTING);
                addNode(checkDocOrFile(currentNode), selectedFile);
//                addObject(checkDocOrFile(currentNode), source.getFileName(), true);
                statusLabel.setText("成功上傳" + source.getFileName() +"至" +target.toString());
                currentNode = rootNode; //因為上傳完成後 點選的東西會不見 所以要重新回歸
                System.out.println("上船完成目前的currentnode是"+currentNode.toString());
            } catch (IOException exception) {
                statusLabel.setText("上傳" + source.getFileName() + "失敗");
                exception.printStackTrace();
            }
        }
    }

    //TODO 下載檔案: 知道使用者要儲存的路徑 > 抓取目前點選的檔案 > 複製到路徑中
    class downloadAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(filePath);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // FILES_ONLY, DIRECTORIES_ONLY, FILES_AND_DIRECTORIES
            //若選擇取消要跳出提醒視窗
            if(fileChooser.showSaveDialog(FileFrame.this) == JFileChooser.CANCEL_OPTION) return;

            //取得使用者選取的路徑
            File selectedFile = fileChooser.getSelectedFile();
            String destination = selectedFile.getPath();

            FileNode fileNode = (FileNode) currentNode.getUserObject();
            //存放目標的位置: folder location\filename.png
            Path target = Paths.get(destination+"\\"+fileNode.toString());


            //取得使用者欲下載的檔案
            File sourcefile = fileNode.getFile();
            Path source = Paths.get(sourcefile.getAbsolutePath());

            try {
                Files.copy( source, target,StandardCopyOption.REPLACE_EXISTING);
//                addObject(checkDocOrFile(currentNode), source.getFileName(), true);
                statusLabel.setText("成功下載" + source.getFileName() +"至" +target.toString());
            } catch (IOException exception) {
                statusLabel.setText("下載" + source.getFileName() + "失敗");
                exception.printStackTrace();
            }
        }
    }

    //TODO 改變視窗畫面
    public void changeLookAndFeel(int myLookAndFeel){
        //裡面是所有的style
        UIManager.LookAndFeelInfo looks[] = UIManager.getInstalledLookAndFeels();
        try {
            //選取第三個style
            UIManager.setLookAndFeel(looks[myLookAndFeel].getClassName());
        }
        catch(Exception x) {}
        //要加上否則不會套用
        SwingUtilities.updateComponentTreeUI(this);
    }

    //TODO 刪除檔案: 取得目前node > 得知該檔案的路徑 > 刪除該檔案
    class deleteItemAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            FileNode deleteNode = (FileNode) currentNode.getUserObject();
            Path deletePath = Paths.get(deleteNode.getFile().getAbsolutePath());
            try {
                System.out.println("[Delete Path]" + deletePath);
                treeModel.removeNodeFromParent(currentNode);
                Files.delete(deletePath);
                statusLabel.setText("已成功刪除"+deleteNode.toString());
                currentNode = rootNode; //因為上傳完成後 點選的東西會不見 所以要重新回歸
            } catch (IOException exception) {
                statusLabel.setText("刪除"+deleteNode.toString()+"失敗");
                System.out.println("[Delete File Error]");
                exception.printStackTrace();
            }
        }
    }

    //TODO 檢查該node為file or directory, 會根據file or directory取得其parent資料夾 用於儲存檔案位置使用
    public DefaultMutableTreeNode checkDocOrFile(DefaultMutableTreeNode node){
        System.out.println("[debug]=>"+node);
        if(node.getChildCount()>0){
            System.out.println("This is Directory!!" + node.toString());
            return node;
        }else if (node == null){
            return rootNode;
        }
        else{
            System.out.println("This is File, and the parent of file is =>"+node.getParent().toString());
            return (DefaultMutableTreeNode) node.getParent();
        }
    }

    //TODO 負責新增節點
    public DefaultMutableTreeNode addNode(DefaultMutableTreeNode parent, File file){
        FileNode fileNode = new FileNode(file);
        DefaultMutableTreeNode childeNode = new DefaultMutableTreeNode(fileNode);
        treeModel.insertNodeInto(childeNode, parent, parent.getChildCount());
        return childeNode;
    }

    //TODO 建立Tree
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean visible){
        //先將檔案寫入系統中
        File newfile = new File(child.toString());
        FileNode fileNode = new FileNode(child.toString(), newfile);

        //(1) 先建立出一個new file node
        DefaultMutableTreeNode childeNode = new DefaultMutableTreeNode(fileNode);
        if(parent == null) parent = rootNode;
        treeModel.insertNodeInto(childeNode, parent, parent.getChildCount());

        //make sure user can see the node we created
        if(visible){
            TreePath path = new TreePath(childeNode.getPath());
            //將tree展開到新建立的點
            tree.scrollPathToVisible(path);
        }
        return childeNode;
    }

    //TODO 當Tree目前被觸發在哪裡
    public void valueChanged(TreeSelectionEvent e) {

        try {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if(node!=null){
                showStatusLabel(node);
                showDocFrame(node.toString(),(FileNode) node.getUserObject());
                currentNode = node;
                System.out.println(checkDocOrFile(currentNode));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    //TODO 在右邊視窗展示檔案內容
    public void showDocFrame(String title, FileNode docFile) throws IOException{
            System.out.println("[showDoc Called] => " + docFile.toString());

            //為了不要讓Frame多個塞在mdiPane裡面
            JInternalFrame[] selectedFrame = mdiPane.getAllFrames();
            if(selectedFrame.length > 0){
                for(JInternalFrame frame : selectedFrame){
                    frame.dispose();
                }
            }

            //可以關閉的框框
            JInternalFrame frame = new JInternalFrame(title, true, true, true, false);
            //框框裡面要塞txtArea
            JTextArea txtArea =  new JTextArea(10, 30);
            if(docFile != null) {
                System.out.println("[showDoc Called] metadata is  => "+ getMetadataByURL(docFile.getFile()));
                txtArea.setText(getMetadataByURL(docFile.getFile()));
            }

            frame.getContentPane().add(new JScrollPane(txtArea), BorderLayout.CENTER);

            frame.pack();
            frame.setSize(500,350);
            this.mdiPane.add(frame);
            frame.setVisible(true);
            //如果要關閉frame
            frame.addVetoableChangeListener(new VetoableChangeListener(){
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException
            {
                if(e.getPropertyName().equals(JInternalFrame.IS_CLOSED_PROPERTY)) {
                    boolean changed = ((Boolean)e.getNewValue()).booleanValue();
                    if(!changed) return;
                    JInternalFrame frame = (JInternalFrame)e.getSource();
                    int confirm = JOptionPane.showOptionDialog(frame,
                            "關閉" + frame.getTitle() + "?",
                            "確認關閉",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, null, null);
                    if(confirm != 0) {
                        statusLabel.setText("取消關閉" + frame.getTitle());
                        throw new java.beans.PropertyVetoException("Cancelled", null);
                    }else{
                        statusLabel.setText("已關閉"+ frame.getTitle());
                    }
                }
            }
        });
    }

    //TODO the method to get metadata
    private String getMetadataByURL(File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        String metadata ="";
        if(attr.isDirectory()){
            metadata = file.getName() + "為資料夾" +"\n" +
                    "裡面包含" + file.list().length + "個檔案" +"\n" +
                    "檔案建立日期: " + attr.creationTime() +"\n" +
                    "上次存取日期: " + attr.lastAccessTime() +"\n" +
                    "上次修改日期: " + attr.lastModifiedTime()+"\n" +
                    "檔案大小: " + attr.size() +"位元組\n";
        }else{
            metadata = file.getName() + "為檔案" +"\n" +
                    "檔案路徑: " + filePath +"\n" +
                    "檔案建立日期: " + attr.creationTime() +"\n" +
                    "上次存取日期: " + attr.lastAccessTime() +"\n" +
                    "上次修改日期: " + attr.lastModifiedTime()+"\n" +
                    "檔案大小: " + attr.size() +"位元組";
        }


        return metadata;
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

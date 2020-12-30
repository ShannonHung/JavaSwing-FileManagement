package com.filemanage;

import jdk.internal.org.objectweb.asm.tree.FieldNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;

public class FileBrowser extends JFrame{

    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private JTree tree;

    public FileBrowser(String name){
        super(name);

        //root of tree
        File fileRoot= new File("C:/");
        root = new DefaultMutableTreeNode(new FileNode(fileRoot));
        //for Dynamically Changing a Tree: 要建立一個可以監控整棵樹的Model 將root放進去代表監控整個root
        treeModel = new DefaultTreeModel(root);
        //建立實體樹出來
        tree = new JTree(treeModel);
        //是否要顯示root那根提示說還有資料的把柄
        tree.setShowsRootHandles(true);
        //因為可能tree很常要放進去ScrollPane
        JScrollPane scrollPane = new JScrollPane(tree);

        //add to frame
        add(scrollPane);
        setLocationByPlatform(true);
        setSize(640, 480);
        setVisible(true);

        CreateChildNode ccn =
                new CreateChildNode(fileRoot, root);
        new Thread(ccn).start();
    }

}

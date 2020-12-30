package com.filemanage;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

public class CreateChildNode implements Runnable{
    private DefaultMutableTreeNode root;
    private File fileRoot;
    public CreateChildNode(File fileRoot, DefaultMutableTreeNode root){
        this.fileRoot = fileRoot;
        this.root= root;
    }
    private void createChildren(File fileRoot, DefaultMutableTreeNode node){
        File[] files = fileRoot.listFiles();
        if(files == null) return;

        for(File file:files){
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(file));
            node.add(childNode);
            if(file.isDirectory()){
                createChildren(file, childNode);
            }
        }

    }

    public void run() {
        createChildren(fileRoot, root);
    }
}

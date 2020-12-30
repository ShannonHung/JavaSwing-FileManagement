package com.filemanage;

import javax.swing.*;

public class FileManagement {
    public static void main(String[] args) {
        FileBrowser frame = new FileBrowser("File Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

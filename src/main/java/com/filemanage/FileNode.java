package com.filemanage;

import java.io.File;

public class FileNode {
    private String name;
    private File file;

    public FileNode(File file){
        this.file = file;
    }
    public FileNode(String name, File file){
        this.name = name;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        if(this.name == null){
            String name = file.getName();
            if(name.equals("")){
                return file.getAbsolutePath();
            }else{
                return name;
            }
        }else{
            return this.name;
        }
}}

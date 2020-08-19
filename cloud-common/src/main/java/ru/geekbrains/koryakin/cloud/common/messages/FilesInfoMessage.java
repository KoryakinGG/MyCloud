package ru.geekbrains.koryakin.cloud.common.messages;

import ru.geekbrains.koryakin.cloud.common.FileInfo;

import java.util.ArrayList;

public class FilesInfoMessage extends Message {
    private ArrayList<FileInfo> files;
    private long size;

    public FilesInfoMessage() {
        this.files = new ArrayList<>();
        this.size = 0;
    }

    public FilesInfoMessage(FileInfo file) {
        this.files = new ArrayList<>();
        this.files.add(file);
        this.size = file.getSizeInBytes();
    }

    public FilesInfoMessage(ArrayList<FileInfo> files) {
        this.files = files;
        long size = 0;
        for (FileInfo file : files) {
            size += file.getSizeInBytes();
        }
        this.size = size;
    }

    public ArrayList<FileInfo> getFiles() {
        return files;
    }

    public long getSize() {
        return size;
    }

    public void add(FileInfo fileInfo) {
        this.files.add(fileInfo);
        this.size += fileInfo.getSizeInBytes();
    }
}

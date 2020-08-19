package ru.geekbrains.koryakin.cloud.common;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo implements Serializable {
    private String name;
    private long size;
    private boolean local;
    private boolean remote;

    public FileInfo(String name, long size, boolean local, boolean remote) {
        this.name = name;
        this.size = size;
        this.local = local;
        this.remote = remote;
    }

    public FileInfo(Path path, boolean local, boolean remote) throws IOException {
        this(path.getFileName().toString(), Files.size(path), local, remote);
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        int sizeDigits = (int) (Math.log10(size) + 1);

        if (sizeDigits <= 3) {
            return size + " B";
        }
        if (sizeDigits <= 6) {
            return getSizeInKBytes() + " KB";
        }
        if (sizeDigits <= 9) {
            return getSizeInMBytes() + " MB";
        }
        return getSizeInGBytes() + " GB";
    }

    public String getLocal() {
        return local ? "+" : "-";
    }

    public String getRemote() {
        return remote ? "+" : "-";
    }

    public long getSizeInBytes() {
        return size;
    }

    public double getSizeInKBytes() {
        return Math.round((double) size / 1024 * 10) / 10.0;
    }

    public double getSizeInMBytes() {
        return Math.round(getSizeInKBytes() / 1024 * 10) / 10.0;
    }

    public double getSizeInGBytes() {
        return Math.round(getSizeInMBytes() / 1024 * 10) / 10.0;
    }

    public void setLocal(boolean value) {
        this.local = value;
    }

    public boolean isLocal() {
        return local;
    }

    public boolean isRemote() {
        return remote;
    }
}

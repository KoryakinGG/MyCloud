package ru.geekbrains.koryakin.cloud.common.messages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends Message {

    private String name;
    private byte[] data;

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public FileMessage(Path path) throws IOException {
        this.name = path.getFileName().toString();
        this.data = Files.readAllBytes(path);
    }
}

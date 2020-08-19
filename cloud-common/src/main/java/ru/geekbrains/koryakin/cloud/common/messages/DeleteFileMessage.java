package ru.geekbrains.koryakin.cloud.common.messages;

public class DeleteFileMessage extends Message {
    private String name;

    public DeleteFileMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

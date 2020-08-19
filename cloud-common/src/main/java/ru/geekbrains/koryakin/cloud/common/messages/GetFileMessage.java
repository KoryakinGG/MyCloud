package ru.geekbrains.koryakin.cloud.common.messages;

public class GetFileMessage extends Message {
    private String name;

    public GetFileMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

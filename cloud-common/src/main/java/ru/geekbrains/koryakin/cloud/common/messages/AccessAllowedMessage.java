package ru.geekbrains.koryakin.cloud.common.messages;


public class AccessAllowedMessage extends Message {
    private int id;
    private String token;

    public AccessAllowedMessage(int id, String token) {
        this.id = id;
        this.token = token;
    }

    public Integer getId() {
        return id;
    }

    public String getToken() {
        return token;
    }
}


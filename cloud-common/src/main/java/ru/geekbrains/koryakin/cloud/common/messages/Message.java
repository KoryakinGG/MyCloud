package ru.geekbrains.koryakin.cloud.common.messages;

import java.io.Serializable;

public abstract class Message implements Serializable {

    private Integer id;
    private String token;

    public Integer getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
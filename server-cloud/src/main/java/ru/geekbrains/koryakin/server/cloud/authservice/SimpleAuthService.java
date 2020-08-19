package ru.geekbrains.koryakin.server.cloud.authservice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimpleAuthService implements AuthService {

    private class User {
        private int id;
        private String login;
        private String password;
        private String token;

        User(int id, String login, String password) {
            this.id = id;
            this.login = login;
            this.password = password;
            this.token = UUID.randomUUID().toString();
        }

        User(String login, String password) {
            this(users.size() + 1, login, password);
        }
    }

    private List<User> users;

    public SimpleAuthService() {
        this.users = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            users.add(new User("user" + i, "pass" + i));
        }
    }

    @Override
    public Integer getUserId(String login, String password) {
        for (User user : users) {
            if (user.login.equals(login)) {
                if (user.password.equals(password)) {
                    return user.id;
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public String getToken(int id) {
        for (User user : users) {
            if (user.id == id) {
                return user.token;
            }
        }
        return null;
    }

    @Override
    public String createToken(int id) {
        for (User user : users) {
            if (user.id == id) {
                user.token = UUID.randomUUID().toString();
                return user.token;
            }
        }
        return null;
    }
}

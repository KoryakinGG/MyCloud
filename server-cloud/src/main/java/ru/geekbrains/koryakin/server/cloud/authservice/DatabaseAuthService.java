package ru.geekbrains.koryakin.server.cloud.authservice;

import ru.geekbrains.koryakin.server.cloud.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseAuthService implements AuthService {

    private class User {
        private int id;
        private String token;

        User(int id, String token) {
            this.id = id;
            this.token = token;
        }
    }

    private List<User> users;

    public DatabaseAuthService() {
        this.users = new ArrayList<>();

        Database.createNewDatabase();
        if (!Database.connect()) {
            throw new RuntimeException("Unable to connect to the database");
        }

        seedUsers();
    }

    @Override
    public Integer getUserId(String login, String password) {
        return Database.getUserId(login, password);
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
        String token = UUID.randomUUID().toString();
        for (User user : users) {
            if (user.id == id) {
                user.token = token;
                return token;
            }
        }
        users.add(new User(id, token));
        return token;
    }

    private void seedUsers() {
        for (int i = 1; i <= 10; i++) {
            Database.createUser("user" + i, "pass" + i);
        }
        System.out.println("Users seeded");
    }
}

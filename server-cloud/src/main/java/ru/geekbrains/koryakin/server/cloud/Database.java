package ru.geekbrains.koryakin.server.cloud;

import java.io.File;
import java.sql.*;

public class Database {
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement createUserStatement;
    private static PreparedStatement getUserIdStatement;
    private static PreparedStatement deleteUserStatement;

    private static final String DATABASE_NAME = "storageInfo.db";

    public static void createNewDatabase() {
        String databaseFolder = System.getProperty("user.dir") + "/database/";
        new File(databaseFolder).mkdirs();
        String url = "jdbc:sqlite:" + databaseFolder + DATABASE_NAME;

        try (Connection connection = DriverManager.getConnection(url)) {
            if (connection != null) {
                DatabaseMetaData meta = connection.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:database/" + DATABASE_NAME);
            System.out.println("Connected to the database");
            statement = connection.createStatement();
            dropAllTables();
            createAllTablesIfNotExists();
            prepareAllStatement();
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void disconnect() {
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createAllTablesIfNotExists() throws SQLException {
        createUserTable();
        System.out.println("All tables has been created");
    }

    public static void createUserTable() throws SQLException {
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS user (" +
                "    id       INTEGER      PRIMARY KEY AUTOINCREMENT" +
                "                          NOT NULL" +
                "                          UNIQUE," +
                "    login    VARCHAR (32) UNIQUE" +
                "                          NOT NULL," +
                "    password VARCHAR (32) NOT NULL" +
                ");"
        );
    }

    private static void dropAllTables() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS user");
    }

    public static void prepareAllStatement() throws SQLException {
        createUserStatement = connection.prepareStatement("INSERT INTO user (login, password) VALUES (?, ?);");
        getUserIdStatement = connection.prepareStatement("SELECT id FROM user WHERE login = ? AND password = ?;");
        deleteUserStatement = connection.prepareStatement("DELETE FROM user WHERE id = ?;");
    }

    public static boolean createUser(String login, String password) {
        try {
            createUserStatement.setString(1, login);
            createUserStatement.setString(2, password);
            createUserStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static Integer getUserId(String login, String password) {
        Integer id = null;
        try {
            getUserIdStatement.setString(1, login);
            getUserIdStatement.setString(2, password);
            ResultSet rs = getUserIdStatement.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static boolean deleteUser(int id) {
        try {
            deleteUserStatement.setInt(1, id);
            deleteUserStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
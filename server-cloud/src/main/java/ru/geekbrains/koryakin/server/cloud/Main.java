package ru.geekbrains.koryakin.server.cloud;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        new File("serverStorage/").mkdirs();
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new Server(port).run();
    }
}

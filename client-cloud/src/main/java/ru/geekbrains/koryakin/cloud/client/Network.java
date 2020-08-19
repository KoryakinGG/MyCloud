package ru.geekbrains.koryakin.cloud.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import ru.geekbrains.koryakin.cloud.common.messages.Message;

import java.io.IOException;
import java.net.Socket;

public class Network {
    private static Socket socket;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;
    private static int userId;
    private static String userToken;

    public static void start() {
        try {
            socket = new Socket("localhost", 8080);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), 100 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean send(Message message) {
        try {
            message.setId(userId);
            message.setToken(userToken);
            out.writeObject(message);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Message messageListener() throws ClassNotFoundException, IOException {
        Object obj = in.readObject();
        return (Message) obj;
    }

    public static void setUserId(int id) {
        userId = id;
    }

    public static void setUserToken(String token) {
        userToken = token;
    }
}

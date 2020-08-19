package ru.geekbrains.koryakin.server.cloud.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.koryakin.cloud.common.messages.AccessAllowedMessage;
import ru.geekbrains.koryakin.cloud.common.messages.AccessDeniedMessage;
import ru.geekbrains.koryakin.cloud.common.messages.AuthMessage;
import ru.geekbrains.koryakin.cloud.common.messages.Message;
import ru.geekbrains.koryakin.server.cloud.authservice.AuthService;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private AuthService authService;

    public AuthHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client requests authentication");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) {
            return;
        }

        if (msg instanceof AuthMessage) {
            AuthMessage authMessage = (AuthMessage) msg;
            Integer userId = authService.getUserId(authMessage.getLogin(), authMessage.getPassword());
            if (userId != null) {
                authMessage.setId(userId);
                String userToken = authService.getToken(userId);
                if (userToken == null) {
                    userToken = authService.createToken(userId);
                }
                ctx.fireChannelRead(authMessage);
                ctx.writeAndFlush(new AccessAllowedMessage(userId, userToken));
                System.out.println("Access allowed");
            } else {
                ctx.writeAndFlush(new AccessDeniedMessage());
                System.out.println("Access denied");
            }
        } else {
            Message message = (Message) msg;
            if (message.getId() != null && message.getToken() != null) {
                System.out.println("User id: " + message.getId() + ", token: " + message.getToken());
                ctx.fireChannelRead(message);
            } else {
                System.out.println("Client not authenticated!");
                return;
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
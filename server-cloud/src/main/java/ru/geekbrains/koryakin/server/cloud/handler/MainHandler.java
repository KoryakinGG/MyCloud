package ru.geekbrains.koryakin.server.cloud.handler;

import ru.geekbrains.koryakin.cloud.common.FileInfo;
import ru.geekbrains.koryakin.cloud.common.messages.*;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private final String STORAGE_FOLDER = "serverStorage/";
    private String userFolder;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }
            System.out.println("Received message: " + msg.getClass());

            if (msg instanceof AuthMessage) {
                AuthMessage authMessage = (AuthMessage) msg;
                this.userFolder = STORAGE_FOLDER + authMessage.getId() + "/";
                new File(userFolder).mkdirs();
                sendFilesInfo(ctx);

            } else if (msg instanceof GetFileMessage) {
                GetFileMessage getFileMessage = (GetFileMessage) msg;
                Path path = Paths.get(userFolder + getFileMessage.getName());
                if (Files.exists(path)) {
                    FileMessage fileMessage = new FileMessage(path);
                    ctx.writeAndFlush(fileMessage);
                }

            } else if (msg instanceof GetFilesInfoMessage) {
                sendFilesInfo(ctx);

            } else if (msg instanceof DeleteFileMessage) {
                DeleteFileMessage deleteFileMessage = (DeleteFileMessage) msg;
                Files.delete(Paths.get(userFolder + deleteFileMessage.getName()));
                sendFilesInfo(ctx);

            } else if (msg instanceof FileMessage) {
                FileMessage fileMessage = (FileMessage) msg;
                Files.write(Paths.get(userFolder + fileMessage.getName()), fileMessage.getData(),
                        StandardOpenOption.CREATE);
                sendFilesInfo(ctx);

            } else {
                System.out.println("Server received wrong object!");
                return;
            }
        } finally {
            ReferenceCountUtil.release(msg);
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

    private void sendFilesInfo(ChannelHandlerContext ctx) throws IOException {
        FilesInfoMessage filesInfoMessage = new FilesInfoMessage();
        Files.list(Paths.get(userFolder))
                .map(path -> {
                    FileInfo fileInfo = null;
                    try {
                        fileInfo = new FileInfo(path.getFileName().toString(), Files.size(path), false, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return fileInfo;
                })
                .forEach(filesInfoMessage::add);
        ctx.writeAndFlush(filesInfoMessage);
    }
}

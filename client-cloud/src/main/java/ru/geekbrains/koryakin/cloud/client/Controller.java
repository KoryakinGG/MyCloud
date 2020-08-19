package ru.geekbrains.koryakin.cloud.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.geekbrains.koryakin.cloud.common.FileInfo;
import ru.geekbrains.koryakin.cloud.common.messages.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class Controller implements Initializable {

    private final String STORAGE_FOLDER = "clientStorage/";
    private String userFolder;
    private Stage stage;
    private String focusFileName;
    private ArrayList<FileInfo> remoteFileList = new ArrayList<>();

    public VBox authScreen;
    public TextField loginField;
    public PasswordField passwordField;
    public HBox accessDeniedNotice;
    public VBox mainScreen;
    public TableView<FileInfo> fileTable;
    public TableColumn<FileInfo, String> fileNameColumn;
    public TableColumn<FileInfo, Long> fileSizeColumn;
    public TableColumn<FileInfo, Boolean> localFlagColumn;
    public TableColumn<FileInfo, Boolean> remoteFlagColumn;
    public Button downloadFile;
    public Button deleteLocalFile;
    public Button deleteFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainScreen.setManaged(false);
        mainScreen.setVisible(false);
        toggleAccessDeniedNotice(false);
    }

    public void setStageAndSetupListeners(Stage stage) {
        this.stage = stage;
    }

    public static void render(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void refreshFileList(FilesInfoMessage remoteFileList) {
        render(() -> {
            this.remoteFileList.clear();
            this.remoteFileList.addAll(remoteFileList.getFiles());
            refreshLocalFileList();
        });
    }

    private void refreshLocalFileList() {
        ArrayList<FileInfo> localFiles = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(userFolder))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            localFiles.add(new FileInfo(file, true, false));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }


        ArrayList<FileInfo> mergedFileList = mergeFileLists(remoteFileList, localFiles);

        fileTable.getItems().clear();
        fileTable.getItems().addAll(mergedFileList);
    }

    private ArrayList<FileInfo> mergeFileLists(ArrayList<FileInfo> remoteFiles, ArrayList<FileInfo> localFiles) {
        for (FileInfo remoteFile : remoteFiles) {
            boolean local = false;
            for (FileInfo localFile : localFiles) {
                if (remoteFile.getName().equals(localFile.getName())) {
                    local = true;
                    break;
                }
            }
            remoteFile.setLocal(local);
        }

        return remoteFiles;
    }

    private void deleteLocalFile(String fileName) {
        try {
            Files.delete(Paths.get(userFolder + fileName));
            refreshLocalFileList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        Path filePath = fileChooser.showOpenDialog(stage).toPath();
        Path newFilePath = Paths.get(userFolder + filePath.getFileName());
        try {
            Files.copy(filePath, newFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Network.send(new FileMessage(newFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(ActionEvent actionEvent) {
        if (focusFileName != null) {
            Network.send(new GetFileMessage(focusFileName));
        }
    }

    public void deleteLocalFile(ActionEvent actionEvent) {
        if (focusFileName != null) {
            deleteLocalFile(focusFileName);
        }
    }

    public void deleteFile(ActionEvent actionEvent) {
        if (focusFileName != null) {
            Network.send(new DeleteFileMessage(focusFileName));
            deleteLocalFile(focusFileName);
        }
    }

    public void signIn(ActionEvent actionEvent) {
        toggleAccessDeniedNotice(false);
        if (!loginField.getText().equals("") && !passwordField.getText().equals("")) {
            Network.start();
            startMessageListener();
            Network.send(new AuthMessage(loginField.getText(), passwordField.getText()));
            this.userFolder = STORAGE_FOLDER + loginField.getText() + "/";
            passwordField.clear();
        }
    }

    private void startMessageListener() {
        Thread messageListeningThread = new Thread(() -> {
            try {
                while (true) {
                    Message msg = Network.messageListener();
                    System.out.println("Received message: " + msg.getClass());

                    if (msg instanceof AccessDeniedMessage) {
                        toggleAccessDeniedNotice(true);

                    } else if (msg instanceof AccessAllowedMessage) {
                        AccessAllowedMessage accessAllowedMessage = (AccessAllowedMessage) msg;
                        Network.setUserToken(accessAllowedMessage.getToken());
                        Network.setUserId(accessAllowedMessage.getId());
                        System.out.println("User token: " + accessAllowedMessage.getToken());
                        new File(userFolder).mkdirs();
                        showMainScreen();

                    } else if (msg instanceof FileMessage) {
                        FileMessage fileMessage = (FileMessage) msg;
                        Files.write(Paths.get(userFolder + fileMessage.getName()), fileMessage.getData(),
                                StandardOpenOption.CREATE);
                        refreshLocalFileList();

                    } else if (msg instanceof FilesInfoMessage) {
                        FilesInfoMessage filesInfoMessage = (FilesInfoMessage) msg;
                        refreshFileList(filesInfoMessage);

                    } else {
                        System.out.println("Undefined message from server: " + msg);
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        messageListeningThread.setDaemon(true);
        messageListeningThread.start();
    }

    private void showMainScreen() {
        authScreen.setManaged(false);
        authScreen.setVisible(false);
        mainScreen.setManaged(true);
        mainScreen.setVisible(true);

        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        localFlagColumn.setCellValueFactory(new PropertyValueFactory<>("local"));
        remoteFlagColumn.setCellValueFactory(new PropertyValueFactory<>("remote"));

        downloadFile.setDisable(true);
        deleteLocalFile.setDisable(true);
        deleteFile.setDisable(true);

        fileTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (obs.getValue() != null) {
                downloadFile.setDisable(obs.getValue().isLocal());
                deleteLocalFile.setDisable(!obs.getValue().isLocal());
            } else {
                downloadFile.setDisable(false);
                deleteLocalFile.setDisable(false);
            }
            deleteFile.setDisable(false);

            if (newSelection != null) {
                focusFileName = newSelection.getName();
            } else {
                focusFileName = null;
            }
        });

        Platform.runLater(() -> fileTable.requestFocus());
    }

    private void toggleAccessDeniedNotice(boolean state) {
        accessDeniedNotice.setManaged(state);
        accessDeniedNotice.setVisible(state);
    }
}

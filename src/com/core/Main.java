package com.core;

import com.tools.FileResourceUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    public static final String TRANSLATION_FILE = "Translation.json";
    public static final String SYNTAX_DEFINITIONS_FILE = "SyntaxDefinitions.json";

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/core/gui/GrammarInput.fxml")));
        primaryStage.setTitle(FileResourceUtils.readJson(TRANSLATION_FILE).getString("APPLICATION_NAME"));

        Scene firstScene = new Scene(root);
        primaryStage.setScene(firstScene);
        primaryStage.show();
    }
}

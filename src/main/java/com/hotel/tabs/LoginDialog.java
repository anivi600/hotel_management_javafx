package com.hotel.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginDialog {

    public static void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Four Square Login");

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #FFF8F2;");

        Label label = new Label("Secure Access");
        label.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #1A0A04;");

        TextField user = new TextField();
        user.setPromptText("Username");
        user.setMaxWidth(250);

        PasswordField pass = new PasswordField();
        pass.setPromptText("Password");
        pass.setMaxWidth(250);

        Button loginBtn = new Button("Login");
        loginBtn.setId("btnLogin");
        loginBtn.setMinWidth(100);

        root.getChildren().addAll(label, user, pass, loginBtn);

        Scene scene = new Scene(root, 400, 350);
        try {
            scene.getStylesheets().add(LoginDialog.class.getResource("/com/hotel/styles.css").toExternalForm());
        } catch (Exception e) {}

        stage.setScene(scene);
        stage.showAndWait();
    }
}

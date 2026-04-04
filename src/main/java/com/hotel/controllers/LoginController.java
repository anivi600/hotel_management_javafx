package com.hotel.controllers;

import com.hotel.MainApp;
import com.hotel.models.User;
import com.hotel.services.DatabaseService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class LoginController {

    private final DatabaseService databaseService;
    private final MainApp mainApp;

    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private Button btnLogin;

    public LoginController(DatabaseService databaseService, MainApp mainApp) {
        this.databaseService = databaseService;
        this.mainApp = mainApp;
    }

    @FXML
    void initialize() {
        btnLogin.setOnAction(e -> handleLogin());
        
        pfPassword.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }

    private void handleLogin() {
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login Failed", "Username and password cannot be empty.");
            return;
        }

        User user = databaseService.authenticate(username, password);
        if (user != null) {
            System.out.println("[Login] Successful login for " + user.getUsername() + " as " + user.getRole());
            mainApp.showMainView(user);
        } else {
            showAlert("Login Failed", "Invalid username or password.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

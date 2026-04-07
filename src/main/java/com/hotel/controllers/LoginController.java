package com.hotel.controllers;

import com.hotel.MainApp;
import com.hotel.dao.UserDAO;
import com.hotel.models.User;
import com.hotel.util.UiAlerts;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.sql.SQLException;

public class LoginController {

    private final UserDAO userDAO;
    private final MainApp mainApp;

    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private Button btnLogin;

    public LoginController(UserDAO userDAO, MainApp mainApp) {
        this.userDAO = userDAO;
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

        try {
            User user = userDAO.findByUsernameAndPassword(username, password).orElse(null);
            if (user != null) {
                System.out.println("[Login] Successful login for " + user.getUsername() + " as " + user.getRole());
                mainApp.showMainView(user);
            } else {
                showAlert("Login Failed", "Invalid username or password.");
            }
        } catch (SQLException e) {
            UiAlerts.showError("Database Error", e);
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

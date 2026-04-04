package com.hotel;

import com.hotel.controllers.*;
import com.hotel.models.*;
import com.hotel.services.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application entry point — loads {@code main_view.fxml} (Scene Builder compatible).
 */
public class MainApp extends Application {

    private RoomService roomService;
    private CustomerService customerService;
    private BillingService billingService;
    private FileService fileService;
    private DatabaseService databaseService;
    
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        
        databaseService = new DatabaseService();
        roomService = new RoomService(databaseService);
        customerService = new CustomerService(databaseService);
        billingService = new BillingService();
        fileService = new FileService();

        showLoginView();
    }
    
    public void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/hotel/login_view.fxml"));
            loader.setControllerFactory(param -> {
                if (param == LoginController.class) {
                    return new LoginController(databaseService, this);
                }
                try {
                    return param.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            Parent root = loader.load();
            Scene scene = new Scene(root, 1050, 720);
            primaryStage.setTitle("Four Square Hotel - Login");
            primaryStage.setMinWidth(950);
            primaryStage.setMinHeight(680);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showMainView(User loggedInUser) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/hotel/main_view.fxml"));
            loader.setControllerFactory(param -> {
                if (param == MainController.class) {
                    return new MainController(roomService, customerService, fileService, loggedInUser, this);
                }
            if (param == RoomController.class) {
                return new RoomController(roomService, fileService);
            }
            if (param == CustomerController.class) {
                return new CustomerController(roomService, customerService, fileService);
            }
            if (param == BookingController.class) {
                return new BookingController(roomService, customerService, billingService, fileService);
            }
            if (param == BillingController.class) {
                return new BillingController(fileService);
            }
            if (param == HousekeepingController.class) {
                return new HousekeepingController(databaseService, loggedInUser);
            }
            try {
                return param.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot create controller: " + param.getName(), e);
            }
        });

            Parent root = loader.load();
            MainController mainController = loader.getController();

            Scene scene = new Scene(root, 1050, 720);
            try {
                String css = getClass().getResource("/com/hotel/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (NullPointerException e) {
                System.err.println("[MainApp] WARNING: styles.css not found — continuing without styling.");
            }

            primaryStage.setTitle("Four Square Hotel Manipal Management System - " + loggedInUser.getRole());
            primaryStage.setScene(scene);

            if (mainController.getStatusLabel() != null) {
                mainController.getStatusLabel().setText("  Logged in as " + loggedInUser.getUsername() + " (" + loggedInUser.getRole() + ") — "
                        + java.time.LocalDate.now());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // loadData() logic is removed as we now rely on DatabaseService directly

    public static void main(String[] args) {
        launch(args);
    }
}

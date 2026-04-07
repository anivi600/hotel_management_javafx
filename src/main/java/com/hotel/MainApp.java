package com.hotel;

import com.hotel.controllers.*;
import com.hotel.dao.*;
import com.hotel.database.DatabaseConnection;
import com.hotel.models.User;
import com.hotel.services.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application entry point — loads {@code login_view.fxml}, then {@code main_view.fxml}.
 */
public class MainApp extends Application {

    private DatabaseService databaseService;
    private RoomService roomService;
    private CustomerService customerService;
    private BillingService billingService;
    private FileService fileService;

    private UserDAO userDAO;
    private BookingDAO bookingDAO;
    private BillDAO billDAO;
    private HousekeepingDAO housekeepingDAO;

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;

        DatabaseConnection dbConn = new DatabaseConnection();
        databaseService = new DatabaseService(dbConn);

        RoomDAO roomDAO = new RoomDAO(dbConn);
        CustomerDAO customerDAO = new CustomerDAO(dbConn);
        bookingDAO = new BookingDAO(dbConn);
        billDAO = new BillDAO(dbConn);
        userDAO = new UserDAO(dbConn);
        housekeepingDAO = new HousekeepingDAO(dbConn);

        try {
            roomService = new RoomService(roomDAO, bookingDAO, customerDAO);
            customerService = new CustomerService(customerDAO);
        } catch (Exception e) {
            throw new IOException("Could not initialize data services. Is MySQL running and database 'hotel_management' created?", e);
        }

        billingService = new BillingService();
        fileService = new FileService();

        showLoginView();
    }

    public void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/hotel/login_view.fxml"));
            loader.setControllerFactory(param -> {
                if (param == LoginController.class) {
                    return new LoginController(userDAO, this);
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
                    return new BookingController(roomService, customerService, billingService, fileService,
                            bookingDAO, billDAO);
                }
                if (param == BillingController.class) {
                    return new BillingController(billDAO);
                }
                if (param == HousekeepingController.class) {
                    return new HousekeepingController(housekeepingDAO, loggedInUser);
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

    public static void main(String[] args) {
        launch(args);
    }

    public RoomService getRoomService() { return roomService; }
    public CustomerService getCustomerService() { return customerService; }
    public BillingService getBillingService() { return billingService; }
    public FileService getFileService() { return fileService; }
    public BookingDAO getBookingDAO() { return bookingDAO; }
    public BillDAO getBillDAO() { return billDAO; }
}

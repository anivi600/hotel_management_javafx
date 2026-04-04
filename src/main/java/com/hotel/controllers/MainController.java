package com.hotel.controllers;

import com.hotel.MainApp;
import com.hotel.models.User;
import com.hotel.services.CustomerService;
import com.hotel.services.FileService;
import com.hotel.services.RoomService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * FXML controller for {@code main_view.fxml} — header, tab includes, status bar.
 */
public class MainController {

    private final RoomService roomService;
    private final CustomerService customerService;
    private final FileService fileService;
    private final User loggedInUser;
    private final MainApp mainApp;

    @FXML private Label dateLabel;
    @FXML private Button btnExport;
    @FXML private Button btnLogout;
    @FXML private Label statusLabel;
    @FXML private TabPane tabPane;
    
    @FXML private Tab tabRoom;
    @FXML private Tab tabCustomer;
    @FXML private Tab tabBooking;
    @FXML private Tab tabBilling;
    @FXML private Tab tabHousekeeping;

    @FXML private RoomController roomTabController;
    @FXML private CustomerController customerTabController;
    @FXML private BookingController bookingTabController;
    @FXML private BillingController billingTabController;
    @FXML private HousekeepingController housekeepingTabController;

    public MainController(RoomService roomService, CustomerService customerService, FileService fileService, User loggedInUser, MainApp mainApp) {
        this.roomService = roomService;
        this.customerService = customerService;
        this.fileService = fileService;
        this.loggedInUser = loggedInUser;
        this.mainApp = mainApp;
    }

    @FXML
    void initialize() {
        dateLabel.setText("📅  " + LocalDate.now());

        Consumer<String> su = msg -> Platform.runLater(() -> statusLabel.setText("  " + msg));
        fileService.setStatusCallback(su);

        roomTabController.setStatusUpdater(su);
        customerTabController.setStatusUpdater(su);
        bookingTabController.setStatusUpdater(su);

        roomTabController.setBookingController(bookingTabController);
        customerTabController.setBookingController(bookingTabController);
        bookingTabController.setSiblingControllers(roomTabController, customerTabController, billingTabController);

        btnExport.setOnAction(e -> {
            fileService.exportDataToText(roomService.getRoomsList(), customerService.getCustomersList());
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Data Exported");
            info.setHeaderText(null);
            info.setContentText("Readable export saved to:\n"
                    + fileService.getDataDirectory() + "\\data_export.txt\n\n"
                    + "Open that file in Notepad or any text editor.");
            info.showAndWait();
            su.accept("Data exported to data_export.txt — "
                    + java.time.LocalTime.now().toString().substring(0, 5));
        });
        
        btnLogout.setOnAction(e -> {
            mainApp.showLoginView();
        });

        applyRoleBasedAccess();
    }
    
    private void applyRoleBasedAccess() {
        if ("Receptionist".equals(loggedInUser.getRole())) {
            tabPane.getTabs().remove(tabHousekeeping);
            tabPane.getTabs().remove(tabBilling); // Depending on requirements, they might not see all bills
        } else if ("Housekeeping".equals(loggedInUser.getRole())) {
            tabPane.getTabs().retainAll(tabHousekeeping);
            btnExport.setDisable(true);
        }
    }

    public Label getStatusLabel() {
        return statusLabel;
    }
}

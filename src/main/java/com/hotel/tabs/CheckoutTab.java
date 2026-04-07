package com.hotel.tabs;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.BillDAO;
import com.hotel.models.*;
import com.hotel.services.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;

public class CheckoutTab extends Tab {
    private final RoomService roomService;
    private final CustomerService customerService;
    private final BillingService billingService;
    private final FileService fileService;
    private final BookingDAO bookingDAO;
    private final BillDAO billDAO;
    private final ListView<String> occupiedList;

    public CheckoutTab(RoomService roomService, CustomerService customerService, 
                       BillingService billingService, FileService fileService,
                       BookingDAO bookingDAO, BillDAO billDAO) {
        this.roomService = roomService;
        this.customerService = customerService;
        this.billingService = billingService;
        this.fileService = fileService;
        this.bookingDAO = bookingDAO;
        this.billDAO = billDAO;
        this.occupiedList = new ListView<>();
        setText("Checkout");
        setClosable(false);

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));

        Label header = new Label("Guest Checkout");
        header.getStyleClass().add("form-title");

        VBox panel = new VBox(15);
        panel.getStyleClass().add("form-panel");

        HBox controls = new HBox(10);
        Label label = new Label("Select Occupied Room:");
        label.getStyleClass().add("sub-heading");
        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.setOnAction(e -> refreshList());
        controls.getChildren().addAll(label, btnRefresh);

        panel.getChildren().add(controls);
        panel.getChildren().add(occupiedList);

        Button btnCheckout = new Button("Process Checkout & Bill");
        btnCheckout.setId("saveBillBtn");
        btnCheckout.setMaxWidth(Double.MAX_VALUE);
        
        btnCheckout.setOnAction(e -> handleCheckout());
        
        panel.getChildren().add(btnCheckout);

        container.getChildren().addAll(header, panel);
        setContent(container);
        
        setOnSelectionChanged(e -> {
            if (isSelected()) refreshList();
        });

        refreshList();
    }

    private void refreshList() {
        occupiedList.getItems().clear();
        try {
            // Load fresh from DB via sync
            List<Booking> active = bookingDAO.findAllActive();
            roomService.syncFromActiveBookings(active);
            
            if (active.isEmpty()) {
                occupiedList.getItems().add("No rooms currently occupied.");
            } else {
                for (Booking b : active) {
                    occupiedList.getItems().add("Room " + b.getRoom().getRoomNumber() + " | Guest: " + b.getCustomer().getName() + " | ID: " + b.getBookingId());
                }
            }
        } catch (SQLException ex) {
            occupiedList.getItems().add("Error loading bookings from database.");
        }
    }

    private void handleCheckout() {
        String selected = occupiedList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.startsWith("No rooms") || selected.startsWith("Error")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Please select an occupied room.");
            alert.showAndWait();
            return;
        }

        try {
            // Extract IDs
            int roomNumber = Integer.parseInt(selected.split(" ")[1]);
            int bookingId = Integer.parseInt(selected.split("ID: ")[1]);

            // Find full booking object
            Booking target = null;
            for (Booking b : bookingDAO.findAllActive()) {
                if (b.getBookingId() == bookingId) {
                    target = b;
                    break;
                }
            }

            if (target == null) throw new Exception("Booking not found in database.");

            // 1. Generate Bill
            Bill bill = billingService.generateBill(target, 0.05); // Default 5% discount

            // 2. Persist to DB
            billDAO.insert(bill, bookingId);
            
            // 3. Update Status
            roomService.checkoutRoom(roomNumber);
            customerService.removeCustomer(target.getCustomer().getCustomerId());

            // 4. Save to file
            fileService.saveBillToFile(bill);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Checkout Successful");
            success.setHeaderText("Checkout for Room " + roomNumber + " completed.");
            success.setContentText("Total Amount: ₹" + String.format("%.2f", bill.getGrandTotal()) + "\nReceipt saved to HotelData/bills.");
            success.showAndWait();
            
            refreshList();

        } catch (Exception ex) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setContentText("Checkout failed: " + ex.getMessage());
            error.showAndWait();
        }
    }
}

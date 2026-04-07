package com.hotel.tabs;

import com.hotel.models.Customer;
import com.hotel.models.Room;
import com.hotel.models.Booking;
import com.hotel.services.RoomService;
import com.hotel.services.CustomerService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;


public class BookingTab extends Tab {
    private final RoomService roomService;
    private final CustomerService customerService;
    private final ComboBox<String> guestCombo = new ComboBox<>();
    private final ComboBox<Integer> roomCombo = new ComboBox<>();
    private final Spinner<Integer> nightSpinner = new Spinner<>(1, 30, 1);

    public BookingTab(RoomService roomService, CustomerService customerService) {
        this.roomService = roomService;
        this.customerService = customerService;
        setText("New Booking");
        setClosable(false);

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label header = new Label("Reserve a Room");
        header.getStyleClass().add("form-title");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.getStyleClass().add("form-panel");

        form.add(new Label("Select Guest:"), 0, 0);
        guestCombo.setPromptText("Choose guest...");
        guestCombo.setMaxWidth(Double.MAX_VALUE);
        form.add(guestCombo, 1, 0);

        form.add(new Label("Select Room:"), 0, 1);
        roomCombo.setPromptText("Choose room...");
        roomCombo.setMaxWidth(Double.MAX_VALUE);
        form.add(roomCombo, 1, 1);

        form.add(new Label("Nights:"), 0, 2);
        nightSpinner.setMaxWidth(Double.MAX_VALUE);
        form.add(nightSpinner, 1, 2);

        HBox actions = new HBox(10);
        Button btnBook = new Button("Confirm Booking");
        btnBook.setId("btnBook");
        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.setOnAction(e -> refreshData());
        actions.getChildren().addAll(btnBook, btnRefresh);
        
        form.add(actions, 1, 3);

        btnBook.setOnAction(e -> handleBooking());

        container.getChildren().addAll(header, form);
        setContent(container);
        
        refreshData();
    }

    private void refreshData() {
        guestCombo.getItems().clear();
        for (Customer c : customerService.getCustomersList()) {
            guestCombo.getItems().add(c.getCustomerId() + " - " + c.getName());
        }

        roomCombo.getItems().clear();
        for (Room r : roomService.getAvailableRooms()) {
            roomCombo.getItems().add(r.getRoomNumber());
        }
    }

    private void handleBooking() {
        String selectedGuest = guestCombo.getValue();
        Integer selectedRoom = roomCombo.getValue();
        int nights = nightSpinner.getValue();

        if (selectedGuest == null || selectedRoom == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Please select both a guest and an available room.");
            alert.showAndWait();
            return;
        }

        try {
            int guestId = Integer.parseInt(selectedGuest.split(" - ")[0]);
            Customer customer = null;
            for (Customer c : customerService.getCustomersList()) {
                if (c.getCustomerId() == guestId) {
                    customer = c;
                    break;
                }
            }

            if (customer != null) {
                Booking booking = roomService.bookRoom(customer, selectedRoom, nights);
                if (booking != null) {
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Booking Confirmed");
                    success.setHeaderText("Success!");
                    success.setContentText("Room " + selectedRoom + " booked for " + customer.getName() + " for " + nights + " nights.");
                    success.showAndWait();
                    refreshData();
                }
            }
        } catch (Exception ex) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setContentText("Booking failed: " + ex.getMessage());
            error.showAndWait();
        }
    }
}

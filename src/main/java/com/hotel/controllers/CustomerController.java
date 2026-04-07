package com.hotel.controllers;

import com.hotel.models.Customer;
import com.hotel.services.CustomerService;
import com.hotel.services.FileService;
import com.hotel.services.RoomService;
import com.hotel.util.UiAlerts;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * FXML controller for {@code customer_tab.fxml}.
 */
public class CustomerController {

    private final RoomService roomService;
    private final CustomerService customerService;
    private final FileService fileService;

    private Consumer<String> statusUpdater = m -> {};

    private BookingController bookingController;

    private ObservableList<Customer> customerObsList;

    @FXML private TextField tfName;
    @FXML private TextField tfContact;
    @FXML private TableView<Customer> tableView;
    @FXML private Button btnAdd;
    @FXML private Button btnRemove;

    public CustomerController(RoomService roomService, CustomerService customerService, FileService fileService) {
        this.roomService = roomService;
        this.customerService = customerService;
        this.fileService = fileService;
    }

    public void setStatusUpdater(Consumer<String> statusUpdater) {
        this.statusUpdater = statusUpdater != null ? statusUpdater : m -> {};
    }

    public void setBookingController(BookingController bookingController) {
        this.bookingController = bookingController;
    }

    @FXML
    void initialize() {
        tfName.setId("customerNameField");
        tfContact.setId("customerContactField");
        btnAdd.setId("addCustomerBtn");
        btnRemove.setId("removeCustomerBtn");

        buildTable();

        btnAdd.setOnAction(e -> handleAddCustomer());
        btnRemove.setOnAction(e -> handleRemoveCustomer());

        refreshTable();
    }

    @SuppressWarnings("unchecked")
    private void buildTable() {
        tableView.setPlaceholder(new Label("No customers registered."));

        TableColumn<Customer, Integer> colId = new TableColumn<>("Customer ID");
        TableColumn<Customer, String> colName = new TableColumn<>("Name");
        TableColumn<Customer, String> colContact = new TableColumn<>("Contact");
        TableColumn<Customer, String> colRoom = new TableColumn<>("Allocated Room");

        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCustomerId()).asObject());
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        colRoom.setCellValueFactory(c -> {
            int rn = c.getValue().getAllocatedRoomNumber();
            return new SimpleStringProperty(rn > 0 ? "Room " + rn : "Not Assigned");
        });

        colId.setPrefWidth(110);
        colName.setPrefWidth(200);
        colContact.setPrefWidth(150);
        colRoom.setPrefWidth(140);

        tableView.getColumns().addAll(colId, colName, colContact, colRoom);
        customerObsList = FXCollections.observableArrayList();
        tableView.setItems(customerObsList);
    }

    private void handleAddCustomer() {
        String name = tfName.getText().trim();
        String contact = tfContact.getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Customer name must not be empty.");
            return;
        }
        if (!contact.matches("\\d{10}")) {
            showAlert(Alert.AlertType.ERROR, "Validation",
                    "Contact number must be exactly 10 digits.");
            return;
        }

        try {
            Customer customer = new Customer(name, contact, 0);
            customerService.addCustomer(customer);

            refreshTable();
            if (bookingController != null) bookingController.populateCombos();

            clearForm();
            statusUpdater.accept("Customer \"" + name + "\" registered — "
                    + java.time.LocalTime.now().toString().substring(0, 5));

        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        } catch (SQLException ex) {
            UiAlerts.showError("Database Error", ex);
        }
    }

    private void handleRemoveCustomer() {
        Customer selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "Selection Error", "Select a customer to remove.");
            return;
        }

        String roomInfo = selected.getAllocatedRoomNumber() > 0
                ? " Room " + selected.getAllocatedRoomNumber() + " will be released."
                : "";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Remove customer \"" + selected.getName() + "\"?" + roomInfo,
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Remove");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    if (selected.getAllocatedRoomNumber() > 0) {
                        try {
                            roomService.setRoomAvailable(selected.getAllocatedRoomNumber(), true);
                        } catch (SQLException e) {
                            UiAlerts.showError("Database Error", e);
                            return;
                        }
                        roomService.getRoomOccupancy().remove(selected.getAllocatedRoomNumber());
                    }

                    customerService.removeCustomer(selected.getCustomerId());

                    refreshTable();
                    if (bookingController != null) bookingController.populateCombos();

                    statusUpdater.accept("Customer \"" + selected.getName()
                            + "\" removed — "
                            + java.time.LocalTime.now().toString().substring(0, 5));
                } catch (SQLException e) {
                    UiAlerts.showError("Database Error", e);
                }
            }
        });
    }

    public void refreshTable() {
        customerObsList.setAll(customerService.getAllCustomers());
    }

    private void clearForm() {
        tfName.clear();
        tfContact.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

package com.hotel.controllers;

import com.hotel.models.*;
import com.hotel.services.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.function.Consumer;

/**
 * FXML controller for {@code booking_tab.fxml}.
 */
public class BookingController {

    private final RoomService roomService;
    private final CustomerService customerService;
    private final BillingService billingService;
    private final FileService fileService;

    private Consumer<String> statusUpdater = m -> {};

    private RoomController roomController;
    private CustomerController customerController;
    private BillingController billingController;

    private final ObservableList<Booking> bookingObsList = FXCollections.observableArrayList();

    @FXML private ComboBox<Customer> cbCustomer;
    @FXML private ComboBox<Room> cbRoom;
    @FXML private TextField tfNights;
    @FXML private TextField tfDiscount;
    @FXML private TableView<Booking> tableView;
    @FXML private Button btnBook;
    @FXML private Button btnCheckout;

    public BookingController(RoomService roomService, CustomerService customerService,
                             BillingService billingService, FileService fileService) {
        this.roomService = roomService;
        this.customerService = customerService;
        this.billingService = billingService;
        this.fileService = fileService;
    }

    public void setStatusUpdater(Consumer<String> statusUpdater) {
        this.statusUpdater = statusUpdater != null ? statusUpdater : m -> {};
    }

    public void setSiblingControllers(RoomController rc, CustomerController cc, BillingController bc) {
        this.roomController = rc;
        this.customerController = cc;
        this.billingController = bc;
    }

    @FXML
    void initialize() {
        cbCustomer.setId("bookingCustomerCombo");
        cbRoom.setId("bookingRoomCombo");
        tfNights.setId("bookingNightsField");
        tfDiscount.setId("bookingDiscountField");
        btnBook.setId("bookRoomBtn");
        btnCheckout.setId("checkoutBtn");

        cbCustomer.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Customer c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getName() + " (ID:" + c.getCustomerId() + ")");
            }
        });
        cbCustomer.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Customer c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getName() + " (ID:" + c.getCustomerId() + ")");
            }
        });

        cbRoom.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null
                        : "Room " + r.getRoomNumber() + " — " + r.getRoomType()
                          + " (₹" + String.format("%.0f", r.calculateTariff()) + "/night)");
            }
        });
        cbRoom.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null
                        : "Room " + r.getRoomNumber() + " — " + r.getRoomType());
            }
        });

        buildTable();

        btnBook.setOnAction(e -> handleBook());
        btnCheckout.setOnAction(e -> handleCheckout());

        populateCombos();
    }

    @SuppressWarnings("unchecked")
    private void buildTable() {
        tableView.setPlaceholder(new Label("No active bookings."));

        TableColumn<Booking, Integer> colId = new TableColumn<>("Booking ID");
        TableColumn<Booking, String> colCust = new TableColumn<>("Customer");
        TableColumn<Booking, Integer> colRoom = new TableColumn<>("Room No");
        TableColumn<Booking, String> colType = new TableColumn<>("Room Type");
        TableColumn<Booking, Integer> colNight = new TableColumn<>("Nights");
        TableColumn<Booking, String> colDate = new TableColumn<>("Check-In");

        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getBookingId()).asObject());
        colCust.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomer().getName()));
        colRoom.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getRoom().getRoomNumber()).asObject());
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoom().getRoomType().toString()));
        colNight.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumberOfNights()).asObject());
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckInDate().toString()));

        colId.setPrefWidth(90);
        colCust.setPrefWidth(160);
        colRoom.setPrefWidth(80);
        colType.setPrefWidth(100);
        colNight.setPrefWidth(70);
        colDate.setPrefWidth(110);

        tableView.getColumns().addAll(colId, colCust, colRoom, colType, colNight, colDate);
        tableView.setItems(bookingObsList);
    }

    private void handleBook() {
        Customer customer = cbCustomer.getValue();
        Room room = cbRoom.getValue();
        String nightsStr = tfNights.getText().trim();

        if (customer == null) {
            alert(Alert.AlertType.ERROR, "Validation", "Please select a customer.");
            return;
        }
        if (room == null) {
            alert(Alert.AlertType.ERROR, "Validation", "Please select a room.");
            return;
        }
        if (nightsStr.isEmpty()) {
            alert(Alert.AlertType.ERROR, "Validation", "Enter number of nights.");
            return;
        }

        int nights;
        try {
            nights = Integer.parseInt(nightsStr);
            if (nights <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            alert(Alert.AlertType.ERROR, "Validation", "Nights must be a positive integer.");
            return;
        }

        if (!room.isAvailable()) {
            alert(Alert.AlertType.ERROR, "Booking Error",
                    "Room " + room.getRoomNumber() + " is already occupied.");
            return;
        }

        for (Booking b : bookingObsList) {
            if (b.getCustomer().getCustomerId() == customer.getCustomerId()) {
                alert(Alert.AlertType.ERROR, "Booking Error",
                        "Customer \"" + customer.getName() + "\" already has an active booking.");
                return;
            }
        }

        Booking booking = roomService.bookRoom(customer, room.getRoomNumber(), nights);
        if (booking == null) {
            alert(Alert.AlertType.ERROR, "Error", "Could not book the room. Please try again.");
            return;
        }

        customer.setAllocatedRoomNumber(room.getRoomNumber());

        bookingObsList.add(booking);

        fileService.updateBookingStatusInRAF(room.getRoomNumber(), false);

        fileService.serializeRooms(roomService.getRoomsList());
        fileService.serializeCustomers(customerService.getCustomersList());

        refreshAllSiblings();

        statusUpdater.accept("Room " + room.getRoomNumber() + " booked for "
                + customer.getName() + " — "
                + java.time.LocalTime.now().toString().substring(0, 5));
    }

    private void handleCheckout() {
        Booking selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert(Alert.AlertType.ERROR, "Selection Error", "Select a booking to checkout.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Checkout Room " + selected.getRoom().getRoomNumber()
                        + " for " + selected.getCustomer().getName() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Checkout");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.YES) return;

            double discountRate = 0.05;
            try {
                discountRate = Double.parseDouble(tfDiscount.getText().trim());
            } catch (NumberFormatException ignored) {
            }

            Bill bill = billingService.generateBill(selected, discountRate);

            roomService.checkoutRoom(selected.getRoom().getRoomNumber());
            customerService.removeCustomer(selected.getCustomer().getCustomerId());

            bookingObsList.remove(selected);

            fileService.updateBookingStatusInRAF(selected.getRoom().getRoomNumber(), true);

            fileService.serializeRooms(roomService.getRoomsList());
            fileService.serializeCustomers(customerService.getCustomersList());

            fileService.saveBillToFile(bill, () -> {
                if (billingController != null) billingController.loadBillHistory();
            });

            refreshAllSiblings();

            BillStage.show(bill, fileService);

            statusUpdater.accept("Checkout complete — Room "
                    + selected.getRoom().getRoomNumber() + " — "
                    + java.time.LocalTime.now().toString().substring(0, 5));
        });
    }

    private void refreshAllSiblings() {
        populateCombos();
        if (roomController != null) roomController.refreshTable();
        if (customerController != null) customerController.refreshTable();
    }

    public void populateCombos() {
        List<Customer> unbooked = new java.util.ArrayList<>();
        for (Customer c : customerService.getAllCustomers()) {
            boolean alreadyBooked = false;
            for (Booking b : bookingObsList) {
                if (b.getCustomer().getCustomerId() == c.getCustomerId()) {
                    alreadyBooked = true;
                    break;
                }
            }
            if (!alreadyBooked) unbooked.add(c);
        }
        cbCustomer.setItems(FXCollections.observableArrayList(unbooked));

        List<Room> avail = roomService.getAvailableRooms();
        cbRoom.setItems(FXCollections.observableArrayList(avail));
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

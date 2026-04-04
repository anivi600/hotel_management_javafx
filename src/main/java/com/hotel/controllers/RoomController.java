package com.hotel.controllers;

import com.hotel.models.*;
import com.hotel.services.FileService;
import com.hotel.services.RoomService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * FXML controller for {@code room_tab.fxml}.
 */
public class RoomController {

    private final RoomService roomService;
    private final FileService fileService;

    private Consumer<String> statusUpdater = m -> {};

    private BookingController bookingController;

    private ObservableList<Room> roomObservableList;

    @FXML private TextField tfRoomNumber;
    @FXML private ComboBox<String> cbRoomType;
    @FXML private TextField tfBasePrice;
    @FXML private TableView<Room> tableView;
    @FXML private Button btnAdd;
    @FXML private Button btnDelete;
    @FXML private Button btnFilter;
    @FXML private Button btnSortP;
    @FXML private Button btnAll;

    public RoomController(RoomService roomService, FileService fileService) {
        this.roomService = roomService;
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
        tfRoomNumber.setId("roomNumberField");
        cbRoomType.setItems(FXCollections.observableArrayList("Standard", "Deluxe", "Luxury"));
        cbRoomType.setId("roomTypeCombo");
        tfBasePrice.setId("basePriceField");
        btnAdd.setId("addRoomBtn");
        btnDelete.setId("deleteRoomBtn");
        btnFilter.setId("filterRoomsBtn");

        buildTable();

        btnAdd.setOnAction(e -> handleAddRoom());
        btnDelete.setOnAction(e -> handleDeleteRoom());
        btnFilter.setOnAction(e -> loadRooms(roomService.getAvailableRooms()));
        btnAll.setOnAction(e -> refreshTable());
        btnSortP.setOnAction(e -> loadRooms(roomService.getRoomsSortedByPrice()));

        refreshTable();
    }

    @SuppressWarnings("unchecked")
    private void buildTable() {
        tableView.setPlaceholder(new Label("No rooms found."));

        TableColumn<Room, Integer> colNum = new TableColumn<>("Room No");
        TableColumn<Room, String> colType = new TableColumn<>("Type");
        TableColumn<Room, Double> colPrice = new TableColumn<>("Base Price / Night");
        TableColumn<Room, Double> colTariff = new TableColumn<>("Tariff (Calculated)");
        TableColumn<Room, Boolean> colAvail = new TableColumn<>("Available");

        colNum.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colType.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getRoomType().toString()));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("basePrice"));
        colTariff.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(
                c.getValue().calculateTariff()).asObject());
        colAvail.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(
                c.getValue().isAvailable()).asObject());

        colAvail.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("room-avail-yes", "room-avail-no");
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item ? "✅ Yes" : "❌ No");
                getStyleClass().add(item ? "room-avail-yes" : "room-avail-no");
            }
        });

        colNum.setPrefWidth(90);
        colType.setPrefWidth(100);
        colPrice.setPrefWidth(170);
        colTariff.setPrefWidth(170);
        colAvail.setPrefWidth(110);

        tableView.getColumns().addAll(colNum, colType, colPrice, colTariff, colAvail);
        roomObservableList = FXCollections.observableArrayList();
        tableView.setItems(roomObservableList);
    }

    private void handleAddRoom() {
        String numStr = tfRoomNumber.getText().trim();
        String typeStr = cbRoomType.getValue();
        String priceStr = tfBasePrice.getText().trim();

        if (numStr.isEmpty() || typeStr == null || priceStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields are required.");
            return;
        }

        int roomNumber;
        double basePrice;
        try {
            roomNumber = Integer.parseInt(numStr);
            if (roomNumber <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Room number must be a positive integer.");
            return;
        }
        try {
            basePrice = Double.parseDouble(priceStr);
            if (basePrice <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Price must be a positive number.");
            return;
        }

        Room newRoom;
        switch (typeStr) {
            case "Standard":
                newRoom = new StandardRoom(roomNumber, basePrice);
                break;
            case "Deluxe":
                newRoom = new DeluxeRoom(roomNumber, basePrice);
                break;
            case "Luxury":
                newRoom = new LuxuryRoom(roomNumber, basePrice);
                break;
            default:
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid room type.");
                return;
        }

        try {
            roomService.addRoom(newRoom);
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Room", ex.getMessage());
            return;
        }

        fileService.saveToRandomAccessFile(newRoom);
        fileService.serializeRooms(roomService.getRoomsList());

        refreshTable();
        clearForm();
        if (bookingController != null) bookingController.populateCombos();
        statusUpdater.accept("Room " + roomNumber + " added successfully — "
                + java.time.LocalTime.now().toString().substring(0, 5));
    }

    private void handleDeleteRoom() {
        Room selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "Selection Error", "Please select a room to delete.");
            return;
        }
        if (!selected.isAvailable()) {
            showAlert(Alert.AlertType.ERROR, "Delete Error",
                    "Cannot delete Room " + selected.getRoomNumber()
                            + " — it is currently booked.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete Room " + selected.getRoomNumber() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                roomService.removeRoom(selected.getRoomNumber());
                fileService.serializeRooms(roomService.getRoomsList());
                refreshTable();
                if (bookingController != null) bookingController.populateCombos();
                statusUpdater.accept("Room " + selected.getRoomNumber()
                        + " deleted — "
                        + java.time.LocalTime.now().toString().substring(0, 5));
            }
        });
    }

    public void refreshTable() {
        loadRooms(roomService.getAllRooms());
    }

    private void loadRooms(List<Room> rooms) {
        roomObservableList.setAll(rooms);
    }

    private void clearForm() {
        tfRoomNumber.clear();
        cbRoomType.setValue(null);
        tfBasePrice.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

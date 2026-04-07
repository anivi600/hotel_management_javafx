package com.hotel.tabs;

import com.hotel.models.Room;
import com.hotel.services.RoomService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class RoomTab extends Tab {
    private final RoomService roomService;
    private TableView<Room> table;

    public RoomTab(RoomService roomService) {
        this.roomService = roomService;
        setText("Rooms");
        setClosable(false);
        
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label header = new Label("Room Inventory");
        header.getStyleClass().add("form-title");

        table = new TableView<>();
        table.getStyleClass().add("table-view");

        TableColumn<Room, Integer> colId = new TableColumn<>("Room #");
        colId.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colId.setPrefWidth(100);

        TableColumn<Room, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        colType.setPrefWidth(150);

        TableColumn<Room, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("basePrice"));
        colPrice.setPrefWidth(150);

        TableColumn<Room, Boolean> colStatus = new TableColumn<>("Available");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("available"));
        colStatus.setPrefWidth(120);

        table.getColumns().setAll(colId, colType, colPrice, colStatus);
        refreshTable();

        container.getChildren().addAll(header, table);
        setContent(container);
    }

    public void refreshTable() {
        table.setItems(FXCollections.observableArrayList(roomService.getAllRooms()));
    }
}

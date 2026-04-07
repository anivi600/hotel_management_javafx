package com.hotel.tabs;

import com.hotel.services.RoomService;
import com.hotel.services.CustomerService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;

public class DashboardTab extends Tab {
    private final RoomService roomService;
    private final CustomerService customerService;

    public DashboardTab(RoomService roomService, CustomerService customerService) {
        this.roomService = roomService;
        this.customerService = customerService;
        setText("Dashboard");
        setClosable(false);
        updateContent();
    }

    public void updateContent() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(20);
        grid.setVgap(20);

        int totalRooms = roomService.getAllRooms().size();
        int occupiedRooms = roomService.getRoomOccupancy().size();
        int availableRooms = totalRooms - occupiedRooms;
        int totalCustomers = customerService.getCustomersList().size();

        grid.add(createStatCard("Total Rooms", String.valueOf(totalRooms), "#8B3A1E"), 0, 0);
        grid.add(createStatCard("Occupied", String.valueOf(occupiedRooms), "#C0392B"), 1, 0);
        grid.add(createStatCard("Available", String.valueOf(availableRooms), "#1E8449"), 2, 0);
        grid.add(createStatCard("Total Guests", String.valueOf(totalCustomers), "#1A5276"), 3, 0);

        setContent(grid);
    }

    private VBox createStatCard(String title, String value, String colorHex) {
        VBox card = new VBox(10);
        card.getStyleClass().add("form-panel");
        card.setMinWidth(220);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25));

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("sub-heading");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }
}

package com.hotel.tabs;

import com.hotel.models.Room;
import com.hotel.services.RoomService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class RoomMonitorTab extends Tab {
    private final RoomService roomService;
    private final FlowPane flow;

    public RoomMonitorTab(RoomService roomService) {
        this.roomService = roomService;
        this.flow = new FlowPane();
        setText("Room Monitor");
        setClosable(false);

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));

        Label header = new Label("Room Status Visualizer (Live)");
        header.getStyleClass().add("form-title");

        flow.setHgap(15);
        flow.setVgap(15);
        flow.setPadding(new Insets(10));

        ScrollPane scroll = new ScrollPane(flow);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        container.getChildren().addAll(header, scroll);
        setContent(container);

        setOnSelectionChanged(e -> {
            if (isSelected()) refresh();
        });

        refresh();
    }

    public void refresh() {
        flow.getChildren().clear();
        List<Room> rooms = roomService.getAllRooms();
        for (Room r : rooms) {
            VBox box = new VBox(5);
            box.setAlignment(Pos.CENTER);
            box.setPrefSize(90, 90);
            
            boolean isOccupied = !r.isAvailable();
            String color = isOccupied ? "#FADBD8" : "#D5F5E3";
            String borderColor = isOccupied ? "#C0392B" : "#1E8449";

            box.setStyle("-fx-border-color: " + borderColor + "; -fx-border-radius: 8; -fx-background-radius: 8; " +
                         "-fx-background-color: " + color + "; -fx-border-width: 2;");
            
            Label num = new Label(String.valueOf(r.getRoomNumber()));
            num.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #1A0A04;");
            
            Label type = new Label(r.getRoomType().toString());
            type.setStyle("-fx-font-size: 10; -fx-text-fill: #2C1810;");
            
            Label status = new Label(isOccupied ? "OCCUPIED" : "VACANT");
            status.setStyle("-fx-font-size: 9; -fx-font-weight: 700;");

            box.getChildren().addAll(num, type, status);
            flow.getChildren().add(box);
        }
    }
}

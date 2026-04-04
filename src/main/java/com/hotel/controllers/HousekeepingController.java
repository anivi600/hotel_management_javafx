package com.hotel.controllers;

import com.hotel.models.HousekeepingTask;
import com.hotel.models.User;
import com.hotel.services.DatabaseService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HousekeepingController {

    private final DatabaseService databaseService;
    private final User loggedInUser;
    
    private final ObservableList<HousekeepingTask> tasksObsList = FXCollections.observableArrayList();

    @FXML private TextField tfRoomNumber;
    @FXML private TextField tfDescription;
    @FXML private TableView<HousekeepingTask> tableView;
    @FXML private Button btnAddTask;
    @FXML private Button btnMarkComplete;

    public HousekeepingController(DatabaseService databaseService, User loggedInUser) {
        this.databaseService = databaseService;
        this.loggedInUser = loggedInUser;
    }

    @FXML
    void initialize() {
        buildTable();
        loadTasks();

        btnAddTask.setOnAction(e -> handleAddTask());
        btnMarkComplete.setOnAction(e -> handleMarkComplete());
        
        if ("Housekeeping".equals(loggedInUser.getRole())) {
            btnAddTask.setDisable(true);
        }
    }

    private void buildTable() {
        tableView.setPlaceholder(new Label("No housekeeping tasks."));

        TableColumn<HousekeepingTask, Integer> colId = new TableColumn<>("Task ID");
        TableColumn<HousekeepingTask, Integer> colRoom = new TableColumn<>("Room No");
        TableColumn<HousekeepingTask, String> colDesc = new TableColumn<>("Description");
        TableColumn<HousekeepingTask, String> colStatus = new TableColumn<>("Status");
        TableColumn<HousekeepingTask, String> colAssigned = new TableColumn<>("Assigned To");

        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTaskId()).asObject());
        colRoom.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getRoomNumber()).asObject());
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAssigned.setCellValueFactory(new PropertyValueFactory<>("assignedTo"));

        tableView.getColumns().addAll(colId, colRoom, colDesc, colStatus, colAssigned);
        tableView.setItems(tasksObsList);
    }

    private void loadTasks() {
        tasksObsList.clear();
        String sql = "SELECT * FROM housekeeping ORDER BY status DESC"; // Pending first
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                tasksObsList.add(new HousekeepingTask(
                        rs.getInt("taskId"),
                        rs.getInt("roomNumber"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("assignedTo")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleAddTask() {
        String rnStr = tfRoomNumber.getText().trim();
        String desc = tfDescription.getText().trim();
        
        if (rnStr.isEmpty() || desc.isEmpty()) {
            showAlert("Required", "Please fill all fields to add a task.");
            return;
        }
        
        int roomNum;
        try {
            roomNum = Integer.parseInt(rnStr);
        } catch (NumberFormatException e) {
            showAlert("Format Error", "Room Number must be integer.");
            return;
        }

        String sql = "INSERT INTO housekeeping (roomNumber, description, status, assignedTo) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomNum);
            pstmt.setString(2, desc);
            pstmt.setString(3, "Pending");
            pstmt.setString(4, "cleaner"); // In real app, choose from combo box
            pstmt.executeUpdate();
            loadTasks();
            tfRoomNumber.clear();
            tfDescription.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleMarkComplete() {
        HousekeepingTask selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection", "Please select a task to mark complete.");
            return;
        }
        if ("Completed".equals(selected.getStatus())) {
            return;
        }
        
        String sql = "UPDATE housekeeping SET status = 'Completed' WHERE taskId = ?";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selected.getTaskId());
            pstmt.executeUpdate();
            loadTasks();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

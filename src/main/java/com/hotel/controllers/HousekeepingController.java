package com.hotel.controllers;

import com.hotel.dao.HousekeepingDAO;
import com.hotel.models.HousekeepingTask;
import com.hotel.models.User;
import com.hotel.util.UiAlerts;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class HousekeepingController {

    private final HousekeepingDAO housekeepingDAO;
    private final User loggedInUser;

    private final ObservableList<HousekeepingTask> tasksObsList = FXCollections.observableArrayList();

    @FXML private TextField tfRoomNumber;
    @FXML private TextField tfDescription;
    @FXML private TableView<HousekeepingTask> tableView;
    @FXML private Button btnAddTask;
    @FXML private Button btnMarkComplete;

    public HousekeepingController(HousekeepingDAO housekeepingDAO, User loggedInUser) {
        this.housekeepingDAO = housekeepingDAO;
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
        try {
            tasksObsList.addAll(housekeepingDAO.findAllOrderByStatus());
        } catch (SQLException e) {
            UiAlerts.showError("Database Error", e);
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

        try {
            housekeepingDAO.insert(roomNum, desc, "Pending", "cleaner");
            loadTasks();
            tfRoomNumber.clear();
            tfDescription.clear();
        } catch (SQLException e) {
            UiAlerts.showError("Database Error", e);
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

        try {
            housekeepingDAO.updateStatus(selected.getTaskId(), "Completed");
            loadTasks();
        } catch (SQLException e) {
            UiAlerts.showError("Database Error", e);
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

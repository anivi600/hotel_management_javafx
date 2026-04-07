package com.hotel.tabs;

import com.hotel.models.Customer;
import com.hotel.services.CustomerService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class GuestsTab extends Tab {
    private final CustomerService customerService;
    private TableView<Customer> table;

    public GuestsTab(CustomerService customerService) {
        this.customerService = customerService;
        setText("Guests List");
        setClosable(false);

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));

        HBox headerBox = new HBox(20);
        Label header = new Label("All Registered Guests");
        header.getStyleClass().add("form-title");
        Button btnRefresh = new Button("↻ Refresh List");
        btnRefresh.setOnAction(e -> refresh());
        headerBox.getChildren().addAll(header, btnRefresh);

        table = new TableView<>();
        table.getStyleClass().add("table-view");

        TableColumn<Customer, Integer> colId = new TableColumn<>("Guest ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        
        TableColumn<Customer, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Customer, String> colEmail = new TableColumn<>("Contact");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        table.getColumns().setAll(colId, colName, colEmail);
        
        container.getChildren().addAll(headerBox, table);
        setContent(container);
        
        setOnSelectionChanged(e -> {
            if (isSelected()) refresh();
        });

        refresh();
    }

    public void refresh() {
        table.setItems(FXCollections.observableArrayList(customerService.getAllCustomers()));
    }
}

package com.hotel.tabs;

import com.hotel.models.Customer;
import com.hotel.services.CustomerService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class GuestManagementTab extends Tab {
    private final CustomerService customerService;
    private final TextField nameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();

    public GuestManagementTab(CustomerService customerService) {
        this.customerService = customerService;
        setText("Manage Guests");
        setClosable(false);

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));

        Label header = new Label("Register/Edit Guest");
        header.getStyleClass().add("form-title");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.getStyleClass().add("form-panel");

        form.add(new Label("Full Name:"), 0, 0);
        form.add(nameField, 1, 0);

        form.add(new Label("Email:"), 0, 1);
        form.add(emailField, 1, 1);

        form.add(new Label("Phone:"), 0, 2);
        form.add(phoneField, 1, 2);

        Button btnAdd = new Button("Register Guest");
        btnAdd.setId("addCustomerBtn");
        btnAdd.setOnAction(e -> handleAddGuest());
        form.add(btnAdd, 1, 3);

        container.getChildren().addAll(header, form);
        setContent(container);
    }

    private void handleAddGuest() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        if (name.isEmpty() || email.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Name and Email are required.");
            alert.showAndWait();
            return;
        }

        try {
            Customer c = new Customer(name, phone, 0); 
            // Note: phone is not in Customer model but we can add it or ignore for now
            customerService.addCustomer(c);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Guest Registered");
            alert.setContentText("Guest " + name + " has been registered successfully.");
            alert.showAndWait();
            
            nameField.clear();
            emailField.clear();
            phoneField.clear();
        } catch (Exception ex) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setContentText("Failed to register guest: " + ex.getMessage());
            error.showAndWait();
        }
    }
}

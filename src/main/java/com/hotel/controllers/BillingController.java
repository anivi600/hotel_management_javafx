package com.hotel.controllers;

import com.hotel.dao.BillDAO;
import com.hotel.util.UiAlerts;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

/**
 * FXML controller for {@code billing_tab.fxml}.
 */
public class BillingController {

    private final BillDAO billDAO;

    @FXML private VBox billListBox;

    public BillingController(BillDAO billDAO) {
        this.billDAO = billDAO;
    }

    @FXML
    void initialize() {
        loadBillHistory();
    }

    /**
     * Loads all bills from MySQL (and falls back to empty if DB error).
     */
    public void loadBillHistory() {
        if (billListBox == null) return;
        billListBox.getChildren().clear();

        List<String> bills;
        try {
            bills = billDAO.findAllBillTextsNewestFirst();
        } catch (SQLException e) {
            UiAlerts.showError("Database Error", e);
            Label err = new Label("Could not load billing history from the database.");
            err.getStyleClass().add("billing-empty-hint");
            billListBox.getChildren().add(err);
            return;
        }

        if (bills.isEmpty()) {
            Label empty = new Label("No billing records found. Bills appear here after checkout.");
            empty.getStyleClass().add("billing-empty-hint");
            billListBox.getChildren().add(empty);
            return;
        }

        for (String billText : bills) {
            VBox card = new VBox(4);
            card.getStyleClass().add("bill-container");
            card.setPadding(new Insets(12));

            TextArea ta = new TextArea(billText);
            ta.setEditable(false);
            ta.setWrapText(false);
            ta.setPrefRowCount(20);
            ta.getStyleClass().addAll("bill-label", "bill-history-text");

            card.getChildren().add(ta);
            billListBox.getChildren().add(card);
        }
    }
}

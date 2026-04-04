package com.hotel.controllers;

import com.hotel.services.FileService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * FXML controller for {@code billing_tab.fxml}.
 */
public class BillingController {

    private final FileService fileService;

    @FXML private VBox billListBox;

    public BillingController(FileService fileService) {
        this.fileService = fileService;
    }

    @FXML
    void initialize() {
        loadBillHistory();
    }

    /**
     * Reads all bill files from disk and populates the bill list.
     */
    public void loadBillHistory() {
        if (billListBox == null) return;
        billListBox.getChildren().clear();

        List<String> bills = fileService.loadBillHistory();
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

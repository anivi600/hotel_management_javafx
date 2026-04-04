package com.hotel.controllers;

import com.hotel.models.Bill;
import com.hotel.services.FileService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * FXML controller for {@code bill_dialog.fxml} — checkout bill popup.
 */
public class BillDialogController {

    private final Bill bill;
    private final FileService fileService;

    @FXML private TextArea receiptArea;
    @FXML private Button btnSave;
    @FXML private Button btnClose;

    public BillDialogController(Bill bill, FileService fileService) {
        this.bill = bill;
        this.fileService = fileService;
    }

    @FXML
    void initialize() {
        receiptArea.setText(FileService.formatBillText(bill));

        btnSave.setOnAction(e -> {
            fileService.saveBillToFile(bill);
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("Bill saved to: " + fileService.getBillsDirectory());
            a.showAndWait();
        });
        btnClose.setOnAction(e -> {
            Stage st = (Stage) btnClose.getScene().getWindow();
            st.close();
        });
    }
}

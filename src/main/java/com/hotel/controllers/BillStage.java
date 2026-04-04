package com.hotel.controllers;

import com.hotel.models.Bill;
import com.hotel.services.FileService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Opens the checkout bill window loaded from {@code bill_dialog.fxml}.
 */
public final class BillStage {

    private BillStage() {}

    public static void show(Bill bill, FileService fileService) {
        try {
            FXMLLoader loader = new FXMLLoader(BillStage.class.getResource("/com/hotel/bill_dialog.fxml"));
            loader.setControllerFactory(param -> {
                if (param == BillDialogController.class) {
                    return new BillDialogController(bill, fileService);
                }
                try {
                    return param.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Four Square — Bill Receipt");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);

            Scene scene = new Scene(root, 520, 580);
            try {
                scene.getStylesheets().add(
                        BillStage.class.getResource("/com/hotel/styles.css").toExternalForm());
            } catch (Exception ignored) {
            }

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load bill dialog", e);
        }
    }
}

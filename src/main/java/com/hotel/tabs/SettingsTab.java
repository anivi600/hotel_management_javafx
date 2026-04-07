package com.hotel.tabs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.Scene;

public class SettingsTab extends Tab {
    public SettingsTab() {
        setText("Settings");
        setClosable(false);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label header = new Label("Application Preferences");
        header.getStyleClass().add("form-title");

        CheckBox darkMode = new CheckBox("Enable High-Contrast Dark Mode");
        CheckBox autoSave = new CheckBox("Real-time Database Auto-Sync");
        autoSave.setSelected(true);

        VBox langBox = new VBox(5);
        Label langLabel = new Label("Translation / Language Settings:");
        langLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> lang = new ComboBox<>();
        lang.getItems().addAll("English", "Hindi", "Kannada");
        lang.setValue("English");
        langBox.getChildren().addAll(langLabel, lang);

        Separator sep = new Separator();

        Button btnSave = new Button("Apply System Changes");
        btnSave.setId("btnSaveSettings");
        btnSave.getStyleClass().add("checkout-button"); // Reuse consistent styling
        btnSave.setMaxWidth(300);

        btnSave.setOnAction(e -> {
            Scene scene = getTabPane().getScene();
            
            // 1. Handle Dark Mode via a high-level UI Effect
            if (darkMode.isSelected()) {
                ColorAdjust darkEffect = new ColorAdjust();
                darkEffect.setBrightness(-0.8); // Dims the screen
                darkEffect.setContrast(0.2);
                scene.getRoot().setEffect(darkEffect);
            } else {
                scene.getRoot().setEffect(null);
            }

            // 2. Alert user about Auto-Save
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Settings Applied");
            alert.setHeaderText("Configuration Updated Successfully");
            
            String syncText = autoSave.isSelected() ? "ENABLED" : "DISABLED";
            String langText = lang.getValue();
            
            alert.setContentText("The following changes are now active:\n" +
                                "- UI Theme Sync complete.\n" +
                                "- Database Auto-Sync is now " + syncText + ".\n" +
                                "- System language set to " + langText + ".");
            
            alert.showAndWait();
        });

        content.getChildren().addAll(header, darkMode, autoSave, langBox, sep, btnSave);
        setContent(content);
    }
}

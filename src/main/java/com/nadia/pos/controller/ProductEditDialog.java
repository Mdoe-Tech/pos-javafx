package com.nadia.pos.controller;

import com.nadia.pos.model.Product;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;

public class ProductEditDialog extends Dialog<Product> {
    private final TextField nameField = new TextField();
    private final TextField codeField = new TextField();
    private final TextArea descriptionField = new TextArea();
    private final TextField priceField = new TextField();
    private final TextField costPriceField = new TextField();
    private final TextField categoryField = new TextField();
    private final TextField unitField = new TextField();
    private final TextField minimumStockField = new TextField();
    private final TextField barcodeField = new TextField();
    private final Button imageButton = new Button("Choose Image");
    private byte[] selectedImage;

    public ProductEditDialog(Product product) {
        setTitle(product == null ? "Add New Product" : "Edit Product");

        // Create the dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        int row = 0;
        grid.add(new Label("Code:"), 0, row);
        grid.add(codeField, 1, row++);

        grid.add(new Label("Name:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(new Label("Description:"), 0, row);
        grid.add(descriptionField, 1, row++);
        descriptionField.setPrefRowCount(3);

        grid.add(new Label("Price:"), 0, row);
        grid.add(priceField, 1, row++);

        grid.add(new Label("Cost Price:"), 0, row);
        grid.add(costPriceField, 1, row++);

        grid.add(new Label("Category:"), 0, row);
        grid.add(categoryField, 1, row++);

        grid.add(new Label("Unit:"), 0, row);
        grid.add(unitField, 1, row++);

        grid.add(new Label("Minimum Stock:"), 0, row);
        grid.add(minimumStockField, 1, row++);

        grid.add(new Label("Barcode:"), 0, row);
        grid.add(barcodeField, 1, row++);

        grid.add(new Label("Image:"), 0, row);
        grid.add(imageButton, 1, row);

        getDialogPane().setContent(grid);

        // Configure image button
        imageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());
            if (file != null) {
                try {
                    selectedImage = Files.readAllBytes(file.toPath());
                    imageButton.setText(file.getName());
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Failed to load image").showAndWait();
                }
            }
        });

        // Set initial values if editing
        if (product != null) {
            nameField.setText(product.getName());
            codeField.setText(product.getCode());
            descriptionField.setText(product.getDescription());
            priceField.setText(product.getPrice().toString());
            costPriceField.setText(product.getCostPrice().toString());
            categoryField.setText(product.getCategory());
            unitField.setText(product.getUnit());
            minimumStockField.setText(product.getMinimumStock().toString());
            barcodeField.setText(product.getBarcode());
            selectedImage = product.getImage();
            if (selectedImage != null) {
                imageButton.setText("Image Selected");
            }
        }

        // Add buttons
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Convert the result
        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    Product result = new Product();
                    result.setName(nameField.getText());
                    result.setCode(codeField.getText());
                    result.setDescription(descriptionField.getText());
                    result.setPrice(new BigDecimal(priceField.getText()));
                    result.setCostPrice(new BigDecimal(costPriceField.getText()));
                    result.setCategory(categoryField.getText());
                    result.setUnit(unitField.getText());
                    result.setMinimumStock(Integer.parseInt(minimumStockField.getText()));
                    result.setBarcode(barcodeField.getText());
                    result.setImage(selectedImage);

                    // Validate the product
                    result.validate();
                    return result;
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });
    }
}
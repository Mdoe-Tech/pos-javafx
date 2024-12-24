package com.nadia.pos.controller;

import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.Product;
import com.nadia.pos.service.ProductService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class ProductController implements Initializable {
    @FXML
    private TextField searchField;
    @FXML
    private Button addButton;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> codeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, BigDecimal> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, Void> actionsColumn;
    @FXML private TableColumn<Product, String> descriptionColumn;
    @FXML private TableColumn<Product, BigDecimal> costPriceColumn;
    @FXML private TableColumn<Product, String> unitColumn;
    @FXML private TableColumn<Product, Integer> minimumStockColumn;
    @FXML private TableColumn<Product, String> barcodeColumn;

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupSearch();
        setupAddButton();
        loadProducts();
    }

    private void setupTable() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        costPriceColumn.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        minimumStockColumn.setCellValueFactory(new PropertyValueFactory<>("minimumStock"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    openEditDialog(product);
                });

                deleteBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDelete(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 3) {
                productTable.setItems(FXCollections.observableArrayList(
                        productService.searchProducts(newValue)
                ));
            }
        });
    }

    private void loadProducts() {
        productTable.setItems(FXCollections.observableArrayList(
                productService.findAllProducts()
        ));
    }

    private void handleDelete(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Delete " + product.getName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                productService.deleteProduct(product.getId());
                loadProducts();
            }
        });
    }

    private void openEditDialog(Product product) {
        ProductEditDialog dialog = new ProductEditDialog(product);
        dialog.showAndWait().ifPresent(updatedProduct -> {
            try {
                productService.updateProduct(updatedProduct);
                loadProducts();
            } catch (ValidationException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });
    }
    private void setupAddButton() {
        addButton.setOnAction(event -> {
            ProductEditDialog dialog = new ProductEditDialog(null);
            dialog.showAndWait().ifPresent(newProduct -> {
                try {
                    productService.createProduct(newProduct);
                    loadProducts();
                } catch (ValidationException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            });
        });
    }
}

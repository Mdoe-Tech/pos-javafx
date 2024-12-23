package com.nadia.pos.model;

import com.nadia.pos.exceptions.ValidationException;

import java.math.BigDecimal;
import java.util.Arrays;

public class Product extends BaseEntity {
    private String name;
    private String code;
    private String description;
    private BigDecimal price;
    private BigDecimal costPrice;
    private final Integer stockQuantity;
    private String category;
    private String unit;
    private Integer minimumStock;
    private String barcode;
    private byte[] image;

    public Product(Long productId) {
        super();
        this.stockQuantity = 0;
        this.minimumStock = 0;
    }

    @Override
    public void validate() throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Product name cannot be empty");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Product code cannot be empty");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than zero");
        }
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Cost price cannot be negative");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new ValidationException("Stock quantity cannot be negative");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", costPrice=" + costPrice +
                ", stockQuantity=" + stockQuantity +
                ", category='" + category + '\'' +
                ", unit='" + unit + '\'' +
                ", minimumStock=" + minimumStock +
                ", barcode='" + barcode + '\'' +
                ", image=" + Arrays.toString(image) +
                '}';
    }
}
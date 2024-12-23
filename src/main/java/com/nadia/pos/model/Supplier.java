package com.nadia.pos.model;

import com.nadia.pos.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.List;

public class Supplier extends BaseEntity {
    private String name;
    private String code;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String taxId;
    private String bankAccount;
    private String notes;
    private List<Product> products;

    public Supplier() {
        super();
        this.products = new ArrayList<>();
    }

    @Override
    public void validate() throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Supplier name cannot be empty");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Phone number cannot be empty");
        }
        if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format");
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

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", taxId='" + taxId + '\'' +
                ", bankAccount='" + bankAccount + '\'' +
                ", notes='" + notes + '\'' +
                ", products=" + products +
                '}';
    }
}
package com.nadia.pos.controller;

import com.nadia.pos.model.*;
import com.nadia.pos.service.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import java.net.URL;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.List;

public class DashboardController implements Initializable {
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalEmployeesLabel;
    @FXML private Label totalSalesLabel;
    @FXML private Label averageOrderValueLabel;
    @FXML private BarChart<String, Number> salesChart;
    @FXML private LineChart<String, Number> orderTrendsChart;

    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final SalesOrderService salesOrderService;

    public DashboardController(CustomerService customerService,
                               EmployeeService employeeService,
                               SalesOrderService salesOrderService) {
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.salesOrderService = salesOrderService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadStatistics();
        setupSalesChart();
        setupOrderTrendsChart();
    }

    private void loadStatistics() {
        // Load key metrics
        int customerCount = customerService.searchCustomers("").size();
        int employeeCount = employeeService.findAllEmployees().size();
        List<SalesOrder> orders = salesOrderService.findAll();

        BigDecimal totalSales = orders.stream()
                .map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgOrderValue = orders.isEmpty() ? BigDecimal.ZERO :
                totalSales.divide(BigDecimal.valueOf(orders.size()), 2, BigDecimal.ROUND_HALF_UP);

        // Update labels
        totalCustomersLabel.setText(String.valueOf(customerCount));
        totalEmployeesLabel.setText(String.valueOf(employeeCount));
        totalSalesLabel.setText(totalSales.toString());
        averageOrderValueLabel.setText(avgOrderValue.toString());
    }

    private void setupSalesChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Sales");

        // Add last 6 months of sales data
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            BigDecimal monthSales = calculateMonthlySales(month);
            series.getData().add(new XYChart.Data<>(
                    month.getMonth().toString(),
                    monthSales));
        }

        salesChart.getData().add(series);
    }

    private void setupOrderTrendsChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Order Count");

        // Add last 6 months of order counts
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            long orderCount = calculateMonthlyOrderCount(month);
            series.getData().add(new XYChart.Data<>(
                    month.getMonth().toString(),
                    orderCount));
        }

        orderTrendsChart.getData().add(series);
    }

    private BigDecimal calculateMonthlySales(LocalDate month) {
        return salesOrderService.findAll().stream()
                .filter(order -> order.getOrderDate().getMonth() == month.getMonth())
                .map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long calculateMonthlyOrderCount(LocalDate month) {
        return salesOrderService.findAll().stream()
                .filter(order -> order.getOrderDate().getMonth() == month.getMonth())
                .count();
    }
}
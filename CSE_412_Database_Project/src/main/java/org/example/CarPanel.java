package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

public class CarPanel extends JPanel {
    private JTextField vinField, priceField;
    private JComboBox<String> makeDropdown, modelDropdown, yearDropdown, conditionDropdown, statusDropdown, categoryDropdown;
    private JTextArea searchResults;

    public CarPanel() {
        setLayout(new BorderLayout());

        // Main form panel with GridBagLayout for fine-tuned control
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        // Row 1: VIN
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("VIN (17 characters):"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        vinField = new JTextField(17);
        formPanel.add(vinField, gbc);
        gbc.gridwidth = 1;

        // Row 2: Make and Model
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Make:"), gbc);
        gbc.gridx = 1;
        makeDropdown = new JComboBox<>(new String[]{"", "Toyota", "Ford", "Honda", "Chevrolet", "Nissan", "Subaru"});
        formPanel.add(makeDropdown, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Model:"), gbc);
        gbc.gridx = 3;
        modelDropdown = new JComboBox<>();
        formPanel.add(modelDropdown, gbc);

        makeDropdown.addActionListener(e -> updateModelDropdown(makeDropdown.getSelectedItem().toString()));

        // Row 3: Year and Price
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        yearDropdown = new JComboBox<>();
        populateYearDropdown();
        formPanel.add(yearDropdown, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Price (<=):"), gbc);
        gbc.gridx = 3;
        priceField = new JTextField();
        formPanel.add(priceField, gbc);

        // Row 4: Condition and Status
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Condition:"), gbc);
        gbc.gridx = 1;
        conditionDropdown = new JComboBox<>(new String[]{"", "New", "Like New", "Good", "Fair", "Poor", "Broken"});
        formPanel.add(conditionDropdown, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3;
        statusDropdown = new JComboBox<>(new String[]{"", "Available", "Sold"});
        formPanel.add(statusDropdown, gbc);

        // Row 5: Category
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        categoryDropdown = new JComboBox<>(new String[]{"", "SUV", "Sedan", "Truck", "Coupe", "Hatchback"});
        formPanel.add(categoryDropdown, gbc);
        gbc.gridwidth = 1;

        // Button panel for Search, Delete, Insert, Modify, and Help buttons
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.insets = new Insets(5, 5, 5, 5);
        buttonGbc.gridx = 0;
        buttonGbc.gridy = 0;
        buttonGbc.fill = GridBagConstraints.HORIZONTAL;
        buttonGbc.weightx = 1;

        JButton searchButton = new JButton("Search Cars");
        JButton deleteButton = new JButton("Delete Cars");
        JButton insertButton = new JButton("Insert Car");
        JButton modifyButton = new JButton("Modify Car(s)");
        JButton helpButton = new JButton("?");

        buttonPanel.add(searchButton, buttonGbc);
        buttonGbc.gridx++;
        buttonPanel.add(deleteButton, buttonGbc);
        buttonGbc.gridx++;
        buttonPanel.add(insertButton, buttonGbc);
        buttonGbc.gridx++;
        buttonPanel.add(modifyButton, buttonGbc);
        buttonGbc.gridx++;
        buttonPanel.add(helpButton, buttonGbc);

        // Event listeners for the buttons
        searchButton.addActionListener(new SearchCarListener());
        deleteButton.addActionListener(new DeleteCarListener());
        insertButton.addActionListener(new InsertCarListener());
        modifyButton.addActionListener(new ModifyCarListener());
        helpButton.addActionListener(new HelpButtonListener());

        // Search results area with a scroll pane
        searchResults = new JTextArea(10, 50);
        searchResults.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(searchResults);

        // Add components to the main panel
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        updateModelDropdown(makeDropdown.getSelectedItem().toString());
    }

    private void updateModelDropdown(String make) {
        modelDropdown.removeAllItems();
        modelDropdown.addItem(""); // Allow empty selection
        String[] models;
        switch (make) {
            case "Toyota" -> models = new String[]{"Corolla", "Camry", "Highlander", "RAV4", "GR86", "GT86"};
            case "Ford" -> models = new String[]{"Escape", "Fusion", "Mustang", "Focus"};
            case "Honda" -> models = new String[]{"Civic", "Accord", "CR-V", "Pilot"};
            case "Chevrolet" -> models = new String[]{"Malibu", "Impala"};
            case "Nissan" -> models = new String[]{"Altima", "Sentra", "Pathfinder", "Rogue"};
            case "Subaru" -> models = new String[]{"BRZ", "WRX"};
            default -> models = new String[]{};
        }
        for (String model : models) {
            modelDropdown.addItem(model);
        }
    }

    private void populateYearDropdown() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
        yearDropdown.addItem(""); // Allow empty selection
        for (int year = currentYear; year >= 1980; year--) {
            yearDropdown.addItem(String.valueOf(year));
        }
    }

    private class HelpButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextArea helpText = new JTextArea(15, 50);
            helpText.setText("""
                How to Use the Car Panel:
                
                - Insert Car:
                  * Required: Fill in VIN (17 characters), Make, Model, Year, Price, Condition, Status, and Category.
                  * The VIN should be unique for each car.
                
                - Search Cars:
                  * Optional: Fill in any field(s) to search for cars matching those criteria.
                  * Leave all fields empty to view all cars.
                
                - Modify Car(s):
                  * Required: Enter the VIN to identify the car you want to modify.
                  * Optional: Fill in any additional fields to update those attributes for the car.
                  * Note: VIN alone will not trigger a modification; at least one additional field must be filled.
                
                - Delete Cars:
                  * Required: Enter the VIN or any other combination of fields to identify cars to delete.
                
                Tips:
                  - Make sure fields are filled out according to the above guidelines to avoid errors.
                  - Use the dropdown menus where available to avoid typos.
                """);
            helpText.setEditable(false);
            helpText.setLineWrap(true);
            helpText.setWrapStyleWord(true);

            JScrollPane helpScrollPane = new JScrollPane(helpText);
            helpText.setCaretPosition(0); // Start scrollbar at the top
            JOptionPane.showMessageDialog(null, helpScrollPane, "Car Panel Help", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class InsertCarListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isValidInput()) {
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO Car (VIN, make, model, year, price, condition, status, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, vinField.getText());
                stmt.setString(2, (String) makeDropdown.getSelectedItem());
                stmt.setString(3, (String) modelDropdown.getSelectedItem());
                stmt.setInt(4, Integer.parseInt((String) yearDropdown.getSelectedItem()));
                stmt.setDouble(5, Double.parseDouble(priceField.getText()));
                stmt.setString(6, (String) conditionDropdown.getSelectedItem());
                stmt.setString(7, (String) statusDropdown.getSelectedItem());
                stmt.setString(8, (String) categoryDropdown.getSelectedItem());
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(null, "Car inserted successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private boolean isValidInput() {
            String vin = vinField.getText().trim();
            if (vin.length() != 17 || !vin.matches("[A-Za-z0-9]+")) {
                JOptionPane.showMessageDialog(null, "VIN must be exactly 17 alphanumeric characters.");
                return false;
            }

            if (makeDropdown.getSelectedItem() == null || makeDropdown.getSelectedItem().toString().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please select a make.");
                return false;
            }

            if (modelDropdown.getSelectedItem() == null || modelDropdown.getSelectedItem().toString().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please select a model.");
                return false;
            }

            if (yearDropdown.getSelectedItem() == null || yearDropdown.getSelectedItem().toString().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please select a year.");
                return false;
            }

            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price <= 0 || price > 1_000_000) {
                    JOptionPane.showMessageDialog(null, "Price must be a positive number less than 1,000,000.");
                    return false;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Price must be a valid decimal number.");
                return false;
            }

            if (conditionDropdown.getSelectedItem() == null || conditionDropdown.getSelectedItem().toString().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please select a condition.");
                return false;
            }

            if (statusDropdown.getSelectedItem() == null || statusDropdown.getSelectedItem().toString().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please select a status.");
                return false;
            }

            if (categoryDropdown.getSelectedItem() == null || categoryDropdown.getSelectedItem().toString().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please select a category.");
                return false;
            }

            return true;
        }
    }

    private class SearchCarListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            searchResults.setText(""); // Clear previous results

            StringBuilder query = new StringBuilder("SELECT VIN, make, model, year, price, condition, status, category FROM Car WHERE 1=1");

            if (!vinField.getText().trim().isEmpty()) {
                query.append(" AND VIN = ?");
            }
            if (!makeDropdown.getSelectedItem().toString().isEmpty()) {
                query.append(" AND make = ?");
            }
            if (!modelDropdown.getSelectedItem().toString().isEmpty()) {
                query.append(" AND model = ?");
            }
            if (!yearDropdown.getSelectedItem().toString().isEmpty()) {
                query.append(" AND year = ?");
            }
            if (!priceField.getText().trim().isEmpty()) {
                try {
                    Double.parseDouble(priceField.getText().trim());
                    query.append(" AND price <= ?");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Price must be a valid decimal number.");
                    return;
                }
            }
            if (!conditionDropdown.getSelectedItem().toString().isEmpty()) {
                query.append(" AND condition = ?");
            }
            if (!statusDropdown.getSelectedItem().toString().isEmpty()) {
                query.append(" AND status = ?");
            }
            if (!categoryDropdown.getSelectedItem().toString().isEmpty()) {
                query.append(" AND category = ?");
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query.toString())) {

                int paramIndex = 1;
                if (!vinField.getText().trim().isEmpty()) stmt.setString(paramIndex++, vinField.getText().trim());
                if (!makeDropdown.getSelectedItem().toString().isEmpty()) stmt.setString(paramIndex++, makeDropdown.getSelectedItem().toString());
                if (!modelDropdown.getSelectedItem().toString().isEmpty()) stmt.setString(paramIndex++, modelDropdown.getSelectedItem().toString());
                if (!yearDropdown.getSelectedItem().toString().isEmpty()) stmt.setInt(paramIndex++, Integer.parseInt(yearDropdown.getSelectedItem().toString()));
                if (!priceField.getText().trim().isEmpty()) stmt.setDouble(paramIndex++, Double.parseDouble(priceField.getText().trim()));
                if (!conditionDropdown.getSelectedItem().toString().isEmpty()) stmt.setString(paramIndex++, conditionDropdown.getSelectedItem().toString());
                if (!statusDropdown.getSelectedItem().toString().isEmpty()) stmt.setString(paramIndex++, statusDropdown.getSelectedItem().toString());
                if (!categoryDropdown.getSelectedItem().toString().isEmpty()) stmt.setString(paramIndex++, categoryDropdown.getSelectedItem().toString());

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    searchResults.append("VIN: " + rs.getString("VIN") + ", Make: " + rs.getString("make") +
                            ", Model: " + rs.getString("model") + ", Year: " + rs.getInt("year") +
                            ", Price: " + rs.getDouble("price") + ", Condition: " + rs.getString("condition") +
                            ", Status: " + rs.getString("status") + ", Category: " + rs.getString("category") + "\n");
                }

                if (searchResults.getText().isEmpty()) {
                    searchResults.setText("No cars found matching the criteria.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing search.");
            }
        }
    }

    private class DeleteCarListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            StringBuilder previewQuery = new StringBuilder("SELECT VIN, make, model, year, price, condition, status, category FROM Car WHERE 1=1");
            StringBuilder deleteQuery = new StringBuilder("DELETE FROM Car WHERE 1=1");

            if (!vinField.getText().trim().isEmpty()) {
                previewQuery.append(" AND VIN = ?");
                deleteQuery.append(" AND VIN = ?");
            }
            if (!makeDropdown.getSelectedItem().toString().isEmpty()) {
                previewQuery.append(" AND make = ?");
                deleteQuery.append(" AND make = ?");
            }
            if (!modelDropdown.getSelectedItem().toString().isEmpty()) {
                previewQuery.append(" AND model = ?");
                deleteQuery.append(" AND model = ?");
            }
            if (!yearDropdown.getSelectedItem().toString().isEmpty()) {
                previewQuery.append(" AND year = ?");
                deleteQuery.append(" AND year = ?");
            }
            if (!priceField.getText().trim().isEmpty()) {
                try {
                    Double.parseDouble(priceField.getText().trim());
                    previewQuery.append(" AND price <= ?");
                    deleteQuery.append(" AND price <= ?");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Price must be a valid decimal number.");
                    return;
                }
            }
            if (!conditionDropdown.getSelectedItem().toString().isEmpty()) {
                previewQuery.append(" AND condition = ?");
                deleteQuery.append(" AND condition = ?");
            }
            if (!statusDropdown.getSelectedItem().toString().isEmpty()) {
                previewQuery.append(" AND status = ?");
                deleteQuery.append(" AND status = ?");
            }
            if (!categoryDropdown.getSelectedItem().toString().isEmpty()) {
                previewQuery.append(" AND category = ?");
                deleteQuery.append(" AND category = ?");
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement previewStmt = conn.prepareStatement(previewQuery.toString())) {

                int paramIndex = 1;
                if (!vinField.getText().trim().isEmpty()) previewStmt.setString(paramIndex++, vinField.getText().trim());
                if (!makeDropdown.getSelectedItem().toString().isEmpty()) previewStmt.setString(paramIndex++, makeDropdown.getSelectedItem().toString());
                if (!modelDropdown.getSelectedItem().toString().isEmpty()) previewStmt.setString(paramIndex++, modelDropdown.getSelectedItem().toString());
                if (!yearDropdown.getSelectedItem().toString().isEmpty()) previewStmt.setInt(paramIndex++, Integer.parseInt(yearDropdown.getSelectedItem().toString()));
                if (!priceField.getText().trim().isEmpty()) previewStmt.setDouble(paramIndex++, Double.parseDouble(priceField.getText().trim()));
                if (!conditionDropdown.getSelectedItem().toString().isEmpty()) previewStmt.setString(paramIndex++, conditionDropdown.getSelectedItem().toString());
                if (!statusDropdown.getSelectedItem().toString().isEmpty()) previewStmt.setString(paramIndex++, statusDropdown.getSelectedItem().toString());
                if (!categoryDropdown.getSelectedItem().toString().isEmpty()) previewStmt.setString(paramIndex++, categoryDropdown.getSelectedItem().toString());

                ResultSet rs = previewStmt.executeQuery();
                StringBuilder itemsToDelete = new StringBuilder();

                while (rs.next()) {
                    itemsToDelete.append("VIN: ").append(rs.getString("VIN"))
                            .append(", Make: ").append(rs.getString("make"))
                            .append(", Model: ").append(rs.getString("model"))
                            .append(", Year: ").append(rs.getInt("year"))
                            .append(", Price: ").append(rs.getDouble("price"))
                            .append(", Condition: ").append(rs.getString("condition"))
                            .append(", Status: ").append(rs.getString("status"))
                            .append(", Category: ").append(rs.getString("category"))
                            .append("\n");
                }

                if (itemsToDelete.length() == 0) {
                    JOptionPane.showMessageDialog(null, "No cars found matching the criteria.");
                    return;
                }

                JTextArea textArea = new JTextArea(10, 40);
                textArea.setText(itemsToDelete.toString());
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 200));

                int confirm = JOptionPane.showConfirmDialog(
                        null,
                        new Object[]{"Are you sure you want to delete the following items?", scrollPane},
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery.toString())) {
                        paramIndex = 1;
                        if (!vinField.getText().trim().isEmpty()) deleteStmt.setString(paramIndex++, vinField.getText().trim());
                        if (!makeDropdown.getSelectedItem().toString().isEmpty()) deleteStmt.setString(paramIndex++, makeDropdown.getSelectedItem().toString());
                        if (!modelDropdown.getSelectedItem().toString().isEmpty()) deleteStmt.setString(paramIndex++, modelDropdown.getSelectedItem().toString());
                        if (!yearDropdown.getSelectedItem().toString().isEmpty()) deleteStmt.setInt(paramIndex++, Integer.parseInt(yearDropdown.getSelectedItem().toString()));
                        if (!priceField.getText().trim().isEmpty()) deleteStmt.setDouble(paramIndex++, Double.parseDouble(priceField.getText().trim()));
                        if (!conditionDropdown.getSelectedItem().toString().isEmpty()) deleteStmt.setString(paramIndex++, conditionDropdown.getSelectedItem().toString());
                        if (!statusDropdown.getSelectedItem().toString().isEmpty()) deleteStmt.setString(paramIndex++, statusDropdown.getSelectedItem().toString());
                        if (!categoryDropdown.getSelectedItem().toString().isEmpty()) deleteStmt.setString(paramIndex++, categoryDropdown.getSelectedItem().toString());

                        int rowsDeleted = deleteStmt.executeUpdate();
                        JOptionPane.showMessageDialog(null, rowsDeleted + " car(s) deleted successfully.");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing delete.");
            }
        }
    }

    private class ModifyCarListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String vin = vinField.getText().trim();
            String make = (String) makeDropdown.getSelectedItem();
            String model = (String) modelDropdown.getSelectedItem();
            String year = (String) yearDropdown.getSelectedItem();
            String price = priceField.getText().trim();
            String condition = (String) conditionDropdown.getSelectedItem();
            String status = (String) statusDropdown.getSelectedItem();
            String category = (String) categoryDropdown.getSelectedItem();

            // Check if only the VIN is provided
            boolean isOnlyVinProvided = !vin.isEmpty() &&
                    (make.isEmpty() || make.equals("")) &&
                    (model.isEmpty() || model.equals("")) &&
                    (year.isEmpty() || year.equals("")) &&
                    price.isEmpty() &&
                    (condition.isEmpty() || condition.equals("")) &&
                    (status.isEmpty() || status.equals("")) &&
                    (category.isEmpty() || category.equals(""));

            if (isOnlyVinProvided) {
                JOptionPane.showMessageDialog(null, "Please provide at least one additional field for modification.");
                return;
            }

            // Proceed with modification logic if there are other fields besides VIN filled
            StringBuilder query = new StringBuilder("UPDATE Car SET ");
            boolean hasPreviousField = false;

            if (!make.isEmpty()) {
                query.append("make = ?");
                hasPreviousField = true;
            }
            if (!model.isEmpty()) {
                if (hasPreviousField) query.append(", ");
                query.append("model = ?");
                hasPreviousField = true;
            }
            if (!year.isEmpty()) {
                if (hasPreviousField) query.append(", ");
                query.append("year = ?");
                hasPreviousField = true;
            }
            if (!price.isEmpty()) {
                if (hasPreviousField) query.append(", ");
                query.append("price = ?");
                hasPreviousField = true;
            }
            if (!condition.isEmpty()) {
                if (hasPreviousField) query.append(", ");
                query.append("condition = ?");
                hasPreviousField = true;
            }
            if (!status.isEmpty()) {
                if (hasPreviousField) query.append(", ");
                query.append("status = ?");
                hasPreviousField = true;
            }
            if (!category.isEmpty()) {
                if (hasPreviousField) query.append(", ");
                query.append("category = ?");
            }

            query.append(" WHERE VIN = ?");

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query.toString())) {

                int paramIndex = 1;

                if (!make.isEmpty()) stmt.setString(paramIndex++, make);
                if (!model.isEmpty()) stmt.setString(paramIndex++, model);
                if (!year.isEmpty()) stmt.setInt(paramIndex++, Integer.parseInt(year));
                if (!price.isEmpty()) stmt.setDouble(paramIndex++, Double.parseDouble(price));
                if (!condition.isEmpty()) stmt.setString(paramIndex++, condition);
                if (!status.isEmpty()) stmt.setString(paramIndex++, status);
                if (!category.isEmpty()) stmt.setString(paramIndex++, category);

                stmt.setString(paramIndex, vin); // VIN is the last parameter

                int rowsUpdated = stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, rowsUpdated + " car modified successfully.");

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing modification.");
            }
        }
    }
}

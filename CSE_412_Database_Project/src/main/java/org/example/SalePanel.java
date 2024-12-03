package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;

public class SalePanel extends JPanel {
    private JTextField saleIdField, vinField, customerIdField;
    private JComboBox<String> conditionDropdown;
    private JTextArea searchResults;
    private JFormattedTextField dateField;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public SalePanel() {
        setLayout(new BorderLayout());

        // Main form panel with GridBagLayout for fine-tuned control
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        // Row 1: Sale ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Sale ID:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        saleIdField = new JTextField();
        formPanel.add(saleIdField, gbc);
        gbc.gridwidth = 1;

        // Row 2: VIN
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("VIN:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        vinField = new JTextField();
        formPanel.add(vinField, gbc);
        gbc.gridwidth = 1;

        // Row 3: Customer ID
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Customer ID:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        customerIdField = new JTextField();
        formPanel.add(customerIdField, gbc);
        gbc.gridwidth = 1;

        // Row 4: Date
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;

        // Configure the date field
        dateField = new JFormattedTextField(dateFormat);
        dateField.setColumns(10);
        dateField.setFocusLostBehavior(JFormattedTextField.PERSIST); // Prevent reverting to the previous value
        dateField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (dateField.getText().trim().isEmpty()) {
                    dateField.setValue(null); // Clear the value when the field is empty
                }
            }
        });
        formPanel.add(dateField, gbc);
        gbc.gridwidth = 1;

        // Row 5: Condition Dropdown with "" option for search
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Condition:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        conditionDropdown = new JComboBox<>(new String[]{"", "New", "Like New", "Good", "Fair", "Poor"});
        formPanel.add(conditionDropdown, gbc);
        gbc.gridwidth = 1;

        // Button panel for Search, Delete, Insert, Modify, and Help buttons
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.insets = new Insets(5, 5, 5, 5);
        buttonGbc.gridx = 0;
        buttonGbc.gridy = 0;
        buttonGbc.fill = GridBagConstraints.HORIZONTAL;
        buttonGbc.weightx = 1;

        JButton searchButton = new JButton("Search Sales");
        JButton deleteButton = new JButton("Delete Sales");
        JButton insertButton = new JButton("Insert Sale");
        JButton modifyButton = new JButton("Modify Sale");
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
        searchButton.addActionListener(new SearchSaleListener());
        deleteButton.addActionListener(new DeleteSaleListener());
        insertButton.addActionListener(new InsertSaleListener());
        modifyButton.addActionListener(new ModifySaleListener());
        helpButton.addActionListener(new HelpButtonListener());

        // Search results area with a scroll pane
        searchResults = new JTextArea(10, 50);
        searchResults.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(searchResults);

        // Add components to the main panel
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
    }

    private class HelpButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextArea helpText = new JTextArea(15, 50);
            helpText.setText("""
                    How to Use the Sale Panel:
                    
                    - Insert Sale:
                      * Required: VIN (17 characters), Customer ID (positive integer), and Date (format: YYYY-MM-DD).
                      * Condition is automatically set based on the car's current condition.
                      * Do NOT fill in Sale ID, as it will be generated automatically.
                    
                    - Search Sales:
                      * Optional: Fill in any field(s) to search for specific sales records.
                      * Leave all fields empty to view all sales.
                    
                    - Modify Sale:
                      * Required: Sale ID to identify the sale record.
                      * Optional: Provide VIN, Customer ID, Date, or Condition to modify respective fields.
                      * Note: At least one additional field besides Sale ID must be filled to make modifications.
                    
                    - Delete Sales:
                      * Required: Provide Sale ID or a combination of fields to identify records for deletion.
                    
                    Tips:
                      - Ensure VIN is exactly 17 characters for accurate entries.
                      - Use the specified date format (YYYY-MM-DD) for date entries.
                    """);
            helpText.setEditable(false);
            helpText.setLineWrap(true);
            helpText.setWrapStyleWord(true);

            JScrollPane helpScrollPane = new JScrollPane(helpText);
            helpText.setCaretPosition(0); // Start scrollbar at the top
            JOptionPane.showMessageDialog(null, helpScrollPane, "Sale Panel Help", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class InsertSaleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isValidInput()) {
                return;
            }

            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false); // Start transaction

                int customerId = Integer.parseInt(customerIdField.getText().trim());
                String vin = vinField.getText().trim();

                // Check if the customer ID and VIN exist
                if (!customerExists(conn, customerId)) {
                    JOptionPane.showMessageDialog(null, "Customer ID does not exist.");
                    return;
                }
                if (!isCarAvailable(conn, vin)) {
                    JOptionPane.showMessageDialog(null, "VIN does not exist in the Car table or the car is already sold.");
                    return;
                }

                // Retrieve sale price and condition from Car table
                double salePrice;
                String condition;
                String carQuery = "SELECT price, condition FROM Car WHERE vin = ?";
                try (PreparedStatement carStmt = conn.prepareStatement(carQuery)) {
                    carStmt.setString(1, vin);
                    try (ResultSet rs = carStmt.executeQuery()) {
                        if (rs.next()) {
                            salePrice = rs.getDouble("price");
                            condition = rs.getString("condition");
                        } else {
                            JOptionPane.showMessageDialog(null, "No car found with the given VIN.");
                            return;
                        }
                    }
                }

                // Insert sale
                String saleQuery = "INSERT INTO Sale (vin, customer_id, date, sale_price, condition) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement saleStmt = conn.prepareStatement(saleQuery)) {
                    saleStmt.setString(1, vin);
                    saleStmt.setInt(2, customerId);
                    saleStmt.setDate(3, Date.valueOf(dateField.getText().trim()));
                    saleStmt.setDouble(4, salePrice);
                    saleStmt.setString(5, condition);
                    saleStmt.executeUpdate();
                }

                // Update customer's purchase count
                String updateCustomerSql = "UPDATE Customer SET purchases = purchases + 1 WHERE customer_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateCustomerSql)) {
                    updateStmt.setInt(1, customerId);
                    updateStmt.executeUpdate();
                }

                // Set the car's status to "Sold"
                String updateCarStatusSql = "UPDATE Car SET status = 'Sold' WHERE vin = ?";
                try (PreparedStatement updateCarStatusStmt = conn.prepareStatement(updateCarStatusSql)) {
                    updateCarStatusStmt.setString(1, vin);
                    updateCarStatusStmt.executeUpdate();
                }

                conn.commit();
                JOptionPane.showMessageDialog(null, "Sale inserted successfully and car status updated to Sold!");

            } catch (Exception ex) {
                ex.printStackTrace();
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                }
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        private boolean isValidInput() {
            if (!saleIdField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Sale ID should not be specified when inserting a new sale.");
                return false;
            }

            if (!"".equals(conditionDropdown.getSelectedItem())) {
                JOptionPane.showMessageDialog(null, "Condition should not be specified when inserting a new sale. It will be set automatically.");
                return false;
            }

            if (vinField.getText().trim().isEmpty() || vinField.getText().trim().length() != 17) {
                JOptionPane.showMessageDialog(null, "VIN must be exactly 17 characters.");
                return false;
            }

            try {
                int customerId = Integer.parseInt(customerIdField.getText().trim());
                if (customerId <= 0) {
                    JOptionPane.showMessageDialog(null, "Customer ID must be a positive integer.");
                    return false;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Customer ID must be a valid integer.");
                return false;
            }

            try {
                dateFormat.parse(dateField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Date must be in the format YYYY-MM-DD.");
                return false;
            }

            return true;
        }

        private boolean customerExists(Connection conn, int customerId) throws SQLException {
            String query = "SELECT 1 FROM Customer WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, customerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        }

        private boolean isCarAvailable(Connection conn, String vin) throws SQLException {
            String query = "SELECT status FROM Car WHERE vin = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, vin);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() && "Available".equalsIgnoreCase(rs.getString("status"));
                }
            }
        }
    }

    private class SearchSaleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            searchResults.setText("");

            StringBuilder query = new StringBuilder("SELECT sale_id, vin, customer_id, date, sale_price, condition FROM Sale WHERE 1=1");

            if (!saleIdField.getText().trim().isEmpty()) query.append(" AND sale_id = ?");
            if (!vinField.getText().trim().isEmpty()) query.append(" AND vin = ?");
            if (!customerIdField.getText().trim().isEmpty()) query.append(" AND customer_id = ?");
            if (!dateField.getText().trim().isEmpty()) query.append(" AND date = ?");
            if (!"".equals(conditionDropdown.getSelectedItem())) query.append(" AND condition = ?");

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query.toString())) {

                int paramIndex = 1;
                if (!saleIdField.getText().trim().isEmpty()) stmt.setInt(paramIndex++, Integer.parseInt(saleIdField.getText().trim()));
                if (!vinField.getText().trim().isEmpty()) stmt.setString(paramIndex++, vinField.getText().trim());
                if (!customerIdField.getText().trim().isEmpty()) stmt.setInt(paramIndex++, Integer.parseInt(customerIdField.getText().trim()));
                if (!dateField.getText().trim().isEmpty()) stmt.setDate(paramIndex++, Date.valueOf(dateField.getText().trim()));
                if (!"".equals(conditionDropdown.getSelectedItem())) stmt.setString(paramIndex++, (String) conditionDropdown.getSelectedItem());

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    searchResults.append("Sale ID: " + rs.getInt("sale_id") +
                            ", VIN: " + rs.getString("vin") +
                            ", Customer ID: " + rs.getInt("customer_id") +
                            ", Date: " + rs.getDate("date") +
                            ", Sale Price: " + rs.getDouble("sale_price") +
                            ", Condition: " + rs.getString("condition") + "\n");
                }

                if (searchResults.getText().isEmpty()) {
                    searchResults.setText("No sales found matching the criteria.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing search.");
            }
        }
    }

    private class DeleteSaleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            StringBuilder previewQuery = new StringBuilder("SELECT sale_id, vin, customer_id, date, sale_price, condition FROM Sale WHERE 1=1");
            StringBuilder deleteQuery = new StringBuilder("DELETE FROM Sale WHERE 1=1");

            // Add conditions to queries based on filled fields
            if (!saleIdField.getText().trim().isEmpty()) {
                previewQuery.append(" AND sale_id = ?");
                deleteQuery.append(" AND sale_id = ?");
            }
            if (!vinField.getText().trim().isEmpty()) {
                previewQuery.append(" AND vin = ?");
                deleteQuery.append(" AND vin = ?");
            }
            if (!customerIdField.getText().trim().isEmpty()) {
                previewQuery.append(" AND customer_id = ?");
                deleteQuery.append(" AND customer_id = ?");
            }
            if (!dateField.getText().trim().isEmpty()) {
                previewQuery.append(" AND date = ?");
                deleteQuery.append(" AND date = ?");
            }
            if (!"".equals(conditionDropdown.getSelectedItem())) {
                previewQuery.append(" AND condition = ?");
                deleteQuery.append(" AND condition = ?");
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement previewStmt = conn.prepareStatement(previewQuery.toString())) {

                // Set parameters for the preview query
                int paramIndex = 1;
                if (!saleIdField.getText().trim().isEmpty()) previewStmt.setInt(paramIndex++, Integer.parseInt(saleIdField.getText().trim()));
                if (!vinField.getText().trim().isEmpty()) previewStmt.setString(paramIndex++, vinField.getText().trim());
                if (!customerIdField.getText().trim().isEmpty()) previewStmt.setInt(paramIndex++, Integer.parseInt(customerIdField.getText().trim()));
                if (!dateField.getText().trim().isEmpty()) previewStmt.setDate(paramIndex++, Date.valueOf(dateField.getText().trim()));
                if (!"".equals(conditionDropdown.getSelectedItem())) previewStmt.setString(paramIndex++, (String) conditionDropdown.getSelectedItem());

                // Execute the preview query
                ResultSet rs = previewStmt.executeQuery();
                StringBuilder itemsToDelete = new StringBuilder();

                while (rs.next()) {
                    itemsToDelete.append("Sale ID: ").append(rs.getInt("sale_id"))
                            .append(", VIN: ").append(rs.getString("vin"))
                            .append(", Customer ID: ").append(rs.getInt("customer_id"))
                            .append(", Date: ").append(rs.getDate("date"))
                            .append(", Sale Price: ").append(rs.getDouble("sale_price"))
                            .append(", Condition: ").append(rs.getString("condition"))
                            .append("\n");
                }

                if (itemsToDelete.length() == 0) {
                    JOptionPane.showMessageDialog(null, "No sales found matching the criteria.");
                    return;
                }

                // Show confirmation dialog
                JTextArea textArea = new JTextArea(10, 40);
                textArea.setText(itemsToDelete.toString());
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 200));

                int confirm = JOptionPane.showConfirmDialog(
                        null,
                        new Object[]{"Are you sure you want to delete the following sales?", scrollPane},
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery.toString())) {
                        // Set parameters for the delete query
                        paramIndex = 1;
                        if (!saleIdField.getText().trim().isEmpty()) deleteStmt.setInt(paramIndex++, Integer.parseInt(saleIdField.getText().trim()));
                        if (!vinField.getText().trim().isEmpty()) deleteStmt.setString(paramIndex++, vinField.getText().trim());
                        if (!customerIdField.getText().trim().isEmpty()) deleteStmt.setInt(paramIndex++, Integer.parseInt(customerIdField.getText().trim()));
                        if (!dateField.getText().trim().isEmpty()) deleteStmt.setDate(paramIndex++, Date.valueOf(dateField.getText().trim()));
                        if (!"".equals(conditionDropdown.getSelectedItem())) deleteStmt.setString(paramIndex++, (String) conditionDropdown.getSelectedItem());

                        int rowsDeleted = deleteStmt.executeUpdate();
                        JOptionPane.showMessageDialog(null, rowsDeleted + " sale(s) deleted successfully.");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing delete.");
            }
        }
    }

    private class ModifySaleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (saleIdField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Sale ID is required for modification.");
                return;
            }

            boolean hasModifiableField = !vinField.getText().trim().isEmpty() ||
                    !customerIdField.getText().trim().isEmpty() ||
                    !dateField.getText().trim().isEmpty() ||
                    !"".equals(conditionDropdown.getSelectedItem());

            if (!hasModifiableField) {
                JOptionPane.showMessageDialog(null, "Please provide at least one field to modify.");
                return;
            }

            if (!isValidInputForModification()) {
                return;
            }

            StringBuilder query = new StringBuilder("UPDATE Sale SET ");
            boolean hasPreviousField = false;

            if (!vinField.getText().trim().isEmpty()) {
                query.append("vin = ?");
                hasPreviousField = true;
            }
            if (!customerIdField.getText().trim().isEmpty()) {
                if (hasPreviousField) query.append(", ");
                query.append("customer_id = ?");
                hasPreviousField = true;
            }
            if (!dateField.getText().trim().isEmpty()) {
                if (hasPreviousField) query.append(", ");
                query.append("date = ?");
                hasPreviousField = true;
            }
            if (!"".equals(conditionDropdown.getSelectedItem())) {
                if (hasPreviousField) query.append(", ");
                query.append("condition = ?");
            }

            query.append(" WHERE sale_id = ?");

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query.toString())) {

                int paramIndex = 1;
                if (!vinField.getText().trim().isEmpty()) stmt.setString(paramIndex++, vinField.getText().trim());
                if (!customerIdField.getText().trim().isEmpty()) stmt.setInt(paramIndex++, Integer.parseInt(customerIdField.getText().trim()));
                if (!dateField.getText().trim().isEmpty()) stmt.setDate(paramIndex++, Date.valueOf(dateField.getText().trim()));
                if (!"".equals(conditionDropdown.getSelectedItem())) stmt.setString(paramIndex++, (String) conditionDropdown.getSelectedItem());

                stmt.setInt(paramIndex, Integer.parseInt(saleIdField.getText().trim()));

                int rowsUpdated = stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, rowsUpdated + " sale modified successfully.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing modification.");
            }
        }

        private boolean isValidInputForModification() {
            if (!vinField.getText().trim().isEmpty() && vinField.getText().trim().length() != 17) {
                JOptionPane.showMessageDialog(null, "VIN must be exactly 17 characters.");
                return false;
            }

            if (!customerIdField.getText().trim().isEmpty()) {
                try {
                    int customerId = Integer.parseInt(customerIdField.getText().trim());
                    if (customerId <= 0) {
                        JOptionPane.showMessageDialog(null, "Customer ID must be a positive integer.");
                        return false;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Customer ID must be a valid integer.");
                    return false;
                }
            }

            if (!dateField.getText().trim().isEmpty()) {
                try {
                    dateFormat.parse(dateField.getText().trim());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Date must be in the format YYYY-MM-DD.");
                    return false;
                }
            }

            return true;
        }
    }
}
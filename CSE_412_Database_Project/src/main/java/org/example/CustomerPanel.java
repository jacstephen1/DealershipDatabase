package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerPanel extends JPanel {
    private JTextField customerIdField, customerNameField, contactNumberField, purchasesField;
    private JTextArea searchResults;

    public CustomerPanel() {
        setLayout(new BorderLayout());

        // Main form panel with GridBagLayout for fine-tuned control
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        // Row 1: Customer ID (only used for search, delete, and modify, not for insert)
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Customer ID:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        customerIdField = new JTextField();
        formPanel.add(customerIdField, gbc);
        gbc.gridwidth = 1;

        // Row 2: Customer Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        customerNameField = new JTextField();
        formPanel.add(customerNameField, gbc);
        gbc.gridwidth = 1;

        // Row 3: Contact Number
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Contact Number:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        contactNumberField = new JTextField();
        formPanel.add(contactNumberField, gbc);
        gbc.gridwidth = 1;

        // Row 4: Purchases
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Purchases:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        purchasesField = new JTextField();
        formPanel.add(purchasesField, gbc);
        gbc.gridwidth = 1;

        // Button panel for Search, Delete, Insert, Modify, and Help buttons
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.insets = new Insets(5, 5, 5, 5);
        buttonGbc.gridx = 0;
        buttonGbc.gridy = 0;
        buttonGbc.fill = GridBagConstraints.HORIZONTAL;
        buttonGbc.weightx = 1;

        JButton searchButton = new JButton("Search Customers");
        JButton deleteButton = new JButton("Delete Customers");
        JButton insertButton = new JButton("Insert Customer");
        JButton modifyButton = new JButton("Modify Customer");
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
        searchButton.addActionListener(new SearchCustomerListener());
        deleteButton.addActionListener(new DeleteCustomerListener());
        insertButton.addActionListener(new InsertCustomerListener());
        modifyButton.addActionListener(new ModifyCustomerListener());
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
                    How to Use the Customer Panel:
                    
                    - Insert Customer:
                      * Required: Fill in Customer Name, Contact Number (10-digit), and Purchases (positive integer).
                      * Do NOT fill in Customer ID, as it will be generated automatically.
                    
                    - Search Customers:
                      * Optional: Fill in any field(s) to search for customers matching those criteria.
                      * Leave all fields empty to view all customers.
                    
                    - Modify Customer:
                      * Required: Enter Customer ID to identify the customer for modification.
                      * Optional: Fill in any additional fields you want to update.
                      * Note: Customer ID alone will not trigger a modification; at least one additional field must be filled.
                    
                    - Delete Customers:
                      * Required: Enter Customer ID or any other combination of fields to identify customers to delete.
                    
                    Tips:
                      - Use proper input format for each field (e.g., 10-digit contact number).
                      - Ensure that fields are filled correctly based on the selected action.
                    """);
            helpText.setEditable(false);
            helpText.setLineWrap(true);
            helpText.setWrapStyleWord(true);

            JScrollPane helpScrollPane = new JScrollPane(helpText);
            helpText.setCaretPosition(0); // Start scrollbar at the top
            JOptionPane.showMessageDialog(null, helpScrollPane, "Customer Panel Help", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class InsertCustomerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isValidInput()) {
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO Customer (customer_name, contact_number, purchases) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, customerNameField.getText().trim());
                stmt.setString(2, contactNumberField.getText().trim());
                stmt.setInt(3, Integer.parseInt(purchasesField.getText().trim()));
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(null, "Customer inserted successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private boolean isValidInput() {
            // Ensure customer_id field is empty for insert
            if (!customerIdField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Customer ID should not be specified when inserting a new customer.");
                return false;
            }

            // Validate customer name
            if (customerNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Customer Name cannot be empty.");
                return false;
            }

            // Validate contact number (should be numeric and a specific length, e.g., 10 digits)
            String contactNumber = contactNumberField.getText().trim();
            if (!contactNumber.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(null, "Contact Number must be a 10-digit numeric value.");
                return false;
            }

            // Validate purchases (should be a positive integer)
            try {
                int purchases = Integer.parseInt(purchasesField.getText().trim());
                if (purchases < 0) {
                    JOptionPane.showMessageDialog(null, "Purchases must be a positive integer.");
                    return false;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Purchases must be a valid integer.");
                return false;
            }

            return true;
        }
    }

    private class SearchCustomerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            searchResults.setText(""); // Clear previous results

            StringBuilder query = new StringBuilder("SELECT customer_id, customer_name, contact_number, purchases FROM Customer WHERE 1=1");

            if (!customerIdField.getText().trim().isEmpty()) {
                query.append(" AND customer_id = ?");
            }
            if (!customerNameField.getText().trim().isEmpty()) {
                query.append(" AND customer_name LIKE ?");
            }
            if (!contactNumberField.getText().trim().isEmpty()) {
                query.append(" AND contact_number = ?");
            }
            if (!purchasesField.getText().trim().isEmpty()) {
                query.append(" AND purchases = ?");
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query.toString())) {

                int paramIndex = 1;
                if (!customerIdField.getText().trim().isEmpty()) stmt.setInt(paramIndex++, Integer.parseInt(customerIdField.getText().trim()));
                if (!customerNameField.getText().trim().isEmpty()) stmt.setString(paramIndex++, "%" + customerNameField.getText().trim() + "%");
                if (!contactNumberField.getText().trim().isEmpty()) stmt.setString(paramIndex++, contactNumberField.getText().trim());
                if (!purchasesField.getText().trim().isEmpty()) stmt.setInt(paramIndex++, Integer.parseInt(purchasesField.getText().trim()));

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    searchResults.append("Customer ID: " + rs.getInt("customer_id") +
                            ", Name: " + rs.getString("customer_name") +
                            ", Contact Number: " + rs.getString("contact_number") +
                            ", Purchases: " + rs.getInt("purchases") + "\n");
                }

                if (searchResults.getText().isEmpty()) {
                    searchResults.setText("No customers found matching the criteria.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing search.");
            }
        }
    }

    private class DeleteCustomerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            StringBuilder previewQuery = new StringBuilder("SELECT customer_id, customer_name, contact_number, purchases FROM Customer WHERE 1=1");
            StringBuilder deleteQuery = new StringBuilder("DELETE FROM Customer WHERE 1=1");

            if (!customerIdField.getText().trim().isEmpty()) {
                previewQuery.append(" AND customer_id = ?");
                deleteQuery.append(" AND customer_id = ?");
            }
            if (!customerNameField.getText().trim().isEmpty()) {
                previewQuery.append(" AND customer_name LIKE ?");
                deleteQuery.append(" AND customer_name LIKE ?");
            }
            if (!contactNumberField.getText().trim().isEmpty()) {
                previewQuery.append(" AND contact_number = ?");
                deleteQuery.append(" AND contact_number = ?");
            }
            if (!purchasesField.getText().trim().isEmpty()) {
                previewQuery.append(" AND purchases = ?");
                deleteQuery.append(" AND purchases = ?");
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement previewStmt = conn.prepareStatement(previewQuery.toString())) {

                int paramIndex = 1;
                if (!customerIdField.getText().trim().isEmpty()) previewStmt.setInt(paramIndex++, Integer.parseInt(customerIdField.getText().trim()));
                if (!customerNameField.getText().trim().isEmpty()) previewStmt.setString(paramIndex++, "%" + customerNameField.getText().trim() + "%");
                if (!contactNumberField.getText().trim().isEmpty()) previewStmt.setString(paramIndex++, contactNumberField.getText().trim());
                if (!purchasesField.getText().trim().isEmpty()) previewStmt.setInt(paramIndex++, Integer.parseInt(purchasesField.getText().trim()));

                ResultSet rs = previewStmt.executeQuery();
                StringBuilder itemsToDelete = new StringBuilder();

                while (rs.next()) {
                    itemsToDelete.append("Customer ID: ").append(rs.getInt("customer_id"))
                            .append(", Name: ").append(rs.getString("customer_name"))
                            .append(", Contact Number: ").append(rs.getString("contact_number"))
                            .append(", Purchases: ").append(rs.getInt("purchases"))
                            .append("\n");
                }

                if (itemsToDelete.length() == 0) {
                    JOptionPane.showMessageDialog(null, "No customers found matching the criteria.");
                    return;
                }

                JTextArea textArea = new JTextArea(10, 40);
                textArea.setText(itemsToDelete.toString());
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 200));

                int confirm = JOptionPane.showConfirmDialog(
                        null,
                        new Object[]{"Are you sure you want to delete the following customers?", scrollPane},
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery.toString())) {
                        paramIndex = 1;
                        if (!customerIdField.getText().trim().isEmpty()) deleteStmt.setInt(paramIndex++, Integer.parseInt(customerIdField.getText().trim()));
                        if (!customerNameField.getText().trim().isEmpty()) deleteStmt.setString(paramIndex++, "%" + customerNameField.getText().trim() + "%");
                        if (!contactNumberField.getText().trim().isEmpty()) deleteStmt.setString(paramIndex++, contactNumberField.getText().trim());
                        if (!purchasesField.getText().trim().isEmpty()) deleteStmt.setInt(paramIndex++, Integer.parseInt(purchasesField.getText().trim()));

                        int rowsDeleted = deleteStmt.executeUpdate();
                        JOptionPane.showMessageDialog(null, rowsDeleted + " customer(s) deleted successfully.");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing delete.");
            }
        }
    }

    private class ModifyCustomerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (customerIdField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Customer ID is required for modification.");
                return;
            }

            // Ensure at least one other field is filled
            boolean hasModifiableField = !customerNameField.getText().trim().isEmpty() ||
                    !contactNumberField.getText().trim().isEmpty() ||
                    !purchasesField.getText().trim().isEmpty();

            if (!hasModifiableField) {
                JOptionPane.showMessageDialog(null, "Please provide at least one field to modify.");
                return;
            }

            // Validate input fields
            if (!isValidInputForModification()) {
                return;
            }

            StringBuilder query = new StringBuilder("UPDATE Customer SET ");
            boolean hasPreviousField = false;

            if (!customerNameField.getText().trim().isEmpty()) {
                query.append("customer_name = ?");
                hasPreviousField = true;
            }
            if (!contactNumberField.getText().trim().isEmpty()) {
                if (hasPreviousField) query.append(", ");
                query.append("contact_number = ?");
                hasPreviousField = true;
            }
            if (!purchasesField.getText().trim().isEmpty()) {
                if (hasPreviousField) query.append(", ");
                query.append("purchases = ?");
            }

            query.append(" WHERE customer_id = ?");

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query.toString())) {

                int paramIndex = 1;

                if (!customerNameField.getText().trim().isEmpty()) {
                    stmt.setString(paramIndex++, customerNameField.getText().trim());
                }
                if (!contactNumberField.getText().trim().isEmpty()) {
                    stmt.setString(paramIndex++, contactNumberField.getText().trim());
                }
                if (!purchasesField.getText().trim().isEmpty()) {
                    stmt.setInt(paramIndex++, Integer.parseInt(purchasesField.getText().trim()));
                }

                stmt.setInt(paramIndex, Integer.parseInt(customerIdField.getText().trim()));

                int rowsUpdated = stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, rowsUpdated + " customer modified successfully.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing modification.");
            }
        }

        private boolean isValidInputForModification() {
            // Validate customer name (if provided)
            if (!customerNameField.getText().trim().isEmpty() && customerNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Customer Name cannot be empty.");
                return false;
            }

            // Validate contact number (if provided)
            String contactNumber = contactNumberField.getText().trim();
            if (!contactNumber.isEmpty() && !contactNumber.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(null, "Contact Number must be a 10-digit numeric value.");
                return false;
            }

            // Validate purchases (if provided)
            if (!purchasesField.getText().trim().isEmpty()) {
                try {
                    int purchases = Integer.parseInt(purchasesField.getText().trim());
                    if (purchases < 0) {
                        JOptionPane.showMessageDialog(null, "Purchases must be a positive integer.");
                        return false;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Purchases must be a valid integer.");
                    return false;
                }
            }
            return true;
        }
    }
}
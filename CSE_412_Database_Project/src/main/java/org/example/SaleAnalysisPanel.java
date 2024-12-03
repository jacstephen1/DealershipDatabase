package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class SaleAnalysisPanel extends JPanel {
    private JTextField analysisIdField, periodStartField, periodEndField;
    private JTextArea searchResults;

    public SaleAnalysisPanel() {
        setLayout(new BorderLayout());

        // Main form panel with GridBagLayout for fine-tuned control
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        // Row 1: Analysis ID (only used for search and delete, not for insert)
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Analysis ID:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        analysisIdField = new JTextField();
        formPanel.add(analysisIdField, gbc);
        gbc.gridwidth = 1;

        // Row 2: Period Start
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Period Start (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        periodStartField = new JTextField();
        formPanel.add(periodStartField, gbc);
        gbc.gridwidth = 1;

        // Row 3: Period End
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Period End (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        periodEndField = new JTextField();
        formPanel.add(periodEndField, gbc);
        gbc.gridwidth = 1;

        // Button panel for Search, Delete, Insert, and Help buttons
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.insets = new Insets(5, 5, 5, 5);
        buttonGbc.gridx = 0;
        buttonGbc.gridy = 0;
        buttonGbc.fill = GridBagConstraints.HORIZONTAL;
        buttonGbc.weightx = 1;

        JButton searchButton = new JButton("Search Analysis");
        JButton deleteButton = new JButton("Delete Analysis");
        JButton insertButton = new JButton("Insert Analysis");
        JButton helpButton = new JButton("?");
        buttonPanel.add(searchButton, buttonGbc);
        buttonGbc.gridx++;
        buttonPanel.add(deleteButton, buttonGbc);
        buttonGbc.gridx++;
        buttonPanel.add(insertButton, buttonGbc);
        buttonGbc.gridx++;
        buttonPanel.add(helpButton, buttonGbc);

        // Event listeners for the buttons
        searchButton.addActionListener(new SearchAnalysisListener());
        deleteButton.addActionListener(new DeleteAnalysisListener());
        insertButton.addActionListener(new InsertAnalysisListener());
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
                    How to Use the Sale Analysis Panel:
                    
                    - Insert Analysis:
                      * Required: Period Start and Period End in YYYY-MM-DD format.
                      * Analysis ID will be auto-generated.
                      * Average sale price and condition, as well as the number of sales within the specified period, will be calculated and stored.
                    
                    - Search Analysis:
                      * Optional: Fill in any field(s) to search for specific analysis records.
                      * Leave all fields empty to view all analysis entries.
                    
                    - Delete Analysis:
                      * Required: Provide Analysis ID or specify Period Start and/or Period End to identify records for deletion.
                      
                    - NOTE: There is no modify button as this information is calculated based on other information in the database.
                    
                    Tips:
                      - Use the YYYY-MM-DD format for all date fields.
                    """);
            helpText.setEditable(false);
            helpText.setLineWrap(true);
            helpText.setWrapStyleWord(true);

            JScrollPane helpScrollPane = new JScrollPane(helpText);
            helpText.setCaretPosition(0); // Start scrollbar at the top
            JOptionPane.showMessageDialog(null, helpScrollPane, "Sale Analysis Panel Help", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class InsertAnalysisListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isValidInput()) {
                return;
            }

            String periodStart = periodStartField.getText().trim();
            String periodEnd = periodEndField.getText().trim();

            String insertQuery = """
            INSERT INTO Sale_Analysis (period_start, period_end, average_sale_price, average_sale_condition, num_of_sales)
            SELECT 
                ?, 
                ?, 
                AVG(sale_price) AS average_sale_price,
                AVG(condition_score) AS average_sale_condition,
                COUNT(*) AS num_of_sales
            FROM (
                SELECT 
                    sale_price,
                    CASE
                        WHEN condition = 'New' THEN 5
                        WHEN condition = 'Like New' THEN 4
                        WHEN condition = 'Good' THEN 3
                        WHEN condition = 'Fair' THEN 2
                        WHEN condition = 'Poor' THEN 1
                        ELSE 0
                    END AS condition_score
                FROM Sale
                WHERE date BETWEEN ? AND ?
            ) AS scored_sales;
        """;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

                // Set parameters for period start and end dates
                stmt.setDate(1, Date.valueOf(periodStart));
                stmt.setDate(2, Date.valueOf(periodEnd));
                stmt.setDate(3, Date.valueOf(periodStart));
                stmt.setDate(4, Date.valueOf(periodEnd));

                int rowsInserted = stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, rowsInserted + " analysis entry added successfully.");

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing insert analysis.");
            } catch (IllegalArgumentException exx) {
                JOptionPane.showMessageDialog(null, "Please enter a valid Period Start and a Period End.");
            }
        }

        private boolean isValidInput() {
            // Ensure sale_id field is empty for insert
            if (!analysisIdField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Analysis ID should not be specified when creating a new analysis.");
                return false;
            }
            // Validate period start and end dates
            if (periodStartField.getText().trim().isEmpty() || periodEndField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Period start and end dates are required.");
                return false;
            }
            try {
                Date.valueOf(periodStartField.getText().trim());
                Date.valueOf(periodEndField.getText().trim());
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, "Invalid date format. Use YYYY-MM-DD.");
                return false;
            }
            return true;
        }
    }

    private class SearchAnalysisListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            searchResults.setText(""); // Clear previous results
            boolean error = false; //Checks if there is period end and period start filled/unfilled

            StringBuilder query = new StringBuilder("SELECT analysis_id, period_start, period_end, average_sale_price, average_sale_condition, num_of_sales FROM Sale_Analysis WHERE 1=1");

            if (!analysisIdField.getText().trim().isEmpty()) {
                query.append(" AND analysis_id = ?");
            }

            if(!periodStartField.getText().trim().isEmpty() && !periodEndField.getText().trim().isEmpty()) {
                query.append(" AND period_start >= ?");
                query.append(" AND period_end <= ?");
            } else if(periodStartField.getText().trim().isEmpty() && periodEndField.getText().trim().isEmpty()) {

            } else {
                searchResults.setText("Please enter a Period Start and a Period End.");
                error = true;
            }

            if(!error) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query.toString())) {

                    int paramIndex = 1;
                    if (!analysisIdField.getText().trim().isEmpty())
                        stmt.setInt(paramIndex++, Integer.parseInt(analysisIdField.getText().trim()));
                    if (!periodStartField.getText().trim().isEmpty())
                        stmt.setDate(paramIndex++, Date.valueOf(periodStartField.getText().trim()));
                    if (!periodEndField.getText().trim().isEmpty())
                        stmt.setDate(paramIndex++, Date.valueOf(periodEndField.getText().trim()));

                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        searchResults.append("Analysis ID: " + rs.getInt("analysis_id") +
                                ", Period Start: " + rs.getDate("period_start") +
                                ", Period End: " + rs.getDate("period_end") +
                                ", Average Sale Price: " + rs.getDouble("average_sale_price") +
                                ", Average Sale Condition: " + rs.getDouble("average_sale_condition") +
                                ", Number of Sales: " + rs.getInt("num_of_sales") + "\n");
                    }

                    if (searchResults.getText().isEmpty()) {
                        searchResults.setText("No analysis records found matching the criteria.");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Error executing search analysis.");
                } catch (IllegalArgumentException exx) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid Period Start and a Period End.");
                }
            }
        }
    }

    private class DeleteAnalysisListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            StringBuilder previewQuery = new StringBuilder("SELECT analysis_id, period_start, period_end, average_sale_price, average_sale_condition, num_of_sales FROM Sale_Analysis WHERE 1=1");
            StringBuilder deleteQuery = new StringBuilder("DELETE FROM Sale_Analysis WHERE 1=1");

            if (!analysisIdField.getText().trim().isEmpty()) {
                previewQuery.append(" AND analysis_id = ?");
                deleteQuery.append(" AND analysis_id = ?");
            }
            if (!periodStartField.getText().trim().isEmpty()) {
                previewQuery.append(" AND period_start >= ?");
                deleteQuery.append(" AND period_start >= ?");
            }
            if (!periodEndField.getText().trim().isEmpty()) {
                previewQuery.append(" AND period_end <= ?");
                deleteQuery.append(" AND period_end <= ?");
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement previewStmt = conn.prepareStatement(previewQuery.toString())) {

                int paramIndex = 1;
                if (!analysisIdField.getText().trim().isEmpty()) previewStmt.setInt(paramIndex++, Integer.parseInt(analysisIdField.getText().trim()));
                if (!periodStartField.getText().trim().isEmpty()) previewStmt.setDate(paramIndex++, Date.valueOf(periodStartField.getText().trim()));
                if (!periodEndField.getText().trim().isEmpty()) previewStmt.setDate(paramIndex++, Date.valueOf(periodEndField.getText().trim()));

                ResultSet rs = previewStmt.executeQuery();
                StringBuilder itemsToDelete = new StringBuilder();

                while (rs.next()) {
                    itemsToDelete.append("Analysis ID: ").append(rs.getInt("analysis_id"))
                            .append(", Period Start: ").append(rs.getDate("period_start"))
                            .append(", Period End: ").append(rs.getDate("period_end"))
                            .append(", Average Sale Price: ").append(rs.getDouble("average_sale_price"))
                            .append(", Average Sale Condition: ").append(rs.getDouble("average_sale_condition"))
                            .append(", Number of Sales: ").append(rs.getInt("num_of_sales"))
                            .append("\n");
                }

                if (itemsToDelete.length() == 0) {
                    JOptionPane.showMessageDialog(null, "No analysis records found matching the criteria.");
                    return;
                }

                JTextArea textArea = new JTextArea(10, 40);
                textArea.setText(itemsToDelete.toString());
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 200));

                int confirm = JOptionPane.showConfirmDialog(
                        null,
                        new Object[]{"Are you sure you want to delete the following analysis records?", scrollPane},
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery.toString())) {
                        paramIndex = 1;
                        if (!analysisIdField.getText().trim().isEmpty()) deleteStmt.setInt(paramIndex++, Integer.parseInt(analysisIdField.getText().trim()));
                        if (!periodStartField.getText().trim().isEmpty()) deleteStmt.setDate(paramIndex++, Date.valueOf(periodStartField.getText().trim()));
                        if (!periodEndField.getText().trim().isEmpty()) deleteStmt.setDate(paramIndex++, Date.valueOf(periodEndField.getText().trim()));

                        int rowsDeleted = deleteStmt.executeUpdate();
                        JOptionPane.showMessageDialog(null, rowsDeleted + " analysis record(s) deleted successfully.");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error executing delete analysis.");
            } catch (IllegalArgumentException exx) {
                JOptionPane.showMessageDialog(null, "Please enter a valid Period Start and a Period End.");
            }
        }
    }
}
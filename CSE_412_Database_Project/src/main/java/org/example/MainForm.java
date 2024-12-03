package org.example;
import javax.swing.*;

public class MainForm extends JFrame {
    private JTabbedPane tabbedPane;
    private JPanel panelMain;

    public MainForm() {
        setTitle("Database GUI");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Car", new CarPanel());
        tabbedPane.addTab("Customer", new CustomerPanel());
        tabbedPane.addTab("Sale", new SalePanel());
        tabbedPane.addTab("Sale Analysis", new SaleAnalysisPanel());

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainForm frame = new MainForm();
            frame.setVisible(true);
        });
    }
}
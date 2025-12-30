package gui;

import database.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class FIRForm extends JFrame {
    private JTextField nameField;
    private JComboBox<String> crimeBox;
    private JTextArea descArea;

    public FIRForm() {
        setTitle("ACP System - Register New FIR");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.add(new JLabel("Accused Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Crime Type:"));
        String[] crimes = { "Theft", "Assault", "Fraud", "Other" };
        crimeBox = new JComboBox<>(crimes);
        formPanel.add(crimeBox);

        formPanel.add(new JLabel("Description:"));
        descArea = new JTextArea(4, 20);
        formPanel.add(new JScrollPane(descArea));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveBtn = new JButton("Save FIR Record");
        JButton viewBtn = new JButton("View All Records");
        buttonPanel.add(saveBtn);
        buttonPanel.add(viewBtn);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        saveBtn.addActionListener(e -> saveToDatabase());
        viewBtn.addActionListener(e -> new ViewRecordsFrame());

        setVisible(true);
    }

    private void saveToDatabase() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO fir_records (accused_name, crime_type, description) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, crimeBox.getSelectedItem().toString());
            pstmt.setString(3, descArea.getText());
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "FIR Saved!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
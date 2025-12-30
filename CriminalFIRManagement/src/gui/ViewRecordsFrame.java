package gui;

import database.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.sql.*;

public class ViewRecordsFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    // Labels for the Dashboard
    private JLabel lblTotal, lblSolved, lblUnsolved;

    public ViewRecordsFrame() {
        setTitle("ACP System - Case Management & Dashboard");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- 1. DASHBOARD PANEL (Top Statistics) ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Case Statistics"));

        lblTotal = new JLabel("Total Reports: 0", SwingConstants.CENTER);
        lblSolved = new JLabel("Solved: 0", SwingConstants.CENTER);
        lblUnsolved = new JLabel("Under Investigation: 0", SwingConstants.CENTER);

        // Colors for stats
        lblSolved.setForeground(new Color(0, 153, 76));
        lblUnsolved.setForeground(new Color(255, 153, 0));

        statsPanel.add(lblTotal);
        statsPanel.add(lblSolved);
        statsPanel.add(lblUnsolved);

        // --- 2. SEARCH & REPORT PANEL ---
        JPanel topActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topActionPanel.add(new JLabel("Search Name: "));
        searchField = new JTextField(15);
        JButton searchBtn = new JButton("Search");
        JButton reportBtn = new JButton("Generate Report");

        reportBtn.setBackground(new Color(70, 70, 70));
        reportBtn.setForeground(Color.WHITE);

        topActionPanel.add(searchField);
        topActionPanel.add(searchBtn);
        topActionPanel.add(reportBtn);

        // Combine Stats and Search into one North container
        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.add(statsPanel, BorderLayout.NORTH);
        northContainer.add(topActionPanel, BorderLayout.SOUTH);

        // --- 3. TABLE SETUP ---
        String[] columns = { "FIR ID", "Accused Name", "Crime Type", "Description", "Status" };
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);

        // Load data initially
        loadData("");

        // --- 4. BOTTOM ACTION PANEL ---
        JPanel bottomPanel = new JPanel();
        JButton investigationBtn = new JButton("Set: Investigation");
        investigationBtn.setBackground(new Color(255, 153, 0));
        investigationBtn.setForeground(Color.WHITE);

        JButton solvedBtn = new JButton("Set: Solved");
        solvedBtn.setBackground(new Color(0, 153, 76));
        solvedBtn.setForeground(Color.WHITE);

        JButton deleteBtn = new JButton("Delete Solved Case");
        deleteBtn.setBackground(new Color(204, 0, 0));
        deleteBtn.setForeground(Color.WHITE);

        bottomPanel.add(investigationBtn);
        bottomPanel.add(solvedBtn);
        bottomPanel.add(new JLabel(" | "));
        bottomPanel.add(deleteBtn);

        // --- ADD TO FRAME ---
        add(northContainer, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- LISTENERS ---
        searchBtn.addActionListener(e -> loadData(searchField.getText()));
        reportBtn.addActionListener(e -> exportToText());
        investigationBtn.addActionListener(e -> updateStatus("Under Investigation"));
        solvedBtn.addActionListener(e -> updateStatus("Solved"));
        deleteBtn.addActionListener(e -> deleteSolvedOnly());

        setVisible(true);
    }

    private void loadData(String searchText) {
        model.setRowCount(0);
        int total = 0, solved = 0, unsolved = 0;

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM fir_records WHERE accused_name LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + searchText + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String status = rs.getString("status");
                model.addRow(new Object[] {
                        rs.getInt("fir_id"), rs.getString("accused_name"),
                        rs.getString("crime_type"), rs.getString("description"),
                        status
                });

                // Update Counters
                total++;
                if ("Solved".equalsIgnoreCase(status))
                    solved++;
                else
                    unsolved++;
            }

            // Update Dashboard Labels
            lblTotal.setText("Total Reports: " + total);
            lblSolved.setText("Solved: " + solved);
            lblUnsolved.setText("Under Investigation: " + unsolved);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row == -1)
            return;
        int id = (int) model.getValueAt(row, 0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE fir_records SET status = ? WHERE fir_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            loadData(searchField.getText()); //  
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportToText() {
        try (FileWriter writer = new FileWriter("FIR_Report.txt")) {
            writer.write("--- ACP CRIME REPORT ---\n");
            writer.write("Generated: " + new java.util.Date() + "\n");
            writer.write(lblTotal.getText() + " | " + lblSolved.getText() + "\n\n");

            for (int i = 0; i < model.getRowCount(); i++) {
                writer.write(model.getValueAt(i, 0) + " | " + model.getValueAt(i, 1) +
                        " | " + model.getValueAt(i, 4) + "\n");
            }
            JOptionPane.showMessageDialog(this, "Report saved as FIR_Report.txt");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteSolvedOnly() {
        int row = table.getSelectedRow();
        if (row == -1)
            return;
        String status = (String) model.getValueAt(row, 4);
        if (!"Solved".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Only solved cases can be deleted!");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete FIR " + id + "?", "Confirm", 0) == 0) {
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM fir_records WHERE fir_id = ?");
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadData(searchField.getText());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
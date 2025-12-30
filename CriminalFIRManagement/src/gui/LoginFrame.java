package gui;

import database.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    public LoginFrame() {
        setTitle("ACP FIR Management - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        panel.add(new JLabel("ACP Username:"));
        JTextField userField = new JTextField();
        panel.add(userField);

        panel.add(new JLabel("Secret Password:"));
        JPasswordField passField = new JPasswordField();
        panel.add(passField);

        JButton loginBtn = new JButton("Login to System");
        panel.add(new JLabel(""));
        panel.add(loginBtn);

        add(panel);

        loginBtn.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());
            if (validateLogin(user, pass)) {
                this.dispose();
                new FIRForm(); // Calls the separate FIRForm file
            } else {
                JOptionPane.showMessageDialog(this, "Login Failed!");
            }
        });
        setVisible(true);
    }

    private boolean validateLogin(String user, String pass) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, user);
            pst.setString(2, pass);
            return pst.executeQuery().next();
        } catch (Exception e) {
            return false;
        }
    }
}
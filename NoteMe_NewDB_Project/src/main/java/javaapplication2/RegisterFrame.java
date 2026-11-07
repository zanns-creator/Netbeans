package javaapplication2;

import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnSubmit;
    private JButton btnBack;

    public RegisterFrame() {
        setTitle("NoteMe - Register Akun");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JLabel lblTitle = new JLabel("Daftar Akun Baru");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(98, 70, 234));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);

        txtUsername = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Email:"), gbc);

        txtEmail = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Password:"), gbc);

        txtPassword = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(txtPassword, gbc);

        btnSubmit = new JButton("Daftar");
        btnSubmit.setBackground(new Color(40, 167, 69));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(btnSubmit, gbc);

        btnBack = new JButton("Kembali");
        btnBack.setBackground(new Color(108, 117, 125));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        gbc.gridx = 1; gbc.gridy = 4;
        panel.add(btnBack, gbc);

        add(panel);

        btnSubmit.addActionListener(e -> handleRegister());
        btnBack.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
    }

    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User newUser = new User(username, password, email);
            session.save(newUser);

            transaction.commit();
            JOptionPane.showMessageDialog(this, "Registrasi Berhasil! Silakan Login.");
            new LoginFrame().setVisible(true);
            this.dispose();
        } catch (Exception ex) {
            if (transaction != null) transaction.rollback();
            JOptionPane.showMessageDialog(this, "Gagal Registrasi (Username mungkin sudah digunakan): " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

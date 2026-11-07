package javaapplication2;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class NoteFrame extends JFrame {
    private final User currentUser;
    private JTable tableNotes;
    private DefaultTableModel tableModel;
    private JTextField txtTitle;
    private JTextArea txtContent;
    private JTextField txtSearch;
    private JLabel lblWelcome;
    private JLabel lblTotalNotes;

    private Long selectedNoteId = null;

    public NoteFrame(User user) {
        this.currentUser = user;
        setTitle("NoteMe - Modern Dashboard");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadNotes("");
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Header Panel (Dashboard Stats & Navigation)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(98, 70, 234));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        lblWelcome = new JLabel("Halo, " + currentUser.getUsername() + " 👋");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblWelcome.setForeground(Color.WHITE);

        lblTotalNotes = new JLabel("Total Catatan: 0");
        lblTotalNotes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTotalNotes.setForeground(new Color(220, 220, 255));

        JPanel leftHeader = new JPanel(new GridLayout(2, 1));
        leftHeader.setOpaque(false);
        leftHeader.add(lblWelcome);
        leftHeader.add(lblTotalNotes);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel (Table + Search)
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Daftar Catatan"));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(25);
        JButton btnSearch = new JButton("Cari");
        btnSearch.addActionListener(e -> loadNotes(txtSearch.getText().trim()));
        searchPanel.add(new JLabel("Cari Catatan:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        tableModel = new DefaultTableModel(new String[]{"ID", "Judul", "Isi Catatan", "Tgl Dibuat", "Tgl Diubah"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableNotes = new JTable(tableModel);
        tableNotes.setRowHeight(24);
        tableNotes.getSelectionModel().addListSelectionListener(e -> fillFormFromSelectedRow());

        JScrollPane scrollTable = new JScrollPane(tableNotes);

        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollTable, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Right Panel (Form Input CRUD)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(340, 0));
        formPanel.setBorder(BorderFactory.createTitledBorder("Kelola Catatan"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Judul:"), gbc);

        txtTitle = new JTextField();
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(txtTitle, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Isi Catatan:"), gbc);

        txtContent = new JTextArea(8, 20);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        JScrollPane scrollContent = new JScrollPane(txtContent);
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(scrollContent, gbc);

        // Action Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JButton btnAdd = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Hapus");
        JButton btnClear = new JButton("Clear");

        btnAdd.setBackground(new Color(40, 167, 69)); btnAdd.setForeground(Color.WHITE);
        btnUpdate.setBackground(new Color(255, 193, 7)); btnUpdate.setForeground(Color.BLACK);
        btnDelete.setBackground(new Color(220, 53, 69)); btnDelete.setForeground(Color.WHITE);
        btnClear.setBackground(new Color(108, 117, 125)); btnClear.setForeground(Color.WHITE);

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.EAST);

        btnAdd.addActionListener(e -> addNote());
        btnUpdate.addActionListener(e -> updateNote());
        btnDelete.addActionListener(e -> deleteNote());
        btnClear.addActionListener(e -> clearForm());
    }

    private void loadNotes(String keyword) {
        tableModel.setRowCount(0);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Note WHERE user.id = :uid";
            if (!keyword.isEmpty()) {
                hql += " AND (title LIKE :kw OR content LIKE :kw)";
            }
            hql += " ORDER BY updatedAt DESC";

            Query<Note> query = session.createQuery(hql, Note.class);
            query.setParameter("uid", currentUser.getId());
            if (!keyword.isEmpty()) {
                query.setParameter("kw", "%" + keyword + "%");
            }

            List<Note> list = query.list();
            for (Note n : list) {
                tableModel.addRow(new Object[]{n.getId(), n.getTitle(), n.getContent(), n.getCreatedAt(), n.getUpdatedAt()});
            }
            lblTotalNotes.setText("Total Catatan: " + list.size());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error memuat catatan: " + ex.getMessage());
        }
    }

    private void fillFormFromSelectedRow() {
        int row = tableNotes.getSelectedRow();
        if (row != -1) {
            selectedNoteId = (Long) tableModel.getValueAt(row, 0);
            txtTitle.setText(tableModel.getValueAt(row, 1).toString());
            txtContent.setText(tableModel.getValueAt(row, 2).toString());
        }
    }

    private void addNote() {
        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Judul catatan tidak boleh kosong!");
            return;
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Note note = new Note(currentUser, title, content);
            session.save(note);
            tx.commit();

            clearForm();
            loadNotes("");
            JOptionPane.showMessageDialog(this, "Catatan berhasil ditambahkan!");
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            JOptionPane.showMessageDialog(this, "Gagal menambah catatan: " + ex.getMessage());
        }
    }

    private void updateNote() {
        if (selectedNoteId == null) {
            JOptionPane.showMessageDialog(this, "Pilih catatan yang ingin diubah dari tabel!");
            return;
        }
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Note note = session.get(Note.class, selectedNoteId);
            if (note != null && note.getUser().getId().equals(currentUser.getId())) {
                note.setTitle(txtTitle.getText().trim());
                note.setContent(txtContent.getText().trim());
                note.setUpdatedAt(new Date());
                session.update(note);
                tx.commit();

                clearForm();
                loadNotes("");
                JOptionPane.showMessageDialog(this, "Catatan berhasil diperbarui!");
            }
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            JOptionPane.showMessageDialog(this, "Gagal memperbarui catatan: " + ex.getMessage());
        }
    }

    private void deleteNote() {
        if (selectedNoteId == null) {
            JOptionPane.showMessageDialog(this, "Pilih catatan yang ingin dihapus dari tabel!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus catatan ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Note note = session.get(Note.class, selectedNoteId);
            if (note != null && note.getUser().getId().equals(currentUser.getId())) {
                session.delete(note);
                tx.commit();

                clearForm();
                loadNotes("");
                JOptionPane.showMessageDialog(this, "Catatan berhasil dihapus!");
            }
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            JOptionPane.showMessageDialog(this, "Gagal menghapus catatan: " + ex.getMessage());
        }
    }

    private void clearForm() {
        selectedNoteId = null;
        txtTitle.setText("");
        txtContent.setText("");
        tableNotes.clearSelection();
    }
}

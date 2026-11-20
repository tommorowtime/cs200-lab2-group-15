import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ChocAn Graphical User Interface (GUI)
 * Features a single-window CardLayout covering:
 * 1. Login Page (with operating mode switcher & quick-fill test accounts)
 * 2. Operator Page (Member & Provider CRUD with tables)
 * 3. Manager Page (Member, Provider, and Summary Report generators)
 * 4. Provider Page (Member Validation status badge & Service Record submission)
 */
public class ChocAnGUI extends JFrame {

    private final AuthService authService = new AuthService();
    private User currentUser = null;

    // CardLayout manager for smooth page switching
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);

    // Card Names
    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_OPERATOR = "OPERATOR";
    private static final String CARD_MANAGER = "MANAGER";
    private static final String CARD_PROVIDER = "PROVIDER";

    // --- Login Components ---
    private JTextField loginUserField;
    private JPasswordField loginPassField;
    private JComboBox<String> modeComboBox;
    private JLabel loginStatusLabel;

    // --- Operator Components ---
    private JLabel opModeBadge;
    private JLabel opUserLabel;
    // Members Table & Form
    private DefaultTableModel memberTableModel;
    private JTable memberTable;
    private JTextField memIdField, memNameField, memAddrField, memCityField, memStateField, memZipField;
    private JCheckBox memSuspendedCheck;
    // Providers Table & Form
    private DefaultTableModel providerTableModel;
    private JTable providerTable;
    private JTextField provIdField, provNameField, provAddrField, provCityField, provStateField, provZipField;

    // --- Manager Components ---
    private JLabel mgrModeBadge;
    private JLabel mgrUserLabel;
    private JTextArea reportTextArea;

    // --- Provider Components ---
    private JLabel prvModeBadge;
    private JLabel prvUserLabel;
    private JTextField valMemberIdField;
    private JLabel valResultBadge;
    private JLabel valResultDetails;
    private JTextField srvMemberIdField;
    private JTextField srvDateField;
    private JTextField srvCodeField;
    private JTextField srvCommentsField;
    private JTextField srvFeeField;
    private DefaultTableModel srvLogModel;

    private JButton loginBtnRef;

    public ChocAnGUI() {
        setTitle("Chocoholics Anonymous (ChocAn) Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);

        // Build Pages
        cardsPanel.add(buildLoginPage(), CARD_LOGIN);
        cardsPanel.add(buildOperatorPage(), CARD_OPERATOR);
        cardsPanel.add(buildManagerPage(), CARD_MANAGER);
        cardsPanel.add(buildProviderPage(), CARD_PROVIDER);

        add(cardsPanel);

        // Show login page initially
        showPage(CARD_LOGIN);
    }

    private void showPage(String cardName) {
        cardLayout.show(cardsPanel, cardName);
        if (CARD_LOGIN.equals(cardName)) {
            loginPassField.setText("");
            loginStatusLabel.setText(" ");
            getRootPane().setDefaultButton(loginBtnRef);
        }
    }

    // =========================================================================
    // 1. LOGIN PAGE
    // =========================================================================
    private JPanel buildLoginPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));

        // Header Banner (Clean light styling with high-contrast text)
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBackground(new Color(235, 242, 250));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(205, 215, 228)),
                new EmptyBorder(22, 20, 22, 20)
        ));

        JLabel title = new JLabel("Chocoholics Anonymous (ChocAn)", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(24, 43, 73)); // High-contrast navy

        JLabel subtitle = new JLabel("Healthcare Provider, Operator & Manager Portal", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(65, 80, 100)); // Slate gray

        header.add(title);
        header.add(subtitle);
        panel.add(header, BorderLayout.NORTH);

        // Center Login Card
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel cardTitle = new JLabel("Sign In to Your Account", SwingConstants.CENTER);
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        cardTitle.setForeground(new Color(44, 62, 80));
        card.add(cardTitle, gbc);

        // Operating Mode Selector
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel modeLabel = new JLabel("System Mode:");
        modeLabel.setForeground(new Color(30, 40, 50));
        card.add(modeLabel, gbc);

        gbc.gridx = 1;
        modeComboBox = new JComboBox<>(new String[]{
                "Regular Mode (Local Memory)",
                "Real Database Mode (Neon Cloud DB)"
        });
        modeComboBox.addActionListener(e -> {
            boolean useDb = modeComboBox.getSelectedIndex() == 1;
            Operator.setUseDatabase(useDb);
            updateAllModeBadges();
        });
        card.add(modeComboBox, gbc);

        // Username
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(new Color(30, 40, 50));
        card.add(userLabel, gbc);

        gbc.gridx = 1;
        loginUserField = new JTextField(18);
        card.add(loginUserField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(new Color(30, 40, 50));
        card.add(passLabel, gbc);

        gbc.gridx = 1;
        loginPassField = new JPasswordField(18);
        card.add(loginPassField, gbc);

        // Status Label
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        loginStatusLabel = new JLabel(" ", SwingConstants.CENTER);
        loginStatusLabel.setForeground(new Color(192, 57, 43));
        loginStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        card.add(loginStatusLabel, gbc);

        // Login Button (Dark, readable text)
        gbc.gridy++;
        JButton loginBtn = new JButton("Log In");
        loginBtn.setForeground(new Color(20, 30, 40));
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> handleLogin());
        loginBtnRef = loginBtn;
        card.add(loginBtn, gbc);

        // Quick-Fill Demo Helpers
        gbc.gridy++;
        JPanel demoPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        demoPanel.setOpaque(false);
        demoPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(200, 205, 210)), "Quick-Fill Test Logins"));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        btnRow.setOpaque(false);

        JButton opDemoBtn = new JButton("Operator (1984)");
        JButton mgrDemoBtn = new JButton("Manager (fishes)");
        JButton prvDemoBtn = new JButton("Provider (HotPotato)");

        styleDemoBtn(opDemoBtn, "operator1", "1984");
        styleDemoBtn(mgrDemoBtn, "manager1", "fishes");
        styleDemoBtn(prvDemoBtn, "provider1", "HotPotato");

        btnRow.add(opDemoBtn);
        btnRow.add(mgrDemoBtn);
        btnRow.add(prvDemoBtn);
        demoPanel.add(btnRow);

        card.add(demoPanel, gbc);
        centerWrapper.add(card);
        panel.add(centerWrapper, BorderLayout.CENTER);

        return panel;
    }

    private void styleDemoBtn(JButton btn, String user, String pass) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(new Color(30, 40, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            loginUserField.setText(user);
            loginPassField.setText(pass);
            loginStatusLabel.setText(" ");
        });
    }

    private void handleLogin() {
        String username = loginUserField.getText().trim();
        String password = new String(loginPassField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            loginStatusLabel.setText("Please enter both username and password.");
            return;
        }

        User user = authService.login(username, password);
        if (user == null) {
            loginStatusLabel.setText("Invalid username or password. Please try again.");
            return;
        }

        currentUser = user;
        updateAllModeBadges();

        switch (user.getRole()) {
            case OPERATOR -> {
                opUserLabel.setText("Operator: " + user.getUsername());
                refreshMemberTable();
                refreshProviderTable();
                showPage(CARD_OPERATOR);
            }
            case MANAGER -> {
                mgrUserLabel.setText("Manager: " + user.getUsername());
                reportTextArea.setText("Welcome, " + user.getUsername() + ".\nClick one of the buttons above to generate a report.");
                showPage(CARD_MANAGER);
            }
            case PROVIDER -> {
                prvUserLabel.setText("Provider: " + user.getUsername());
                resetProviderValidation();
                showPage(CARD_PROVIDER);
            }
        }
    }

    private void handleLogout() {
        currentUser = null;
        showPage(CARD_LOGIN);
    }

    // =========================================================================
    // 2. OPERATOR PAGE
    // =========================================================================
    private JPanel buildOperatorPage() {
        JPanel panel = new JPanel(new BorderLayout());

        // Header
        JPanel topBar = createTopBar("OPERATOR DASHBOARD",
                opUserLabel = new JLabel("Operator: -"),
                opModeBadge = createModeBadgeLabel(),
                e -> handleLogout());
        panel.add(topBar, BorderLayout.NORTH);

        // Tabbed Interface: Members & Providers
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("  Manage Members  ", buildMemberTab());
        tabbedPane.addTab("  Manage Providers  ", buildProviderTab());

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMemberTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Form Panel (Left)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Member Details"));
        formPanel.setPreferredSize(new Dimension(320, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0;

        formPanel.add(new JLabel("Member ID (9 digits):"), g);
        g.gridy++; memIdField = new JTextField(12); formPanel.add(memIdField, g);

        g.gridy++; formPanel.add(new JLabel("Full Name:"), g);
        g.gridy++; memNameField = new JTextField(12); formPanel.add(memNameField, g);

        g.gridy++; formPanel.add(new JLabel("Street Address:"), g);
        g.gridy++; memAddrField = new JTextField(12); formPanel.add(memAddrField, g);

        g.gridy++; formPanel.add(new JLabel("City:"), g);
        g.gridy++; memCityField = new JTextField(12); formPanel.add(memCityField, g);

        g.gridy++; formPanel.add(new JLabel("State (2 letters):"), g);
        g.gridy++; memStateField = new JTextField(4); formPanel.add(memStateField, g);

        g.gridy++; formPanel.add(new JLabel("ZIP Code (5 digits):"), g);
        g.gridy++; memZipField = new JTextField(6); formPanel.add(memZipField, g);

        g.gridy++;
        memSuspendedCheck = new JCheckBox("Account Suspended");
        formPanel.add(memSuspendedCheck, g);

        // Action Buttons Row (Clear dark text)
        g.gridy++;
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        JButton addBtn = new JButton("Add Member");
        JButton updBtn = new JButton("Update");
        JButton delBtn = new JButton("Delete");
        JButton clrBtn = new JButton("Clear");

        addBtn.setForeground(new Color(20, 30, 40));
        updBtn.setForeground(new Color(20, 30, 40));
        delBtn.setForeground(new Color(160, 25, 25)); // High contrast dark red
        clrBtn.setForeground(new Color(20, 30, 40));

        addBtn.addActionListener(e -> onAddMember());
        updBtn.addActionListener(e -> onUpdateMember());
        delBtn.addActionListener(e -> onDeleteMember());
        clrBtn.addActionListener(e -> clearMemberFields());

        btnPanel.add(addBtn);
        btnPanel.add(updBtn);
        btnPanel.add(delBtn);
        btnPanel.add(clrBtn);
        formPanel.add(btnPanel, g);

        tab.add(formPanel, BorderLayout.WEST);

        // Table Panel (Center)
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Registered Members"));

        String[] cols = {"ID", "Name", "Status", "Address", "City", "State", "ZIP"};
        memberTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        memberTable = new JTable(memberTableModel);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.getSelectionModel().addListSelectionListener(e -> {
            int row = memberTable.getSelectedRow();
            if (row >= 0) {
                memIdField.setText((String) memberTableModel.getValueAt(row, 0));
                memNameField.setText((String) memberTableModel.getValueAt(row, 1));
                String status = (String) memberTableModel.getValueAt(row, 2);
                memSuspendedCheck.setSelected("SUSPENDED".equalsIgnoreCase(status));
                memAddrField.setText((String) memberTableModel.getValueAt(row, 3));
                memCityField.setText((String) memberTableModel.getValueAt(row, 4));
                memStateField.setText((String) memberTableModel.getValueAt(row, 5));
                memZipField.setText((String) memberTableModel.getValueAt(row, 6));
            }
        });

        tablePanel.add(new JScrollPane(memberTable), BorderLayout.CENTER);

        JPanel tableBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh Table");
        refreshBtn.setForeground(new Color(20, 30, 40));
        refreshBtn.addActionListener(e -> refreshMemberTable());
        tableBottom.add(refreshBtn);
        tablePanel.add(tableBottom, BorderLayout.SOUTH);

        tab.add(tablePanel, BorderLayout.CENTER);
        return tab;
    }

    private void onAddMember() {
        String id = memIdField.getText().trim();
        String name = memNameField.getText().trim();
        if (!id.matches("\\d{9}")) {
            JOptionPane.showMessageDialog(this, "Member ID must be exactly 9 numeric digits.", "Invalid ID", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Member name cannot be empty.", "Missing Name", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (Operator.findMemberById(id) != null) {
            JOptionPane.showMessageDialog(this, "A member with ID " + id + " already exists.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Member m = new Member(id, name, memAddrField.getText().trim(), memCityField.getText().trim(),
                memStateField.getText().trim(), memZipField.getText().trim(), memSuspendedCheck.isSelected());

        if (Operator.addMember(m)) {
            JOptionPane.showMessageDialog(this, "Member added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshMemberTable();
            clearMemberFields();
        } else {
            JOptionPane.showMessageDialog(this, "Could not add member.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdateMember() {
        String id = memIdField.getText().trim();
        Member existing = Operator.findMemberById(id);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "No member found with ID: " + id, "Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        existing.setName(memNameField.getText().trim());
        existing.setAddress(memAddrField.getText().trim());
        existing.setCity(memCityField.getText().trim());
        existing.setState(memStateField.getText().trim());
        existing.setZip(memZipField.getText().trim());
        existing.setSuspended(memSuspendedCheck.isSelected());

        if (Operator.updateMember(existing)) {
            JOptionPane.showMessageDialog(this, "Member updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshMemberTable();
        } else {
            JOptionPane.showMessageDialog(this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteMember() {
        String id = memIdField.getText().trim();
        if (id.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete member " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (Operator.deleteMember(id)) {
                JOptionPane.showMessageDialog(this, "Member deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshMemberTable();
                clearMemberFields();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete member.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearMemberFields() {
        memIdField.setText("");
        memNameField.setText("");
        memAddrField.setText("");
        memCityField.setText("");
        memStateField.setText("");
        memZipField.setText("");
        memSuspendedCheck.setSelected(false);
        memberTable.clearSelection();
    }

    private void refreshMemberTable() {
        memberTableModel.setRowCount(0);
        List<Member> list = Operator.getMembers();
        for (Member m : list) {
            memberTableModel.addRow(new Object[]{
                    m.getMemberId(),
                    m.getName(),
                    m.isSuspended() ? "SUSPENDED" : "ACTIVE",
                    m.getAddress(),
                    m.getCity(),
                    m.getState(),
                    m.getZip()
            });
        }
    }

    // --- Provider Tab ---
    private JPanel buildProviderTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Form Panel (Left)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Provider Details"));
        formPanel.setPreferredSize(new Dimension(320, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0;

        formPanel.add(new JLabel("Provider ID (9 digits):"), g);
        g.gridy++; provIdField = new JTextField(12); formPanel.add(provIdField, g);

        g.gridy++; formPanel.add(new JLabel("Provider / Clinic Name:"), g);
        g.gridy++; provNameField = new JTextField(12); formPanel.add(provNameField, g);

        g.gridy++; formPanel.add(new JLabel("Street Address:"), g);
        g.gridy++; provAddrField = new JTextField(12); formPanel.add(provAddrField, g);

        g.gridy++; formPanel.add(new JLabel("City:"), g);
        g.gridy++; provCityField = new JTextField(12); formPanel.add(provCityField, g);

        g.gridy++; formPanel.add(new JLabel("State (2 letters):"), g);
        g.gridy++; provStateField = new JTextField(4); formPanel.add(provStateField, g);

        g.gridy++; formPanel.add(new JLabel("ZIP Code (5 digits):"), g);
        g.gridy++; provZipField = new JTextField(6); formPanel.add(provZipField, g);

        g.gridy++;
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        JButton addBtn = new JButton("Add Provider");
        JButton updBtn = new JButton("Update");
        JButton delBtn = new JButton("Delete");
        JButton clrBtn = new JButton("Clear");

        addBtn.setForeground(new Color(20, 30, 40));
        updBtn.setForeground(new Color(20, 30, 40));
        delBtn.setForeground(new Color(160, 25, 25));
        clrBtn.setForeground(new Color(20, 30, 40));

        addBtn.addActionListener(e -> onAddProvider());
        updBtn.addActionListener(e -> onUpdateProvider());
        delBtn.addActionListener(e -> onDeleteProvider());
        clrBtn.addActionListener(e -> clearProviderFields());

        btnPanel.add(addBtn);
        btnPanel.add(updBtn);
        btnPanel.add(delBtn);
        btnPanel.add(clrBtn);
        formPanel.add(btnPanel, g);

        tab.add(formPanel, BorderLayout.WEST);

        // Table Panel (Center)
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Registered Providers"));

        String[] cols = {"ID", "Name", "Address", "City", "State", "ZIP"};
        providerTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        providerTable = new JTable(providerTableModel);
        providerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        providerTable.getSelectionModel().addListSelectionListener(e -> {
            int row = providerTable.getSelectedRow();
            if (row >= 0) {
                provIdField.setText((String) providerTableModel.getValueAt(row, 0));
                provNameField.setText((String) providerTableModel.getValueAt(row, 1));
                provAddrField.setText((String) providerTableModel.getValueAt(row, 2));
                provCityField.setText((String) providerTableModel.getValueAt(row, 3));
                provStateField.setText((String) providerTableModel.getValueAt(row, 4));
                provZipField.setText((String) providerTableModel.getValueAt(row, 5));
            }
        });

        tablePanel.add(new JScrollPane(providerTable), BorderLayout.CENTER);

        JPanel tableBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh Table");
        refreshBtn.setForeground(new Color(20, 30, 40));
        refreshBtn.addActionListener(e -> refreshProviderTable());
        tableBottom.add(refreshBtn);
        tablePanel.add(tableBottom, BorderLayout.SOUTH);

        tab.add(tablePanel, BorderLayout.CENTER);
        return tab;
    }

    private void onAddProvider() {
        String id = provIdField.getText().trim();
        String name = provNameField.getText().trim();
        if (!id.matches("\\d{9}")) {
            JOptionPane.showMessageDialog(this, "Provider ID must be exactly 9 numeric digits.", "Invalid ID", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Provider name cannot be empty.", "Missing Name", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (Operator.findProviderById(id) != null) {
            JOptionPane.showMessageDialog(this, "A provider with ID " + id + " already exists.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Provider p = new Provider(id, name, provAddrField.getText().trim(), provCityField.getText().trim(),
                provStateField.getText().trim(), provZipField.getText().trim());

        if (Operator.addProvider(p)) {
            JOptionPane.showMessageDialog(this, "Provider added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshProviderTable();
            clearProviderFields();
        } else {
            JOptionPane.showMessageDialog(this, "Could not add provider.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdateProvider() {
        String id = provIdField.getText().trim();
        Provider existing = Operator.findProviderById(id);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "No provider found with ID: " + id, "Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        existing.setName(provNameField.getText().trim());
        existing.setAddress(provAddrField.getText().trim());
        existing.setCity(provCityField.getText().trim());
        existing.setState(provStateField.getText().trim());
        existing.setZip(provZipField.getText().trim());

        if (Operator.updateProvider(existing)) {
            JOptionPane.showMessageDialog(this, "Provider updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshProviderTable();
        } else {
            JOptionPane.showMessageDialog(this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteProvider() {
        String id = provIdField.getText().trim();
        if (id.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete provider " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (Operator.deleteProvider(id)) {
                JOptionPane.showMessageDialog(this, "Provider deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshProviderTable();
                clearProviderFields();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete provider.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearProviderFields() {
        provIdField.setText("");
        provNameField.setText("");
        provAddrField.setText("");
        provCityField.setText("");
        provStateField.setText("");
        provZipField.setText("");
        providerTable.clearSelection();
    }

    private void refreshProviderTable() {
        providerTableModel.setRowCount(0);
        List<Provider> list = Operator.getProviders();
        for (Provider p : list) {
            providerTableModel.addRow(new Object[]{
                    p.getProviderId(),
                    p.getName(),
                    p.getAddress(),
                    p.getCity(),
                    p.getState(),
                    p.getZip()
            });
        }
    }

    // =========================================================================
    // 3. MANAGER PAGE
    // =========================================================================
    private JPanel buildManagerPage() {
        JPanel panel = new JPanel(new BorderLayout());

        // Header
        JPanel topBar = createTopBar("MANAGER PORTAL",
                mgrUserLabel = new JLabel("Manager: -"),
                mgrModeBadge = createModeBadgeLabel(),
                e -> handleLogout());
        panel.add(topBar, BorderLayout.NORTH);

        // Center Content
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JButton memRepBtn = new JButton("Generate Member Report");
        JButton provRepBtn = new JButton("Generate Provider Report");
        JButton sumRepBtn = new JButton("Generate Summary Report");
        JButton clearBtn = new JButton("Clear Output");

        memRepBtn.setForeground(new Color(20, 30, 40));
        provRepBtn.setForeground(new Color(20, 30, 40));
        sumRepBtn.setForeground(new Color(20, 30, 40));
        clearBtn.setForeground(new Color(20, 30, 40));

        memRepBtn.addActionListener(e -> generateMemberReport());
        provRepBtn.addActionListener(e -> generateProviderReport());
        sumRepBtn.addActionListener(e -> generateSummaryReport());
        clearBtn.addActionListener(e -> reportTextArea.setText(""));

        toolbar.add(memRepBtn);
        toolbar.add(provRepBtn);
        toolbar.add(sumRepBtn);
        toolbar.add(clearBtn);
        content.add(toolbar, BorderLayout.NORTH);

        // Report Text Area
        reportTextArea = new JTextArea();
        reportTextArea.setEditable(false);
        reportTextArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        reportTextArea.setForeground(new Color(30, 40, 50));
        reportTextArea.setBackground(new Color(250, 252, 255));
        reportTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(reportTextArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 205, 210)));
        content.add(scroll, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void generateMemberReport() {
        List<Member> members = Operator.getMembers();
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                         CHOCOHOLICS ANONYMOUS - MEMBER REPORT                          \n");
        sb.append("                         Generated: ").append(LocalDate.now()).append(" | Mode: ").append(Operator.getModeName()).append("\n");
        sb.append("========================================================================================\n\n");

        if (members.isEmpty()) {
            sb.append("No members currently registered in the system.\n");
        } else {
            sb.append(String.format("%-11s | %-22s | %-10s | %s\n", "MEMBER ID", "NAME", "STATUS", "LOCATION"));
            sb.append("----------------------------------------------------------------------------------------\n");
            for (Member m : members) {
                String loc = m.getAddress() + ", " + m.getCity() + ", " + m.getState() + " " + m.getZip();
                sb.append(String.format("%-11s | %-22s | %-10s | %s\n",
                        m.getMemberId(),
                        m.getName(),
                        m.isSuspended() ? "SUSPENDED" : "ACTIVE",
                        loc));
            }
            sb.append("----------------------------------------------------------------------------------------\n");
            sb.append("Total Members Listed: ").append(members.size()).append("\n");
        }
        reportTextArea.setText(sb.toString());
        reportTextArea.setCaretPosition(0);
    }

    private void generateProviderReport() {
        List<Provider> providers = Operator.getProviders();
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                        CHOCOHOLICS ANONYMOUS - PROVIDER REPORT                         \n");
        sb.append("                         Generated: ").append(LocalDate.now()).append(" | Mode: ").append(Operator.getModeName()).append("\n");
        sb.append("========================================================================================\n\n");

        if (providers.isEmpty()) {
            sb.append("No providers currently registered in the system.\n");
        } else {
            sb.append(String.format("%-11s | %-25s | %s\n", "PROVIDER ID", "NAME / CLINIC", "LOCATION"));
            sb.append("----------------------------------------------------------------------------------------\n");
            for (Provider p : providers) {
                String loc = p.getAddress() + ", " + p.getCity() + ", " + p.getState() + " " + p.getZip();
                sb.append(String.format("%-11s | %-25s | %s\n",
                        p.getProviderId(),
                        p.getName(),
                        loc));
            }
            sb.append("----------------------------------------------------------------------------------------\n");
            sb.append("Total Healthcare Providers Listed: ").append(providers.size()).append("\n");
        }
        reportTextArea.setText(sb.toString());
        reportTextArea.setCaretPosition(0);
    }

    private void generateSummaryReport() {
        List<Member> members = Operator.getMembers();
        List<Provider> providers = Operator.getProviders();

        long activeCount = members.stream().filter(m -> !m.isSuspended()).count();
        long suspendedCount = members.stream().filter(Member::isSuspended).count();

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                     CHOCOHOLICS ANONYMOUS - EXECUTIVE SUMMARY                          \n");
        sb.append("                         Generated: ").append(LocalDate.now()).append("\n");
        sb.append("========================================================================================\n\n");
        sb.append("  OPERATING SYSTEM MODE : ").append(Operator.getModeName()).append("\n");
        sb.append("  DATABASE BACKEND      : ").append(Operator.isUseDatabase() ? "Neon PostgreSQL (Connected)" : "Local In-Memory Cache").append("\n\n");
        sb.append("  MEMBERSHIP METRICS:\n");
        sb.append("    * Total Registered Members : ").append(members.size()).append("\n");
        sb.append("    * Active Members           : ").append(activeCount).append("\n");
        sb.append("    * Suspended Members        : ").append(suspendedCount).append("\n\n");
        sb.append("  HEALTHCARE PROVIDER METRICS:\n");
        sb.append("    * Registered Providers     : ").append(providers.size()).append("\n\n");
        sb.append("========================================================================================\n");
        sb.append("Status: All services online and operational.\n");

        reportTextArea.setText(sb.toString());
        reportTextArea.setCaretPosition(0);
    }

    // =========================================================================
    // 4. PROVIDER PAGE
    // =========================================================================
    private JPanel buildProviderPage() {
        JPanel panel = new JPanel(new BorderLayout());

        // Header
        JPanel topBar = createTopBar("PROVIDER TERMINAL",
                prvUserLabel = new JLabel("Provider: -"),
                prvModeBadge = createModeBadgeLabel(),
                e -> handleLogout());
        panel.add(topBar, BorderLayout.NORTH);

        // Split Page: Left (Member Validation), Right (Submit Service Record)
        JPanel content = new JPanel(new GridLayout(1, 2, 15, 15));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Card 1: Validate Member
        JPanel valCard = new JPanel(new BorderLayout(10, 15));
        valCard.setBackground(Color.WHITE);
        valCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel valHeader = new JLabel("1. Validate Member Card", SwingConstants.LEFT);
        valHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valHeader.setForeground(new Color(30, 40, 50));
        valCard.add(valHeader, BorderLayout.NORTH);

        JPanel valCenter = new JPanel(new GridBagLayout());
        valCenter.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0;

        JLabel valPrompt = new JLabel("Enter 9-Digit Member ID:");
        valPrompt.setForeground(new Color(40, 50, 60));
        valCenter.add(valPrompt, g);

        g.gridy++;
        valMemberIdField = new JTextField(15);
        valCenter.add(valMemberIdField, g);

        g.gridy++;
        JButton valBtn = new JButton("Validate Member");
        valBtn.setForeground(new Color(20, 30, 40));
        valBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valBtn.addActionListener(e -> onValidateMember());
        valCenter.add(valBtn, g);

        // Result Badge
        g.gridy++;
        g.insets = new Insets(15, 6, 4, 6);
        valResultBadge = new JLabel("READY TO SCAN", SwingConstants.CENTER);
        valResultBadge.setOpaque(true);
        valResultBadge.setBackground(new Color(240, 243, 246));
        valResultBadge.setForeground(new Color(50, 60, 70));
        valResultBadge.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valResultBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 205, 210), 1),
                new EmptyBorder(12, 10, 12, 10)
        ));
        valCenter.add(valResultBadge, g);

        g.gridy++;
        valResultDetails = new JLabel("Enter a member ID to verify active status.", SwingConstants.CENTER);
        valResultDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valResultDetails.setForeground(new Color(70, 80, 90));
        valCenter.add(valResultDetails, g);

        valCard.add(valCenter, BorderLayout.CENTER);
        content.add(valCard);

        // Card 2: Submit Service Record
        JPanel srvCard = new JPanel(new BorderLayout(10, 10));
        srvCard.setBackground(Color.WHITE);
        srvCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel srvHeader = new JLabel("2. Submit Service Record", SwingConstants.LEFT);
        srvHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        srvHeader.setForeground(new Color(30, 40, 50));
        srvCard.add(srvHeader, BorderLayout.NORTH);

        JPanel srvForm = new JPanel(new GridBagLayout());
        srvForm.setOpaque(false);
        GridBagConstraints sg = new GridBagConstraints();
        sg.insets = new Insets(4, 6, 4, 6);
        sg.fill = GridBagConstraints.HORIZONTAL;
        sg.gridx = 0; sg.gridy = 0;

        srvForm.add(new JLabel("Member ID:"), sg);
        sg.gridy++; srvMemberIdField = new JTextField(12); srvForm.add(srvMemberIdField, sg);

        sg.gridy++; srvForm.add(new JLabel("Service Date (MM-DD-YYYY):"), sg);
        sg.gridy++;
        srvDateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")));
        srvForm.add(srvDateField, sg);

        sg.gridy++; srvForm.add(new JLabel("6-Digit Service Code:"), sg);
        sg.gridy++; srvCodeField = new JTextField(10); srvForm.add(srvCodeField, sg);

        sg.gridy++; srvForm.add(new JLabel("Fee ($):"), sg);
        sg.gridy++; srvFeeField = new JTextField("50.00"); srvForm.add(srvFeeField, sg);

        sg.gridy++; srvForm.add(new JLabel("Comments (Optional):"), sg);
        sg.gridy++; srvCommentsField = new JTextField(15); srvForm.add(srvCommentsField, sg);

        sg.gridy++;
        JButton submitSrvBtn = new JButton("Submit Service Record");
        submitSrvBtn.setForeground(new Color(20, 30, 40));
        submitSrvBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submitSrvBtn.addActionListener(e -> onSubmitService());
        srvForm.add(submitSrvBtn, sg);

        srvCard.add(srvForm, BorderLayout.NORTH);

        // Recent Submissions Log Table
        String[] srvCols = {"Member ID", "Date", "Code", "Fee"};
        srvLogModel = new DefaultTableModel(srvCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable srvTable = new JTable(srvLogModel);
        JScrollPane srvScroll = new JScrollPane(srvTable);
        srvScroll.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Session Submissions Log"));
        srvCard.add(srvScroll, BorderLayout.CENTER);

        content.add(srvCard);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void onValidateMember() {
        String id = valMemberIdField.getText().trim();
        if (!id.matches("\\d{9}")) {
            setValidationResult("INVALID NUMBER", new Color(254, 226, 226), new Color(153, 27, 27), "Member ID must be exactly 9 digits.");
            return;
        }

        Member member = Operator.findMemberById(id);
        if (member == null) {
            setValidationResult("INVALID NUMBER", new Color(254, 226, 226), new Color(153, 27, 27), "Member ID not recognized in system.");
        } else if (member.isSuspended()) {
            setValidationResult("MEMBER SUSPENDED", new Color(255, 243, 205), new Color(146, 64, 14),
                    "Account suspended: " + member.getName() + " (Fees Overdue)");
        } else {
            setValidationResult("VALIDATED", new Color(220, 245, 225), new Color(15, 90, 40),
                    "Active Member: " + member.getName() + " (" + member.getCity() + ", " + member.getState() + ")");
            // Auto-fill member ID into service record form
            srvMemberIdField.setText(member.getMemberId());
        }
    }

    private void setValidationResult(String badgeText, Color badgeBg, Color badgeFg, String details) {
        valResultBadge.setText(badgeText);
        valResultBadge.setBackground(badgeBg);
        valResultBadge.setForeground(badgeFg);
        valResultDetails.setText(details);
    }

    private void resetProviderValidation() {
        valMemberIdField.setText("");
        valResultBadge.setText("READY TO SCAN");
        valResultBadge.setBackground(new Color(240, 243, 246));
        valResultBadge.setForeground(new Color(50, 60, 70));
        valResultDetails.setText("Enter a member ID to verify active status.");
        srvMemberIdField.setText("");
        srvCodeField.setText("");
        srvCommentsField.setText("");
    }

    private void onSubmitService() {
        String memId = srvMemberIdField.getText().trim();
        String date = srvDateField.getText().trim();
        String code = srvCodeField.getText().trim();
        String fee = srvFeeField.getText().trim();

        if (!memId.matches("\\d{9}")) {
            JOptionPane.showMessageDialog(this, "Valid 9-digit Member ID required.", "Invalid Member", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Member member = Operator.findMemberById(memId);
        if (member == null) {
            JOptionPane.showMessageDialog(this, "Member not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (member.isSuspended()) {
            JOptionPane.showMessageDialog(this, "Cannot submit service for a SUSPENDED member.", "Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!code.matches("\\d{6}")) {
            JOptionPane.showMessageDialog(this, "Service code must be exactly 6 digits.", "Invalid Code", JOptionPane.WARNING_MESSAGE);
            return;
        }

        srvLogModel.addRow(new Object[]{memId, date, code, "$" + fee});
        JOptionPane.showMessageDialog(this,
                "Service record recorded successfully!\nMember: " + member.getName() + "\nFee: $" + fee,
                "Service Submitted", JOptionPane.INFORMATION_MESSAGE);

        srvCodeField.setText("");
        srvCommentsField.setText("");
    }

    // =========================================================================
    // UI HELPERS (Top Bar, Badges)
    // =========================================================================
    private JPanel createTopBar(String portalTitle, JLabel userLabel, JLabel modeBadge, java.awt.event.ActionListener onLogout) {
        JPanel bar = new JPanel(new BorderLayout(15, 0));
        bar.setBackground(new Color(235, 242, 250));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(205, 215, 228)),
                new EmptyBorder(12, 18, 12, 18)
        ));

        // Left: Title
        JLabel title = new JLabel(portalTitle);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(24, 43, 73)); // Dark navy

        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(new Color(65, 80, 100)); // Dark slate

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title);
        left.add(userLabel);
        bar.add(left, BorderLayout.WEST);

        // Right: Mode Badge & Logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        right.setOpaque(false);

        JButton switchModeBtn = new JButton("Toggle Mode");
        switchModeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        switchModeBtn.setForeground(new Color(20, 30, 40));
        switchModeBtn.addActionListener(e -> {
            boolean newMode = !Operator.isUseDatabase();
            Operator.setUseDatabase(newMode);
            modeComboBox.setSelectedIndex(newMode ? 1 : 0);
            updateAllModeBadges();
            refreshMemberTable();
            refreshProviderTable();
            JOptionPane.showMessageDialog(this, "Switched to: " + Operator.getModeName(), "Mode Changed", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setForeground(new Color(160, 25, 25)); // High contrast dark red
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(onLogout);

        right.add(modeBadge);
        right.add(switchModeBtn);
        right.add(logoutBtn);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JLabel createModeBadgeLabel() {
        JLabel label = new JLabel(Operator.getModeName());
        label.setOpaque(true);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setBorder(new EmptyBorder(4, 8, 4, 8));
        updateBadgeStyle(label);
        return label;
    }

    private void updateBadgeStyle(JLabel label) {
        if (Operator.isUseDatabase()) {
            label.setText("NEON DATABASE MODE");
            label.setBackground(new Color(220, 245, 225));
            label.setForeground(new Color(15, 90, 40)); // Dark green
        } else {
            label.setText("LOCAL MEMORY MODE");
            label.setBackground(new Color(255, 243, 205));
            label.setForeground(new Color(146, 64, 14)); // Dark amber
        }
    }

    private void updateAllModeBadges() {
        if (opModeBadge != null) updateBadgeStyle(opModeBadge);
        if (mgrModeBadge != null) updateBadgeStyle(mgrModeBadge);
        if (prvModeBadge != null) updateBadgeStyle(prvModeBadge);
    }

    // =========================================================================
    // MAIN METHOD (Direct GUI Launch)
    // =========================================================================
    public static void main(String[] args) {
        // Set native system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            ChocAnGUI gui = new ChocAnGUI();
            gui.setVisible(true);
        });
    }
}

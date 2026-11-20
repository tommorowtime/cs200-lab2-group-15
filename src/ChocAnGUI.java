import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ChocAn Graphical User Interface (GUI)
 * Features a single-window CardLayout covering:
 * 1. Login Page (with operating mode switcher, 9-digit Provider ID login, & quick-fill test accounts)
 * 2. Operator Page (Member & Provider CRUD with tables and ChocAn field validations)
 * 3. Manager Page (Main Accounting Procedure, Member, Provider, Summary, EFT, and Provider Payment Search)
 * 4. Provider Page (Member Card Validation, Provider Directory request, and Service Record billing)
 *
 * @author Daniel (Team 15 - Frontend & UML)
 * @author Jackson (Team 15 - Provider Features)
 * @author Cameron (Team 15 - Manager Features)
 * @author Teal'c (Team 15 - Operator Features)
 * @author Simeon (Team 15 - Backend & Database)
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
    private JComboBox<String> activeProviderCombo;
    private JTextField valMemberIdField;
    private JLabel valResultBadge;
    private JLabel valResultDetails;
    private JLabel lastFeeBannerLabel;
    private JTextField srvMemberIdField;
    private JTextField srvDateField;
    private JTextField srvCodeField;
    private JComboBox<String> srvDirectoryCombo;
    private JLabel srvLookedUpLabel;
    private JTextField srvCommentsField;
    private DefaultTableModel srvLogModel;

    private JButton loginBtnRef;

    public ChocAnGUI() {
        setTitle("Chocoholics Anonymous (ChocAn) Data Center & Terminal System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1060, 760);
        setMinimumSize(new Dimension(920, 660));
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

        // Header Banner
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBackground(new Color(235, 242, 250));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(205, 215, 228)),
                new EmptyBorder(22, 20, 22, 20)
        ));

        JLabel title = new JLabel("Chocoholics Anonymous (ChocAn)", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(24, 43, 73));

        JLabel subtitle = new JLabel("Healthcare Provider Terminal, Operator Data Center & Manager Portal", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(65, 80, 100));

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
                new EmptyBorder(28, 38, 28, 38)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel cardTitle = new JLabel("Sign In to ChocAn Terminal", SwingConstants.CENTER);
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
                "Regular Mode (Local Persistent Storage)",
                "Real Database Mode (Neon Cloud DB)"
        });
        modeComboBox.setSelectedIndex(Operator.isUseDatabase() ? 1 : 0);
        modeComboBox.addActionListener(e -> {
            boolean useDb = modeComboBox.getSelectedIndex() == 1;
            Operator.setUseDatabase(useDb);
            modeComboBox.setSelectedIndex(Operator.isUseDatabase() ? 1 : 0);
            updateAllModeBadges();
        });
        card.add(modeComboBox, gbc);

        // Username / 9-Digit Provider ID
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel userLabel = new JLabel("Username or 9-Digit Provider ID:");
        userLabel.setForeground(new Color(30, 40, 50));
        card.add(userLabel, gbc);

        gbc.gridx = 1;
        loginUserField = new JTextField(18);
        card.add(loginUserField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passLabel = new JLabel("Password (optional for 9-digit ID):");
        passLabel.setForeground(new Color(30, 40, 50));
        card.add(passLabel, gbc);

        gbc.gridx = 1;
        loginPassField = new JPasswordField(18);
        card.add(loginPassField, gbc);

        // Tip Note for Operator-created Providers
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JLabel tipLabel = new JLabel("Tip: Any Provider added by an Operator can log in directly with their 9-digit Provider ID.", SwingConstants.CENTER);
        tipLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        tipLabel.setForeground(new Color(80, 95, 115));
        card.add(tipLabel, gbc);

        // Status Label
        gbc.gridy++;
        loginStatusLabel = new JLabel(" ", SwingConstants.CENTER);
        loginStatusLabel.setForeground(new Color(192, 57, 43));
        loginStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        card.add(loginStatusLabel, gbc);

        // Login Button
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
        JPanel demoPanel = new JPanel(new GridLayout(1, 1, 4, 4));
        demoPanel.setOpaque(false);
        demoPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(200, 205, 210)), "Quick-Fill Test Logins"));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        btnRow.setOpaque(false);

        JButton opDemoBtn = new JButton("Operator (1984)");
        JButton mgrDemoBtn = new JButton("Manager (fishes)");
        JButton prvDemoBtn = new JButton("Provider 1 (111222333)");
        JButton prv2DemoBtn = new JButton("Provider 2 (444555666)");

        styleDemoBtn(opDemoBtn, "operator1", "1984");
        styleDemoBtn(mgrDemoBtn, "manager1", "fishes");
        styleDemoBtn(prvDemoBtn, "111222333", "");
        styleDemoBtn(prv2DemoBtn, "444555666", "");

        btnRow.add(opDemoBtn);
        btnRow.add(mgrDemoBtn);
        btnRow.add(prvDemoBtn);
        btnRow.add(prv2DemoBtn);
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

        if (username.isEmpty()) {
            loginStatusLabel.setText("Please enter a username or 9-digit Provider Number.");
            return;
        }

        // Require password only for non-9-digit usernames unless matching a registered Provider Name
        if (!username.matches("\\d{9}") && password.isEmpty()) {
            loginStatusLabel.setText("Please enter your password (or use a 9-digit Provider ID).");
            return;
        }

        User user = authService.login(username, password);
        if (user == null) {
            if (username.matches("\\d{9}")) {
                loginStatusLabel.setText("Invalid Provider Number (" + username + "). Provider not found.");
            } else {
                loginStatusLabel.setText("Invalid username or password. Please try again.");
            }
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
                reportTextArea.setText("Welcome, " + user.getUsername() + ".\n"
                        + "Use the toolbar above to run the Friday Midnight Main Accounting Procedure or individual ChocAn reports.\n"
                        + "All generated reports are automatically saved to the 'reports/' directory.");
                showPage(CARD_MANAGER);
            }
            case PROVIDER -> {
                prvUserLabel.setText("Active Provider: " + user.getUsername());
                refreshProviderTerminalState();
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

        JPanel topBar = createTopBar("OPERATOR DATA CENTER DASHBOARD",
                opUserLabel = new JLabel("Operator: -"),
                opModeBadge = createModeBadgeLabel(),
                e -> handleLogout());
        panel.add(topBar, BorderLayout.NORTH);

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

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Member Details"));
        formPanel.setPreferredSize(new Dimension(330, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;

        formPanel.add(new JLabel("Member Number (9 digits):"), g);
        g.gridy++;
        memIdField = new JTextField(12);
        formPanel.add(memIdField, g);

        g.gridy++;
        formPanel.add(new JLabel("Member Name (<=25 chars):"), g);
        g.gridy++;
        memNameField = new JTextField(12);
        formPanel.add(memNameField, g);

        g.gridy++;
        formPanel.add(new JLabel("Street Address (<=25 chars):"), g);
        g.gridy++;
        memAddrField = new JTextField(12);
        formPanel.add(memAddrField, g);

        g.gridy++;
        formPanel.add(new JLabel("City (<=14 chars):"), g);
        g.gridy++;
        memCityField = new JTextField(12);
        formPanel.add(memCityField, g);

        g.gridy++;
        formPanel.add(new JLabel("State (2 letters):"), g);
        g.gridy++;
        memStateField = new JTextField(4);
        formPanel.add(memStateField, g);

        g.gridy++;
        formPanel.add(new JLabel("ZIP Code (5 digits):"), g);
        g.gridy++;
        memZipField = new JTextField(6);
        formPanel.add(memZipField, g);

        g.gridy++;
        memSuspendedCheck = new JCheckBox("Member Suspended (Fees Overdue)");
        formPanel.add(memSuspendedCheck, g);

        g.gridy++;
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        JButton addBtn = new JButton("Add Member");
        JButton updBtn = new JButton("Update");
        JButton delBtn = new JButton("Delete");
        JButton clrBtn = new JButton("Clear");

        addBtn.setForeground(new Color(20, 30, 40));
        updBtn.setForeground(new Color(20, 30, 40));
        delBtn.setForeground(new Color(160, 25, 25));
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

        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Registered ChocAn Members"));

        String[] cols = {"Member ID", "Name (25)", "Status", "Street Address (25)", "City (14)", "State (2)", "ZIP (5)"};
        memberTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
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
        String state = memStateField.getText().trim();
        String zip = memZipField.getText().trim();
        if (!id.matches("\\d{9}")) {
            JOptionPane.showMessageDialog(this, "Member Number must be exactly 9 numeric digits.", "Invalid ID", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Member name cannot be empty.", "Missing Name", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!state.isEmpty() && !state.matches("[A-Za-z]{2}")) {
            JOptionPane.showMessageDialog(this, "State must be 2 letters (e.g., AL, OR, WA).", "Invalid State", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!zip.isEmpty() && !zip.matches("\\d{5}")) {
            JOptionPane.showMessageDialog(this, "ZIP Code must be exactly 5 numeric digits.", "Invalid ZIP", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (Operator.findMemberById(id) != null) {
            JOptionPane.showMessageDialog(this, "A member with ID " + id + " already exists.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Member m = new Member(id, name, memAddrField.getText().trim(), memCityField.getText().trim(),
                state, zip, memSuspendedCheck.isSelected());

        if (Operator.addMember(m)) {
            JOptionPane.showMessageDialog(this,
                    "Member added successfully!\nMember: " + m.getName() + " [" + m.getMemberId() + "]",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
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
        String state = memStateField.getText().trim();
        if (!state.isEmpty() && !state.matches("[A-Za-z]{2}")) {
            JOptionPane.showMessageDialog(this, "State must be 2 letters.", "Invalid State", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String zip = memZipField.getText().trim();
        if (!zip.isEmpty() && !zip.matches("\\d{5}")) {
            JOptionPane.showMessageDialog(this, "ZIP Code must be 5 numeric digits.", "Invalid ZIP", JOptionPane.WARNING_MESSAGE);
            return;
        }

        existing.setName(memNameField.getText().trim());
        existing.setAddress(memAddrField.getText().trim());
        existing.setCity(memCityField.getText().trim());
        existing.setState(state);
        existing.setZip(zip);
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
        if (memberTableModel == null) return;
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

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Provider Details"));
        formPanel.setPreferredSize(new Dimension(330, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;

        formPanel.add(new JLabel("Provider Number (9 digits):"), g);
        g.gridy++;
        provIdField = new JTextField(12);
        formPanel.add(provIdField, g);

        g.gridy++;
        formPanel.add(new JLabel("Provider Name (<=25 chars):"), g);
        g.gridy++;
        provNameField = new JTextField(12);
        formPanel.add(provNameField, g);

        g.gridy++;
        formPanel.add(new JLabel("Street Address (<=25 chars):"), g);
        g.gridy++;
        provAddrField = new JTextField(12);
        formPanel.add(provAddrField, g);

        g.gridy++;
        formPanel.add(new JLabel("City (<=14 chars):"), g);
        g.gridy++;
        provCityField = new JTextField(12);
        formPanel.add(provCityField, g);

        g.gridy++;
        formPanel.add(new JLabel("State (2 letters):"), g);
        g.gridy++;
        provStateField = new JTextField(4);
        formPanel.add(provStateField, g);

        g.gridy++;
        formPanel.add(new JLabel("ZIP Code (5 digits):"), g);
        g.gridy++;
        provZipField = new JTextField(6);
        formPanel.add(provZipField, g);

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

        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Registered ChocAn Providers"));

        String[] cols = {"Provider ID", "Name (25)", "Street Address (25)", "City (14)", "State (2)", "ZIP (5)"};
        providerTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
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
        String state = provStateField.getText().trim();
        String zip = provZipField.getText().trim();
        if (!id.matches("\\d{9}")) {
            JOptionPane.showMessageDialog(this, "Provider Number must be exactly 9 numeric digits.", "Invalid ID", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Provider name cannot be empty.", "Missing Name", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!state.isEmpty() && !state.matches("[A-Za-z]{2}")) {
            JOptionPane.showMessageDialog(this, "State must be 2 letters (e.g., AL, OR, WA).", "Invalid State", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!zip.isEmpty() && !zip.matches("\\d{5}")) {
            JOptionPane.showMessageDialog(this, "ZIP Code must be 5 numeric digits.", "Invalid ZIP", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (Operator.findProviderById(id) != null) {
            JOptionPane.showMessageDialog(this, "A provider with ID " + id + " already exists.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Provider p = new Provider(id, name, provAddrField.getText().trim(), provCityField.getText().trim(),
                state, zip);

        if (Operator.addProvider(p)) {
            JOptionPane.showMessageDialog(this,
                    "Provider added successfully!\n\n"
                            + "Provider Name   : " + p.getName() + "\n"
                            + "Provider Number : " + p.getProviderId() + "\n\n"
                            + "This provider can now log in directly on the Login screen using their 9-digit Provider Number (" + p.getProviderId() + ")!",
                    "Provider Created", JOptionPane.INFORMATION_MESSAGE);
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
        String state = provStateField.getText().trim();
        if (!state.isEmpty() && !state.matches("[A-Za-z]{2}")) {
            JOptionPane.showMessageDialog(this, "State must be 2 letters.", "Invalid State", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String zip = provZipField.getText().trim();
        if (!zip.isEmpty() && !zip.matches("\\d{5}")) {
            JOptionPane.showMessageDialog(this, "ZIP Code must be 5 numeric digits.", "Invalid ZIP", JOptionPane.WARNING_MESSAGE);
            return;
        }

        existing.setName(provNameField.getText().trim());
        existing.setAddress(provAddrField.getText().trim());
        existing.setCity(provCityField.getText().trim());
        existing.setState(state);
        existing.setZip(zip);

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
        if (providerTableModel == null) return;
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

        JPanel topBar = createTopBar("MANAGER ACCOUNTING & REPORTING PORTAL",
                mgrUserLabel = new JLabel("Manager: -"),
                mgrModeBadge = createModeBadgeLabel(),
                e -> handleLogout());
        panel.add(topBar, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JButton mainAcctBtn = new JButton("Run Main Accounting Procedure");
        JButton memRepBtn = new JButton("Member Report");
        JButton provRepBtn = new JButton("Provider Report");
        JButton sumRepBtn = new JButton("Summary Report (A/P)");
        JButton eftRepBtn = new JButton("EFT Data File");
        JButton searchPayBtn = new JButton("Search Provider to Pay");
        JButton clearBtn = new JButton("Clear");

        mainAcctBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        mainAcctBtn.setForeground(new Color(15, 90, 40));
        memRepBtn.setForeground(new Color(20, 30, 40));
        provRepBtn.setForeground(new Color(20, 30, 40));
        sumRepBtn.setForeground(new Color(20, 30, 40));
        eftRepBtn.setForeground(new Color(20, 30, 40));
        searchPayBtn.setForeground(new Color(20, 30, 40));
        clearBtn.setForeground(new Color(20, 30, 40));

        mainAcctBtn.addActionListener(e -> {
            reportTextArea.setText(ReportService.runMainAccountingProcedure());
            reportTextArea.setCaretPosition(0);
        });
        memRepBtn.addActionListener(e -> onGenerateMemberReportGUI());
        provRepBtn.addActionListener(e -> onGenerateProviderReportGUI());
        sumRepBtn.addActionListener(e -> {
            reportTextArea.setText(ReportService.generateSummaryReport());
            reportTextArea.setCaretPosition(0);
        });
        eftRepBtn.addActionListener(e -> {
            reportTextArea.setText(ReportService.generateEftReport());
            reportTextArea.setCaretPosition(0);
        });
        searchPayBtn.addActionListener(e -> {
            String query = JOptionPane.showInputDialog(this,
                    "Enter Provider ID or Name to search payment details:",
                    "Search Providers Requiring Payment",
                    JOptionPane.QUESTION_MESSAGE);
            if (query != null && !query.isBlank()) {
                reportTextArea.setText(Manager.searchProvidersToPay(query));
                reportTextArea.setCaretPosition(0);
            }
        });
        clearBtn.addActionListener(e -> reportTextArea.setText(""));

        toolbar.add(mainAcctBtn);
        toolbar.add(memRepBtn);
        toolbar.add(provRepBtn);
        toolbar.add(sumRepBtn);
        toolbar.add(eftRepBtn);
        toolbar.add(searchPayBtn);
        toolbar.add(clearBtn);
        content.add(toolbar, BorderLayout.NORTH);

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

    private void onGenerateMemberReportGUI() {
        List<Member> members = Operator.getMembers();
        String[] options = new String[members.size() + 1];
        options[0] = "ALL Members Who Consulted a Provider This Week";
        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            options[i + 1] = m.getMemberId() + " - " + m.getName();
        }
        String choice = (String) JOptionPane.showInputDialog(this,
                "Select a Member to generate their ChocAn Member Report (or choose ALL):",
                "Generate ChocAn Member Report",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (choice == null) return;
        if (choice.equals(options[0])) {
            reportTextArea.setText(ReportService.generateAllMemberReports(true));
        } else {
            String memberId = choice.substring(0, 9);
            Member m = Operator.findMemberById(memberId);
            if (m != null) {
                reportTextArea.setText(ReportService.generateMemberReport(m));
            }
        }
        reportTextArea.setCaretPosition(0);
    }

    private void onGenerateProviderReportGUI() {
        List<Provider> providers = Operator.getProviders();
        String[] options = new String[providers.size() + 1];
        options[0] = "ALL Providers Who Billed ChocAn This Week";
        for (int i = 0; i < providers.size(); i++) {
            Provider p = providers.get(i);
            options[i + 1] = p.getProviderId() + " - " + p.getName();
        }
        String choice = (String) JOptionPane.showInputDialog(this,
                "Select a Provider to generate their ChocAn Provider Report (or choose ALL):",
                "Generate ChocAn Provider Report",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (choice == null) return;
        if (choice.equals(options[0])) {
            reportTextArea.setText(ReportService.generateAllProviderReports(true));
        } else {
            String providerId = choice.substring(0, 9);
            Provider p = Operator.findProviderById(providerId);
            if (p != null) {
                reportTextArea.setText(ReportService.generateProviderReport(p));
            }
        }
        reportTextArea.setCaretPosition(0);
    }

    // =========================================================================
    // 4. PROVIDER PAGE
    // =========================================================================
    private JPanel buildProviderPage() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topBar = createTopBar("CHOCAN PROVIDER TERMINAL",
                prvUserLabel = new JLabel("Active Provider: -"),
                prvModeBadge = createModeBadgeLabel(),
                e -> handleLogout());
        panel.add(topBar, BorderLayout.NORTH);

        JPanel mainWrapper = new JPanel(new BorderLayout(10, 10));
        mainWrapper.setBorder(new EmptyBorder(12, 15, 15, 15));

        // Top Bar inside Provider Terminal: Active Provider ID & Provider Directory Request
        JPanel terminalToolbar = new JPanel(new BorderLayout(10, 5));
        terminalToolbar.setBackground(Color.WHITE);
        terminalToolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel provSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        provSelectPanel.setOpaque(false);
        provSelectPanel.add(new JLabel("Active Terminal Provider Number:"));
        activeProviderCombo = new JComboBox<>();
        activeProviderCombo.setPreferredSize(new Dimension(280, 26));
        activeProviderCombo.addActionListener(e -> {
            Object sel = activeProviderCombo.getSelectedItem();
            if (sel != null) {
                String s = sel.toString();
                if (s.length() >= 9 && s.substring(0, 9).matches("\\d{9}")) {
                    String pid = s.substring(0, 9);
                    if (currentUser != null) {
                        currentUser.setProviderId(pid);
                    }
                    prvUserLabel.setText("Active Provider: " + s);
                }
            }
        });
        provSelectPanel.add(activeProviderCombo);
        terminalToolbar.add(provSelectPanel, BorderLayout.WEST);

        JButton dirBtn = new JButton("Request Provider Directory (View & Save File)");
        dirBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dirBtn.setForeground(new Color(24, 43, 73));
        dirBtn.addActionListener(e -> onRequestProviderDirectory());
        terminalToolbar.add(dirBtn, BorderLayout.EAST);

        mainWrapper.add(terminalToolbar, BorderLayout.NORTH);

        // Split Page: Left (Validate Member Card), Right (Bill ChocAn / Submit Service Record)
        JPanel content = new JPanel(new GridLayout(1, 2, 15, 15));

        // Card 1: Validate Member Card
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
        g.gridx = 0;
        g.gridy = 0;

        JLabel valPrompt = new JLabel("Slide Card or Enter 9-Digit Member Number:");
        valPrompt.setForeground(new Color(40, 50, 60));
        valCenter.add(valPrompt, g);

        g.gridy++;
        valMemberIdField = new JTextField(15);
        valMemberIdField.addActionListener(e -> onValidateMember());
        valCenter.add(valMemberIdField, g);

        g.gridy++;
        JButton valBtn = new JButton("Verify Member Number");
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
        g.insets = new Insets(4, 6, 12, 6);
        valResultDetails = new JLabel("Enter a 9-digit member number to verify status.", SwingConstants.CENTER);
        valResultDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valResultDetails.setForeground(new Color(70, 80, 90));
        valCenter.add(valResultDetails, g);

        // Last Billed Service Fee Terminal Display Banner
        g.gridy++;
        g.insets = new Insets(15, 6, 4, 6);
        lastFeeBannerLabel = new JLabel("Terminal Fee Display: $0.00 (No service billed yet)", SwingConstants.CENTER);
        lastFeeBannerLabel.setOpaque(true);
        lastFeeBannerLabel.setBackground(new Color(235, 242, 250));
        lastFeeBannerLabel.setForeground(new Color(24, 43, 73));
        lastFeeBannerLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lastFeeBannerLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 215, 228), 1),
                new EmptyBorder(10, 8, 10, 8)
        ));
        valCenter.add(lastFeeBannerLabel, g);

        valCard.add(valCenter, BorderLayout.CENTER);
        content.add(valCard);

        // Card 2: Bill ChocAn (Submit Service Record)
        JPanel srvCard = new JPanel(new BorderLayout(10, 10));
        srvCard.setBackground(Color.WHITE);
        srvCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel srvHeader = new JLabel("2. Bill ChocAn (Submit Service Record)", SwingConstants.LEFT);
        srvHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        srvHeader.setForeground(new Color(30, 40, 50));
        srvCard.add(srvHeader, BorderLayout.NORTH);

        JPanel srvForm = new JPanel(new GridBagLayout());
        srvForm.setOpaque(false);
        GridBagConstraints sg = new GridBagConstraints();
        sg.insets = new Insets(3, 6, 3, 6);
        sg.fill = GridBagConstraints.HORIZONTAL;
        sg.gridx = 0;
        sg.gridy = 0;

        srvForm.add(new JLabel("Member Number (9 digits):"), sg);
        sg.gridy++;
        srvMemberIdField = new JTextField(12);
        srvForm.add(srvMemberIdField, sg);

        sg.gridy++;
        srvForm.add(new JLabel("Date Service Provided (MM-DD-YYYY):"), sg);
        sg.gridy++;
        srvDateField = new JTextField(LocalDate.now().format(Operator.DATE_FMT));
        srvForm.add(srvDateField, sg);

        sg.gridy++;
        srvForm.add(new JLabel("Provider Directory Lookup (Quick Fill Code):"), sg);
        sg.gridy++;
        srvDirectoryCombo = new JComboBox<>();
        srvDirectoryCombo.addItem("-- Select Service from Directory or Type 6-Digit Code Below --");
        for (ServiceEntry entry : ProviderDirectory.getAllServices()) {
            srvDirectoryCombo.addItem(entry.getServiceCode() + " - " + entry.getServiceName() + String.format(" ($%.2f)", entry.getFee()));
        }
        srvDirectoryCombo.addActionListener(e -> {
            int idx = srvDirectoryCombo.getSelectedIndex();
            if (idx > 0) {
                String item = (String) srvDirectoryCombo.getSelectedItem();
                if (item != null && item.length() >= 6) {
                    String code = item.substring(0, 6);
                    srvCodeField.setText(code);
                    updateLookedUpServicePreview();
                }
            }
        });
        srvForm.add(srvDirectoryCombo, sg);

        sg.gridy++;
        srvForm.add(new JLabel("6-Digit Service Code:"), sg);
        sg.gridy++;
        JPanel codeRow = new JPanel(new BorderLayout(6, 0));
        codeRow.setOpaque(false);
        srvCodeField = new JTextField(10);
        srvCodeField.addActionListener(e -> updateLookedUpServicePreview());
        JButton lookupBtn = new JButton("Lookup Code");
        lookupBtn.addActionListener(e -> updateLookedUpServicePreview());
        codeRow.add(srvCodeField, BorderLayout.CENTER);
        codeRow.add(lookupBtn, BorderLayout.EAST);
        srvForm.add(codeRow, sg);

        sg.gridy++;
        srvLookedUpLabel = new JLabel("Service Name: (enter 6-digit code) | Fee: $0.00");
        srvLookedUpLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        srvLookedUpLabel.setForeground(new Color(24, 43, 73));
        srvForm.add(srvLookedUpLabel, sg);

        sg.gridy++;
        srvForm.add(new JLabel("Comments (Optional, up to 100 chars):"), sg);
        sg.gridy++;
        srvCommentsField = new JTextField(15);
        srvForm.add(srvCommentsField, sg);

        sg.gridy++;
        JButton submitSrvBtn = new JButton("Verify & Submit Service Record");
        submitSrvBtn.setForeground(new Color(20, 30, 40));
        submitSrvBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submitSrvBtn.addActionListener(e -> onSubmitService());
        srvForm.add(submitSrvBtn, sg);

        srvCard.add(srvForm, BorderLayout.NORTH);

        // Recorded Service Consultations Log Table
        String[] srvCols = {"Received Date/Time", "Service Date", "Provider ID", "Member ID", "Code", "Service (20)", "Fee", "Comments"};
        srvLogModel = new DefaultTableModel(srvCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable srvTable = new JTable(srvLogModel);
        JScrollPane srvScroll = new JScrollPane(srvTable);
        srvScroll.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Recorded Service Consultations (Disk / Database)"));
        srvCard.add(srvScroll, BorderLayout.CENTER);

        content.add(srvCard);
        mainWrapper.add(content, BorderLayout.CENTER);
        panel.add(mainWrapper, BorderLayout.CENTER);
        return panel;
    }

    private void refreshProviderTerminalState() {
        activeProviderCombo.removeAllItems();
        List<Provider> providers = Operator.getProviders();
        int selectedIdx = 0;
        String userProvId = currentUser != null ? currentUser.getProviderId() : null;
        for (int i = 0; i < providers.size(); i++) {
            Provider p = providers.get(i);
            activeProviderCombo.addItem(p.getProviderId() + " - " + p.getName());
            if (userProvId != null && userProvId.equals(p.getProviderId())) {
                selectedIdx = i;
            }
        }
        if (activeProviderCombo.getItemCount() > 0) {
            activeProviderCombo.setSelectedIndex(selectedIdx);
        }
        resetProviderValidation();
        refreshServiceRecordsTable();
    }

    private String getSelectedProviderId() {
        Object sel = activeProviderCombo.getSelectedItem();
        if (sel != null) {
            String s = sel.toString();
            if (s.length() >= 9 && s.substring(0, 9).matches("\\d{9}")) {
                return s.substring(0, 9);
            }
        }
        return currentUser != null && currentUser.getProviderId() != null ? currentUser.getProviderId() : "111222333";
    }

    private void updateLookedUpServicePreview() {
        String code = srvCodeField.getText().trim();
        ServiceEntry entry = ProviderDirectory.findServiceByCode(code);
        if (entry == null) {
            srvLookedUpLabel.setText("Service Name: [Nonexistent Code] | Fee: --");
            srvLookedUpLabel.setForeground(new Color(160, 25, 25));
        } else {
            srvLookedUpLabel.setText(String.format("Service Name: %s | Fee: $%.2f", entry.getServiceName(), entry.getFee()));
            srvLookedUpLabel.setForeground(new Color(15, 90, 40));
        }
    }

    private void onRequestProviderDirectory() {
        String dirText = ProviderDirectory.formatDirectory();
        File savedFile = ProviderDirectory.writeDirectoryToFile();
        String fileNote = savedFile != null
                ? "\nSaved file (simulated email attachment): " + savedFile.getPath()
                : "";

        JTextArea area = new JTextArea(dirText + fileNote, 18, 68);
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "ChocAn Provider Directory", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onValidateMember() {
        String id = valMemberIdField.getText().trim();
        String status = Operator.validateMemberCard(id);

        if ("Invalid number".equals(status)) {
            setValidationResult("Invalid number", new Color(254, 226, 226), new Color(153, 27, 27),
                    "Member number is invalid or not found in ChocAn Data Center.");
        } else if ("Member suspended".equals(status)) {
            Member member = Operator.findMemberById(id);
            String name = member != null ? member.getName() : id;
            setValidationResult("Member suspended", new Color(255, 243, 205), new Color(146, 64, 14),
                    "Membership suspended (fees owed): " + name);
        } else {
            Member member = Operator.findMemberById(id);
            String name = member != null ? member.getName() : id;
            setValidationResult("Validated", new Color(220, 245, 225), new Color(15, 90, 40),
                    "Active Member: " + name + " (" + id + ")");
            srvMemberIdField.setText(id);
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
        valResultDetails.setText("Enter a 9-digit member number to verify status.");
        lastFeeBannerLabel.setText("Terminal Fee Display: $0.00 (No service billed yet)");
        srvMemberIdField.setText("");
        srvDateField.setText(LocalDate.now().format(Operator.DATE_FMT));
        srvCodeField.setText("");
        srvDirectoryCombo.setSelectedIndex(0);
        srvLookedUpLabel.setText("Service Name: (enter 6-digit code) | Fee: $0.00");
        srvLookedUpLabel.setForeground(new Color(24, 43, 73));
        srvCommentsField.setText("");
    }

    private void onSubmitService() {
        String providerId = getSelectedProviderId();
        String memId = srvMemberIdField.getText().trim();
        String date = srvDateField.getText().trim();
        String code = srvCodeField.getText().trim();
        String comments = srvCommentsField.getText().trim();

        // Step 1: Re-validate member card per ChocAn billing requirement and update terminal display badge
        valMemberIdField.setText(memId);
        onValidateMember();
        String status = Operator.validateMemberCard(memId);
        if (!"Validated".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "Terminal Display: " + status + "\nCannot bill ChocAn unless member status is 'Validated'.",
                    status, JOptionPane.ERROR_MESSAGE);
            return;
        }
        Member member = Operator.findMemberById(memId);

        // Step 2: Validate service date (MM-DD-YYYY)
        if (!Operator.isValidDate(date)) {
            JOptionPane.showMessageDialog(this,
                    "Date of service must be in format MM-DD-YYYY (e.g., 11-20-2026).",
                    "Invalid Service Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Step 3: Lookup 6-digit service code in Provider Directory
        if (!code.matches("\\d{6}")) {
            JOptionPane.showMessageDialog(this,
                    "Service code must be a 6-digit number.",
                    "Invalid Service Code", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ServiceEntry serviceEntry = ProviderDirectory.findServiceByCode(code);
        if (serviceEntry == null) {
            updateLookedUpServicePreview();
            JOptionPane.showMessageDialog(this,
                    "Error: Nonexistent 6-digit service code '" + code + "'.\nPlease consult the Provider Directory.",
                    "Nonexistent Service Code", JOptionPane.ERROR_MESSAGE);
            return;
        }
        updateLookedUpServicePreview();

        // Step 4: Display 20-char service name and ask provider to verify
        int verify = JOptionPane.showConfirmDialog(this,
                "Service Code: " + serviceEntry.getServiceCode() + "\n"
                        + "Service Name (<=20 chars): " + serviceEntry.getServiceName() + "\n\n"
                        + "Please verify: Is '" + serviceEntry.getServiceName() + "' indeed the service that was provided?",
                "Verify Service Provided",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (verify != JOptionPane.YES_OPTION) {
            return;
        }

        // Step 5: Truncate comments to 100 characters if necessary
        if (comments.length() > 100) {
            comments = comments.substring(0, 100);
        }

        // Step 6: Write service record to disk/database
        String currentDateTime = LocalDateTime.now().format(Operator.DATETIME_FMT);
        ServiceRecord record = new ServiceRecord(currentDateTime, date, providerId, memId, code, comments);

        if (Operator.addServiceRecord(record)) {
            refreshServiceRecordsTable();
            // Step 7: Look up and display fee to be paid on provider's terminal
            lastFeeBannerLabel.setText(String.format("FEE TO BE PAID: $%.2f  (%s - Code %s)",
                    serviceEntry.getFee(), serviceEntry.getServiceName(), serviceEntry.getServiceCode()));

            JOptionPane.showMessageDialog(this,
                    "Service Record Written to Disk!\n\n"
                            + "Current Date & Time  : " + currentDateTime + "\n"
                            + "Date Service Provided: " + date + "\n"
                            + "Provider Number      : " + providerId + "\n"
                            + "Member Name & Number : " + (member != null ? member.getName() : "") + " (" + memId + ")\n"
                            + "Service Code & Name  : " + serviceEntry.getServiceCode() + " (" + serviceEntry.getServiceName() + ")\n"
                            + "Comments             : " + (comments.isEmpty() ? "(none)" : comments) + "\n"
                            + String.format("FEE TO BE PAID       : $%.2f", serviceEntry.getFee()),
                    "Service Billed — Fee Displayed",
                    JOptionPane.INFORMATION_MESSAGE);

            srvCodeField.setText("");
            srvDirectoryCombo.setSelectedIndex(0);
            srvCommentsField.setText("");
            srvLookedUpLabel.setText("Service Name: (enter 6-digit code) | Fee: $0.00");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to write service record.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshServiceRecordsTable() {
        if (srvLogModel == null) return;
        srvLogModel.setRowCount(0);
        for (ServiceRecord sr : Operator.getServiceRecords()) {
            srvLogModel.addRow(new Object[]{
                    sr.getCurrentDateTime(),
                    sr.getServiceDate(),
                    sr.getProviderId(),
                    sr.getMemberId(),
                    sr.getServiceCode(),
                    sr.getServiceName(),
                    String.format("$%.2f", sr.getFee()),
                    sr.getComments()
            });
        }
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

        JLabel title = new JLabel(portalTitle);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(24, 43, 73));

        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(new Color(65, 80, 100));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title);
        left.add(userLabel);
        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        right.setOpaque(false);

        JButton switchModeBtn = new JButton("Toggle Mode");
        switchModeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        switchModeBtn.setForeground(new Color(20, 30, 40));
        switchModeBtn.addActionListener(e -> {
            boolean newMode = !Operator.isUseDatabase();
            Operator.setUseDatabase(newMode);
            modeComboBox.setSelectedIndex(Operator.isUseDatabase() ? 1 : 0);
            updateAllModeBadges();
            refreshMemberTable();
            refreshProviderTable();
            refreshServiceRecordsTable();
            JOptionPane.showMessageDialog(this, "Switched to: " + Operator.getModeName(), "Mode Changed", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setForeground(new Color(160, 25, 25));
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
            label.setForeground(new Color(15, 90, 40));
        } else {
            label.setText("LOCAL PERSISTENT MODE");
            label.setBackground(new Color(255, 243, 205));
            label.setForeground(new Color(146, 64, 14));
        }
    }

    private void updateAllModeBadges() {
        if (opModeBadge != null) updateBadgeStyle(opModeBadge);
        if (mgrModeBadge != null) updateBadgeStyle(mgrModeBadge);
        if (prvModeBadge != null) updateBadgeStyle(prvModeBadge);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            ChocAnGUI gui = new ChocAnGUI();
            gui.setVisible(true);
        });
    }
}

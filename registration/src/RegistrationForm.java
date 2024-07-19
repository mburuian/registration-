import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Date;
import java.util.Calendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;
import javax.swing.table.DefaultTableModel;

public class RegistrationForm extends JFrame {
    private JTextField nameField, mobileField;
    private JRadioButton maleButton, femaleButton;
    private JComboBox<String> dayComboBox, monthComboBox, yearComboBox;
    private JTextArea addressArea;
    private JCheckBox termsCheckBox;
    private JButton submitButton, resetButton;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    // Update these with your actual database information
    private static final String DB_URL = "jdbc:mysql://localhost:3306/registration_db";
    private static final String USER = "root";
    private static final String PASS = "Mburuian";

    public RegistrationForm() {
        setTitle("Registration Form");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        addComponent(new JLabel("Name:"), gbc, 0, 0);
        nameField = new JTextField(20);
        addComponent(nameField, gbc, 1, 0);

        // Mobile
        addComponent(new JLabel("Mobile:"), gbc, 0, 1);
        mobileField = new JTextField(20);
        addComponent(mobileField, gbc, 1, 1);

        // Gender
        addComponent(new JLabel("Gender:"), gbc, 0, 2);
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maleButton = new JRadioButton("Male");
        femaleButton = new JRadioButton("Female");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleButton);
        genderGroup.add(femaleButton);
        genderPanel.add(maleButton);
        genderPanel.add(femaleButton);
        addComponent(genderPanel, gbc, 1, 2);

        // DOB
        addComponent(new JLabel("DOB:"), gbc, 0, 3);
        JPanel dobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dayComboBox = new JComboBox<>(getDays());
        monthComboBox = new JComboBox<>(getMonths());
        yearComboBox = new JComboBox<>(getYears());
        dobPanel.add(dayComboBox);
        dobPanel.add(monthComboBox);
        dobPanel.add(yearComboBox);
        addComponent(dobPanel, gbc, 1, 3);

        // Address
        addComponent(new JLabel("Address:"), gbc, 0, 4);
        addressArea = new JTextArea(4, 20);
        addComponent(new JScrollPane(addressArea), gbc, 1, 4);

        // Terms and Conditions
        gbc.gridwidth = 2;
        termsCheckBox = new JCheckBox("Accept Terms And Conditions");
        addComponent(termsCheckBox, gbc, 0, 5);

        // Buttons
        gbc.gridwidth = 1;
        submitButton = new JButton("Submit");
        addComponent(submitButton, gbc, 0, 6);
        resetButton = new JButton("Reset");
        addComponent(resetButton, gbc, 1, 6);

        submitButton.addActionListener(e -> submitForm());
        resetButton.addActionListener(e -> resetForm());

        // Table to display data
        String[] columnNames = { "ID", "Name", "Mobile", "Gender", "DOB", "Address" };
        tableModel = new DefaultTableModel(columnNames, 0);
        dataTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(dataTable);

        gbc.gridwidth = 2;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 7;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollPane, gbc);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Load data from database
        loadData();
    }

    private void addComponent(Component component, GridBagConstraints gbc, int gridx, int gridy) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        add(component, gbc);
    }

    private String[] getDays() {
        String[] days = new String[31];
        for (int i = 1; i <= 31; i++) {
            days[i - 1] = String.format("%02d", i);
        }
        return days;
    }

    private String[] getMonths() {
        String[] months = new String[12];
        for (int i = 1; i <= 12; i++) {
            months[i - 1] = String.format("%02d", i);
        }
        return months;
    }

    private String[] getYears() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int startYear = currentYear - 100; // Adjust range as needed
        String[] years = new String[101];
        for (int i = 0; i <= 100; i++) {
            years[i] = Integer.toString(startYear + i);
        }
        return years;
    }

    private void submitForm() {
        if (!termsCheckBox.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please accept the terms and conditions.");
            return;
        }

        String name = nameField.getText().trim();
        String mobile = mobileField.getText().trim();
        String gender = maleButton.isSelected() ? "Male" : (femaleButton.isSelected() ? "Female" : "");
        String day = (String) dayComboBox.getSelectedItem();
        String month = (String) monthComboBox.getSelectedItem();
        String year = (String) yearComboBox.getSelectedItem();
        String dobString = year + "-" + month + "-" + day;
        String address = addressArea.getText().trim();

        if (name.isEmpty() || mobile.isEmpty() || gender.isEmpty() || dobString.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        if (!Pattern.matches("\\d{10}", mobile)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit mobile number.");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dob = sdf.parse(dobString);

            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                    PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO registrations (name, mobile, gender, dob, address) VALUES (?, ?, ?, ?, ?)")) {

                pstmt.setString(1, name);
                pstmt.setString(2, mobile);
                pstmt.setString(3, gender);
                pstmt.setDate(4, new java.sql.Date(dob.getTime()));
                pstmt.setString(5, address);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Registration successful!");
                    resetForm();
                    loadData(); // Reload data after successful registration
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed.");
                }
            }
        } catch (ClassNotFoundException | SQLException | ParseException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void resetForm() {
        nameField.setText("");
        mobileField.setText("");
        maleButton.setSelected(false);
        femaleButton.setSelected(false);
        dayComboBox.setSelectedIndex(0);
        monthComboBox.setSelectedIndex(0);
        yearComboBox.setSelectedIndex(0);
        addressArea.setText("");
        termsCheckBox.setSelected(false);
    }

    private void loadData() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM registrations")) {

                tableModel.setRowCount(0); // Clear existing data

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String mobile = rs.getString("mobile");
                    String gender = rs.getString("gender");
                    Date dob = rs.getDate("dob");
                    String address = rs.getString("address");

                    tableModel.addRow(new Object[] { id, name, mobile, gender, dob, address });
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegistrationForm::new);
    }
}

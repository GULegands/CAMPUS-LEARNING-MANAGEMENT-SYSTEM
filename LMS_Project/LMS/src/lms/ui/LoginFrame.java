package lms.ui;

import lms.dao.UserDAO;
import lms.exception.LMSException;
import lms.model.*;
import lms.service.ActivityLogger;

import javax.swing.*;
import java.awt.*;

/** Login / registration window. Opens the dashboard that matches the user's role. */
public class LoginFrame extends JFrame {
    private final UserDAO userDAO = new UserDAO();
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    public LoginFrame() {
        setTitle("Online Learning Management System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Email:"));    form.add(emailField);
        form.add(new JLabel("Password:")); form.add(passwordField);

        JPanel buttons = new JPanel();
        buttons.add(UIUtil.button("Login", UIUtil.safe(this, this::login)));
        buttons.add(UIUtil.button("Register as Student", UIUtil.safe(this, this::register)));

        JLabel heading = new JLabel("Online Learning Management System", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        root.add(heading, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton((JButton) buttons.getComponent(0));
        pack();
        setLocationRelativeTo(null);
    }

    private void login() throws LMSException {
        User user = userDAO.authenticate(emailField.getText().trim(), new String(passwordField.getPassword()));
        ActivityLogger.getInstance().log(user.getRole() + " logged in: " + user.getEmail());
        dispose();
        switch (user.getRole()) {              // each role opens its own dashboard
            case ADMIN:      new AdminDashboard(user).setVisible(true); break;
            case INSTRUCTOR: new InstructorDashboard(user).setVisible(true); break;
            default:         new StudentDashboard(user).setVisible(true);
        }
    }

    private void register() throws LMSException {
        JTextField name = new JTextField(), email = new JTextField();
        JPasswordField pw = new JPasswordField();
        if (!UIUtil.form(this, "Student Registration", new String[]{"Name", "Email", "Password"},
                         new JComponent[]{name, email, pw})) return;
        userDAO.add(new Student(0, name.getText().trim(), email.getText().trim(), new String(pw.getPassword())));
        ActivityLogger.getInstance().log("New student registered: " + email.getText().trim());
        UIUtil.info(this, "Registration successful. You can log in now.");
    }
}

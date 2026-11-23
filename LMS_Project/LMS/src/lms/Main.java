package lms;

import lms.dao.UserDAO;
import lms.exception.LMSException;
import lms.ui.LoginFrame;

import javax.swing.*;

/** Application entry point. */
public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> {
            try {
                new UserDAO().ensureDefaultAdmin();       // creates admin@lms.com / admin123 on first run
                new LoginFrame().setVisible(true);
            } catch (LMSException e) {
                JOptionPane.showMessageDialog(null, "Cannot start the application:\n" + e.getMessage()
                    + "\n\nCheck MySQL is running, sql/schema.sql was executed and DBConnection.java credentials are correct.",
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}

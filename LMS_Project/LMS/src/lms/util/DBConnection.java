package lms.util;

import lms.exception.LMSException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Central place for JDBC connection settings. EDIT USER/PASSWORD for your MySQL. */
public final class DBConnection {
    private static final String URL =
        "jdbc:mysql://localhost:3306/lms_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private DBConnection() {}

    /** Opens a new connection (callers close it with try-with-resources). */
    public static Connection getConnection() throws LMSException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new LMSException("Database connection failed: " + e.getMessage(), e);
        }
    }
}

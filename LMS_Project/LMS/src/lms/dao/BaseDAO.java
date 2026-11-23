package lms.dao;

import lms.exception.LMSException;
import lms.util.DBConnection;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/** Shared JDBC helper methods (PreparedStatements, mapping rows, building table models). */
public abstract class BaseDAO {

    /** Converts one ResultSet row into an object. */
    @FunctionalInterface
    protected interface RowMapper<T> { T map(ResultSet rs) throws SQLException; }

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
    }

    /** Runs INSERT / UPDATE / DELETE. */
    protected int execute(String sql, Object... params) throws LMSException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new LMSException("Database error: " + e.getMessage(), e);
        }
    }

    /** Runs a SELECT and maps every row to an object. */
    protected <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) throws LMSException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) list.add(mapper.map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new LMSException("Database error: " + e.getMessage(), e);
        }
    }

    /** Returns the first integer of the first row (e.g. COUNT(*)). */
    protected int scalar(String sql, Object... params) throws LMSException {
        List<Integer> r = query(sql, rs -> rs.getInt(1), params);
        return r.isEmpty() ? 0 : r.get(0);
    }

    /** Runs a SELECT and returns a read-only table model for a JTable. */
    protected DefaultTableModel tableModel(String sql, Object... params) throws LMSException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                Vector<String> cols = new Vector<>();
                for (int i = 1; i <= n; i++) cols.add(md.getColumnLabel(i));
                Vector<Vector<Object>> rows = new Vector<>();
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    for (int i = 1; i <= n; i++) row.add(rs.getObject(i));
                    rows.add(row);
                }
                return new DefaultTableModel(rows, cols) {
                    @Override public boolean isCellEditable(int r, int c2) { return false; }
                };
            }
        } catch (SQLException e) {
            throw new LMSException("Database error: " + e.getMessage(), e);
        }
    }
}

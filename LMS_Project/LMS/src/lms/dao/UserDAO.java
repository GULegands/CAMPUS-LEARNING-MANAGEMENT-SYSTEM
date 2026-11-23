package lms.dao;

import lms.exception.LMSException;
import lms.model.*;
import lms.util.PasswordUtil;

import javax.swing.table.DefaultTableModel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Database operations for users: CRUD, login, role queries. */
public class UserDAO extends BaseDAO implements Dao<User> {

    private static final RowMapper<User> MAPPER = rs -> UserFactory.create(
        rs.getInt("id"), rs.getString("name"), rs.getString("email"),
        rs.getString("password"), Role.valueOf(rs.getString("role")));

    private void validate(User u, boolean checkPassword) throws LMSException {
        if (u.getName() == null || u.getName().trim().isEmpty()) throw new LMSException("Name is required.");
        if (u.getEmail() == null || !u.getEmail().contains("@")) throw new LMSException("A valid email is required.");
        if (checkPassword && (u.getPassword() == null || u.getPassword().length() < 4))
            throw new LMSException("Password must be at least 4 characters.");
    }

    @Override public void add(User u) throws LMSException {
        validate(u, true);
        if (findByEmail(u.getEmail()) != null) throw new LMSException("Email is already registered.");
        execute("INSERT INTO users(name,email,password,role) VALUES(?,?,?,?)",
                u.getName(), u.getEmail(), PasswordUtil.hash(u.getPassword()), u.getRole().name());
    }

    @Override public void update(User u) throws LMSException {
        validate(u, false);
        execute("UPDATE users SET name=?, email=?, role=? WHERE id=?",
                u.getName(), u.getEmail(), u.getRole().name(), u.getId());
    }

    @Override public void delete(int id) throws LMSException { execute("DELETE FROM users WHERE id=?", id); }

    @Override public User findById(int id) throws LMSException {
        List<User> r = query("SELECT * FROM users WHERE id=?", MAPPER, id);
        if (r.isEmpty()) throw new LMSException("User not found.");
        return r.get(0);
    }

    @Override public List<User> findAll() throws LMSException { return query("SELECT * FROM users", MAPPER); }

    public User findByEmail(String email) throws LMSException {
        List<User> r = query("SELECT * FROM users WHERE email=?", MAPPER, email);
        return r.isEmpty() ? null : r.get(0);
    }

    public List<User> findByRole(Role role) throws LMSException {
        return query("SELECT * FROM users WHERE role=?", MAPPER, role.name());
    }

    /** Validates login credentials; throws LMSException when they are wrong. */
    public User authenticate(String email, String password) throws LMSException {
        List<User> r = query("SELECT * FROM users WHERE email=? AND password=?", MAPPER,
                             email, PasswordUtil.hash(password));
        if (r.isEmpty()) throw new LMSException("Invalid email or password.");
        return r.get(0);
    }

    public DefaultTableModel tableModel() throws LMSException {
        return tableModel("SELECT id AS ID, name AS Name, email AS Email, role AS Role FROM users ORDER BY id");
    }

    /** Number of users per role (for analytics). */
    public Map<String, Integer> userStats() throws LMSException {
        DefaultTableModel m = tableModel("SELECT role, COUNT(*) FROM users GROUP BY role");
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < m.getRowCount(); i++)
            map.put(String.valueOf(m.getValueAt(i, 0)), ((Number) m.getValueAt(i, 1)).intValue());
        return map;
    }

    /** Creates the first admin account (admin@lms.com / admin123) if none exists. */
    public void ensureDefaultAdmin() throws LMSException {
        if (findByRole(Role.ADMIN).isEmpty()) add(new Admin(0, "Administrator", "admin@lms.com", "admin123"));
    }
}

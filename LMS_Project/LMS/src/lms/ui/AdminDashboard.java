package lms.ui;

import lms.dao.CourseDAO;
import lms.dao.SystemDAO;
import lms.dao.UserDAO;
import lms.exception.LMSException;
import lms.model.*;
import lms.service.ActivityLogger;
import lms.service.ActivityMonitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Admin dashboard: users, courses, analytics, settings and live activity monitoring. */
public class AdminDashboard extends JFrame {
    private final User admin;
    private final UserDAO userDAO = new UserDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final SystemDAO systemDAO = new SystemDAO();
    private final ActivityLogger logger = ActivityLogger.getInstance();

    private final JTable usersTable = new JTable(), coursesTable = new JTable(), analyticsTable = new JTable();
    private final BarChartPanel chart = new BarChartPanel("Enrollments per course");
    private final JLabel userStatsLabel = new JLabel(" ");
    private final JTextArea activityArea = new JTextArea();
    private final Map<String, JTextField> settingFields = new LinkedHashMap<>();
    private final ActivityMonitor monitor;

    public AdminDashboard(User admin) {
        this.admin = admin;
        setTitle(admin.getDashboardTitle());
        setSize(980, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(UIUtil.logoutBar(this));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("User Management", usersPanel());
        tabs.addTab("Course Management", coursesPanel());
        tabs.addTab("Performance Analytics", analyticsPanel());
        tabs.addTab("System Settings", settingsPanel());
        tabs.addTab("Activity Monitor", activityPanel());
        add(tabs);

        monitor = new ActivityMonitor(items -> activityArea.setText(String.join("\n", items)), 3000);
        Thread t = new Thread(monitor, "activity-monitor");
        t.setDaemon(true);
        t.start();
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { monitor.stop(); }
        });
        try { loadAll(); } catch (LMSException e) { UIUtil.error(this, e); }
    }

    private void loadAll() throws LMSException { loadUsers(); loadCourses(); loadAnalytics(); loadSettings(); }

    // ---------- Users ----------
    private JPanel usersPanel() {
        return UIUtil.tablePanel(usersTable,
            UIUtil.button("Add User", UIUtil.safe(this, this::addUser)),
            UIUtil.button("Edit User", UIUtil.safe(this, this::editUser)),
            UIUtil.button("Delete User", UIUtil.safe(this, this::deleteUser)),
            UIUtil.button("Refresh", UIUtil.safe(this, this::loadUsers)));
    }
    private void loadUsers() throws LMSException { usersTable.setModel(userDAO.tableModel()); }

    private void addUser() throws LMSException {
        JTextField name = new JTextField(), email = new JTextField();
        JPasswordField pw = new JPasswordField();
        JComboBox<Role> role = new JComboBox<>(Role.values());
        if (!UIUtil.form(this, "Add User", new String[]{"Name", "Email", "Password", "Role"},
                         new JComponent[]{name, email, pw, role})) return;
        User u = UserFactory.create(0, name.getText().trim(), email.getText().trim(),
                                    new String(pw.getPassword()), (Role) role.getSelectedItem());
        userDAO.add(u);
        logger.log("Admin created user " + u.getEmail());
        UIUtil.info(this, "User created successfully.");
        loadUsers();
    }

    private void editUser() throws LMSException {
        User u = userDAO.findById(UIUtil.selectedId(usersTable));
        JTextField name = new JTextField(u.getName()), email = new JTextField(u.getEmail());
        JComboBox<Role> role = new JComboBox<>(Role.values());
        role.setSelectedItem(u.getRole());
        if (!UIUtil.form(this, "Edit User", new String[]{"Name", "Email", "Role"},
                         new JComponent[]{name, email, role})) return;
        User updated = UserFactory.create(u.getId(), name.getText().trim(), email.getText().trim(),
                                          u.getPassword(), (Role) role.getSelectedItem());
        userDAO.update(updated);
        logger.log("Admin updated user " + updated.getEmail());
        UIUtil.info(this, "User updated successfully.");
        loadUsers();
    }

    private void deleteUser() throws LMSException {
        int id = UIUtil.selectedId(usersTable);
        if (id == admin.getId()) throw new LMSException("You cannot delete your own account.");
        if (!UIUtil.confirm(this, "Delete this user?")) return;
        userDAO.delete(id);
        logger.log("Admin deleted user id " + id);
        UIUtil.info(this, "User deleted successfully.");
        loadUsers();
    }

    // ---------- Courses ----------
    private JPanel coursesPanel() {
        return UIUtil.tablePanel(coursesTable,
            UIUtil.button("Create Course", UIUtil.safe(this, () -> courseForm(null))),
            UIUtil.button("Edit Course", UIUtil.safe(this,
                () -> courseForm(courseDAO.findById(UIUtil.selectedId(coursesTable))))),
            UIUtil.button("Delete Course", UIUtil.safe(this, this::deleteCourse)),
            UIUtil.button("Refresh", UIUtil.safe(this, this::loadCourses)));
    }
    private void loadCourses() throws LMSException { coursesTable.setModel(courseDAO.tableModel()); }

    /** Used for both create (existing == null) and edit. */
    private void courseForm(Course existing) throws LMSException {
        JTextField title = new JTextField(existing == null ? "" : existing.getTitle());
        JTextArea desc = new JTextArea(existing == null ? "" : existing.getDescription(), 3, 20);
        JTextArea syl = new JTextArea(existing == null ? "" : existing.getSyllabus(), 3, 20);
        List<User> instructors = userDAO.findByRole(Role.INSTRUCTOR);
        JComboBox<User> inst = new JComboBox<>(instructors.toArray(new User[0]));
        if (existing != null)
            for (User i : instructors) if (i.getId() == existing.getInstructorId()) inst.setSelectedItem(i);
        if (!UIUtil.form(this, existing == null ? "Create Course" : "Edit Course",
                new String[]{"Title", "Description", "Syllabus", "Instructor"},
                new JComponent[]{title, UIUtil.scroll(desc), UIUtil.scroll(syl), inst})) return;
        User chosen = (User) inst.getSelectedItem();
        int instructorId = chosen == null ? 0 : chosen.getId();
        if (existing == null) {
            courseDAO.add(new Course(0, title.getText().trim(), desc.getText(), syl.getText(), instructorId));
            logger.log("Admin created course " + title.getText().trim());
            UIUtil.info(this, "Course created successfully.");
        } else {
            existing.setTitle(title.getText().trim()); existing.setDescription(desc.getText());
            existing.setSyllabus(syl.getText()); existing.setInstructorId(instructorId);
            courseDAO.update(existing);
            logger.log("Admin updated course " + existing.getTitle());
            UIUtil.info(this, "Course updated successfully.");
        }
        loadCourses();
    }
    private void deleteCourse() throws LMSException {
        int id = UIUtil.selectedId(coursesTable);
        if (!UIUtil.confirm(this, "Delete this course and all its enrollments?")) return;
        courseDAO.delete(id);
        logger.log("Admin deleted course id " + id);
        UIUtil.info(this, "Course deleted successfully.");
        loadCourses();
    }

    // ---------- Analytics ----------
    private JPanel analyticsPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(userStatsLabel, BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(analyticsTable), chart);
        split.setResizeWeight(0.4);
        p.add(split, BorderLayout.CENTER);
        p.add(UIUtil.button("Refresh", UIUtil.safe(this, this::loadAnalytics)), BorderLayout.SOUTH);
        return p;
    }
    private void loadAnalytics() throws LMSException {
        analyticsTable.setModel(courseDAO.analyticsTable());
        UIUtil.background(this, () -> new Object[]{courseDAO.enrollmentStats(0), userDAO.userStats()}, result -> {
            @SuppressWarnings("unchecked") Map<String, Integer> enrol = (Map<String, Integer>) result[0];
            chart.setData(enrol);
            userStatsLabel.setText("Users by role: " + result[1]);
        });
    }

    // ---------- Settings ----------
    private JPanel settingsPanel() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        for (String key : new String[]{"system_name", "max_enrollments", "maintenance_mode"}) {
            JTextField f = new JTextField(20);
            settingFields.put(key, f);
            form.add(new JLabel(key));
            form.add(f);
        }
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.add(form, BorderLayout.NORTH);
        p.add(UIUtil.button("Save Settings", UIUtil.safe(this, this::saveSettings)), BorderLayout.SOUTH);
        return p;
    }
    private void loadSettings() throws LMSException {
        Map<String, String> s = systemDAO.getSettings();
        settingFields.forEach((k, f) -> f.setText(s.getOrDefault(k, "")));
    }
    private void saveSettings() throws LMSException {
        UIUtil.parseInt(settingFields.get("max_enrollments").getText(), "max_enrollments");
        for (Map.Entry<String, JTextField> e : settingFields.entrySet())
            systemDAO.saveSetting(e.getKey(), e.getValue().getText().trim());
        logger.log("Admin updated system settings");
        UIUtil.info(this, "Settings updated successfully.");
    }

    // ---------- Activity ----------
    private JPanel activityPanel() {
        activityArea.setEditable(false);
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("  Live activity (refreshes every 3 seconds by a background thread)"), BorderLayout.NORTH);
        p.add(new JScrollPane(activityArea), BorderLayout.CENTER);
        return p;
    }
}

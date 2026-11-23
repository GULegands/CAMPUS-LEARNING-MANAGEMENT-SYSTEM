package lms.ui;

import lms.dao.*;
import lms.exception.LMSException;
import lms.model.*;
import lms.service.ActivityLogger;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

/** Student dashboard: enrol, view/download materials, submit assignments, track progress, see grades. */
public class StudentDashboard extends JFrame {
    private final User student;
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final ActivityLogger logger = ActivityLogger.getInstance();

    private final JTable availableTable = new JTable(), myCoursesTable = new JTable(), materialsTable = new JTable(),
                         assignmentsTable = new JTable(), gradesTable = new JTable();
    private final BarChartPanel progressChart = new BarChartPanel("Progress % per course");

    public StudentDashboard(User student) {
        this.student = student;
        setTitle(student.getDashboardTitle());
        setSize(980, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(UIUtil.logoutBar(this));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Course Enrollment", UIUtil.tablePanel(availableTable,
            UIUtil.button("Enroll in Selected Course", UIUtil.safe(this, this::enroll))));
        tabs.addTab("Material Access", UIUtil.tablePanel(materialsTable,
            UIUtil.button("View Material", UIUtil.safe(this, this::viewMaterial)),
            UIUtil.button("Download Material", UIUtil.safe(this, this::downloadMaterial))));
        tabs.addTab("Assignment Submission", UIUtil.tablePanel(assignmentsTable,
            UIUtil.button("Submit Selected", UIUtil.safe(this, this::submit))));
        tabs.addTab("Progress Tracking", progressPanel());
        tabs.addTab("Feedback & Grades", UIUtil.tablePanel(gradesTable));
        add(tabs);
        try { loadAll(); } catch (LMSException e) { UIUtil.error(this, e); }
    }

    private JPanel progressPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel top = UIUtil.tablePanel(myCoursesTable,
            UIUtil.button("Update Progress", UIUtil.safe(this, this::updateProgress)));
        p.add(top, BorderLayout.CENTER);
        p.add(progressChart, BorderLayout.SOUTH);
        return p;
    }

    private void loadAll() throws LMSException {
        int id = student.getId();
        availableTable.setModel(courseDAO.availableForStudent(id));
        myCoursesTable.setModel(enrollmentDAO.myCourses(id));
        materialsTable.setModel(enrollmentDAO.myCourses(id));
        assignmentsTable.setModel(assignmentDAO.forStudent(id));
        gradesTable.setModel(submissionDAO.gradesForStudent(id));
        UIUtil.background(this, () -> enrollmentDAO.progressMap(id), progressChart::setData);
    }

    private void enroll() throws LMSException {
        int courseId = UIUtil.selectedId(availableTable);
        enrollmentDAO.enroll(student.getId(), courseId);
        logger.log("Student " + student.getName() + " enrolled in course id " + courseId);
        UIUtil.info(this, "Enrollment successful.");
        loadAll();
    }

    private void viewMaterial() throws LMSException {
        Course c = courseDAO.findById(UIUtil.selectedId(materialsTable));
        JTextArea area = new JTextArea(materialText(c), 15, 50);
        area.setEditable(false);
        UIUtil.scroll(area);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), c.getTitle(), JOptionPane.PLAIN_MESSAGE);
    }

    private void downloadMaterial() throws Exception {
        Course c = courseDAO.findById(UIUtil.selectedId(materialsTable));
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(c.getTitle().replaceAll("\\W+", "_") + ".txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        Files.write(path, materialText(c).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        UIUtil.info(this, "Material saved to " + path);
    }

    private String materialText(Course c) {
        return "COURSE: " + c.getTitle() + "\n\nDESCRIPTION:\n" + c.getDescription()
             + "\n\nSYLLABUS / CONTENT:\n" + c.getSyllabus();
    }

    private void submit() throws LMSException {
        int assignmentId = UIUtil.selectedId(assignmentsTable);
        JTextArea content = new JTextArea(8, 30);
        if (!UIUtil.form(this, "Submit Assignment", new String[]{"Your answer"},
                         new JComponent[]{UIUtil.scroll(content)})) return;
        submissionDAO.submit(assignmentId, student.getId(), content.getText());
        logger.log("Student " + student.getName() + " submitted assignment id " + assignmentId);
        UIUtil.info(this, "Assignment submitted successfully.");
        loadAll();
    }

    private void updateProgress() throws LMSException {
        int courseId = UIUtil.selectedId(myCoursesTable);
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 5));
        if (!UIUtil.form(this, "Update Progress", new String[]{"Progress %"}, new JComponent[]{spinner})) return;
        enrollmentDAO.updateProgress(student.getId(), courseId, ((Number) spinner.getValue()).intValue());
        UIUtil.info(this, "Progress updated.");
        loadAll();
    }
}

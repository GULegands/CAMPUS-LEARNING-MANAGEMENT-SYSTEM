package lms.ui;

import lms.dao.*;
import lms.exception.LMSException;
import lms.model.*;
import lms.service.ActivityLogger;

import javax.swing.*;
import java.awt.*;

/** Instructor dashboard: courses, assignments, grading, student performance, stats, feedback. */
public class InstructorDashboard extends JFrame {
    private final User instructor;
    private final CourseDAO courseDAO = new CourseDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final ActivityLogger logger = ActivityLogger.getInstance();

    private final JTable coursesTable = new JTable(), gradingTable = new JTable(),
                         performanceTable = new JTable(), feedbackTable = new JTable();
    private final BarChartPanel chart = new BarChartPanel("Enrollment per course");

    public InstructorDashboard(User instructor) {
        this.instructor = instructor;
        setTitle(instructor.getDashboardTitle());
        setSize(980, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(UIUtil.logoutBar(this));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Course Management", UIUtil.tablePanel(coursesTable,
            UIUtil.button("Create Course", UIUtil.safe(this, () -> courseForm(null))),
            UIUtil.button("Edit Course", UIUtil.safe(this, this::editCourse)),
            UIUtil.button("Delete Course", UIUtil.safe(this, this::deleteCourse)),
            UIUtil.button("Add Assignment / Quiz", UIUtil.safe(this, this::addAssignment))));
        tabs.addTab("Assignment Grading", UIUtil.tablePanel(gradingTable,
            UIUtil.button("Grade Selected", UIUtil.safe(this, this::gradeSelected)),
            UIUtil.button("Refresh", UIUtil.safe(this, this::loadAll))));
        tabs.addTab("Student Performance", UIUtil.tablePanel(performanceTable,
            UIUtil.button("Refresh", UIUtil.safe(this, this::loadAll))));
        tabs.addTab("Enrollment Stats", chart);
        tabs.addTab("Feedback Summary", UIUtil.tablePanel(feedbackTable));
        add(tabs);
        try { loadAll(); } catch (LMSException e) { UIUtil.error(this, e); }
    }

    private void loadAll() throws LMSException {
        int id = instructor.getId();
        coursesTable.setModel(courseDAO.tableForInstructor(id));
        gradingTable.setModel(submissionDAO.forInstructor(id, false));
        performanceTable.setModel(enrollmentDAO.performance(id));
        feedbackTable.setModel(submissionDAO.forInstructor(id, true));
        UIUtil.background(this, () -> courseDAO.enrollmentStats(id), chart::setData);
    }

    private void courseForm(Course existing) throws LMSException {
        JTextField title = new JTextField(existing == null ? "" : existing.getTitle());
        JTextArea desc = new JTextArea(existing == null ? "" : existing.getDescription(), 3, 20);
        JTextArea syl = new JTextArea(existing == null ? "" : existing.getSyllabus(), 3, 20);
        if (!UIUtil.form(this, existing == null ? "Create Course" : "Edit Course",
                new String[]{"Title", "Description", "Syllabus / Content"},
                new JComponent[]{title, UIUtil.scroll(desc), UIUtil.scroll(syl)})) return;
        if (existing == null) {
            courseDAO.add(new Course(0, title.getText().trim(), desc.getText(), syl.getText(), instructor.getId()));
            logger.log("Instructor " + instructor.getName() + " created course " + title.getText().trim());
            UIUtil.info(this, "Course created successfully.");
        } else {
            existing.setTitle(title.getText().trim()); existing.setDescription(desc.getText());
            existing.setSyllabus(syl.getText());
            courseDAO.update(existing);
            logger.log("Instructor " + instructor.getName() + " updated course " + existing.getTitle());
            UIUtil.info(this, "Course updated successfully.");
        }
        loadAll();
    }

    private void editCourse() throws LMSException {
        Course c = courseDAO.findById(UIUtil.selectedId(coursesTable));
        if (c.getInstructorId() != instructor.getId()) throw new LMSException("You can only edit your own courses.");
        courseForm(c);
    }

    private void deleteCourse() throws LMSException {
        Course c = courseDAO.findById(UIUtil.selectedId(coursesTable));
        if (c.getInstructorId() != instructor.getId()) throw new LMSException("You can only delete your own courses.");
        if (!UIUtil.confirm(this, "Delete course '" + c.getTitle() + "'?")) return;
        courseDAO.delete(c.getId());
        logger.log("Instructor " + instructor.getName() + " deleted course " + c.getTitle());
        UIUtil.info(this, "Course deleted successfully.");
        loadAll();
    }

    private void addAssignment() throws LMSException {
        int courseId = UIUtil.selectedId(coursesTable);
        JTextField title = new JTextField();
        JTextArea desc = new JTextArea(3, 20);
        JComboBox<String> type = new JComboBox<>(new String[]{"ASSIGNMENT", "QUIZ"});
        if (!UIUtil.form(this, "Add Assignment / Quiz", new String[]{"Title", "Description", "Type"},
                         new JComponent[]{title, UIUtil.scroll(desc), type})) return;
        assignmentDAO.add(new Assignment(0, courseId, title.getText().trim(), desc.getText(), (String) type.getSelectedItem()));
        logger.log("Instructor " + instructor.getName() + " added " + type.getSelectedItem() + " " + title.getText().trim());
        UIUtil.info(this, "Assignment created successfully.");
    }

    private void gradeSelected() throws LMSException {
        int submissionId = UIUtil.selectedId(gradingTable);
        JTextField grade = new JTextField();
        JTextArea feedback = new JTextArea(3, 20);
        if (!UIUtil.form(this, "Grade Submission", new String[]{"Grade (0-100)", "Feedback"},
                         new JComponent[]{grade, UIUtil.scroll(feedback)})) return;
        submissionDAO.grade(submissionId, UIUtil.parseInt(grade.getText(), "Grade"), feedback.getText());
        logger.log("Instructor " + instructor.getName() + " graded submission " + submissionId);
        UIUtil.info(this, "Grade and feedback saved.");
        loadAll();
    }
}

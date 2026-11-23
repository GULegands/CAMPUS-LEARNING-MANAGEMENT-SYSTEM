package lms.dao;

import lms.exception.LMSException;

import javax.swing.table.DefaultTableModel;
import java.util.LinkedHashMap;
import java.util.Map;

/** Database operations for enrollments and progress. */
public class EnrollmentDAO extends BaseDAO {
    private final SystemDAO systemDAO = new SystemDAO();

    public void enroll(int studentId, int courseId) throws LMSException {
        if (scalar("SELECT COUNT(*) FROM enrollments WHERE student_id=? AND course_id=?", studentId, courseId) > 0)
            throw new LMSException("You are already enrolled in this course.");
        int max;
        try { max = Integer.parseInt(systemDAO.getSetting("max_enrollments", "100")); }
        catch (NumberFormatException e) { max = 100; }
        if (scalar("SELECT COUNT(*) FROM enrollments WHERE course_id=?", courseId) >= max)
            throw new LMSException("This course has reached its maximum enrollment (" + max + ").");
        execute("INSERT INTO enrollments(student_id,course_id) VALUES(?,?)", studentId, courseId);
    }

    public void updateProgress(int studentId, int courseId, int progress) throws LMSException {
        if (progress < 0 || progress > 100) throw new LMSException("Progress must be between 0 and 100.");
        execute("UPDATE enrollments SET progress=? WHERE student_id=? AND course_id=?", progress, studentId, courseId);
    }

    public DefaultTableModel myCourses(int studentId) throws LMSException {
        return tableModel("SELECT c.id AS ID, c.title AS Course, e.progress AS `Progress %` FROM enrollments e "
            + "JOIN courses c ON c.id=e.course_id WHERE e.student_id=?", studentId);
    }

    public Map<String, Integer> progressMap(int studentId) throws LMSException {
        DefaultTableModel m = myCourses(studentId);
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < m.getRowCount(); i++)
            map.put(String.valueOf(m.getValueAt(i, 1)), ((Number) m.getValueAt(i, 2)).intValue());
        return map;
    }

    /** Progress and average grade of every student in an instructor's courses. */
    public DefaultTableModel performance(int instructorId) throws LMSException {
        return tableModel("SELECT u.name AS Student, c.title AS Course, e.progress AS `Progress %`, "
            + "(SELECT ROUND(AVG(s.grade),1) FROM submissions s JOIN assignments a ON a.id=s.assignment_id "
            + "WHERE s.student_id=u.id AND a.course_id=c.id) AS `Avg Grade` "
            + "FROM enrollments e JOIN users u ON u.id=e.student_id JOIN courses c ON c.id=e.course_id "
            + "WHERE c.instructor_id=?", instructorId);
    }
}

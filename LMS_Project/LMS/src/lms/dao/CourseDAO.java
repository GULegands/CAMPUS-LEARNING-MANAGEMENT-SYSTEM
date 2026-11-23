package lms.dao;

import lms.exception.LMSException;
import lms.model.Course;

import javax.swing.table.DefaultTableModel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Database operations for courses and course analytics. */
public class CourseDAO extends BaseDAO implements Dao<Course> {

    private static final RowMapper<Course> MAPPER = rs -> new Course(rs.getInt("id"), rs.getString("title"),
        rs.getString("description"), rs.getString("syllabus"), rs.getInt("instructor_id"));

    private void validate(Course c) throws LMSException {
        if (c.getTitle() == null || c.getTitle().trim().isEmpty()) throw new LMSException("Course title is required.");
    }
    private Integer instructor(Course c) { return c.getInstructorId() == 0 ? null : c.getInstructorId(); }

    @Override public void add(Course c) throws LMSException {
        validate(c);
        execute("INSERT INTO courses(title,description,syllabus,instructor_id) VALUES(?,?,?,?)",
                c.getTitle(), c.getDescription(), c.getSyllabus(), instructor(c));
    }
    @Override public void update(Course c) throws LMSException {
        validate(c);
        execute("UPDATE courses SET title=?, description=?, syllabus=?, instructor_id=? WHERE id=?",
                c.getTitle(), c.getDescription(), c.getSyllabus(), instructor(c), c.getId());
    }
    @Override public void delete(int id) throws LMSException { execute("DELETE FROM courses WHERE id=?", id); }

    @Override public Course findById(int id) throws LMSException {
        List<Course> r = query("SELECT * FROM courses WHERE id=?", MAPPER, id);
        if (r.isEmpty()) throw new LMSException("Course not found.");
        return r.get(0);
    }
    @Override public List<Course> findAll() throws LMSException { return query("SELECT * FROM courses", MAPPER); }

    public DefaultTableModel tableModel() throws LMSException {
        return tableModel("SELECT c.id AS ID, c.title AS Title, c.description AS Description, "
            + "COALESCE(u.name,'-') AS Instructor FROM courses c LEFT JOIN users u ON u.id=c.instructor_id ORDER BY c.id");
    }
    public DefaultTableModel tableForInstructor(int instructorId) throws LMSException {
        return tableModel("SELECT id AS ID, title AS Title, description AS Description FROM courses WHERE instructor_id=?",
                          instructorId);
    }
    /** Courses the student has not enrolled in yet. */
    public DefaultTableModel availableForStudent(int studentId) throws LMSException {
        return tableModel("SELECT id AS ID, title AS Title, description AS Description FROM courses "
            + "WHERE id NOT IN (SELECT course_id FROM enrollments WHERE student_id=?)", studentId);
    }
    /** Enrollment count per course title; instructorId = 0 means all courses. */
    public Map<String, Integer> enrollmentStats(int instructorId) throws LMSException {
        String sql = "SELECT c.title, COUNT(e.id) FROM courses c LEFT JOIN enrollments e ON e.course_id=c.id "
            + (instructorId > 0 ? "WHERE c.instructor_id=? " : "") + "GROUP BY c.id, c.title";
        DefaultTableModel m = tableModel(sql, instructorId > 0 ? new Object[]{instructorId} : new Object[0]);
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < m.getRowCount(); i++)
            map.put(String.valueOf(m.getValueAt(i, 0)), ((Number) m.getValueAt(i, 1)).intValue());
        return map;
    }
    public DefaultTableModel analyticsTable() throws LMSException {
        return tableModel("SELECT c.title AS Course, COUNT(e.id) AS Enrolled, ROUND(AVG(e.progress),1) AS `Avg Progress %`, "
            + "(SELECT ROUND(AVG(s.grade),1) FROM submissions s JOIN assignments a ON a.id=s.assignment_id "
            + "WHERE a.course_id=c.id) AS `Avg Grade` FROM courses c LEFT JOIN enrollments e ON e.course_id=c.id "
            + "GROUP BY c.id, c.title");
    }
}

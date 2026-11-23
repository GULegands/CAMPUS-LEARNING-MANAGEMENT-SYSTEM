package lms.dao;

import lms.exception.LMSException;
import lms.model.Assignment;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/** Database operations for assignments and quizzes. */
public class AssignmentDAO extends BaseDAO implements Dao<Assignment> {

    private static final RowMapper<Assignment> MAPPER = rs -> new Assignment(rs.getInt("id"),
        rs.getInt("course_id"), rs.getString("title"), rs.getString("description"), rs.getString("type"));

    private void validate(Assignment a) throws LMSException {
        if (a.getTitle() == null || a.getTitle().trim().isEmpty()) throw new LMSException("Assignment title is required.");
    }
    @Override public void add(Assignment a) throws LMSException {
        validate(a);
        execute("INSERT INTO assignments(course_id,title,description,type) VALUES(?,?,?,?)",
                a.getCourseId(), a.getTitle(), a.getDescription(), a.getType());
    }
    @Override public void update(Assignment a) throws LMSException {
        validate(a);
        execute("UPDATE assignments SET title=?, description=?, type=? WHERE id=?",
                a.getTitle(), a.getDescription(), a.getType(), a.getId());
    }
    @Override public void delete(int id) throws LMSException { execute("DELETE FROM assignments WHERE id=?", id); }
    @Override public Assignment findById(int id) throws LMSException {
        List<Assignment> r = query("SELECT * FROM assignments WHERE id=?", MAPPER, id);
        if (r.isEmpty()) throw new LMSException("Assignment not found.");
        return r.get(0);
    }
    @Override public List<Assignment> findAll() throws LMSException { return query("SELECT * FROM assignments", MAPPER); }

    /** Assignments for the student's enrolled courses with submission status. */
    public DefaultTableModel forStudent(int studentId) throws LMSException {
        return tableModel("SELECT a.id AS ID, c.title AS Course, a.title AS Title, a.type AS Type, "
            + "CASE WHEN s.id IS NULL THEN 'Not submitted' WHEN s.grade IS NULL THEN 'Submitted' ELSE 'Graded' END AS Status "
            + "FROM assignments a JOIN courses c ON c.id=a.course_id "
            + "JOIN enrollments e ON e.course_id=c.id AND e.student_id=? "
            + "LEFT JOIN submissions s ON s.assignment_id=a.id AND s.student_id=?", studentId, studentId);
    }
}

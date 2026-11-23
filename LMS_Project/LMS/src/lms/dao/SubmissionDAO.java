package lms.dao;

import lms.exception.LMSException;

import javax.swing.table.DefaultTableModel;

/** Database operations for submissions, grading and feedback. */
public class SubmissionDAO extends BaseDAO {

    public void submit(int assignmentId, int studentId, String content) throws LMSException {
        if (content == null || content.trim().isEmpty()) throw new LMSException("Submission cannot be empty.");
        if (scalar("SELECT COUNT(*) FROM submissions WHERE assignment_id=? AND student_id=?", assignmentId, studentId) > 0)
            throw new LMSException("You have already submitted this assignment.");
        execute("INSERT INTO submissions(assignment_id,student_id,content) VALUES(?,?,?)", assignmentId, studentId, content);
    }

    public void grade(int submissionId, int grade, String feedback) throws LMSException {
        if (grade < 0 || grade > 100) throw new LMSException("Grade must be between 0 and 100.");
        execute("UPDATE submissions SET grade=?, feedback=? WHERE id=?", grade, feedback, submissionId);
    }

    /** Submissions for an instructor's courses; gradedOnly=true is used for the feedback summary. */
    public DefaultTableModel forInstructor(int instructorId, boolean gradedOnly) throws LMSException {
        return tableModel("SELECT s.id AS ID, u.name AS Student, a.title AS Assignment, s.content AS Submission, "
            + "s.grade AS Grade, s.feedback AS Feedback FROM submissions s "
            + "JOIN assignments a ON a.id=s.assignment_id JOIN courses c ON c.id=a.course_id "
            + "JOIN users u ON u.id=s.student_id WHERE c.instructor_id=?"
            + (gradedOnly ? " AND s.grade IS NOT NULL" : ""), instructorId);
    }

    public DefaultTableModel gradesForStudent(int studentId) throws LMSException {
        return tableModel("SELECT a.title AS Assignment, a.type AS Type, s.grade AS Grade, s.feedback AS Feedback "
            + "FROM submissions s JOIN assignments a ON a.id=s.assignment_id WHERE s.student_id=?", studentId);
    }
}

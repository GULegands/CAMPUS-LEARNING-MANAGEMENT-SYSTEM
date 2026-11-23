package lms.model;

/** A course created by an instructor. */
public class Course {
    private int id, instructorId;
    private String title, description, syllabus;

    public Course(int id, String title, String description, String syllabus, int instructorId) {
        this.id = id; this.title = title; this.description = description;
        this.syllabus = syllabus; this.instructorId = instructorId;
    }
    public int getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String t) { title = t; }
    public String getDescription() { return description; }
    public void setDescription(String d) { description = d; }
    public String getSyllabus() { return syllabus; }
    public void setSyllabus(String s) { syllabus = s; }
    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int i) { instructorId = i; }
    @Override public String toString() { return title; }
}

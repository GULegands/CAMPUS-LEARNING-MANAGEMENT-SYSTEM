package lms.model;

/** An assignment or quiz that belongs to a course. */
public class Assignment {
    private int id, courseId;
    private String title, description, type;

    public Assignment(int id, int courseId, String title, String description, String type) {
        this.id = id; this.courseId = courseId; this.title = title;
        this.description = description; this.type = type;
    }
    public int getId() { return id; }
    public int getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public void setTitle(String t) { title = t; }
    public String getDescription() { return description; }
    public void setDescription(String d) { description = d; }
    public String getType() { return type; }
    public void setType(String t) { type = t; }
    @Override public String toString() { return title; }
}

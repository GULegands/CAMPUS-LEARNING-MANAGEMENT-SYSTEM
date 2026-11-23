package lms.model;

/** Student user: extends {@link User} and defines its own dashboard title. */
public class Student extends User {
    public Student(int id, String name, String email, String password) {
        super(id, name, email, password, Role.STUDENT);
    }
    @Override public String getDashboardTitle() { return "Student Dashboard - " + getName(); }
}

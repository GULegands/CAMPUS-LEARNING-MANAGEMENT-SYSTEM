package lms.model;

/** Instructor user: extends {@link User} and defines its own dashboard title. */
public class Instructor extends User {
    public Instructor(int id, String name, String email, String password) {
        super(id, name, email, password, Role.INSTRUCTOR);
    }
    @Override public String getDashboardTitle() { return "Instructor Dashboard - " + getName(); }
}

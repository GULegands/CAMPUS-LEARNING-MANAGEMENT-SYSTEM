package lms.model;

/** Admin user: extends {@link User} and defines its own dashboard title. */
public class Admin extends User {
    public Admin(int id, String name, String email, String password) {
        super(id, name, email, password, Role.ADMIN);
    }
    @Override public String getDashboardTitle() { return "Admin Dashboard - " + getName(); }
}

package lms.model;

/** Abstract base class for all users (Inheritance + Polymorphism). */
public abstract class User {
    private int id;
    private String name, email, password;
    private final Role role;

    protected User(int id, String name, String email, String password, Role role) {
        this.id = id; this.name = name; this.email = email; this.password = password; this.role = role;
    }
    /** Each subclass supplies its own dashboard title (polymorphism). */
    public abstract String getDashboardTitle();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }

    @Override public String toString() { return name + " (" + email + ")"; }
}

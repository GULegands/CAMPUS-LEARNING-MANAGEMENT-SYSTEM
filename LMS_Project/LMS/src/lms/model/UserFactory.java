package lms.model;

/** Factory that creates the right User subclass from a Role. */
public final class UserFactory {
    private UserFactory() {}
    public static User create(int id, String name, String email, String password, Role role) {
        switch (role) {
            case ADMIN:      return new Admin(id, name, email, password);
            case INSTRUCTOR: return new Instructor(id, name, email, password);
            default:         return new Student(id, name, email, password);
        }
    }
}

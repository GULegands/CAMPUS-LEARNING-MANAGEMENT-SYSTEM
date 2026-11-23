# Online Learning Management System (Java Swing + MySQL + JDBC)

## Setup
1. Install JDK 11+ and MySQL 8.
2. Run `sql/schema.sql` in MySQL (`mysql -u root -p < sql/schema.sql`).
3. Edit `src/lms/util/DBConnection.java` -> set your MySQL USER and PASSWORD.
4. Download MySQL Connector/J (mysql-connector-j-8.x.jar) and put it in a `lib/` folder.

## Compile & Run
Linux/Mac:
    javac -d out $(find src -name "*.java")
    java -cp "out:lib/*" lms.Main
Windows:
    dir /s /b src\*.java > sources.txt
    javac -d out @sources.txt
    java -cp "out;lib/*" lms.Main

## First login
Admin: admin@lms.com / admin123  (auto-created). Students can self-register.
The admin creates Instructor accounts; instructors create courses; students enrol.

## Structure
- model/      User (abstract), Admin, Instructor, Student, UserFactory, Role, Course, Assignment
- exception/  LMSException
- util/       DBConnection (JDBC), PasswordUtil
- dao/        Dao<T> interface, BaseDAO, UserDAO, CourseDAO, EnrollmentDAO, AssignmentDAO, SubmissionDAO, SystemDAO
- service/    ActivityLogger (synchronized singleton), ActivityMonitor (background thread)
- ui/         LoginFrame, AdminDashboard, InstructorDashboard, StudentDashboard, BarChartPanel, UIUtil

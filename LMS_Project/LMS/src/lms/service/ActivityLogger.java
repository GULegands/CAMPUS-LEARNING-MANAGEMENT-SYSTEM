package lms.service;

import lms.dao.SystemDAO;
import lms.exception.LMSException;

/** Thread-safe singleton: many threads/windows can log at once, writes are synchronized. */
public final class ActivityLogger {
    private static final ActivityLogger INSTANCE = new ActivityLogger();
    private final SystemDAO dao = new SystemDAO();

    private ActivityLogger() {}
    public static ActivityLogger getInstance() { return INSTANCE; }

    public synchronized void log(String message) {
        try { dao.log(message); }
        catch (LMSException e) { System.err.println("Could not log activity: " + e.getMessage()); }
    }
}

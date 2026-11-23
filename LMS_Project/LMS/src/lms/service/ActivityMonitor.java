package lms.service;

import lms.dao.SystemDAO;
import lms.exception.LMSException;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.function.Consumer;

/** Background thread that polls the activity log and pushes updates to the Admin dashboard. */
public class ActivityMonitor implements Runnable {
    private final SystemDAO dao = new SystemDAO();
    private final Consumer<List<String>> listener;
    private final long intervalMs;
    private volatile boolean running = true;

    public ActivityMonitor(Consumer<List<String>> listener, long intervalMs) {
        this.listener = listener; this.intervalMs = intervalMs;
    }
    public void stop() { running = false; }

    @Override public void run() {
        while (running) {
            try {
                List<String> items = dao.recentActivity(50);
                SwingUtilities.invokeLater(() -> listener.accept(items));   // update UI on the EDT
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (LMSException e) {
                System.err.println("Monitor error: " + e.getMessage());
                try { Thread.sleep(intervalMs); } catch (InterruptedException ie) { return; }
            }
        }
    }
}

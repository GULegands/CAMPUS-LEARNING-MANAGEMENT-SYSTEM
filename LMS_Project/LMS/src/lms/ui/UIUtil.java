package lms.ui;

import lms.exception.LMSException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/** Small helpers shared by all screens (dialogs, forms, background tasks). */
public final class UIUtil {
    private UIUtil() {}

    /** An action that may throw; used so button handlers need no repeated try/catch. */
    @FunctionalInterface
    public interface ThrowingAction { void run() throws Exception; }

    public static ActionListener safe(Component parent, ThrowingAction action) {
        return e -> { try { action.run(); } catch (Exception ex) { error(parent, ex); } };
    }

    public static void error(Component parent, Throwable t) {
        Throwable cause = (t instanceof ExecutionException && t.getCause() != null) ? t.getCause() : t;
        String msg = cause.getMessage() == null ? cause.toString() : cause.getMessage();
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** ID stored in column 0 of the selected row. */
    public static int selectedId(JTable table) throws LMSException {
        int row = table.getSelectedRow();
        if (row < 0) throw new LMSException("Please select a row first.");
        return ((Number) table.getValueAt(row, 0)).intValue();
    }

    public static int parseInt(String text, String field) throws LMSException {
        try { return Integer.parseInt(text.trim()); }
        catch (NumberFormatException e) { throw new LMSException(field + " must be a whole number."); }
    }

    /** Shows a label/field form in a dialog; returns true when OK is pressed. */
    public static boolean form(Component parent, String title, String[] labels, JComponent[] fields) {
        JPanel p = new JPanel(new GridLayout(labels.length, 2, 6, 6));
        for (int i = 0; i < labels.length; i++) { p.add(new JLabel(labels[i])); p.add(fields[i]); }
        return JOptionPane.showConfirmDialog(parent, p, title, JOptionPane.OK_CANCEL_OPTION,
                                             JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public static JScrollPane scroll(JTextArea area) {
        area.setLineWrap(true); area.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(260, 70));
        return sp;
    }

    public static JButton button(String text, ActionListener l) {
        JButton b = new JButton(text); b.addActionListener(l); return b;
    }

    /** Table in the centre, buttons at the bottom. */
    public static JPanel tablePanel(JTable table, JButton... buttons) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JButton b : buttons) bar.add(b);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    /** Runs work on a background thread (SwingWorker) and gives the result back on the UI thread. */
    public static <T> void background(Component parent, Callable<T> work, Consumer<T> onDone) {
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return work.call(); }
            @Override protected void done() {
                try { onDone.accept(get()); } catch (Exception e) { error(parent, e); }
            }
        }.execute();
    }

    /** Menu bar with a Logout item that returns to the login screen. */
    public static JMenuBar logoutBar(JFrame frame) {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Account");
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(e -> { frame.dispose(); new LoginFrame().setVisible(true); });
        menu.add(logout); bar.add(menu);
        return bar;
    }
}

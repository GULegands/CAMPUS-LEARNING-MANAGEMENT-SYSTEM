package lms.ui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Simple custom-painted bar chart used for analytics and progress graphs. */
public class BarChartPanel extends JPanel {
    private Map<String, Integer> data = new LinkedHashMap<>();
    private final String title;

    public BarChartPanel(String title) {
        this.title = title;
        setPreferredSize(new Dimension(500, 260));
        setBackground(Color.WHITE);
    }

    public void setData(Map<String, Integer> data) { this.data = data; repaint(); }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString(title, 15, 20);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        if (data.isEmpty()) { g2.drawString("No data available", 15, 45); return; }

        int w = getWidth(), h = getHeight(), pad = 40;
        int max = Math.max(1, java.util.Collections.max(data.values()));
        int barW = Math.max(14, (w - 2 * pad) / data.size() - 12);
        int x = pad;
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            int barH = (int) ((h - 2 * pad - 10) * (e.getValue() / (double) max));
            g2.setColor(new Color(52, 120, 200));
            g2.fillRect(x, h - pad - barH, barW, barH);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(String.valueOf(e.getValue()), x, h - pad - barH - 4);
            String label = e.getKey().length() > 12 ? e.getKey().substring(0, 11) + "..." : e.getKey();
            g2.drawString(label, x, h - pad + 16);
            x += barW + 12;
        }
        g2.drawLine(pad - 5, h - pad, w - pad, h - pad);
    }
}

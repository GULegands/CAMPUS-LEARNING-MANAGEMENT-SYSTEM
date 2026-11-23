package lms.dao;

import lms.exception.LMSException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Database operations for system settings and the activity log. */
public class SystemDAO extends BaseDAO {

    public void log(String message) throws LMSException {
        execute("INSERT INTO activity_log(message) VALUES(?)", message);
    }

    public List<String> recentActivity(int limit) throws LMSException {
        return query("SELECT message, created_at FROM activity_log ORDER BY id DESC LIMIT ?",
                     rs -> rs.getTimestamp(2) + "  -  " + rs.getString(1), limit);
    }

    public Map<String, String> getSettings() throws LMSException {
        Map<String, String> map = new LinkedHashMap<>();
        for (String[] row : query("SELECT setting_key, setting_value FROM settings",
                                  rs -> new String[]{rs.getString(1), rs.getString(2)}))
            map.put(row[0], row[1]);
        return map;
    }

    public String getSetting(String key, String defaultValue) throws LMSException {
        String v = getSettings().get(key);
        return v == null ? defaultValue : v;
    }

    public void saveSetting(String key, String value) throws LMSException {
        execute("INSERT INTO settings(setting_key,setting_value) VALUES(?,?) "
              + "ON DUPLICATE KEY UPDATE setting_value=?", key, value, value);
    }
}

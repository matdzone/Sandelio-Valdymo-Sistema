package wms.sandeliukas.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import wms.sandeliukas.model.Notification;
import wms.sandeliukas.model.User;
import wms.sandeliukas.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final String TABLE_NAME = "Notification";
    private static final String RECIPIENT_TABLE_NAME = "NotificationRecipient";

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;

    public NotificationService(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
    }

    public String notificationList(Model model, String userEmail) {
        List<String> columns = notificationListRequest();
        String idColumn = findColumn(columns, "id");
        String titleColumn = findColumn(columns, "title", "name", "pavadinimas");
        String contentColumn = findColumn(columns, "content", "text", "body", "message", "turinys");
        String readColumn = findColumn(columns, "read", "isRead", "seen", "viewed", "status", "busena");

        if (idColumn == null) {
            throw new RuntimeException("Notification lenteleje nerastas id stulpelis");
        }

        String sql = "select n.* from " + TABLE_NAME + " n";
        List<Object> params = new ArrayList<>();

        if (tableExists(RECIPIENT_TABLE_NAME)) {
            sql += " join " + RECIPIENT_TABLE_NAME + " nr on nr.fk_Notification = n." + idColumn;
            String recipientUserColumn = findRecipientUserColumn();
            if (recipientUserColumn != null) {
                sql += " where nr." + recipientUserColumn + " = ?";
                params.add(userEmail);
            }
        }

        sql += " order by n." + idColumn + " desc";

        model.addAttribute("notifications", jdbcTemplate.query(sql, (rs, rowNum) -> {
            Notification notification = new Notification();
            notification.setId(rs.getInt(idColumn));
            notification.setTitle(titleColumn == null ? "Pranesimas" : readString(rs.getObject(titleColumn), "Pranesimas"));
            notification.setContent(contentColumn == null ? "" : readString(rs.getObject(contentColumn), ""));
            notification.setRead(readBoolean(readColumn == null ? null : rs.getObject(readColumn)));
            return notification;
        }, params.toArray()));
        return "customer/notifications";
    }

    @Transactional
    public void deleteNotification(Integer notificationId, String userEmail) {
        List<String> columns = notificationListRequest();
        String idColumn = findColumn(columns, "id");

        if (idColumn == null) {
            throw new RuntimeException("Notification lenteleje nerastas id stulpelis");
        }

        if (tableExists(RECIPIENT_TABLE_NAME)) {
            jdbcTemplate.update(
                    "delete from " + RECIPIENT_TABLE_NAME + " where fk_Notification = ?",
                    notificationId
            );
        }

        int updatedRows = jdbcTemplate.update(
                "delete from " + TABLE_NAME + " where " + idColumn + " = ?",
                notificationId
        );

        if (updatedRows == 0) {
            throw new RuntimeException("Pranesimas nerastas");
        }
    }

    @Transactional
    public User saveNotificationSettings(String userEmail, boolean showSystemNotifications, boolean showMessageNotifications) {
        User user = userRepository.findById(userEmail)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));

        user.setShowSystemNotifications(showSystemNotifications);
        user.setShowMessageNotifications(showMessageNotifications);
        return userRepository.save(user);
    }

    public User notificationSettingsRequest(String userEmail) {
        return userRepository.findById(userEmail)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));
    }

    private List<String> notificationListRequest() {
        return getColumns(TABLE_NAME);
    }

    private String findRecipientUserColumn() {
        List<String> columns = getColumns(RECIPIENT_TABLE_NAME);
        return findColumn(columns, "fk_User", "fk_Recipient", "fk_Receiver", "recipient", "receiver", "userEmail", "email");
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.tables
                where table_schema = database()
                  and table_name = ?
                """, Integer.class, tableName);
        return count != null && count > 0;
    }

    private List<String> getColumns(String tableName) {
        String sql = """
                select column_name
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                order by ordinal_position
                """;

        List<String> columns = jdbcTemplate.queryForList(sql, String.class, tableName);
        if (columns.isEmpty()) {
            throw new RuntimeException("Duomenu bazeje nerasta " + tableName + " lentele");
        }

        return columns;
    }

    private String findColumn(List<String> columns, String... candidates) {
        Map<String, String> normalizedColumns = columns.stream()
                .collect(Collectors.toMap(this::normalize, column -> column, (first, second) -> first));
        Set<String> keys = normalizedColumns.keySet();

        for (String candidate : candidates) {
            String normalizedCandidate = normalize(candidate);
            if (keys.contains(normalizedCandidate)) {
                return normalizedColumns.get(normalizedCandidate);
            }
        }

        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.replace("_", "").toLowerCase(Locale.ROOT);
    }

    private String readString(Object value, String fallback) {
        return value == null ? fallback : value.toString();
    }

    private Boolean readBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue() != 0;
        }
        String text = value.toString();
        return "true".equalsIgnoreCase(text) || "1".equals(text) || "perskaityta".equalsIgnoreCase(text);
    }
}

package wms.sandeliukas.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;

    public NotificationService(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
    }

    @Transactional
    public void sendNotification(String userEmail, String title, String content) {
        Integer maxId = jdbcTemplate.queryForObject(
                "select coalesce(max(id), 0) from " + TABLE_NAME, Integer.class);
        Integer newId = (maxId == null ? 0 : maxId) + 1;

        jdbcTemplate.update(
                "insert into " + TABLE_NAME + " (id, title, body) values (?, ?, ?)",
                newId, title, content
        );

        jdbcTemplate.update(
                "insert into NotificationRecipient (fk_Notification, fk_User) values (?, ?)",
                newId, userEmail
        );
    }

    public List<Notification> selectNotifications(String userEmail) {
        List<String> columns = getNotificationColumns();
        String idColumn = findColumn(columns, "id");
        String titleColumn = findColumn(columns, "title", "name", "pavadinimas");
        String contentColumn = findColumn(columns, "content", "text", "body", "message", "turinys");
        String readColumn = findColumn(columns, "read", "isRead", "seen", "viewed", "status", "busena", "būsena");
        String receiverColumn = findColumn(columns, "fk_User", "fk_Receiver", "fk_Recipient", "fk_ReceiverUser", "receiver", "recipient", "userEmail", "email");

        if (idColumn == null) {
            throw new RuntimeException("Notification lentelėje nerastas id stulpelis");
        }

        String sql = "select * from " + TABLE_NAME;
        List<Object> params = new ArrayList<>();

        if (receiverColumn != null) {
            sql += " where " + receiverColumn + " = ?";
            params.add(userEmail);
        }

        sql += " order by " + idColumn + " desc";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Notification notification = new Notification();
            notification.setId(rs.getInt(idColumn));
            notification.setTitle(titleColumn == null ? "Pranešimas" : readString(rs.getObject(titleColumn), "Pranešimas"));
            notification.setContent(contentColumn == null ? "" : readString(rs.getObject(contentColumn), ""));
            notification.setRead(readBoolean(readColumn == null ? null : rs.getObject(readColumn)));
            return notification;
        }, params.toArray());
    }

    @Transactional
    public void deleteNotification(Integer notificationId, String userEmail) {
        List<String> columns = getNotificationColumns();
        String idColumn = findColumn(columns, "id");
        String receiverColumn = findColumn(columns, "fk_User", "fk_Receiver", "fk_Recipient", "fk_ReceiverUser", "receiver", "recipient", "userEmail", "email");

        if (idColumn == null) {
            throw new RuntimeException("Notification lentelėje nerastas id stulpelis");
        }

        if (receiverColumn == null) {
            jdbcTemplate.update("delete from " + TABLE_NAME + " where " + idColumn + " = ?", notificationId);
            return;
        }

        int updatedRows = jdbcTemplate.update(
                "delete from " + TABLE_NAME + " where " + idColumn + " = ? and " + receiverColumn + " = ?",
                notificationId,
                userEmail
        );

        if (updatedRows == 0) {
            throw new RuntimeException("Pranešimas nerastas");
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

    public User getNotificationSettings(String userEmail) {
        return userRepository.findById(userEmail)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));
    }

    private List<String> getNotificationColumns() {
        String sql = """
                select column_name
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                order by ordinal_position
                """;

        List<String> columns = jdbcTemplate.queryForList(sql, String.class, TABLE_NAME);
        if (columns.isEmpty()) {
            throw new RuntimeException("Duomenų bazėje nerasta Notification lentelė");
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

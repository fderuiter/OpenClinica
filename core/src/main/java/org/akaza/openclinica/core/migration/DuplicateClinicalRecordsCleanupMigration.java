package org.akaza.openclinica.core.migration;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Date;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.bean.submit.ItemDataBean;

public class DuplicateClinicalRecordsCleanupMigration extends AbstractJavaManagedDataMigration {

    private static class DuplicatePair {
        int minId;
        int maxId;
    }

    @Override
    protected void doMigration() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        ItemDataDAO itemDataDAO = new ItemDataDAO(dataSource);

        boolean hasDuplicates = true;
        while (hasDuplicates) {
            String sql = "SELECT max(id.item_data_id) as max_id, min(id.item_data_id) as min_id " +
                         "FROM item_data id " +
                         "GROUP BY id.item_id, id.event_crf_id, id.ordinal " +
                         "HAVING count(id.item_data_id) > 1";

            jdbcTemplate.setMaxRows(DEFAULT_BATCH_SIZE);
            List<DuplicatePair> pairs = jdbcTemplate.query(sql, new RowMapper<DuplicatePair>() {
                @Override
                public DuplicatePair mapRow(ResultSet rs, int rowNum) throws SQLException {
                    DuplicatePair p = new DuplicatePair();
                    p.minId = rs.getInt("min_id");
                    p.maxId = rs.getInt("max_id");
                    return p;
                }
            });

            if (pairs.isEmpty()) {
                hasDuplicates = false;
                break;
            }

            for (DuplicatePair pair : pairs) {
                processDuplicate(jdbcTemplate, itemDataDAO, pair);
            }
        }
    }

    private void processDuplicate(JdbcTemplate jdbcTemplate, ItemDataDAO itemDataDAO, DuplicatePair p) {
        ItemDataBean minItem = (ItemDataBean) itemDataDAO.findByPK(p.minId);
        ItemDataBean maxItem = (ItemDataBean) itemDataDAO.findByPK(p.maxId);
        
        if (minItem == null || maxItem == null || !minItem.isActive() || !maxItem.isActive()) return;

        String minVal = minItem.getValue();
        String maxVal = maxItem.getValue();
        Date minDateUpdated = minItem.getUpdatedDate();
        Date maxDateUpdated = maxItem.getUpdatedDate();

        int removeId;
        int promoteId;
        ItemDataBean itemToRemove;

        if (minVal != null && minVal.equals(maxVal)) {
            removeId = p.minId;
            promoteId = p.maxId;
            itemToRemove = minItem;
        } else {
            long minLastTouched = minDateUpdated != null ? minDateUpdated.getTime() : 0;
            long maxLastTouched = maxDateUpdated != null ? maxDateUpdated.getTime() : 0;

            if (minLastTouched < maxLastTouched) {
                removeId = p.minId;
                promoteId = p.maxId;
                itemToRemove = minItem;
            } else if (maxLastTouched < minLastTouched) {
                removeId = p.maxId;
                promoteId = p.minId;
                itemToRemove = maxItem;
            } else {
                boolean minBlank = minVal == null || minVal.trim().isEmpty();
                boolean maxBlank = maxVal == null || maxVal.trim().isEmpty();
                if (minBlank && !maxBlank) {
                    removeId = p.minId;
                    promoteId = p.maxId;
                    itemToRemove = minItem;
                } else if (maxBlank && !minBlank) {
                    removeId = p.maxId;
                    promoteId = p.minId;
                    itemToRemove = maxItem;
                } else {
                    removeId = p.maxId;
                    promoteId = p.minId;
                    itemToRemove = maxItem;
                }
            }
        }

        deleteAndPromote(jdbcTemplate, itemDataDAO, itemToRemove, promoteId);
    }

    private void deleteAndPromote(JdbcTemplate jdbcTemplate, ItemDataDAO itemDataDAO, ItemDataBean itemToRemove, int promoteId) {
        int removeId = itemToRemove.getId();

        jdbcTemplate.update("UPDATE dn_item_data_map SET item_data_id = ? WHERE item_data_id = ?", promoteId, removeId);
        jdbcTemplate.update("UPDATE audit_log_event SET entity_id = ? WHERE entity_id = ? AND audit_table = 'item_data'", promoteId, removeId);

        // Assign specific system-user identity to automated audit entries via DAO
        itemToRemove.setUpdaterId(systemUser.getId());
        itemDataDAO.updateUser(itemToRemove);
        
        // DAO delete method correctly fires row-level database audit triggers
        itemDataDAO.delete(removeId);
    }
}

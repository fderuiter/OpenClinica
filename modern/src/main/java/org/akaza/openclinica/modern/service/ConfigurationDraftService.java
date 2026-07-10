package org.akaza.openclinica.modern.service;

import org.akaza.openclinica.modern.model.ConfigurationDraft;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

@Service
public class ConfigurationDraftService {

    private final JdbcTemplate jdbcTemplate;
    
    @Value("${draft.expiration.hours:24}")
    private int draftExpirationHours;

    @Autowired
    public ConfigurationDraftService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ConfigurationDraft saveDraft(String userName, String draftType, String draftData) {
        String id = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + (draftExpirationHours * 3600000L));

        String sql = "INSERT INTO configuration_drafts (id, user_name, draft_type, draft_data, created_at, expires_at) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, id, userName, draftType, draftData, now, expiresAt);

        ConfigurationDraft draft = new ConfigurationDraft();
        draft.setId(id);
        draft.setUserName(userName);
        draft.setDraftType(draftType);
        draft.setDraftData(draftData);
        draft.setCreatedAt(now);
        draft.setExpiresAt(expiresAt);
        return draft;
    }

    public ConfigurationDraft getDraft(String id) {
        String sql = "SELECT * FROM configuration_drafts WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new RowMapper<ConfigurationDraft>() {
            @Override
            public ConfigurationDraft mapRow(ResultSet rs, int rowNum) throws SQLException {
                ConfigurationDraft draft = new ConfigurationDraft();
                draft.setId(rs.getString("id"));
                draft.setUserName(rs.getString("user_name"));
                draft.setDraftType(rs.getString("draft_type"));
                draft.setDraftData(rs.getString("draft_data"));
                draft.setCreatedAt(rs.getTimestamp("created_at"));
                draft.setExpiresAt(rs.getTimestamp("expires_at"));
                return draft;
            }
        });
    }

    public void updateDraft(String id, String draftData) {
        String sql = "UPDATE configuration_drafts SET draft_data = ? WHERE id = ?";
        jdbcTemplate.update(sql, draftData, id);
    }
    
    public java.util.List<ConfigurationDraft> getDraftsByType(String draftType) {
        String sql = "SELECT * FROM configuration_drafts WHERE draft_type = ?";
        return jdbcTemplate.query(sql, new Object[]{draftType}, new RowMapper<ConfigurationDraft>() {
            @Override
            public ConfigurationDraft mapRow(ResultSet rs, int rowNum) throws SQLException {
                ConfigurationDraft draft = new ConfigurationDraft();
                draft.setId(rs.getString("id"));
                draft.setUserName(rs.getString("user_name"));
                draft.setDraftType(rs.getString("draft_type"));
                draft.setDraftData(rs.getString("draft_data"));
                draft.setCreatedAt(rs.getTimestamp("created_at"));
                draft.setExpiresAt(rs.getTimestamp("expires_at"));
                return draft;
            }
        });
    }

    public ConfigurationDraft saveDraftWithId(String id, String userName, String draftType, String draftData) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + (draftExpirationHours * 3600000L));

        // Check if exists
        String checkSql = "SELECT COUNT(*) FROM configuration_drafts WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, new Object[]{id}, Integer.class);
        if (count != null && count > 0) {
            String sql = "UPDATE configuration_drafts SET user_name = ?, draft_type = ?, draft_data = ?, expires_at = ? WHERE id = ?";
            jdbcTemplate.update(sql, userName, draftType, draftData, expiresAt, id);
        } else {
            String sql = "INSERT INTO configuration_drafts (id, user_name, draft_type, draft_data, created_at, expires_at) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, id, userName, draftType, draftData, now, expiresAt);
        }

        ConfigurationDraft draft = new ConfigurationDraft();
        draft.setId(id);
        draft.setUserName(userName);
        draft.setDraftType(draftType);
        draft.setDraftData(draftData);
        draft.setCreatedAt(now);
        draft.setExpiresAt(expiresAt);
        return draft;
    }

    public void deleteDraft(String id) {
        String sql = "DELETE FROM configuration_drafts WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}

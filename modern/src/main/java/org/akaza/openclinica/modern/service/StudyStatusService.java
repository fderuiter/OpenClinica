package org.akaza.openclinica.modern.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class StudyStatusService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PreAuthorize("@studySecurityValidator.hasAdminOrCoordinatorRole(#uniqueProtocolID)")
    @Transactional
    public void updateStudyStatus(String uniqueProtocolID, int statusId, String newStatus, UserAccountBean userBean) {
        // Find study ID and old status
        List<Map<String, Object>> studyInfo = jdbcTemplate.queryForList("SELECT study_id, status_id FROM study WHERE unique_identifier = ?", uniqueProtocolID);
        if (studyInfo.isEmpty()) {
            throw new IllegalArgumentException("Study not found");
        }
        Integer studyId = ((Number) studyInfo.get(0).get("study_id")).intValue();
        
        // Lock/Freeze/Unlock study and its sites (preserve old_status_id)
        jdbcTemplate.update("UPDATE study SET old_status_id = status_id, status_id = ? WHERE study_id = ? OR parent_study_id = ?", statusId, studyId, studyId);

        // Update study event definitions (only applies if locking a parent study)
        jdbcTemplate.update("UPDATE study_event_definition SET status_id = ? WHERE study_id = ?", statusId, studyId);
        
        // Update event definition CRFs (only applies if locking a parent study)
        jdbcTemplate.update("UPDATE event_definition_crf SET status_id = ? WHERE study_event_definition_id IN (SELECT study_event_definition_id FROM study_event_definition WHERE study_id = ?)", statusId, studyId);
        
        // Update study subjects for this study (and its sites, if this is a parent study)
        jdbcTemplate.update("UPDATE study_subject SET status_id = ? WHERE study_id = ? OR study_id IN (SELECT study_id FROM study WHERE parent_study_id = ?)", statusId, studyId, studyId);

        // Update study events linked to these subjects
        jdbcTemplate.update("UPDATE study_event SET status_id = ? WHERE study_subject_id IN (SELECT study_subject_id FROM study_subject WHERE study_id = ? OR study_id IN (SELECT study_id FROM study WHERE parent_study_id = ?))", statusId, studyId, studyId);
        
        // Update event CRFs linked to these study events
        jdbcTemplate.update("UPDATE event_crf SET status_id = ? WHERE study_event_id IN (SELECT study_event_id FROM study_event WHERE study_subject_id IN (SELECT study_subject_id FROM study_subject WHERE study_id = ? OR study_id IN (SELECT study_id FROM study WHERE parent_study_id = ?)))", statusId, studyId, studyId);
        
        // Update item data linked to these event CRFs
        jdbcTemplate.update("UPDATE item_data SET status_id = ? WHERE event_crf_id IN (SELECT event_crf_id FROM event_crf WHERE study_event_id IN (SELECT study_event_id FROM study_event WHERE study_subject_id IN (SELECT study_subject_id FROM study_subject WHERE study_id = ? OR study_id IN (SELECT study_id FROM study WHERE parent_study_id = ?))))", statusId, studyId, studyId);

        // Record audit event
        String auditSql = "INSERT INTO audit_event (audit_id, audit_date, audit_table, user_id, entity_id, reason_for_change, action_message) " +
                          "VALUES (nextval('audit_event_audit_id_seq'), NOW(), 'study', ?, ?, 'Status Update', ?)";
        jdbcTemplate.update(auditSql, userBean.getId(), studyId, "Status updated to " + newStatus);
    }
}

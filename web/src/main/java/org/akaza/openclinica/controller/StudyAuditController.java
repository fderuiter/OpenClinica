package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.xml.bind.DatatypeConverter;

@Controller
@RequestMapping(value = "/auth/api/v1/studies")
public class StudyAuditController {
    private StudyDAO _studyDAO;
    private UserAccountDAO _userAccountDAO;

    @Autowired
    public StudyAuditController(StudyDAO _studyDAO, UserAccountDAO _userAccountDAO) {
        this._studyDAO = _studyDAO;
        this._userAccountDAO = _userAccountDAO;
    }


    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @RequestMapping(value = "/{studyOid}/audit", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<Object> getStudyAuditLogs(
            @PathVariable("studyOid") String studyOid,
            @RequestParam(value = "since", required = true) String sinceStr,
            @RequestParam(value = "limit", required = false, defaultValue = "1000") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset) {
        
        try {
            // 1. Validate since parameter
            Date sinceDate;
            try {
                sinceDate = DatatypeConverter.parseDateTime(sinceStr).getTime();
            } catch (Exception e) {
                try {
                    sinceDate = new SimpleDateFormat("yyyy-MM-dd").parse(sinceStr);
                } catch (ParseException e2) {
                    return new ResponseEntity<>("Invalid timestamp format for 'since'. Expected ISO 8601.", HttpStatus.BAD_REQUEST);
                }
            }

            // 2. Resolve study and check existence
            StudyDAO studyDAO = this._studyDAO;
            StudyBean study = (StudyBean) studyDAO.findByOid(studyOid);
            if (study == null || study.getId() <= 0) {
                return new ResponseEntity<>("Study not found", HttpStatus.NOT_FOUND);
            }

            // 3. Authorization Check
            UserAccountDAO userAccountDAO = this._userAccountDAO;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null) {
                return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
            }
            
            String username;
            if (auth.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) auth.getPrincipal()).getUsername();
            } else {
                username = auth.getPrincipal().toString();
            }
            
            UserAccountBean user = (UserAccountBean) userAccountDAO.findByUserName(username);
            if (user == null || !user.isActive()) {
                return new ResponseEntity<>("User inactive or not found", HttpStatus.FORBIDDEN);
            }

            if (!hasAccessToStudy(user, study.getId(), study.getParentStudyId(), userAccountDAO)) {
                return new ResponseEntity<>("Access Denied", HttpStatus.FORBIDDEN);
            }

            // 4. Query Audit Logs
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            String sql = "SELECT ale.audit_id, ale.audit_date, ale.audit_table, ale.user_id, " +
                         "ale.entity_id, ale.entity_name, ale.reason_for_change, alet.name as event_type_name, " +
                         "ale.old_value, ale.new_value, ua.user_name, ss.oc_oid as subject_oid, " +
                         "sed.oc_oid as study_event_oid, crf.oc_oid as crf_oid " +
                         "FROM audit_log_event ale " +
                         "JOIN audit_log_event_type alet ON ale.audit_log_event_type_id = alet.audit_log_event_type_id " +
                         "LEFT JOIN user_account ua ON ale.user_id = ua.user_id " +
                         "LEFT JOIN study_event se ON se.study_event_id = COALESCE(ale.study_event_id, CASE WHEN ale.audit_table = 'study_event' THEN ale.entity_id END) " +
                         "JOIN study_subject ss ON ss.study_subject_id = COALESCE(CASE WHEN ale.audit_table = 'study_subject' THEN ale.entity_id END, se.study_subject_id) " +
                         "LEFT JOIN study_event_definition sed ON se.study_event_definition_id = sed.study_event_definition_id " +
                         "LEFT JOIN crf_version cv ON ale.event_crf_version_id = cv.crf_version_id " +
                         "LEFT JOIN crf ON cv.crf_id = crf.crf_id " +
                         "WHERE ss.study_id = ? AND ale.audit_date >= ? " +
                         "ORDER BY ale.audit_date ASC LIMIT ? OFFSET ?";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, study.getId(), sinceDate, limit, offset);
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                Map<String, Object> result = new HashMap<>();
                result.put("audit_id", row.get("audit_id"));
                result.put("timestamp", row.get("audit_date"));
                result.put("audit_table", row.get("audit_table"));
                result.put("event_type", row.get("event_type_name"));
                result.put("user", row.get("user_name"));
                result.put("subject_oid", row.get("subject_oid")); 
                result.put("study_event_oid", row.get("study_event_oid"));
                result.put("crf_oid", row.get("crf_oid"));
                
                // Keep entity_oid and parent_oid as requested in acceptance criteria
                // "The response includes the entity_oid and parent_oid necessary for the consumer to map the audit back to the clinical data structure."
                String auditTable = (String) row.get("audit_table");
                if ("study_subject".equalsIgnoreCase(auditTable)) {
                    result.put("entity_oid", row.get("subject_oid"));
                } else if ("study_event".equalsIgnoreCase(auditTable)) {
                    result.put("entity_oid", row.get("study_event_oid"));
                    result.put("parent_oid", row.get("subject_oid"));
                } else if ("event_crf".equalsIgnoreCase(auditTable) || "item_data".equalsIgnoreCase(auditTable)) {
                    result.put("entity_oid", row.get("crf_oid"));
                    result.put("parent_oid", row.get("study_event_oid"));
                }
                
                result.put("entity_name", row.get("entity_name"));
                result.put("old_value", row.get("old_value"));
                result.put("new_value", row.get("new_value"));
                result.put("reason_for_change", row.get("reason_for_change"));
                results.add(result);
            }
            
            return new ResponseEntity<>(results, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error retrieving audit logs", e);
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean hasAccessToStudy(UserAccountBean user, int studyId, int parentStudyId, UserAccountDAO udao) {
        if (user.isSysAdmin()) {
            return true;
        }
        
        Collection<?> roles = udao.findAllRolesByUserName(user.getName());
        for (Object obj : roles) {
            if (obj instanceof org.akaza.openclinica.bean.login.StudyUserRoleBean) {
                org.akaza.openclinica.bean.login.StudyUserRoleBean role = (org.akaza.openclinica.bean.login.StudyUserRoleBean) obj;
                if ((role.getStudyId() == studyId || role.getStudyId() == parentStudyId) && role.getStatus().isAvailable()) {
                    return true;
                }
            }
        }
        return false;
    }
}

package org.akaza.openclinica.modern.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReportingSyncService {

    private static final Logger log = LoggerFactory.getLogger(ReportingSyncService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long lastSyncTime = 0;

    public long getLastSyncTime() {
        return lastSyncTime;
    }

    @Scheduled(fixedDelayString = "${reporting.sync.interval:10000}")
    @Transactional("transactionManager")
    public void syncReportingData() {
        log.info("Starting background synchronization for decoupled reporting schema.");
        try {
            // Segment data logically using study boundary rules
            List<String> studyOids = jdbcTemplate.queryForList(
                "SELECT DISTINCT study_oid FROM clinical_records WHERE study_oid IS NOT NULL", 
                String.class
            );

            log.info("Found {} studies to sync.", studyOids.size());

            for (String studyOid : studyOids) {
                log.debug("Synchronizing data segment for study OID: {}", studyOid);
                
                // Replicate study segment data into reporting schema in a database-agnostic way (DELETE then INSERT)
                jdbcTemplate.update(
                    "DELETE FROM reporting_clinical_records WHERE id IN (SELECT id FROM clinical_records WHERE study_oid = ?)",
                    studyOid
                );

                String insertSql = "INSERT INTO reporting_clinical_records (id, study_oid, subject_oid, data, synced_at) " +
                                   "SELECT id, study_oid, subject_oid, data, CURRENT_TIMESTAMP " +
                                   "FROM clinical_records " +
                                   "WHERE study_oid = ?";
                                   
                int updatedRows = jdbcTemplate.update(insertSql, studyOid);
                log.info("Segmented sync completed for study OID: {}. Processed rows: {}", studyOid, updatedRows);
            }

            // Clean up any orphaned records in reporting schema that are no longer in the transactional schema
            int deletedRows = jdbcTemplate.update(
                "DELETE FROM reporting_clinical_records WHERE id NOT IN (SELECT id FROM clinical_records)"
            );
            if (deletedRows > 0) {
                log.info("Cleaned up {} deleted records from reporting schema.", deletedRows);
            }

            lastSyncTime = System.currentTimeMillis();
            log.info("Decoupled reporting schema background synchronization completed successfully.");
        } catch (Exception e) {
            log.error("Error occurred during reporting schema synchronization: ", e);
        }
    }
}

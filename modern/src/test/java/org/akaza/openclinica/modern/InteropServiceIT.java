package org.akaza.openclinica.modern;

import org.akaza.openclinica.modern.service.ConfigurationDraftService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class InteropServiceIT extends AbstractIntegrationTest {

    @Autowired
    private InteropService interopService;

    @Autowired
    private ConfigurationDraftService draftService;

    @Test
    public void testUpsertSyntaxExecution() {
        // We do NOT recreate the whole database manually. We will rely on Hibernate to generate the DB schema
        // using spring.jpa.hibernate.ddl-auto=update
        
        jdbcTemplate.execute("DROP TABLE IF EXISTS configuration");
        jdbcTemplate.execute("DROP TABLE IF EXISTS configuration_drafts");
        jdbcTemplate.execute("DROP TABLE IF EXISTS study");
        jdbcTemplate.execute("DROP TABLE IF EXISTS study_subject");
        jdbcTemplate.execute("DROP TABLE IF EXISTS subject");
        jdbcTemplate.execute("DROP TABLE IF EXISTS study_event_definition");
        jdbcTemplate.execute("DROP TABLE IF EXISTS study_event");
        jdbcTemplate.execute("DROP TABLE IF EXISTS event_crf");
        jdbcTemplate.execute("DROP TABLE IF EXISTS item");
        jdbcTemplate.execute("DROP TABLE IF EXISTS item_data");
        jdbcTemplate.execute("DROP TABLE IF EXISTS user_account");
        jdbcTemplate.execute("DROP TABLE IF EXISTS status");
        jdbcTemplate.execute("DROP TABLE IF EXISTS audit_log_event");
        jdbcTemplate.execute("DROP TABLE IF EXISTS audit_event");
        jdbcTemplate.execute("DROP TABLE IF EXISTS completion_status");
        
        jdbcTemplate.execute("CREATE TABLE configuration_drafts (id VARCHAR(255) PRIMARY KEY, draft_type VARCHAR(255), draft_value VARCHAR(4000), created_at TIMESTAMP, draft_data VARCHAR(4000), expires_at TIMESTAMP, user_name VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE status (status_id INT PRIMARY KEY, name VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE audit_log_event (audit_id SERIAL PRIMARY KEY, audit_log_event_type_id INT, audit_date TIMESTAMP, user_id INT, audit_table VARCHAR(255), entity_id INT, entity_name VARCHAR(255), reason_for_change VARCHAR(255), event_crf_id INT, old_value VARCHAR(255), new_value VARCHAR(255), event_crf_version_id INT, study_event_id INT, chain_hash VARCHAR(255))");
        jdbcTemplate.execute("CREATE SEQUENCE audit_event_audit_id_seq START WITH 1");
        jdbcTemplate.execute("CREATE SEQUENCE audit_log_event_audit_id_seq START WITH 1");
        jdbcTemplate.execute("CREATE SEQUENCE subject_subject_id_seq START WITH 1");
        jdbcTemplate.execute("CREATE SEQUENCE study_subject_study_subject_id_seq START WITH 1");
        jdbcTemplate.execute("CREATE SEQUENCE study_event_study_event_id_seq START WITH 1");
        jdbcTemplate.execute("CREATE SEQUENCE event_crf_event_crf_id_seq START WITH 1");
        jdbcTemplate.execute("CREATE SEQUENCE item_data_item_data_id_seq START WITH 1");
        jdbcTemplate.execute("create table crf_version (crf_version_id integer not null, crf_id integer not null, date_created date, date_updated date, description varchar(4000), name varchar(255), oc_oid varchar(255), revision_notes varchar(255), status_id integer, update_id integer, owner_id integer, xform varchar(4000), xform_name varchar(255), primary key (crf_version_id))");
        jdbcTemplate.execute("create table crf (crf_id integer not null, date_created date, date_updated date, description varchar(4000), name varchar(255), oc_oid varchar(255), source_study_id integer, status_id integer, update_id integer, owner_id integer, primary key (crf_id))");
        jdbcTemplate.execute("create table audit_event (audit_id integer not null, id SERIAL, version integer, action_message varchar(4000), audit_date timestamp(6) not null, audit_table varchar(500) not null, entity_id integer, reason_for_change varchar(1000), user_id integer, primary key (audit_id))");
        jdbcTemplate.execute("create table completion_status (completion_status_id integer not null, description varchar(1000), name varchar(255), status_id smallint check (status_id between 0 and 11), primary key (completion_status_id))");
        jdbcTemplate.execute("create table configuration (id integer not null, version integer, description varchar(255), \"key\" varchar(255), value varchar(255), primary key (id))");
        jdbcTemplate.execute("create table event_crf (event_crf_id integer not null, annotations varchar(4000), date_completed timestamp(6), date_created timestamp(6), date_interviewed date, date_updated timestamp(6), date_validate date, date_validate_completed timestamp(6), electronic_signature_status boolean, interviewer_name varchar(255), old_status_id integer, sdv_status boolean not null, sdv_update_id integer, status_id integer, update_id integer, validate_string varchar(256), validator_annotations varchar(4000), validator_id integer, completion_status_id integer, crf_version_id integer, study_event_id integer, study_subject_id integer, owner_id integer, primary key (event_crf_id))");
        jdbcTemplate.execute("create table item (item_id integer not null, date_created date, date_updated date, description varchar(4000), name varchar(255), oc_oid varchar(40) not null, phi_status boolean, status_id smallint check (status_id between 0 and 11), units varchar(64), update_id integer, item_data_type_id integer, item_reference_type_id integer, owner_id integer, primary key (item_id))");
        jdbcTemplate.execute("create table item_data (item_data_id integer not null, date_created date, date_updated date, deleted boolean, old_status_id smallint check (old_status_id between 0 and 11), ordinal integer, status_id smallint check (status_id between 0 and 11), update_id integer, \"value\" varchar(4000), event_crf_id integer, item_id integer, owner_id integer, primary key (item_data_id))");
        jdbcTemplate.execute("create table study (study_id integer not null, age_max varchar(3), age_min varchar(3), allocation varchar(64), assignment varchar(30), collaborators varchar(1000), conditions varchar(500), control varchar(30), date_created date, date_planned_end date, date_planned_start date, date_updated date, duration varchar(30), eligibility varchar(500), endpoint varchar(64), expected_total_enrollment integer, facility_city varchar(255), facility_contact_degree varchar(255), facility_contact_email varchar(255), facility_contact_name varchar(255), facility_contact_phone varchar(255), facility_country varchar(64), facility_name varchar(255), facility_recruitment_status varchar(60), facility_state varchar(20), facility_zip varchar(64), gender varchar(30), healthy_volunteer_accepted boolean, interventions varchar(1000), keywords varchar(255), masking varchar(30), medline_identifier varchar(255), name varchar(255), oc_oid varchar(40) not null, official_title varchar(255), old_status_id integer, phase varchar(30), principal_investigator varchar(255), protocol_date_verification date, protocol_description varchar(1000), protocol_type varchar(30), purpose varchar(64), results_reference boolean, secondary_identifier varchar(255), selection varchar(30), sponsor varchar(255), status_id smallint check (status_id between 0 and 11), summary varchar(255), timing varchar(30), unique_identifier varchar(30), update_id integer, url varchar(255), url_description varchar(255), parent_study_id integer, owner_id integer, primary key (study_id))");
        jdbcTemplate.execute("create table study_event (study_event_id integer not null, date_created timestamp(6), date_end timestamp(6), date_start timestamp(6), date_updated timestamp(6), end_time_flag boolean, location varchar(2000), sample_ordinal integer, start_time_flag boolean, status_id integer, subject_event_status_id integer, update_id integer, study_event_definition_id integer, study_subject_id integer, owner_id integer, primary key (study_event_id))");
        jdbcTemplate.execute("create table study_event_definition (study_event_definition_id integer not null, category varchar(2000), date_created date, date_updated date, description varchar(2000), name varchar(2000), oc_oid varchar(40) not null, ordinal integer, repeating boolean, status_id smallint check (status_id between 0 and 11), type varchar(20), update_id integer, study_id integer, owner_id integer, primary key (study_event_definition_id))");
        jdbcTemplate.execute("create table study_subject (study_subject_id integer not null, date_created timestamp(6), date_updated timestamp(6), enrollment_date date, label varchar(30), oc_oid varchar(40) not null, secondary_label varchar(30), status_id smallint check (status_id between 0 and 11), update_id integer, study_id integer, subject_id integer, owner_id integer, primary key (study_subject_id))");
        jdbcTemplate.execute("create table subject (subject_id integer not null, date_created timestamp(6), date_of_birth date, date_updated timestamp(6), dob_collected boolean, gender char(1), status_id smallint check (status_id between 0 and 11), unique_identifier varchar(255), update_id integer, father_id integer, mother_id integer, owner_id integer, primary key (subject_id))");
        jdbcTemplate.execute("create table user_account (user_id integer not null, access_code varchar(64), account_non_locked boolean not null, api_key varchar(255), date_created date, date_lastvisit timestamp(6), date_updated date, email varchar(120), enable_api_key boolean, enabled boolean not null, first_name varchar(50), institutional_affiliation varchar(255), last_name varchar(50), lock_counter integer not null, passwd varchar(255), passwd_challenge_answer varchar(255), passwd_challenge_question varchar(64), passwd_timestamp date, phone varchar(64), run_webservices boolean not null, status_id smallint check (status_id between 0 and 11), time_zone varchar(255), update_id integer, user_name varchar(64), active_study integer, owner_id integer, user_type_id integer, primary key (user_id))");

        // We will insert a dummy record into the staging map and then call commit
        // This validates the PostgreSQL-specific ON CONFLICT DO NOTHING upsert syntax
        
        String recordId = "test-record-1";
        String payload = "{\"subject_id\":\"sub1\",\"event_id\":\"ev1\",\"item_value\":\"val1\"}";
        
        interopService.validate(recordId, payload);
        
        // Before committing, we must have active field mappings.
        draftService.saveDraftWithId("field-mappings-singleton", "system", "MAPPINGS", "{\"subject_id\":\"/subject_id\",\"event_id\":\"/event_id\",\"item_value\":\"/item_value\"}");
        
        assertDoesNotThrow(() -> {
            interopService.commit(recordId);
        });
    }
}

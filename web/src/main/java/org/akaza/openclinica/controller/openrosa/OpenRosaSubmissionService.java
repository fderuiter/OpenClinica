package org.akaza.openclinica.controller.openrosa;
import org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import java.util.Date;
import org.akaza.openclinica.dao.hibernate.NotificationOutboxDao;
import org.akaza.openclinica.domain.datamap.NotificationOutbox;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

@Component
public class OpenRosaSubmissionService {

    @Autowired
    SubmissionProcessorChain submissionProcessorChain;
    
    @Autowired
    StudyDao studyDao;
    
    @Autowired
    CrfVersionDao crfVersionDao;

    @Autowired
    NotificationOutboxDao notificationOutboxDao;
    
    @Autowired
    UnifiedWorkflowEnforcementService unifiedWorkflowEnforcementService;

    @Transactional
    public void processRequest(Study study, HashMap<String,String> subjectContext, String requestBody, Errors errors, Locale locale, ArrayList <HashMap> listOfUploadFilePaths) throws Exception {
        // Execute save as Hibernate transaction to avoid partial imports
        CrfVersion crfVersion = crfVersionDao.findByOcOID(subjectContext.get("crfVersionOID"));
        String requestPayload = parseSubmission(requestBody, crfVersion);
        runAsTransaction(study, requestPayload, subjectContext, errors, locale ,listOfUploadFilePaths);

        if (!errors.hasErrors()) {
            NotificationOutbox outbox = new NotificationOutbox();
            outbox.setStudyOid(study.getOc_oid());
            outbox.setStudyEventDefId(Integer.valueOf(subjectContext.get("studyEventDefinitionID")));
            outbox.setStudyEventDefOrdinal(Integer.valueOf(subjectContext.get("studyEventOrdinal")));
            outbox.setCrfVersionOid(subjectContext.get("crfVersionOID"));
            outbox.setStatus("PENDING");
            outbox.setAttemptCount(0);
            outbox.setCreatedAt(new Date());
            notificationOutboxDao.saveOrUpdate(outbox);
        }
    }
    
    private void runAsTransaction(Study study, String requestBody, HashMap<String, String> subjectContext, Errors errors, Locale locale,ArrayList <HashMap> listOfUploadFilePaths) throws Exception{

        SubmissionContainer container = new SubmissionContainer(study,requestBody,subjectContext,errors,locale ,listOfUploadFilePaths);
        try {
            submissionProcessorChain.processSubmission(container);
        } finally {
            if (container.getEventCrf() != null) {
                unifiedWorkflowEnforcementService.unlock(container.getEventCrf());
            }
        }

    }

    private String parseSubmission(String body, CrfVersion crfVersion) {
        if (crfVersion.getXform() != null && !crfVersion.getXform().equals("")) {
            body = body.substring(body.indexOf("<" + crfVersion.getXformName()));
            int length = body.indexOf(" ");
            body = body.replace(body.substring(body.lastIndexOf("<meta>"), body.lastIndexOf("</meta>") + 7), "");
            body = body.substring(0, body.lastIndexOf("</" + crfVersion.getXformName()) + length + 2);
            body = "<instance>" + body + "</instance>";
        } else {
            body = body.substring(body.indexOf("<F_"));
            int length = body.indexOf(" ");
            body = body.replace(body.substring(body.indexOf("<meta>"), body.indexOf("</meta>") + 7), "");
            body = body.substring(0, body.indexOf("</F_") + length + 2);
            body = "<instance>" + body + "</instance>";
        }
        return body;
    }


}

package org.akaza.openclinica.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/auth/api/v1/studyevent")
public class StudyEventController {

	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;
	
	@Autowired
	private org.akaza.openclinica.service.EventService eventService;
	
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());


	/**
	 * @api {put} /pages/auth/api/v1/studyevent/studysubject/{studySubjectOid}/studyevent/{studyEventDefOid}/ordinal/{ordinal}/complete Complete a Participant Event
	 * @apiName completeParticipantEvent
	 * @apiPermission Authenticate using api-key. admin
	 * @apiVersion 1.0.0
	 * @apiParam {String} studySubjectOid Study Subject OID.
	 * @apiParam {String} studyEventDefOid Study Event Definition OID.
	 * @apiParam {Integer} ordinal Ordinal of Study Event Repetition.
	 * @apiGroup Form
	 * @apiHeader {String} api_key Users unique access-key.
	 * @apiDescription Completes a participant study event.
	 * @apiErrorExample {json} Error-Response:
	 *                  HTTP/1.1 403 Forbidden
	 *                  {
	 *                  "code": "403",
	 *                  "message": "Request Denied.  Operation not allowed."
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "code": "200",
	 *                    "message": "Success."
	 *                    }
	 */
	@RequestMapping(value = "/studysubject/{studySubjectOid}/studyevent/{studyEventDefOid}/ordinal/{ordinal}/complete", method = RequestMethod.PUT)
	public @ResponseBody Map<String,String> completeParticipantEvent(HttpServletRequest request, @PathVariable("studySubjectOid") String studySubjectOid, 
			@PathVariable("studyEventDefOid") String studyEventDefOid,
			@PathVariable("ordinal") Integer ordinal)
			throws Exception {
		
		Map<String,String> response = new HashMap<String,String>();
		
        try {
            boolean success = eventService.completeParticipantEvent(studySubjectOid, studyEventDefOid, ordinal);
            if (!success) {
                response.put("code", String.valueOf(HttpStatus.FORBIDDEN.value()));
                response.put("message", "Request Denied.  Operation not allowed.");
                return response;
            }
        } catch (Exception e) {
            logger.error("Error encountered while completing Study Event: " + e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));

            response.put("code", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
			response.put("message", "Error encountered while completing participant event.");
			return response;
        }

		response.put("code",  String.valueOf(HttpStatus.OK.value()));
		response.put("message", "Success.");
		return response;
	}
}

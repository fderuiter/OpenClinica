package org.akaza.openclinica.controller;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.web.restful.ODMClinicaDataResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

@Controller
@RequestMapping(value = "/auth/api/v1/clinicaldata")
public class ODMClinicalDataController {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	@Autowired
	ODMClinicaDataResource odmClinicaDataResource;

	@RequestMapping(value = "/json/view/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}", method = RequestMethod.GET)
	public void getClinicalData(
			@PathVariable("studyOID") String studyOID,
			@PathVariable("formVersionOID") String formVersionOID,
			@PathVariable("studyEventOID") String studyEventOID,
			@PathVariable("studySubjectIdentifier") String studySubjectIdentifier,
			@RequestParam(value = "includeDNs", defaultValue = "n", required = false) String includeDns,
			@RequestParam(value = "includeAudits", defaultValue = "n", required = false) String includeAudits,
			@RequestParam(value = "modifiedSince", required = false) String modifiedSince,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		ResourceBundleProvider.updateLocale(new Locale("en_US"));

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            jakarta.ws.rs.core.StreamingOutput stream = (jakarta.ws.rs.core.StreamingOutput) odmClinicaDataResource.getODMClinicaldata(
				studyOID, formVersionOID, studyEventOID, studySubjectIdentifier, includeDns, includeAudits, modifiedSince, request);
            stream.write(response.getOutputStream());
        } catch (Exception e) {
            // "the system must terminate the network connection abruptly to signal failure if an error occurs mid-stream"
            // We just throw the exception so the servlet container drops the connection or sends 500 if headers aren't committed
            throw e;
        }
	}
}

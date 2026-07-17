package org.akaza.openclinica.web.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

import org.akaza.openclinica.bean.extract.odm.JsonClinicalDataStreamWriter;
import org.akaza.openclinica.bean.extract.odm.XmlClinicalDataStreamWriter;
import org.akaza.openclinica.bean.extract.odm.JsonPostProcessor;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.glassfish.jersey.server.mvc.Viewable;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedHashMap;

@Path("/clinicaldata")
@Component
@Scope("prototype")
public class ODMClinicaDataResource {
    private StudyDAO _studyDAO;
    private StudySubjectDAO _studySubjectDAO;

    @Autowired
    public ODMClinicaDataResource(StudyDAO _studyDAO, StudySubjectDAO _studySubjectDAO) {
        this._studyDAO = _studyDAO;
        this._studySubjectDAO = _studySubjectDAO;
    }

	private static final Logger LOGGER = LoggerFactory.getLogger(ODMClinicaDataResource.class);

	private ClinicalDataCollectorResource clinicalDataCollectorResource;
	private MetadataCollectorResource metadataCollectorResource;
	private DataSource dataSource;

	public MetadataCollectorResource getMetadataCollectorResource() { return metadataCollectorResource; }
	public void setMetadataCollectorResource(MetadataCollectorResource metadataCollectorResource) { this.metadataCollectorResource = metadataCollectorResource; }
	public ClinicalDataCollectorResource getClinicalDataCollectorResource() { return clinicalDataCollectorResource; }
	public void setClinicalDataCollectorResource(ClinicalDataCollectorResource clinicalDataCollectorResource) { this.clinicalDataCollectorResource = clinicalDataCollectorResource; }
	public DataSource getDataSource() { return dataSource; }
	public void setDataSource(DataSource dataSource) { this.dataSource = dataSource; }

	@GET
	@Path("/json/view/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}")
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getODMClinicaldata(@PathParam("studyOID") final String studyOID,
			@PathParam("formVersionOID") final String formVersionOID,
			@PathParam("studyEventOID") final String studyEventOID,
			@PathParam("studySubjectIdentifier") final String studySubjectIdentifier,
			@DefaultValue("n") @QueryParam("includeDNs") String includeDns,
			@DefaultValue("n") @QueryParam("includeAudits") String includeAudits,
			@QueryParam("modifiedSince") String modifiedSince,
			@Context final HttpServletRequest request) {
		
        LOGGER.debug("Requesting clinical data resource");
		java.util.Date modifiedSinceDate = null;
		if (modifiedSince != null && !modifiedSince.trim().isEmpty()) {
			try {
				modifiedSinceDate = java.util.Date.from(java.time.Instant.parse(modifiedSince));
			} catch (Exception e) {
				throw new jakarta.ws.rs.WebApplicationException(jakarta.ws.rs.core.Response.status(400).entity("Invalid date format").build());
			}
		}
		boolean includeDN = false;
		boolean includeAudit = false;
		if(includeDns.equalsIgnoreCase("yes")||includeDns.equalsIgnoreCase("y")) includeDN=true;
		if(includeAudits.equalsIgnoreCase("yes")||includeAudits.equalsIgnoreCase("y")) includeAudit=true;
		
        final boolean finalIncludeDN = includeDN;
        final boolean finalIncludeAudit = includeAudit;
        final Date finalModifiedSinceDate = modifiedSinceDate;
		final int userId = ((UserAccountBean)request.getSession().getAttribute("userBean")).getId();
        final java.util.Locale locale = request.getLocale();

        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, jakarta.ws.rs.WebApplicationException {
                try {
                    int totalSubjects = 2;
                    if (!"*".equals(studySubjectIdentifier)) {
                        totalSubjects = 1;
                    }
                    
                    org.akaza.openclinica.bean.extract.odm.FullReportBean report = getMetadataCollectorResource()
                            .collectODMMetadataForClinicalData(studyOID, formVersionOID, new LinkedHashMap<String, org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean>());
                    if (finalModifiedSinceDate != null) {
                        report.getOdmBean().setFileType("Transactional");
                    }
                    report.createStudyMetaOdmXml(false);
                    String metadataXml = report.getXmlOutput().toString().trim();

                    JsonPostProcessor processor = new JsonPostProcessor() {
                        JSONClinicalDataPostProcessor p = new JSONClinicalDataPostProcessor(locale);
                        @Override
                        public void process(JsonNode json) {
                            p.process(json);
                        }
                    };

                    JsonClinicalDataStreamWriter writer = new JsonClinicalDataStreamWriter(output, "oc1.3", locale, totalSubjects, processor);
                    writer.writeStartDocument(studyOID, formVersionOID, metadataXml);
                    
                    getClinicalDataCollectorResource().getGenerateClinicalDataService().streamClinicalData(
                        studyOID, getStudySubjectOID(studySubjectIdentifier, studyOID), studyEventOID, formVersionOID,
                        finalIncludeDN, finalIncludeAudit, locale, userId, finalModifiedSinceDate, writer
                    );
                    writer.writeEndDocument();
                    writer.close();
                } catch (Exception e) {
                    LOGGER.error("Error streaming json clinical data", e);
                    throw new IOException(e);
                }
            }
        };
	}

	@GET
	@Path("/html/print/{studyOID}/{studySubjectIdentifier}/{eventOID}/{formVersionOID}")
	public Viewable getPrintCRFController(@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@PathParam("studyOID") String studyOID,
			@PathParam("studySubjectIdentifier") String studySubjectIdentifier,
			@PathParam("eventOID") String eventOID,
			@PathParam("formVersionOID") String formVersionOID,	@DefaultValue("n") @QueryParam("includeDNs") String includeDns,
			@DefaultValue("n") @QueryParam("includeAudits") String includeAudits)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		request.setAttribute("studyOID", studyOID);
		request.setAttribute("studySubjectOID", getStudySubjectOID(studySubjectIdentifier,studyOID));
		request.setAttribute("eventOID", eventOID);
		request.setAttribute("formVersionOID", formVersionOID);
		request.setAttribute("includeAudits", includeAudits);
		request.setAttribute("includeDNs", includeDns);
		return new Viewable("/WEB-INF/jsp/printcrf.jsp", null);
	}

	@GET
	@Path("/xml/view/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}")
	@Produces(MediaType.TEXT_XML)
	public StreamingOutput getODMMetadata(@PathParam("studyOID") final String studyOID,
			@PathParam("formVersionOID") final String formVersionOID,
			@PathParam("studySubjectIdentifier") final String studySubjectIdentifier,
			@PathParam("studyEventOID") final String studyEventOID,
			@DefaultValue("n") @QueryParam("includeDNs") String includeDns,
			@DefaultValue("n") @QueryParam("includeAudits") String includeAudits,
			@QueryParam("modifiedSince") String modifiedSince,
			@Context final HttpServletRequest request) {
		
        LOGGER.debug("Requesting clinical data resource XML");
		java.util.Date modifiedSinceDate = null;
		if (modifiedSince != null && !modifiedSince.trim().isEmpty()) {
			try {
				modifiedSinceDate = java.util.Date.from(java.time.Instant.parse(modifiedSince));
			} catch (Exception e) {
				throw new jakarta.ws.rs.WebApplicationException(jakarta.ws.rs.core.Response.status(400).entity("Invalid date format").build());
			}
		}
		boolean includeDN=false;
		boolean includeAudit= false;
		if(includeDns.equalsIgnoreCase("yes")||includeDns.equalsIgnoreCase("y")) includeDN=true;
		if(includeAudits.equalsIgnoreCase("yes")||includeAudits.equalsIgnoreCase("y")) includeAudit=true;
		
        final boolean finalIncludeDN = includeDN;
        final boolean finalIncludeAudit = includeAudit;
        final Date finalModifiedSinceDate = modifiedSinceDate;
		final int userId = ((UserAccountBean)request.getSession().getAttribute("userBean")).getId();

        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, jakarta.ws.rs.WebApplicationException {
                try {
                    org.akaza.openclinica.bean.extract.odm.FullReportBean report = getMetadataCollectorResource()
                            .collectODMMetadataForClinicalData(studyOID, formVersionOID, new LinkedHashMap<String, org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean>());
                    if (finalModifiedSinceDate != null) {
                        report.getOdmBean().setFileType("Transactional");
                    }
                    report.createStudyMetaOdmXml(false);
                    String metadataXml = report.getXmlOutput().toString().trim();

                    XmlClinicalDataStreamWriter writer = new XmlClinicalDataStreamWriter(output, "oc1.3");
                    writer.writeStartDocument(studyOID, formVersionOID, metadataXml);
                    
                    getClinicalDataCollectorResource().getGenerateClinicalDataService().streamClinicalData(
                        studyOID, getStudySubjectOID(studySubjectIdentifier, studyOID), studyEventOID, formVersionOID,
                        finalIncludeDN, finalIncludeAudit, request.getLocale(), userId, finalModifiedSinceDate, writer
                    );
                    writer.writeEndDocument();
                    writer.close();
                } catch (Exception e) {
                    LOGGER.error("Error streaming xml clinical data", e);
                    throw new IOException(e);
                }
            }
        };
	}

	private String getStudySubjectOID(String subjectIdentifier, String studyOID) {
		StudySubjectDAO studySubjectDAO = this._studySubjectDAO;
		StudySubjectBean studySubject = studySubjectDAO.findByOid(subjectIdentifier);
		if (subjectIdentifier.equals("*") || (studySubject != null  && studySubject.getOid() != null)) return subjectIdentifier;
		else {
			StudyDAO studyDAO = this._studyDAO;
			StudyBean study = studyDAO.findByOid(studyOID);
			studySubject = studySubjectDAO.findByLabelAndStudy(subjectIdentifier,study);
			if (studySubject != null && studySubject.getOid() != null) return studySubject.getOid();
			else return subjectIdentifier;
		}
	}
}

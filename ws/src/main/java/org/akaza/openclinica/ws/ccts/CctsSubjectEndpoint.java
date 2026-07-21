/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2009 Akaza Research 
 */
package org.akaza.openclinica.ws.ccts;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.service.subject.SubjectServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.xml.DomUtils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

/**
 * @author Krikor Krumlian
 *
 */
@Endpoint
public class CctsSubjectEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/ccts/subject/v1";
    private final String SUCCESS_MESSAGE = "success";
    private String dateFormat;

    private final SubjectServiceInterface subjectService;
    private final DataSource dataSource;
    private final MessageSource messages;
    private TransactionTemplate transactionTemplate;

    /**
     * Constructor
     * @param subjectService
     * @param dataSource
     * @param messages
     */
    public CctsSubjectEndpoint(SubjectServiceInterface subjectService, DataSource dataSource, MessageSource messages) {
        this.subjectService = subjectService;
        this.dataSource = dataSource;
        this.messages = messages;
    }

    /**
     * if NAMESPACE_URI_V1:commitRequest execute this method
     * @param gridId
     * @param subject
     * @param studyOid
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "commitRequest", namespace = NAMESPACE_URI_V1)
    public Source createSubject(@XPathParam("//s:gridId") String gridId, @XPathParam("//s:subject") NodeList subject,
            @XPathParam("//s:study/@oid") String studyOid) throws Exception {
        Element subjectElement = (Element) (subject.item(0));
        final SubjectTransferBean subjectTransferBean = unMarshallToSubjectTransfer(gridId, subjectElement, studyOid);
        
        return getTransactionTemplate().execute(new TransactionCallback<Source>() {
            public Source doInTransaction(TransactionStatus status) {
                try {
                    logger.debug("In CreateSubject");
                    StudyDAO studyDao = org.akaza.openclinica.dao.core.DaoBridge.getDao(StudyDAO.class);
                    StudyBean studyBean = studyDao.findByOid(subjectTransferBean.getStudyOid());
                    if (studyBean == null || studyBean.getId() <= 0) {
                        throw new RuntimeException("Study not found with OID: " + subjectTransferBean.getStudyOid());
                    }
                    subjectTransferBean.setStudy(studyBean);

                    StudySubjectDAO ssdao = org.akaza.openclinica.dao.core.DaoBridge.getDao(StudySubjectDAO.class);
                    StudySubjectBean ssbean = ssdao.findByLabelAndStudy(subjectTransferBean.getStudySubjectId(), studyBean);
                    if (ssbean != null && ssbean.getId() > 0) {
                        // Subject already exists
                        return new DOMSource(mapConfirmation(SUCCESS_MESSAGE));
                    }

                    SubjectBean subjectBean = new SubjectBean();
                    subjectBean.setUniqueIdentifier(subjectTransferBean.getPersonId());
                    subjectBean.setLabel(subjectTransferBean.getStudySubjectId());
                    subjectBean.setDateOfBirth(subjectTransferBean.getDateOfBirth());
                    if (subjectBean.getDateOfBirth() != null) {
                        subjectBean.setDobCollected(true);
                    } else {
                        subjectBean.setDobCollected(false);
                    }
                    subjectBean.setGender(subjectTransferBean.getGender());
                    
                    UserAccountBean userAccount = getUserAccount();
                    if (userAccount != null) {
                        subjectBean.setOwner(userAccount);
                    }

                    subjectBean.setCreatedDate(new Date());
                    
                    subjectService.createSubject(subjectBean, studyBean, subjectTransferBean.getEnrollmentDate(), null);

                    return new DOMSource(mapConfirmation(SUCCESS_MESSAGE));
                } catch (Exception e) {
                    status.setRollbackOnly();
                    try {
                        return new DOMSource(mapErrorConfirmation(e));
                    } catch (Exception ex) {
                        throw new RuntimeException("Error processing subject", e);
                    }
                }
            }
        });
    }

    /**
     * if NAMESPACE_URI_V1:rollbackRequest execute this method
     * @param gridId
     * @param subject
     * @param studyOid
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "rollbackRequest", namespace = NAMESPACE_URI_V1)
    public Source rollBackSubject(@XPathParam("//s:gridId") String gridId, @XPathParam("//s:subject") NodeList subject,
            @XPathParam("//s:study/@oid") String studyOid) throws Exception {
        Element subjectElement = (Element) (subject.item(0));
        final SubjectTransferBean subjectTransferBean = unMarshallToSubjectTransfer(gridId, subjectElement, studyOid);
        
        return getTransactionTemplate().execute(new TransactionCallback<Source>() {
            public Source doInTransaction(TransactionStatus status) {
                try {
                    // CCTS Rollback logic - typically just acknowledges or marks as failed
                    // For now, since it was skeletal, returning success. If subject removal is needed, we'd implement it here.
                    return new DOMSource(mapConfirmation(SUCCESS_MESSAGE));
                } catch (Exception e) {
                    status.setRollbackOnly();
                    try {
                        return new DOMSource(mapErrorConfirmation(e));
                    } catch (Exception ex) {
                        throw new RuntimeException("Error processing rollback", e);
                    }
                }
            }
        });
    }

    /**
     * UnMarshall SubjectTransferBean, aka create SubjectTransferBean from XML
     * @param gridId
     * @param subjectElement
     * @param studyOidValue
     * @return
     * @throws ParseException
     */
    private SubjectTransferBean unMarshallToSubjectTransfer(String gridId, Element subjectElement, String studyOidValue) throws ParseException {

        Element personIdElement = DomUtils.getChildElementByTagName(subjectElement, "personId");
        Element studySubjectIdElement = DomUtils.getChildElementByTagName(subjectElement, "studySubjectId");
        Element secondaryIdElement = DomUtils.getChildElementByTagName(subjectElement, "secondaryId");
        Element enrollmentDateElement = DomUtils.getChildElementByTagName(subjectElement, "enrollmentDate");
        Element genderElement = DomUtils.getChildElementByTagName(subjectElement, "gender");
        Element dateOfBirthElement = DomUtils.getChildElementByTagName(subjectElement, "dateOfBirth");

        String personIdValue = DomUtils.getTextValue(personIdElement);
        String studySubjectIdValue = DomUtils.getTextValue(studySubjectIdElement);
        String genderValue = DomUtils.getTextValue(genderElement);
        String secondaryIdValue = DomUtils.getTextValue(secondaryIdElement);
        String enrollmentDateValue = DomUtils.getTextValue(enrollmentDateElement);
        String dateOfBirthValue = DomUtils.getTextValue(dateOfBirthElement);

        SubjectTransferBean subjectTransferBean = new SubjectTransferBean();

        subjectTransferBean.setStudyOid(studyOidValue);
        subjectTransferBean.setPersonId(personIdValue);
        subjectTransferBean.setStudySubjectId(studySubjectIdValue);
        subjectTransferBean.setGender(genderValue.toCharArray()[0]);
        subjectTransferBean.setDateOfBirth(getDate(dateOfBirthValue));
        subjectTransferBean.setSecondaryId(secondaryIdValue);
        subjectTransferBean.setEnrollmentDate(getDate(enrollmentDateValue));
        return subjectTransferBean;

    }

    /**
     * Create Response 
     * @param confirmation
     * @return
     * @throws Exception
     */
    private Element mapConfirmation(String confirmation) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "commitResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);
        return responseElement;

    }

    /**
     * Create Error Response
     * @param e Exception
     * @return Element
     * @throws Exception
     */
    private Element mapErrorConfirmation(Exception e) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "commitResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent("Fail");
        responseElement.appendChild(resultElement);
        
        Element errorElement = document.createElementNS(NAMESPACE_URI_V1, "error");
        
        String theMessage;
        if (e instanceof org.akaza.openclinica.exception.OpenClinicaSystemException) {
            org.akaza.openclinica.exception.OpenClinicaSystemException oe = (org.akaza.openclinica.exception.OpenClinicaSystemException) e;
            try {
                theMessage = messages.getMessage(oe.getErrorCode(), oe.getErrorParams(), Locale.getDefault());
            } catch (Exception ex) {
                theMessage = oe.getMessage() != null ? oe.getMessage() : oe.getErrorCode();
            }
        } else {
            theMessage = e.getMessage() != null ? e.getMessage() : e.toString();
        }

        errorElement.setTextContent(theMessage);
        responseElement.appendChild(errorElement);
        
        return responseElement;
    }

    /**
     * Helper Method to resolve dates
     * @param dateAsString
     * @return
     * @throws ParseException
     */
    private Date getDate(String dateAsString) throws ParseException {
        if (dateAsString == null || dateAsString.isEmpty()) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(getDateFormat());
        return sdf.parse(dateAsString);
    }

    /**
     * Helper Method to get the user account
     * @return UserAccountBean
     */
    private UserAccountBean getUserAccount() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        UserAccountDAO userAccountDAO = org.akaza.openclinica.dao.core.DaoBridge.getDao(UserAccountDAO.class);
        return (UserAccountBean) userAccountDAO.findByUserName(username);
    }

    /**
     * @return
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * @param dateFormat
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

}
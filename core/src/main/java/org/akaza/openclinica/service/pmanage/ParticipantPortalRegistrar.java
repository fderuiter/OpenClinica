package org.akaza.openclinica.service.pmanage;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import org.akaza.openclinica.bean.login.ParticipantDTO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.akaza.openclinica.sdk.ApiClient;
import org.akaza.openclinica.sdk.api.DefaultApi;



public class ParticipantPortalRegistrar {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String AVAILABLE = "available";
    public static final String UNAVAILABLE = "unavailable";
    public static final String INVALID = "invalid";
    public static final String UNKNOWN = "unknown";
    public static final int PARTICIPATE_READ_TIMEOUT = 5000;

    private DefaultApi getApi(String baseUrl) {
        ApiClient client = new ApiClient();
        client.updateBaseUri(baseUrl);
        client.setConnectTimeout(Duration.ofMillis(PARTICIPATE_READ_TIMEOUT));
        return new DefaultApi(client);
    }

    public Authorization getAuthorization(String studyOid) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String baseUrl = CoreResources.getField("portalURL");
        try {
            List<org.akaza.openclinica.sdk.model.Authorization> sdkResponse = getApi(baseUrl).appRestOcAuthorizationsGet(studyOid, ocUrl);
            com.fasterxml.jackson.databind.ObjectMapper mapper = org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext().getBean("permissiveObjectMapper", com.fasterxml.jackson.databind.ObjectMapper.class);
            java.util.List<Authorization> response = mapper.convertValue(sdkResponse, mapper.getTypeFactory().constructCollectionType(java.util.List.class, Authorization.class));
            if (response != null && response.size() > 0 && response.get(0).getAuthorizationStatus() != null)
                return response.get(0);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    public String getRegistrationStatus(String studyOid) throws Exception {
        return loadRegistrationStatus(studyOid);
    }

    private String loadRegistrationStatus(String studyOid) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String baseUrl = CoreResources.getField("portalURL");
        try {
            List<org.akaza.openclinica.sdk.model.Authorization> sdkResponse = getApi(baseUrl).appRestOcAuthorizationsGet(studyOid, ocUrl);
            com.fasterxml.jackson.databind.ObjectMapper mapper = org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext().getBean("permissiveObjectMapper", com.fasterxml.jackson.databind.ObjectMapper.class);
            java.util.List<Authorization> response = mapper.convertValue(sdkResponse, mapper.getTypeFactory().constructCollectionType(java.util.List.class, Authorization.class));
            if (response != null && response.size() > 0 && response.get(0).getAuthorizationStatus() != null)
                return response.get(0).getAuthorizationStatus().getStatus();
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.debug(ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

    public String getHostNameAvailability(String hostName) {
        String baseUrl = CoreResources.getField("portalURL");
        try {
            if (!validHostNameCheck(hostName))
                return INVALID;
            String response = getApi(baseUrl).appPermitStudysNameGet(hostName);
            if ("UNAVAILABLE".equals(response))
                return UNAVAILABLE;
            else if ("INVALID".equals(response))
                return INVALID;
            else if ("AVAILABLE".equals(response))
                return AVAILABLE;
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return UNKNOWN;
    }

    public boolean validHostNameCheck(String hostName) {
        String pManageBaseUrl = CoreResources.getField("portalURL");
        if (hostName.contains("."))
            return false;
        try {
            URL baseUrl = new URL(pManageBaseUrl);
            String port = "";
            if (baseUrl.getPort() > 0)
                port = ":" + String.valueOf(baseUrl.getPort());
            // Check that hostname makes a valid URL
            URL customerUrl = new URL(baseUrl.getProtocol() + "://" + hostName + "." + baseUrl.getHost() + port);
            // Check that hostname only contains alphanumeric characters and/or hyphens
            if (hostName.matches("^[A-Za-z0-9-]+$"))
                return true;
        } catch (MalformedURLException mue) {
            logger.error("Error validating customer selected Participate subdomain.");
            logger.error(mue.getMessage());
            logger.error(ExceptionUtils.getStackTrace(mue));
        }
        return false;
    }

    public String registerStudy(String studyOid) {
        return registerStudy(studyOid, null, null);
    }

    public String sendEmailThruMandrillViaOcui(ParticipantDTO participantDTO, String hostname) {
    	String host = hostname.substring(0,hostname.indexOf("/app/oauth2"));
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext().getBean("permissiveObjectMapper", com.fasterxml.jackson.databind.ObjectMapper.class);
            org.akaza.openclinica.sdk.model.ParticipantDTO sdkDto = mapper.convertValue(participantDTO, org.akaza.openclinica.sdk.model.ParticipantDTO.class);
            getApi(host).appRestOcEmailPost(sdkDto);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

    public String registerStudy(String studyOid, String hostName, String studyName) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String baseUrl = CoreResources.getField("portalURL");
        
        org.akaza.openclinica.sdk.model.Authorization authRequest = new org.akaza.openclinica.sdk.model.Authorization();
        org.akaza.openclinica.sdk.model.Study authStudy = new org.akaza.openclinica.sdk.model.Study();
        authStudy.setStudyOid(studyOid);
        authStudy.setInstanceUrl(ocUrl);
        authStudy.setHost(hostName);
        authStudy.setStudyName(studyName);
        authStudy.setOpenClinicaVersion(CoreResources.getField("OpenClinica.version"));
        authRequest.setStudy(authStudy);

        try {
            org.akaza.openclinica.sdk.model.Authorization response = getApi(baseUrl).appRestOcAuthorizationsPost(authRequest);
            if (response != null && response.getAuthorizationStatus() != null)
                return response.getAuthorizationStatus().getStatus();
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

    public String getStudyHost(String studyOid) throws Exception {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL");

        try {
            List<org.akaza.openclinica.sdk.model.Authorization> sdkResponse = getApi(pManageUrl).appRestOcAuthorizationsGet(studyOid, ocUrl);
            com.fasterxml.jackson.databind.ObjectMapper mapper = org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext().getBean("permissiveObjectMapper", com.fasterxml.jackson.databind.ObjectMapper.class);
            java.util.List<Authorization> response = mapper.convertValue(sdkResponse, mapper.getTypeFactory().constructCollectionType(java.util.List.class, Authorization.class));
            if (response != null && response.size() > 0 && response.get(0).getStudy() != null 
                    && response.get(0).getStudy().getHost() != null
                    && !response.get(0).getStudy().getHost().equals("")) {
                URL url = new URL(pManageUrl);
                String port = "";
                if (url.getPort() > 0)
                    port = ":" + String.valueOf(url.getPort());
                return url.getProtocol() + "://" + response.get(0).getStudy().getHost() + "." + url.getHost() + port + "/app/oauth2";
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

}

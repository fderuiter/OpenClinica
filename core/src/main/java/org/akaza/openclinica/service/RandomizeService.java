package org.akaza.openclinica.service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.DynamicsItemGroupMetadataDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.action.StratificationFactorBean;
import org.akaza.openclinica.service.pmanage.RandomizationRegistrar;
import org.akaza.openclinica.service.pmanage.SeRandomizationDTO;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.akaza.openclinica.randomize.ApiClient;
import org.akaza.openclinica.randomize.ApiException;
import org.akaza.openclinica.randomize.api.DefaultApi;
import org.akaza.openclinica.randomize.model.GetRandomisation200Response;
import org.apache.commons.codec.binary.Base64;
import java.nio.charset.Charset;

public class RandomizeService extends RandomizationRegistrar {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String ESCAPED_SEPERATOR = "\\.";
    private DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;
    private DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao;
    DataSource ds;
    private EventCRFDAO eventCRFDAO;
    private ItemDataDAO itemDataDAO;
    private ItemDAO itemDAO;
    private ItemGroupDAO itemGroupDAO;
    private SectionDAO sectionDAO;
    // private CRFVersionDAO crfVersionDAO;
    private ItemFormMetadataDAO itemFormMetadataDAO;
    private ItemGroupMetadataDAO itemGroupMetadataDAO;
    private StudyEventDAO studyEventDAO;
    private EventDefinitionCRFDAO eventDefinitionCRFDAO;
    private ExpressionService expressionService;
    public static final int RANDOMIZATION_READ_TIMEOUT = 10000;
    StudyDAO sdao=null;


    public RandomizeService(DataSource ds) {
        this.ds = ds;
        this.expressionService = new ExpressionService(ds);
    }

    // Rest Call to OCUI to get Randomization

    public String getRandomizationCode(EventCRFBean eventCrfBean, List<StratificationFactorBean> stratificationFactorBeans, RuleSetBean ruleSet) {
        StudySubjectDAO ssdao = new StudySubjectDAO<>(ds);
        StudySubjectBean ssBean = (StudySubjectBean) ssdao.findByPK(eventCrfBean.getStudySubjectId());
        String identifier = ssBean.getOid(); // study subject oid
        StudyDAO sdao = new StudyDAO<>(ds);
        StudyBean sBean = (StudyBean) sdao.findByPK(ssBean.getStudyId());
        String siteIdentifier = sBean.getOid(); // site or study oid
        String name = sBean.getName(); // site or study name
        UserAccountDAO udao = new UserAccountDAO(ds);
        int userId = 0;
        if (eventCrfBean.getUpdaterId() == 0) {
            userId = eventCrfBean.getOwnerId();
        } else {
            userId = eventCrfBean.getUpdaterId();
        }
        UserAccountBean uBean = (UserAccountBean) udao.findByPK(userId);
        String user = uBean.getName();

        // sBean should be parent study
        // put randomization object in cache
       
        StudyBean study = getParentStudy(sBean.getOid());        
        SeRandomizationDTO randomization = null;

        try {
            randomization = getCachedRandomizationDTOObject(study.getOid(), false);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String randomiseUrl = randomization.getUrl();
        String username = randomization.getUsername();
        String password = randomization.getPassword();
        String timezone = "America/New_York";

        ApiClient client = new ApiClient();
        client.setBasePath(randomiseUrl);
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        client.setRequestInterceptor(builder -> builder.header("Authorization", authHeader));
        
        DefaultApi api = new DefaultApi(client);

        try {
            GetRandomisation200Response randResponse = api.getRandomisation(identifier);
            if (randResponse != null && randResponse.getCode() != null) {
                return randResponse.getCode();
            }
        } catch (ApiException e) {
            logger.error("Failed to retrieve randomisation: " + e.getMessage());
        }
        
        try {
            api.addSite(siteIdentifier, name, timezone);
        } catch (ApiException e) {
            logger.error("Failed to add or update site: " + e.getMessage());
        }

        int i = 1;
        String exp = "";
        String[] questions = new String[10];
        for (StratificationFactorBean stratificationFactorBean : stratificationFactorBeans) {
            exp = stratificationFactorBean.getStratificationFactor().getValue();
            if (exp.startsWith("SS.")) {
                questions[i - 1] = getStudySubjectAttrValue(exp, eventCrfBean, ruleSet);
            } else {
                questions[i - 1] = getExpressionValue(exp, eventCrfBean, ruleSet);
            }
            i++;
        }

        try {
            GetRandomisation200Response randResponse = api.randomiseSubject(
                identifier, siteIdentifier, user, 
                questions[0], questions[1], questions[2], questions[3], questions[4], 
                questions[5], questions[6], questions[7], questions[8], questions[9]
            );
            if (randResponse != null && randResponse.getCode() != null) {
                return randResponse.getCode();
            }
        } catch (ApiException e) {
            logger.error("Failed to randomise subject: " + e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

    private String getExpressionValue(String expr, EventCRFBean eventCrfBean, RuleSetBean ruleSet) {
        String expression = getExpressionService().constructFullExpressionIfPartialProvided(expr, ruleSet.getTarget().getValue());
        ItemDataBean itemData = null;
        if (expression != null && !expression.isEmpty()) {
            ItemBean itemBean = getExpressionService().getItemBeanFromExpression(expression);
            String itemGroupBOrdinal = getExpressionService().getGroupOrdninalCurated(expression);
            itemData = getItemDataDAO().findByItemIdAndEventCRFIdAndOrdinal(itemBean.getId(), eventCrfBean.getId(),
                    itemGroupBOrdinal == "" ? 1 : Integer.valueOf(itemGroupBOrdinal));
        }
        return itemData.getValue();
    }

    private String getStudySubjectAttrValue(String expr, EventCRFBean eventCrfBean, RuleSetBean ruleSet) {
        String value = "";
        StudySubjectDAO<String, ArrayList> ssdao = new StudySubjectDAO<>(ds);
        SubjectDAO subdao = new SubjectDAO(ds);
        StudyGroupClassDAO sgcdao = new StudyGroupClassDAO(ds);
        StudyGroupDAO sgdao = new StudyGroupDAO(ds);
        StudyDAO<String, ArrayList> sdao = new StudyDAO<>(ds);
        StudySubjectBean ssBean = (StudySubjectBean) ssdao.findByPK(eventCrfBean.getStudySubjectId());
        SubjectBean subjectBean = (SubjectBean) subdao.findByPK(ssBean.getSubjectId());

        String prefix = "STUDYGROUPCLASSLIST";
        String param = expr.split("\\.", -1)[1].trim();

        if (param.equalsIgnoreCase("BIRTHDATE")) {
            value = subjectBean.getDateOfBirth().toString();
        } else if (param.equalsIgnoreCase("SEX")) {
            if (String.valueOf(subjectBean.getGender()).equals("m"))
                value = "Male";
            else
                value = "Female";

            // value =String.valueOf(ssBean.getGender());

        } else if (param.startsWith(prefix)) {
            String gcName = param.substring(21, param.indexOf("\"]"));

            StudyGroupBean sgBean = sgdao.findSubjectStudyGroup(ssBean.getId(), gcName);
            if (sgBean != null)
                value = sgBean.getName();
        }
        return value;
    }

    public ExpressionService getExpressionService() {
        return expressionService;
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public ItemDataDAO getItemDataDAO() {
        return new ItemDataDAO(ds);
    }

    public void setItemDataDAO(ItemDataDAO itemDataDAO) {
        this.itemDataDAO = itemDataDAO;
    }
    private StudyBean getStudy(String oid) {
        sdao = new StudyDAO(ds);
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private StudyBean getParentStudy(String studyOid) {
        StudyBean study = getStudy(studyOid);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }

}

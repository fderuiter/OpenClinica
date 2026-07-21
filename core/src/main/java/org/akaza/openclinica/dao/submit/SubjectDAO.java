/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.submit;

import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.sql.DataSource;

/**
 * @author jxu
 */
@Repository
@Scope("prototype")
public class SubjectDAO extends AuditableEntityDAO {
    // private DataSource ds;
    // private DAODigester digester;
    // protected String

    protected void setQueryNames() {
        getCurrentPKName = "getCurrentPrimaryKey";
    }

    @Autowired
    public SubjectDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public SubjectDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public SubjectDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_SUBJECT;
    }

    @Override
    public void setTypesExpected() {
        // SERIAL, NUMERIC, NUMERIC, NUMERIC,
        // DATE, CHAR(1), VARCHAR(255),DATE,
        // NUMERIC, DATE, NUMERIC
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.INT);
        this.setTypeExpected(5, TypeNames.DATE);
        this.setTypeExpected(6, TypeNames.CHAR);
        this.setTypeExpected(7, TypeNames.STRING);
        this.setTypeExpected(8, TypeNames.DATE);
        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.DATE);
        this.setTypeExpected(11, TypeNames.INT);
        this.setTypeExpected(12, TypeNames.BOOL);
        this.setTypeExpected(13, TypeNames.STRING);

    }

    /**
     * findAllSubjectsAndStudies()
     *
     * For every subject find all studies that subject belongs to.
     *
     * smw
     *
     */
    public ArrayList findAllSubjectsAndStudies() {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();
        this.setTypeExpected(13, TypeNames.CHAR); // label from study_subject table
        this.setTypeExpected(14, TypeNames.CHAR); // unique_identifier from study table

        String sql = digester.getQuery("findAllSubjectsAndStudies");

        ArrayList alist = this.select(sql);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            SubjectBean sb = (SubjectBean) this.getEntityFromHashMap(hm);
            sb.setLabel((String) hm.get("label"));
            sb.setStudyIdentifier((String) hm.get("study_unique_identifier"));

            answer.add(sb);
        }

        return answer;
    }

    /**
     * @param gender
     *            Use 'm' for males, 'f' for females.
     * @return All subjects who are male, if <code>gender == 'm'</code>, or all
     *         subjects who are female, if <code>gender == 'f'</code>, or a
     *         blank list, otherwise.
     */
    public ArrayList findAllByGender(char gender) {
        if (gender == 'm') {
            return findAllMales();
        } else if (gender == 'f') {
            return findAllFemales();
        }
        return new ArrayList();
    }

    public ArrayList findAllFemales() {

        return executeFindAllQuery("findAllFemales");
    }

    public ArrayList findAllMales() {

        return executeFindAllQuery("findAllMales");
    }

    /**
     * @param gender
     *            Use 'm' for males, 'f' for females, not include himself.
     * @return All subjects who are male, if <code>gender == 'm'</code>, or all
     *         subjects who are female, if <code>gender == 'f'</code>, or a
     *         blank list, otherwise.
     */
    public ArrayList findAllByGenderNotSelf(char gender, int id) {
        if (gender == 'm') {
            return findAllMalesNotSelf(id);
        } else if (gender == 'f') {
            return findAllFemalesNotSelf(id);
        }
        return new ArrayList();
    }

    public ArrayList findAllFemalesNotSelf(int id) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(id));
        return executeFindAllQuery("findAllFemalesNotSelf", variables);
    }

    public ArrayList findAllMalesNotSelf(int id) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(id));
        return executeFindAllQuery("findAllMalesNotSelf", variables);
    }

    public ArrayList<SubjectBean> getWithFilterAndSort(StudyBean currentStudy, ListSubjectFilter filter, ListSubjectSort sort, int rowStart, int rowEnd) {
        ArrayList<SubjectBean> subjects = new ArrayList<SubjectBean>();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getId());
        variables.put(new Integer(2), currentStudy.getId());
        String sql = digester.getQuery("getWithFilterAndSort");
        sql = sql + filter.execute("");

        if (CoreResources.getDBName().equals("oracle")) {
            sql += " )x)where r between " + (rowStart + 1) + " and " + rowEnd;
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        //        System.out.println("SQL: "+sql);
        ArrayList rows = this.select(sql);
        Iterator it = rows.iterator();

        while (it.hasNext()) {
            SubjectBean subjectBean = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            subjects.add(subjectBean);
        }
        return subjects;
    }

    public Integer getCountWithFilter(ListSubjectFilter filter, StudyBean currentStudy) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getId());
        variables.put(new Integer(2), currentStudy.getId());
        String sql = digester.getQuery("getCountWithFilter");
        sql += filter.execute("");

        ArrayList rows = this.select(sql);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database
     * query.
     */
    public Object getEntityFromHashMap(HashMap hm) {
        SubjectBean eb = new SubjectBean();
        super.setEntityAuditInformation(eb, hm);
        eb.setId(((Integer) hm.get("subject_id")).intValue());
        Date birthday = (Date) hm.get("date_of_birth");
        eb.setDateOfBirth(birthday);
        try {
            Object genderObj = hm.get("gender");
            if (genderObj != null) {
                if (genderObj instanceof String && ((String) genderObj).length() > 0) {
                    eb.setGender(((String) genderObj).charAt(0));
                } else if (genderObj instanceof Character) {
                    eb.setGender(((Character) genderObj).charValue());
                }
            }
        } catch (Exception ce) {
            eb.setGender(' ');
        }
        eb.setUniqueIdentifier((String) hm.get("unique_identifier"));
        eb.setDobCollected(((Boolean) hm.get("dob_collected")).booleanValue());

        return eb;
    }

    public Collection findAll() {

        return findAllByLimit(false);
    }

    public Collection findAllByLimit(boolean hasLimit) {
        this.setTypesExpected();
        ArrayList alist = null;
        if (hasLimit) {
            alist = this.select(digester.getQuery("findAllByLimit"));
        } else {
            alist = this.select(digester.getQuery("findAll"));
        }
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            SubjectBean eb = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public EntityBean findAnotherByIdentifier(String name, int subjectId) {
        SubjectBean eb = new SubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), name);
        variables.put(new Integer(2), new Integer(subjectId));

        String sql = digester.getQuery("findAnotherByIdentifier");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return eb;
    }

    public Collection findAllChildrenByPK(int subjectId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(subjectId));
        variables.put(new Integer(2), new Integer(subjectId));
        ArrayList alist = this.select(digester.getQuery("findAllChildrenByPK"), variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            SubjectBean eb = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    /**
     * Finds the subject which has the given identifier and is inside given
     * study
     *
     * @param uniqueIdentifier
     * @param studyId
     * @return
     */

    public SubjectBean findByUniqueIdentifierAndAnyStudy(String uniqueIdentifier, int studyId) {
        SubjectBean answer = new SubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), uniqueIdentifier);
        variables.put(new Integer(2), new Integer(studyId));
        variables.put(new Integer(3), new Integer(studyId));

        String sql = digester.getQuery("findByUniqueIdentifierAndAnyStudy");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            answer = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;

    }

    
    public SubjectBean findByUniqueIdentifierAndStudy(String uniqueIdentifier, int studyId) {
        SubjectBean answer = new SubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), uniqueIdentifier);
        variables.put(new Integer(2), new Integer(studyId));

        String sql = digester.getQuery("findByUniqueIdentifierAndStudy");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            answer = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;

    }

    /**
     * Finds the subject which has the given identifier and is inside given
     * study
     *
     * @param uniqueIdentifier
     * @param studyId
     * @return
     */
    public SubjectBean findByUniqueIdentifierAndParentStudy(String uniqueIdentifier, int studyId) {
        SubjectBean answer = new SubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), uniqueIdentifier);
        variables.put(new Integer(2), new Integer(studyId));

        String sql = digester.getQuery("findByUniqueIdentifierAndParentStudy");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            answer = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;

    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public EntityBean findByPK(int ID) {
        SubjectBean eb = new SubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return eb;
    }

    /**
     * @deprecated Creates a new subject
     */
    @Deprecated
    public EntityBean create(EntityBean eb) {
        org.akaza.openclinica.bean.submit.SubjectBean sb = (org.akaza.openclinica.bean.submit.SubjectBean) eb;
        org.akaza.openclinica.dao.hibernate.SubjectDao subjectDao = (org.akaza.openclinica.dao.hibernate.SubjectDao) org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext().getBean("subjectDao");
        org.akaza.openclinica.dao.hibernate.UserAccountDao userDao = (org.akaza.openclinica.dao.hibernate.UserAccountDao) org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext().getBean("userDaoDomain");
        
        org.akaza.openclinica.domain.datamap.Subject subject = new org.akaza.openclinica.domain.datamap.Subject();
        subject.setGender(sb.getGender());
        subject.setUniqueIdentifier(sb.getUniqueIdentifier());
        subject.setDateOfBirth(sb.getDateOfBirth());
        subject.setDobCollected(sb.isDobCollected());
        subject.setDateCreated(sb.getCreatedDate() != null ? sb.getCreatedDate() : new java.util.Date());
        if (sb.getStatus() != null) {
            subject.setStatus(org.akaza.openclinica.domain.Status.getByCode(sb.getStatus().getId()));
        } else {
            subject.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        }
        if (sb.getOwner() != null) {
            subject.setUserAccount(userDao.findById(sb.getOwner().getId()));
        }
        
        jakarta.validation.Validator validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        java.util.Set<jakarta.validation.ConstraintViolation<org.akaza.openclinica.domain.datamap.Subject>> violations = validator.validate(subject);
        if (!violations.isEmpty()) {
            throw new RuntimeException("Validation failed for subject: " + violations.iterator().next().getMessage());
        }

        subjectDao.saveOrUpdate(subject);
        sb.setId(subject.getSubjectId());
        return sb;
    }

    /**
     * Create a subject.
     *
     * @param sb
     *            The subject to create. <code>true</code> if the father and
     *            mother id have been properly set; primarily for use with
     *            genetic studies. <code>false</code> if the father and mother
     *            id have not been properly set; primarily for use with
     *            non-genetic studies.
     * @return
     */
    public SubjectBean create(SubjectBean sb) {
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        logger.debug("Logged in subject DAO.create");
        int ind = 1;

        

        variables.put(new Integer(ind), new Integer(sb.getStatus().getId()));
        ind++;

        if (sb.getDateOfBirth() == null) {
            nullVars.put(new Integer(ind), new Integer(Types.DATE));
            variables.put(new Integer(ind), null);
        } else {
            variables.put(new Integer(ind), sb.getDateOfBirth());
        }
        ind++;

        if (sb.getGender() != 'm' && sb.getGender() != 'f') {
            nullVars.put(new Integer(ind), new Integer(Types.CHAR));
            variables.put(new Integer(ind), null);
        } else {
            char[] ch = { sb.getGender() };
            variables.put(new Integer(ind), new String(ch));
        }
        ind++;
        variables.put(new Integer(ind), sb.getUniqueIdentifier());
        ind++;
        variables.put(new Integer(ind), new Integer(sb.getOwnerId()));
        ind++;
        variables.put(new Integer(ind), new Boolean(sb.isDobCollected()));
        ind++;

        executeWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            sb.setId(getLatestPK());
        }

        return sb;
    }

    /**
     * Create a subject whose father and mother id have been properly set. This
     * is primarily for use when creating subjects in genetic studies.
     *
     * @param sb
     *            The subject to create.
     * @return The created subject, with id set according to the insert id if
     *         the operation was successful, or id set to 0 otherwise.
     * @deprecated
     */
    @Deprecated
    public SubjectBean createWithParents(SubjectBean sb) {
        return create(sb);
    }

    /**
     * Create a subject whose father and mother id have not been properly set.
     * This is primarily for use when creating subjects in non-genetic studies.
     *
     * @param sb
     *            The subject to create.
     * @return The created subject, with id set according to the insert id if
     *         the operation was successful, or id set to 0 otherwise.
     * @deprecated
     */
    @Deprecated
    public SubjectBean createWithoutParents(SubjectBean sb) {
        return create(sb);
    }

    public SubjectBean findByUniqueIdentifier(String uniqueIdentifier) {
        SubjectBean answer = new SubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), uniqueIdentifier);

        String sql = digester.getQuery("findByUniqueIdentifier");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            answer = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;
    }

    /**
     * <b>update </b>, the method that returns an updated subject bean after it
     * updates the database.
     *
     * @return sb, an updated study bean.
     */
    public EntityBean update(EntityBean eb) {
        org.akaza.openclinica.bean.submit.SubjectBean sb = (org.akaza.openclinica.bean.submit.SubjectBean) eb;
        org.akaza.openclinica.dao.hibernate.SubjectDao subjectDao = (org.akaza.openclinica.dao.hibernate.SubjectDao) org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext().getBean("subjectDao");
        
        org.akaza.openclinica.domain.datamap.Subject subject = subjectDao.findById(sb.getId());
        if (subject == null) {
            throw new RuntimeException("Subject not found");
        }
        subject.setGender(sb.getGender());
        subject.setUniqueIdentifier(sb.getUniqueIdentifier());
        subject.setDateOfBirth(sb.getDateOfBirth());
        subject.setDobCollected(sb.isDobCollected());
        subject.setDateUpdated(new java.util.Date());
        if (sb.getStatus() != null) {
            subject.setStatus(org.akaza.openclinica.domain.Status.getByCode(sb.getStatus().getId()));
        }
        if (sb.getUpdater() != null) {
            subject.setUpdateId(sb.getUpdater().getId());
        }
        
        jakarta.validation.Validator validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        java.util.Set<jakarta.validation.ConstraintViolation<org.akaza.openclinica.domain.datamap.Subject>> violations = validator.validate(subject);
        if (!violations.isEmpty()) {
            throw new RuntimeException("Validation failed for subject: " + violations.iterator().next().getMessage());
        }

        subjectDao.saveOrUpdate(subject);
        sb.setUpdatedDate(subject.getDateUpdated());
        return sb;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return findAllByPermission(objCurrentUser, intActionType);
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        org.akaza.openclinica.bean.login.UserAccountBean user = (org.akaza.openclinica.bean.login.UserAccountBean) objCurrentUser;
        if (user.isSysAdmin()) {
            return findAll();
        }
        
        java.util.List<org.akaza.openclinica.bean.login.StudyUserRoleBean> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            return new ArrayList();
        }

        ArrayList<Integer> studyIds = new ArrayList<Integer>();
        for (org.akaza.openclinica.bean.login.StudyUserRoleBean role : roles) {
            org.akaza.openclinica.bean.core.Role r = role.getRole();
            if (r != null && r.hasPrivilege(org.akaza.openclinica.bean.core.Privilege.VIEW_SUBJECTS)) {
                studyIds.add(role.getStudyId());
            }
        }
        if (studyIds.isEmpty()) {
            return new ArrayList();
        }

        StringBuilder sql = new StringBuilder("SELECT DISTINCT s.* FROM subject s ");
        sql.append("JOIN study_subject ss ON s.subject_id = ss.subject_id ");
        sql.append("WHERE ss.study_id IN (");
        for (int i = 0; i < studyIds.size(); i++) {
            sql.append(studyIds.get(i));
            if (i < studyIds.size() - 1) sql.append(",");
        }
        sql.append(")");
        
        this.setTypesExpected();
        ArrayList alist = this.select(sql.toString());
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            org.akaza.openclinica.bean.submit.SubjectBean eb = (org.akaza.openclinica.bean.submit.SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public void deleteTestSubject(String uniqueIdentifier) {
        HashMap variables = new HashMap();
        variables.put(1, uniqueIdentifier);
        this.execute(digester.getQuery("deleteTestSubject"), variables);
    }
}

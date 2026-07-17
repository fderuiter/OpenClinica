package org.akaza.openclinica.web.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.core.CRFLocker;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.akaza.openclinica.domain.datamap.AuditLogEventType;
import org.akaza.openclinica.dao.hibernate.AuditLogEventDao;

@Component
public class OpenClinicaSessionRegistryImpl extends SessionRegistryImpl {
    private UserAccountDAO _userAccountDAO;

    @Autowired
    public OpenClinicaSessionRegistryImpl(UserAccountDAO _userAccountDAO) {
        this._userAccountDAO = _userAccountDAO;
    }


    AuditUserLoginDao auditUserLoginDao;
    UserAccountDAO userAccountDao;
    DataSource dataSource;
    CRFLocker crfLocker;
    AuditLogEventDao auditLogEventDao;

    @Override
    public void removeSessionInformation(String sessionId) {
        SessionInformation info = getSessionInformation(sessionId);

        if (info != null) {
            String username = null;
            Object p = info.getPrincipal();
            if (p instanceof User) {
                username = ((User) p).getUsername();
            } else if (p instanceof LdapUserDetails) {
                username = ((LdapUserDetails) p).getUsername();
            }

            auditLogout(username);
        }
        super.removeSessionInformation(sessionId);
    }

    void auditLogout(String username) {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        UserAccountBean userAccount = (UserAccountBean) getUserAccountDao().findByUserName(username);
        if (userAccount != null) {
            crfLocker.unlockAllForUser(userAccount.getId());
        }

        AuditUserLoginBean auditUserLogin = new AuditUserLoginBean();
        auditUserLogin.setUserName(username);
        auditUserLogin.setLoginStatus(LoginStatus.SUCCESSFUL_LOGOUT);
        auditUserLogin.setLoginAttemptDate(new Date());
        auditUserLogin.setUserAccountId(userAccount != null ? userAccount.getId() : null);
        getAuditUserLoginDao().saveOrUpdate(auditUserLogin);

        AuditLogEvent auditEvent = new AuditLogEvent();
        auditEvent.setAuditDate(new Date());
        auditEvent.setAuditTable("user_account");
        auditEvent.setEntityId(userAccount != null ? userAccount.getId() : null);
        if (userAccount != null) {
            org.akaza.openclinica.domain.user.UserAccount ua = new org.akaza.openclinica.domain.user.UserAccount();
            ua.setUserId(userAccount.getId());
            auditEvent.setUserAccount(ua);
        }
        auditEvent.setEntityName(username);
        
        AuditLogEventType eventType = new AuditLogEventType();
        eventType.setAuditLogEventTypeId(46);
        auditEvent.setAuditLogEventType(eventType);
        
        if (getAuditLogEventDao() != null) {
            getAuditLogEventDao().saveOrUpdate(auditEvent);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserAccountDAO getUserAccountDao() {
        return userAccountDao != null ? userAccountDao : this._userAccountDAO;
    }

    public AuditUserLoginDao getAuditUserLoginDao() {
        return auditUserLoginDao;
    }

    public void setAuditUserLoginDao(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
    }

    public void setCrfLocker(CRFLocker crfLocker) {
        this.crfLocker = crfLocker;
    }
    
    public AuditLogEventDao getAuditLogEventDao() {
        return auditLogEventDao;
    }

    public void setAuditLogEventDao(AuditLogEventDao auditLogEventDao) {
        this.auditLogEventDao = auditLogEventDao;
    }
}

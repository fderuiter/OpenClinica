package org.akaza.openclinica.modern;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.core.CRFLocker;
import org.akaza.openclinica.dao.hibernate.AuditLogEventDao;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.akaza.openclinica.domain.datamap.AuditLogEventType;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.session.events.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class SessionCleanupListener implements ApplicationListener<SessionDestroyedEvent> {

    @Autowired(required = false)
    private CRFLocker crfLocker;

    @Autowired(required = false)
    private AuditUserLoginDao auditUserLoginDao;

    @Autowired(required = false)
    private AuditLogEventDao auditLogEventDao;

    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
        UserAccountBean userAccount = null;

        // "userBean" or "userBean1" may be in the session
        Object bean = event.getSession().getAttribute("userBean");
        if (bean instanceof UserAccountBean) {
            userAccount = (UserAccountBean) bean;
        } else {
            bean = event.getSession().getAttribute("userBean1");
            if (bean instanceof UserAccountBean) {
                userAccount = (UserAccountBean) bean;
            }
        }

        if (userAccount != null) {
            String username = userAccount.getName();

            if (crfLocker != null) {
                crfLocker.unlockAllForUser(userAccount.getId());
            }

            if (auditUserLoginDao != null) {
                AuditUserLoginBean auditUserLogin = new AuditUserLoginBean();
                auditUserLogin.setUserName(username);
                auditUserLogin.setLoginStatus(LoginStatus.SUCCESSFUL_LOGOUT);
                auditUserLogin.setLoginAttemptDate(new Date());
                auditUserLogin.setUserAccountId(userAccount.getId());
                auditUserLoginDao.saveOrUpdate(auditUserLogin);
            }

            if (auditLogEventDao != null) {
                AuditLogEvent auditEvent = new AuditLogEvent();
                auditEvent.setAuditDate(new Date());
                auditEvent.setAuditTable("user_account");
                auditEvent.setEntityId(userAccount.getId());

                org.akaza.openclinica.domain.user.UserAccount ua = new org.akaza.openclinica.domain.user.UserAccount();
                ua.setUserId(userAccount.getId());
                auditEvent.setUserAccount(ua);
                auditEvent.setEntityName(username);

                AuditLogEventType eventType = new AuditLogEventType();
                eventType.setAuditLogEventTypeId(46); // 46 = Logout
                auditEvent.setAuditLogEventType(eventType);

                auditLogEventDao.saveOrUpdate(auditEvent);
            }
        }
    }
}

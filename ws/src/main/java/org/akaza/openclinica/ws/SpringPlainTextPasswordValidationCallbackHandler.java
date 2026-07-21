package org.akaza.openclinica.ws;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;
import org.springframework.ws.soap.security.callback.CleanupCallback;

import org.apache.wss4j.common.ext.WSPasswordCallback;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.core.CRFLocker;
import org.akaza.openclinica.dao.hibernate.AuditLogEventDao;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.akaza.openclinica.domain.datamap.AuditLogEventType;
import org.akaza.openclinica.domain.user.UserAccount;

import java.io.IOException;
import java.util.Date;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class SpringPlainTextPasswordValidationCallbackHandler extends AbstractCallbackHandler implements InitializingBean {

    private AuthenticationManager authenticationManager;
    private boolean ignoreFailure = false;
    private AuditUserLoginDao auditUserLoginDao;
    private AuditLogEventDao auditLogEventDao;
    private UserAccountDAO userAccountDao;
    private CRFLocker crfLocker;

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setIgnoreFailure(boolean ignoreFailure) {
        this.ignoreFailure = ignoreFailure;
    }

    public void setAuditUserLoginDao(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
    }

    public void setAuditLogEventDao(AuditLogEventDao auditLogEventDao) {
        this.auditLogEventDao = auditLogEventDao;
    }

    public void setUserAccountDao(UserAccountDAO userAccountDao) {
        this.userAccountDao = userAccountDao;
    }

    public void setCrfLocker(CRFLocker crfLocker) {
        this.crfLocker = crfLocker;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(authenticationManager, "authenticationManager is required");
    }

    private void logLogin(String username, LoginStatus status, String reason) {
        if (auditUserLoginDao == null || auditLogEventDao == null || userAccountDao == null) {
            return;
        }
        
        UserAccountBean userAccount = (UserAccountBean) userAccountDao.findByUserName(username);
        
        AuditUserLoginBean auditUserLogin = new AuditUserLoginBean();
        auditUserLogin.setUserName(username);
        auditUserLogin.setLoginStatus(status);
        auditUserLogin.setLoginAttemptDate(new Date());
        auditUserLogin.setUserAccountId(userAccount != null ? userAccount.getId() : null);
        auditUserLoginDao.saveOrUpdate(auditUserLogin);

        AuditLogEvent auditEvent = new AuditLogEvent();
        auditEvent.setAuditDate(new Date());
        auditEvent.setAuditTable("user_account");
        auditEvent.setEntityId(userAccount != null ? userAccount.getId() : null);
        if (userAccount != null) {
            UserAccount ua = new UserAccount();
            ua.setUserId(userAccount.getId());
            auditEvent.setUserAccount(ua);
        }
        auditEvent.setEntityName(username);
        
        AuditLogEventType eventType = new AuditLogEventType();
        if (status == LoginStatus.SUCCESSFUL_LOGIN) {
            eventType.setAuditLogEventTypeId(44);
        } else {
            eventType.setAuditLogEventTypeId(45);
            auditEvent.setReasonForChange(reason);
        }
        auditEvent.setAuditLogEventType(eventType);
        auditLogEventDao.saveOrUpdate(auditEvent);
    }

    @Override
    protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
        if (callback instanceof WSPasswordCallback) {
            WSPasswordCallback pc = (WSPasswordCallback) callback;
            try {
                Authentication authResult =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(pc.getIdentifier(), pc.getPassword()));
                if (logger.isDebugEnabled()) {
                    logger.debug("Authentication success: " + authResult.toString());
                }
                SecurityContextHolder.getContext().setAuthentication(authResult);
                
                logLogin(pc.getIdentifier(), LoginStatus.SUCCESSFUL_LOGIN, null);
                
            } catch (AuthenticationException failed) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Authentication request for user '" + pc.getIdentifier() + "' failed: " + failed.toString());
                }
                SecurityContextHolder.clearContext();
                
                logLogin(pc.getIdentifier(), LoginStatus.FAILED_LOGIN, failed.getMessage());
                
                if (!ignoreFailure) {
                    throw new IOException("Authentication failed", failed);
                }
            }
        } else if (callback instanceof CleanupCallback) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && crfLocker != null) {
                Object principal = auth.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                    if (userAccountDao != null) {
                        UserAccountBean uab = (UserAccountBean) userAccountDao.findByUserName(username);
                        if (uab != null) {
                            crfLocker.unlockAllForUser(uab.getId());
                        }
                    }
                }
            }
            SecurityContextHolder.clearContext();
            return;
        } else {
            throw new UnsupportedCallbackException(callback);
        }
    }
}

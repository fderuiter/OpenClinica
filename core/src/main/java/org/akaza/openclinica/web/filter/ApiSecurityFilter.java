package org.akaza.openclinica.web.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.apache.commons.codec.binary.Base64;
import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.akaza.openclinica.domain.datamap.AuditLogEventType;
import org.akaza.openclinica.dao.hibernate.AuditLogEventDao;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Created by krikorkrumlian on 8/7/15.
 */
public class ApiSecurityFilter extends OncePerRequestFilter {

    private String realm = "Protected";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuditLogEventDao auditLogEventDao;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        System.out.println("Oh look at you triggering API calls i see !!!!!!");


        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {
                    try {
                        String credentials = new String(Base64.decodeBase64(st.nextToken().getBytes()), "UTF-8");
                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String _username = credentials.substring(0, p).trim();
                            String _password = credentials.substring(p + 1).trim();

                            UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
                            UserAccountBean ub = (UserAccountBean) userAccountDAO.findByApiKey(_username);
                            if (!_username.equals("") && ub.getId() != 0) {
                                request.getSession().setAttribute("userBean",ub);
                                auditApiLogin(_username, ub, true, "Successful API Login");
                            }else{
                                auditApiLogin(_username, null, false, "Bad credentials");
                                unauthorized(response, "Bad credentials");
                                return;
                            }
                        } else {
                            auditApiLogin("unknown", null, false, "Invalid authentication token");
                            unauthorized(response, "Invalid authentication token");
                            return;
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new Error("Couldn't retrieve authentication", e);
                    }
                }
            }
        } else {
            unauthorized(response);
        }

        filterChain.doFilter(request, response);
    }

    private void auditApiLogin(String username, UserAccountBean ub, boolean success, String reason) {
        AuditLogEvent auditEvent = new AuditLogEvent();
        auditEvent.setAuditDate(new Date());
        auditEvent.setAuditTable("user_account");
        auditEvent.setEntityId(ub != null ? ub.getId() : null);
        if (ub != null) {
            org.akaza.openclinica.domain.user.UserAccount ua = new org.akaza.openclinica.domain.user.UserAccount();
            ua.setUserId(ub.getId());
            auditEvent.setUserAccount(ua);
        }
        auditEvent.setEntityName(username);
        AuditLogEventType eventType = new AuditLogEventType();
        if (success) {
            eventType.setAuditLogEventTypeId(44);
        } else {
            eventType.setAuditLogEventTypeId(45);
            auditEvent.setReasonForChange(reason);
        }
        auditEvent.setAuditLogEventType(eventType);
        auditLogEventDao.saveOrUpdate(auditEvent);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        
        org.codehaus.jackson.map.ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();
        org.akaza.openclinica.bean.api.ApiError error = new org.akaza.openclinica.bean.api.ApiError("401", message);
        org.akaza.openclinica.bean.api.ApiResponse<Object> apiResponse = new org.akaza.openclinica.bean.api.ApiResponse<>(java.util.Collections.singletonList(error));
        mapper.writeValue(response.getWriter(), apiResponse);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }
}

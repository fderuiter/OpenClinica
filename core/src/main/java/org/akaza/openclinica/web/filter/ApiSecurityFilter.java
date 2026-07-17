package org.akaza.openclinica.web.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("standardObjectMapper")
    private com.fasterxml.jackson.databind.ObjectMapper mapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        System.out.println("Oh look at you triggering API calls i see !!!!!!");

        String authHeader = request.getHeader("Authorization");
        String apiKeyHeader = request.getHeader("api_key");
        String apiKey = null;

        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String tokenType = st.nextToken();
                if (tokenType.equalsIgnoreCase("Basic")) {
                    auditApiLogin("unknown", null, false, "Deprecated Basic authentication is not supported");
                    unauthorized(response, "Deprecated Basic authentication is not supported");
                    return;
                } else if (tokenType.equalsIgnoreCase("Bearer")) {
                    if (st.hasMoreTokens()) {
                        apiKey = st.nextToken();
                    }
                }
            }
        }

        if (apiKey == null && apiKeyHeader != null) {
            apiKey = apiKeyHeader;
        }

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            apiKey = apiKey.trim();
            UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
            UserAccountBean ub = (UserAccountBean) userAccountDAO.findByApiKey(apiKey);
            if (ub != null && ub.getId() != 0) {
                request.getSession().setAttribute("userBean", ub);
                
                java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));
                org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(ub.getName(), "", true, true, true, true, authorities);
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = 
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, "", authorities);
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
                request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", org.springframework.security.core.context.SecurityContextHolder.getContext());
                
                auditApiLogin(ub.getName(), ub, true, "Successful API Login");
            } else {
                auditApiLogin("unknown", null, false, "Bad credentials");
                unauthorized(response, "Bad credentials");
                return;
            }
        } else {
            unauthorized(response);
            return;
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
        response.setHeader("WWW-Authenticate", "Bearer realm=\"" + realm + "\"");
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        
        org.akaza.openclinica.sdk.dto.ApiError error = new org.akaza.openclinica.sdk.dto.ApiError("401", message);
        org.akaza.openclinica.sdk.dto.ApiResponse<Object> apiResponse = new org.akaza.openclinica.sdk.dto.ApiResponse<>(java.util.Collections.singletonList(error));
        mapper.writeValue(response.getWriter(), apiResponse);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }
}

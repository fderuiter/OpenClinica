/**
 * 
 */
package org.akaza.openclinica.control.core;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.log.LoggingConstants;
import org.slf4j.MDC;

/**
 * @author pgawade
 *
 */
public class OCServletFilter implements javax.servlet.Filter {

    public static final String USER_BEAN_NAME = "userBean";

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        UserAccountBean ub = (UserAccountBean) req.getSession().getAttribute(USER_BEAN_NAME);
        
        String remoteUserHeader = req.getHeader("REMOTE_USER");
        if (ub == null && remoteUserHeader != null && !remoteUserHeader.trim().isEmpty()) {
            org.springframework.web.context.WebApplicationContext ctx = 
                org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(req.getSession().getServletContext());
            javax.sql.DataSource ds = (javax.sql.DataSource) ctx.getBean("dataSource");
            org.akaza.openclinica.dao.login.UserAccountDAO udao = new org.akaza.openclinica.dao.login.UserAccountDAO(ds);
            ub = (UserAccountBean) udao.findByUserName(remoteUserHeader);
            if (ub != null) {
                req.getSession().setAttribute(USER_BEAN_NAME, ub);
            }
        }
        
        if (ub != null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() == null) {
            java.util.ArrayList<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<org.springframework.security.core.GrantedAuthority>();
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(ub.getName(), "", authorities);
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            
            // Also store it in session so that SessionRegistry and other filters find it if needed
            req.getSession().setAttribute("SPRING_SECURITY_CONTEXT", org.springframework.security.core.context.SecurityContextHolder.getContext());
        }

        boolean successfulRegistration = false;
        String username = "";

        Principal principal = req.getUserPrincipal();

        if ((ub != null) && (null != ub.getName()) && (!ub.getName().equals(""))) {
            username = ub.getName();
            successfulRegistration = registerUsernameWithLogContext(username);
        } else if (principal != null) {
            username = principal.getName();
            successfulRegistration = registerUsernameWithLogContext(username);
        }

        try {
            chain.doFilter(request, response);
          } finally {
            if (successfulRegistration) {
                MDC.remove(LoggingConstants.USERNAME);
            }
        }
    }

    public void init(FilterConfig arg0) throws ServletException {
    }

    public void destroy() {
    }

    /**
     * Register the user in the MDC under USERNAME.
     * 
     * @param username
     * @return true id the user can be successfully registered
     */
    private boolean registerUsernameWithLogContext(String username) {
        if (username != null && username.trim().length() > 0) {
            MDC.put(LoggingConstants.USERNAME, username);
            return true;
        }
        return false;
    }

}

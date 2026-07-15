package org.akaza.openclinica.modern.filter;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.log.LoggingConstants;
import org.akaza.openclinica.repository.UnifiedRepository;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LegacyModernContextBridgeFilter extends OncePerRequestFilter {

    private final DataSource dataSource;
    private final UnifiedRepository unifiedRepository;

    public LegacyModernContextBridgeFilter(DataSource dataSource, UnifiedRepository unifiedRepository) {
        this.dataSource = dataSource;
        this.unifiedRepository = unifiedRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean mdcSet = false;
        HttpServletRequest requestToChain = request;

        try {
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                String username = authentication.getName();
                
                UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
                UserAccountBean userBean = (UserAccountBean) userAccountDAO.findByUserName(username);
                
                if (userBean != null && userBean.getId() > 0) {
                    requestToChain = new StatelessSessionRequestWrapper(request);
                    HttpSession session = requestToChain.getSession(true);
                    session.setAttribute("userBean", userBean);
                    
                    if (userBean.getActiveStudyId() > 0) {
                        StudyBean studyBean = unifiedRepository.getStudyBean(userBean.getActiveStudyId());
                        if (studyBean != null && studyBean.getId() > 0) {
                            session.setAttribute("studyBean", studyBean);
                            session.setAttribute("study", studyBean);
                        }
                    }
                    
                    MDC.put(LoggingConstants.USERNAME, username);
                    mdcSet = true;
                }
            }

            filterChain.doFilter(requestToChain, response);
        } finally {
            if (mdcSet) {
                MDC.remove(LoggingConstants.USERNAME);
            }
        }
    }

    private static class StatelessSessionRequestWrapper extends HttpServletRequestWrapper {
        private HttpSession statelessSession;

        public StatelessSessionRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public HttpSession getSession(boolean create) {
            if (statelessSession == null && create) {
                statelessSession = createStatelessSession(super.getSession(false));
            } else if (statelessSession == null && !create) {
                return super.getSession(false);
            }
            return statelessSession;
        }

        @Override
        public HttpSession getSession() {
            return getSession(true);
        }

        private HttpSession createStatelessSession(HttpSession originalSession) {
            Map<String, Object> attributes = new HashMap<>();
            return (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class<?>[]{HttpSession.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("getAttribute".equals(methodName)) {
                            Object val = attributes.get(args[0]);
                            if (val == null && originalSession != null) {
                                return originalSession.getAttribute((String) args[0]);
                            }
                            return val;
                        } else if ("setAttribute".equals(methodName)) {
                            attributes.put((String) args[0], args[1]);
                            return null;
                        } else if ("removeAttribute".equals(methodName)) {
                            attributes.remove(args[0]);
                            return null;
                        } else if ("getAttributeNames".equals(methodName)) {
                            return Collections.enumeration(attributes.keySet());
                        } else if (originalSession != null) {
                            return method.invoke(originalSession, args);
                        }
                        
                        if ("getId".equals(methodName)) return "stateless-session";
                        if ("getCreationTime".equals(methodName)) return System.currentTimeMillis();
                        if ("getLastAccessedTime".equals(methodName)) return System.currentTimeMillis();
                        if ("getServletContext".equals(methodName)) return super.getServletContext();
                        
                        if (method.getReturnType().equals(Void.TYPE)) {
                            return null;
                        }
                        return null;
                    }
            );
        }
    }
}

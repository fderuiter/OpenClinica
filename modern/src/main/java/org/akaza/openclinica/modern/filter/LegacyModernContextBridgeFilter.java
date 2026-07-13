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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;

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
        try {
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                String username = authentication.getName();
                
                UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
                UserAccountBean userBean = (UserAccountBean) userAccountDAO.findByUserName(username);
                
                if (userBean != null && userBean.getId() > 0) {
                    HttpSession session = request.getSession(true);
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

            filterChain.doFilter(request, response);
        } finally {
            if (mdcSet) {
                MDC.remove(LoggingConstants.USERNAME);
            }
        }
    }
}

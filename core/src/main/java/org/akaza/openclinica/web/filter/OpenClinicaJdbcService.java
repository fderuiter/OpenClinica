package org.akaza.openclinica.web.filter;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

public class OpenClinicaJdbcService extends JdbcDaoImpl {

    public OpenClinicaJdbcService() {
        super();
    }

    public OpenClinicaJdbcService(DataSource dataSource) {
        super();
        setDataSource(dataSource);
        setUsersByUsernameQuery("SELECT user_name,passwd,enabled,account_non_locked FROM user_account WHERE user_name = ?");
        try {
            afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize OpenClinicaJdbcService", e);
        }
    }

    public static org.springframework.security.core.Authentication establishAuthenticatedContext(jakarta.servlet.http.HttpServletRequest request, org.akaza.openclinica.bean.login.UserAccountBean userAccount, OpenClinicaJdbcService jdbcService) {
        UserDetails userDetails = jdbcService.loadUserByUsername(userAccount.getName());
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        if (request != null && request.getSession(false) != null) {
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", org.springframework.security.core.context.SecurityContextHolder.getContext());
            request.getSession().setAttribute("userBean", userAccount);
        }
        return auth;
    }

    private MappingSqlQuery ocUsersByUsernameMapping;

    /**
     * Executes the <tt>usersByUsernameQuery</tt> and returns a list of UserDetails objects (there should normally only be one matching user).
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List loadUsersByUsername(String username) {
        this.ocUsersByUsernameMapping = new OcUsersByUsernameMapping(getDataSource());
        return ocUsersByUsernameMapping.execute(username);
    }

    /**
     * Can be overridden to customize the creation of the final UserDetailsObject returnd from <tt>loadUserByUsername</tt>.
     * 
     * @param username
     *            the name originally passed to loadUserByUsername
     * @param userFromUserQuery
     *            the object returned from the execution of the
     * @param combinedAuthorities
     *            the combined array of authorities from all the authority loading queries.
     * @return the final UserDetails which should be used in the system.
     */
    protected UserDetails createUserDetails(String username, UserDetails userFromUserQuery, GrantedAuthority[] combinedAuthorities) {
        String returnUsername = userFromUserQuery.getUsername();

        if (!isUsernameBasedPrimaryKey()) {
            returnUsername = username;
        }

        return new User(
                returnUsername,
                userFromUserQuery.getPassword(),
                userFromUserQuery.isEnabled(),
                true,
                true,
                userFromUserQuery.isAccountNonLocked(),
                Arrays.asList(combinedAuthorities));
    }

    /**
     * Query object to look up a user.
     */
    private class OcUsersByUsernameMapping extends MappingSqlQuery {
        protected OcUsersByUsernameMapping(DataSource ds) {
            super(ds, getUsersByUsernameQuery());
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        @Override
        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            String username = rs.getString(1);
            String password = rs.getString(2);
            boolean enabled = rs.getBoolean(3);
            boolean nonLocked = rs.getBoolean(4);
            UserDetails user = new User(username, password, enabled, true, true, nonLocked,
                    Arrays.asList(new GrantedAuthority[] { new SimpleGrantedAuthority("HOLDER") }));

            return user;
        }
    }

}

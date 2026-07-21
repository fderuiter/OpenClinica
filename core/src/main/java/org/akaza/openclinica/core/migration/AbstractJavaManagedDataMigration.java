package org.akaza.openclinica.core.migration;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.sql.DataSource;

public abstract class AbstractJavaManagedDataMigration implements ApplicationListener<ContextRefreshedEvent> {

    protected UserAccountBean systemUser;
    protected DataSource dataSource;
    protected ApplicationContext applicationContext;
    
    protected static final int DEFAULT_BATCH_SIZE = 1000;
    
    private boolean executed = false;

    @Override
    public synchronized void onApplicationEvent(ContextRefreshedEvent event) {
        if (executed) {
            return;
        }
        try {
            this.applicationContext = event.getApplicationContext();
            setupDependencies(this.applicationContext);
            doMigration();
            executed = true;
        } catch (Exception e) {
            throw new RuntimeException("Migration failed", e);
        }
    }

    protected void setupDependencies(ApplicationContext context) {
        this.dataSource = context.getBean("dataSource", DataSource.class);
        
        systemUser = new UserAccountBean();
        systemUser.setId(1); // default
        systemUser.setName("root");
        
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement("SELECT user_id, user_name FROM user_account WHERE user_name = 'root'")) {
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    systemUser.setId(rs.getInt("user_id"));
                    systemUser.setName(rs.getString("user_name"));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch root user, defaulting to ID 1: " + e.getMessage());
        }
    }

    protected abstract void doMigration() throws Exception;
}

package org.akaza.openclinica.core.migration;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
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
            System.err.println("Migration skipped or failed (likely tables not created): " + e.getMessage());
        }
    }

    protected void setupDependencies(ApplicationContext context) {
        this.dataSource = context.getBean("dataSource", DataSource.class);
        
        systemUser = new UserAccountBean();
        systemUser.setId(1);
        systemUser.setName("root");
        
        try (java.sql.Connection conn = this.dataSource.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM user_account WHERE user_name = 'root'")) {
             java.sql.ResultSet rs = stmt.executeQuery();
             if (rs.next()) {
                 systemUser.setId(rs.getInt("user_id"));
             }
        } catch (Exception e) {
             // Fallback to ID 1 if table doesn't exist yet or query fails
        }
    }

    protected abstract void doMigration() throws Exception;
}

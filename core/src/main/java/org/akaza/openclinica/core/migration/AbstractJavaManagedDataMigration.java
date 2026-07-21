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
            String driver = applicationContext.getEnvironment().getProperty("spring.datasource.driver-class-name");
            if ("org.h2.Driver".equals(driver)) {
                executed = true;
                return;
            }
            setupDependencies(this.applicationContext);
            doMigration();
            executed = true;
        } catch (Exception e) {
            throw new RuntimeException("Migration failed", e);
        }
    }

    protected void setupDependencies(ApplicationContext context) {
        this.dataSource = context.getBean("dataSource", DataSource.class);
        
        UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
        systemUser = (UserAccountBean) userAccountDAO.findByUserName("root");
        if (systemUser == null || systemUser.getId() <= 0) {
            systemUser = new UserAccountBean();
            systemUser.setId(1);
        }
    }

    protected abstract void doMigration() throws Exception;
}

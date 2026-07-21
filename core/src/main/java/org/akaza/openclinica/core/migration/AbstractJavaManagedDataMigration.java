package org.akaza.openclinica.core.migration;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.exception.SetupException;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.core.ApplicationContextProvider;
import org.akaza.openclinica.dao.login.UserAccountDAO;

import javax.sql.DataSource;

public abstract class AbstractJavaManagedDataMigration implements CustomTaskChange {

    protected ResourceAccessor resourceAccessor;
    protected UserAccountBean systemUser;
    protected DataSource dataSource;
    
    protected static final int DEFAULT_BATCH_SIZE = 1000;

    @Override
    public void execute(Database database) throws CustomChangeException {
        try {
            setupDependencies();
            doMigration();
        } catch (Exception e) {
            throw new CustomChangeException("Migration failed", e);
        }
    }

    protected void setupDependencies() {
        if (ApplicationContextProvider.getApplicationContext() == null) {
            throw new IllegalStateException("ApplicationContext is not initialized");
        }
        
        this.dataSource = ApplicationContextProvider.getApplicationContext().getBean("dataSource", DataSource.class);
        
        UserAccountDAO userAccountDAO = org.akaza.openclinica.core.ApplicationContextProvider.getApplicationContext().getBean(UserAccountDAO.class);
        systemUser = (UserAccountBean) userAccountDAO.findByUserName("root");
        if (systemUser == null || systemUser.getId() <= 0) {
            systemUser = new UserAccountBean();
            systemUser.setId(1);
        }
    }

    protected abstract void doMigration() throws Exception;

    @Override
    public String getConfirmationMessage() {
        return "Java-Managed Data Migration completed successfully.";
    }

    @Override
    public void setUp() throws SetupException {
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}

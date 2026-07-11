package org.akaza.openclinica.core.migration;

import liquibase.change.custom.CustomTaskChange;
import liquibase.change.custom.CustomTaskRollback;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.exception.SetupException;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.core.ApplicationContextProvider;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

public abstract class AbstractJavaManagedDataMigration implements CustomTaskChange, CustomTaskRollback {

    protected ResourceAccessor resourceAccessor;
    protected UserAccountBean systemUser;
    protected DataSource dataSource;
    
    protected static final int DEFAULT_BATCH_SIZE = 1000;

    @Override
    public void execute(Database database) throws CustomChangeException {
        try {
            setupDependencies(database);
            doMigration();
        } catch (Exception e) {
            throw new CustomChangeException("Migration failed", e);
        }
    }

    protected void setupDependencies(Database database) {
        if (ApplicationContextProvider.getApplicationContext() != null) {
            this.dataSource = ApplicationContextProvider.getApplicationContext().getBean("dataSource", DataSource.class);
        } else {
            // Fallback for tests
            java.sql.Connection conn = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
            this.dataSource = new SingleConnectionDataSource(conn, true);
        }
        
        try {
            UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
            systemUser = (UserAccountBean) userAccountDAO.findByUserName("root");
        } catch (Exception e) {
            // ignore
        }
        if (systemUser == null || systemUser.getId() <= 0) {
            systemUser = new UserAccountBean();
            systemUser.setId(1);
        }
    }

    protected abstract void doMigration() throws Exception;

    @Override
    public void rollback(Database database) throws CustomChangeException, RollbackImpossibleException {
        throw new RollbackImpossibleException("Irreversible Data Migration. Rollback is not supported and explicitly blocked to prevent data corruption.");
    }

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

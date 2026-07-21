package org.akaza.openclinica.core.migration;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.exception.SetupException;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.core.ApplicationContextProvider;

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

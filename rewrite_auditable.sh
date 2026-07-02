sed -i 's/import org.akaza.openclinica.core.SessionManager;//g' core/src/main/java/org/akaza/openclinica/bean/core/AuditableEntityBean.java
sed -i 's/import org.akaza.openclinica.dao.login.UserAccountDAO;//g' core/src/main/java/org/akaza/openclinica/bean/core/AuditableEntityBean.java
sed -i 's/protected UserAccountDAO udao;//g' core/src/main/java/org/akaza/openclinica/bean/core/AuditableEntityBean.java
sed -i 's/udao = null;//g' core/src/main/java/org/akaza/openclinica/bean/core/AuditableEntityBean.java
sed -i '/if (udao == null)/,+6d' core/src/main/java/org/akaza/openclinica/bean/core/AuditableEntityBean.java
sed -i '/if (udao == null)/,+7d' core/src/main/java/org/akaza/openclinica/bean/core/AuditableEntityBean.java

import os
import glob

test_files = [
    "/app/core/src/test/java/org/akaza/openclinica/dao/AuditUserLoginDaoTest.java",
    "/app/core/src/test/java/org/akaza/openclinica/dao/rule/RuleSetRuleDaoTest.java",
    "/app/core/src/test/java/org/akaza/openclinica/dao/DatabaseChangeLogDaoTest.java",
    "/app/core/src/test/java/org/akaza/openclinica/dao/rule/RuleSetDaoTest.java",
    "/app/core/src/test/java/org/akaza/openclinica/dao/rule/RuleSetAuditDaoTest.java",
    "/app/core/src/test/java/org/akaza/openclinica/dao/ConfigurationDaoTest.java",
    "/app/core/src/test/java/org/akaza/openclinica/dao/rule/RuleSetRuleAuditDaoTest.java",
    "/app/core/src/test/java/org/akaza/openclinica/dao/rule/RuleDaoTest.java",
    "/app/core/src/test/java/org/akaza/openclinica/dao/AuthoritiesDaoTest.java"
]

for file in test_files:
    if os.path.exists(file):
        with open(file, 'r') as f:
            content = f.read()
        
        content = content.replace("import org.springframework.orm.hibernate3.HibernateTemplate;", "import jakarta.persistence.EntityManager;")
        content = content.replace("import org.hibernate.classic.Session;", "")
        content = content.replace("import org.hibernate.Session;", "")
        content = content.replace("HibernateTemplate mockHibernateTemplate;", "EntityManager mockEntityManager;")
        content = content.replace("setHibernateTemplate(mockHibernateTemplate)", "setEntityManager(mockEntityManager)")
        content = content.replace("when(mockHibernateTemplate.getSessionFactory()).thenReturn(mockSessionFactory);", "")
        content = content.replace("when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);", "")
        content = content.replace("mockSession.saveOrUpdate", "mockEntityManager.persist")
        content = content.replace("mockSession.createQuery", "mockEntityManager.createQuery")
        content = content.replace("mockQuery.setInteger", "mockQuery.setParameter")
        content = content.replace("mockQuery.uniqueResult()", "mockQuery.getSingleResult()")
        content = content.replace("@Mock\n    private Session mockSession;", "")
        
        # for DatabaseChangeLogDao which uses SessionFactory
        content = content.replace("setSessionFactory(mockSessionFactory)", "setEntityManager(mockEntityManager)")
        
        with open(file, 'w') as f:
            f.write(content)


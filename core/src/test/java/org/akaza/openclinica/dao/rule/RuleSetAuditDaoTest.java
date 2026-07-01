package org.akaza.openclinica.dao.rule;

import org.akaza.openclinica.dao.hibernate.RuleSetAuditDao;
import org.hibernate.Query;
import org.hibernate.classic.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.orm.hibernate3.HibernateTemplate;
import static org.mockito.Mockito.*;

public class RuleSetAuditDaoTest {

    private RuleSetAuditDao ruleSetAuditDao;

    @Mock
    private HibernateTemplate mockHibernateTemplate;

    @Mock
    private SessionFactory mockSessionFactory;

    @Mock
    private Session mockSession;

    @Mock
    private Query mockQuery;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ruleSetAuditDao = new RuleSetAuditDao();
        ruleSetAuditDao.setHibernateTemplate(mockHibernateTemplate);
        when(mockHibernateTemplate.getSessionFactory()).thenReturn(mockSessionFactory);
        when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);
    }

    @Test
    public void testMock() {
        // Mock test
    }
}

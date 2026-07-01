package org.akaza.openclinica.dao.rule;

import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.hibernate.Query;
import org.hibernate.classic.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.orm.hibernate3.HibernateTemplate;
import static org.mockito.Mockito.*;

public class RuleSetRuleDaoTest {

    private RuleSetRuleDao ruleSetRuleDao;

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
        ruleSetRuleDao = new RuleSetRuleDao();
        ruleSetRuleDao.setHibernateTemplate(mockHibernateTemplate);
        when(mockHibernateTemplate.getSessionFactory()).thenReturn(mockSessionFactory);
        when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);
    }

    @Test
    public void testMock() {
        // Mock test
    }
}

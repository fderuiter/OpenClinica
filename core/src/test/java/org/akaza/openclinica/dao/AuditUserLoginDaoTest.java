package org.akaza.openclinica.dao;

import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.hibernate.Query;
import org.hibernate.classic.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class AuditUserLoginDaoTest {

    private AuditUserLoginDao auditUserLoginDao;

    @Mock
    private HibernateTemplate mockHibernateTemplate;

    @Mock
    private SessionFactory mockSessionFactory;

    @Mock
    private Session mockSession;

    @Mock
    private Statistics mockStatistics;

    @Mock
    private Query mockQuery;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        auditUserLoginDao = new AuditUserLoginDao();
        auditUserLoginDao.setHibernateTemplate(mockHibernateTemplate);

        when(mockHibernateTemplate.getSessionFactory()).thenReturn(mockSessionFactory);
        when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);
        when(mockSessionFactory.getStatistics()).thenReturn(mockStatistics);
    }

    @Test
    public void testSaveOrUpdate() {
        AuditUserLoginBean auditUserLoginBean = new AuditUserLoginBean();
        auditUserLoginBean.setUserName("testUser");
        auditUserLoginBean.setLoginAttemptDate(new Date());
        auditUserLoginBean.setLoginStatus(LoginStatus.SUCCESSFUL_LOGIN);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                AuditUserLoginBean bean = (AuditUserLoginBean) invocation.getArguments()[0];
                bean.setId(1);
                return null;
            }
        }).when(mockSession).saveOrUpdate(any(AuditUserLoginBean.class));

        AuditUserLoginBean result = auditUserLoginDao.saveOrUpdate(auditUserLoginBean);

        assertNotNull("Persistant id is null", result.getId());
        assertEquals(Integer.valueOf(1), result.getId());
    }

    @Test
    public void testfindById() {
        AuditUserLoginBean mockBean = new AuditUserLoginBean();
        mockBean.setId(-1);
        mockBean.setUserName("testUser");

        when(mockSession.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setInteger(eq("id"), eq(-1))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(mockBean);

        AuditUserLoginBean result = auditUserLoginDao.findById(-1);

        assertEquals("UserName should be testUser", "testUser", result.getUserName());
    }
}

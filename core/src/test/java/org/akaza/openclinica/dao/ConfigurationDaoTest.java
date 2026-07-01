package org.akaza.openclinica.dao;

import org.akaza.openclinica.dao.hibernate.ConfigurationDao;
import org.akaza.openclinica.domain.technicaladmin.ConfigurationBean;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class ConfigurationDaoTest {

    private ConfigurationDao configurationDao;

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
        configurationDao = new ConfigurationDao();
        configurationDao.setHibernateTemplate(mockHibernateTemplate);

        when(mockHibernateTemplate.getSessionFactory()).thenReturn(mockSessionFactory);
        when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);
        when(mockSessionFactory.getStatistics()).thenReturn(mockStatistics);
    }

    @Test
    public void testSaveOrUpdate() {
        ConfigurationBean configurationBean = new ConfigurationBean();
        configurationBean.setKey("user.test");
        configurationBean.setValue("test");
        configurationBean.setDescription("Testing attention please");

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ConfigurationBean bean = (ConfigurationBean) invocation.getArguments()[0];
                bean.setId(1);
                return null;
            }
        }).when(mockSession).saveOrUpdate(any(ConfigurationBean.class));

        configurationBean = configurationDao.saveOrUpdate(configurationBean);

        assertNotNull("Persistant id is null", configurationBean.getId());
    }

    @Test
    public void testfindById() {
        ConfigurationBean mockBean = new ConfigurationBean();
        mockBean.setId(-1);
        mockBean.setKey("test.test");

        when(mockSession.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setInteger(eq("id"), eq(-1))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(mockBean);

        ConfigurationBean configurationBean = configurationDao.findById(-1);

        assertEquals("Key should be test.test", "test.test", configurationBean.getKey());
    }

    @Test
    public void testfindByKey() {
        ConfigurationBean mockBean = new ConfigurationBean();
        mockBean.setId(1);
        mockBean.setKey("test.test");

        when(mockSession.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setString(eq("key"), eq("test.test"))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(mockBean);

        ConfigurationBean configurationBean = configurationDao.findByKey("test.test");

        assertEquals("Key should be test.test", "test.test", configurationBean.getKey());
    }
}

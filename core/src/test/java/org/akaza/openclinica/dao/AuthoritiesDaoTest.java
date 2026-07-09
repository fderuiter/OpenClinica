package org.akaza.openclinica.dao;

import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import jakarta.persistence.Query;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import jakarta.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class AuthoritiesDaoTest {
    private AuthoritiesDao authoritiesDao;

    @Mock
    private EntityManager mockEntityManager;

    @Mock
    private SessionFactory mockSessionFactory;

    

    @Mock
    private Statistics mockStatistics;

    @Mock
    private Query mockQuery;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        authoritiesDao = new AuthoritiesDao();
        authoritiesDao.setEntityManager(mockEntityManager);

        
        
        when(mockSessionFactory.getStatistics()).thenReturn(mockStatistics);
    }

    @Test
    public void testSaveOrUpdate() {
        AuthoritiesBean authorities = new AuthoritiesBean();
        authorities.setUsername("root");
        authorities.setAuthority("ROLE_USER");
        authorities.setId(-1);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                AuthoritiesBean bean = (AuthoritiesBean) invocation.getArguments()[0];
                bean.setId(1); // Set some persisted ID
                return null;
            }
        }).when(mockEntityManager).persist(any(AuthoritiesBean.class));

        authorities = authoritiesDao.saveOrUpdate(authorities);

        assertNotNull("Persistant id is null", authorities.getId());
    }

    @Test
    public void testFindById() {
        AuthoritiesBean mockBean = new AuthoritiesBean();
        mockBean.setId(-1);
        mockBean.setUsername("root");

        when(mockEntityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("id"), eq(-1))).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(mockBean);

        AuthoritiesBean authorities = authoritiesDao.findById(-1);

        assertNotNull("RuleSet is null", authorities);
        assertEquals("The id of the retrieved Domain Object should be -1", new Integer(-1), authorities.getId());
    }

    @Test
    public void testFindByUsername() {
        AuthoritiesBean mockBean = new AuthoritiesBean();
        mockBean.setId(-1);
        mockBean.setUsername("root");

        when(mockEntityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("username"), eq("root"))).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(mockBean);

        AuthoritiesBean authorities = authoritiesDao.findByUsername("root");

        assertNotNull("RuleSet is null", authorities);
        assertEquals("The id of the retrieved Domain Object should be -1", new Integer(-1), authorities.getId());
    }
}

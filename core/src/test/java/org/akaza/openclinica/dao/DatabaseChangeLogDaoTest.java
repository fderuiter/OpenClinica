package org.akaza.openclinica.dao;

import org.akaza.openclinica.dao.hibernate.DatabaseChangeLogDao;
import org.akaza.openclinica.domain.technicaladmin.DatabaseChangeLogBean;
import org.hibernate.Query;
import org.hibernate.classic.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class DatabaseChangeLogDaoTest {

    private DatabaseChangeLogDao databaseChangeLogDao;

    @Mock
    private SessionFactory mockSessionFactory;

    @Mock
    private Session mockSession;

    @Mock
    private Query mockQuery;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        databaseChangeLogDao = new DatabaseChangeLogDao();
        databaseChangeLogDao.setSessionFactory(mockSessionFactory);

        when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);
    }

    @Test
    public void testfindById() {
        DatabaseChangeLogBean mockBean = new DatabaseChangeLogBean();
        mockBean.setId("1235684743487-1");
        mockBean.setAuthor("pgawade (generated)");
        mockBean.setFileName("migration/2.5/changeLogCreateTables.xml");

        when(mockSession.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setString(eq("id"), eq("1235684743487-1"))).thenReturn(mockQuery);
        when(mockQuery.setString(eq("author"), eq("pgawade (generated)"))).thenReturn(mockQuery);
        when(mockQuery.setString(eq("fileName"), eq("migration/2.5/changeLogCreateTables.xml"))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(mockBean);

        DatabaseChangeLogBean result = databaseChangeLogDao.findById("1235684743487-1", "pgawade (generated)", "migration/2.5/changeLogCreateTables.xml");

        assertNotNull(result);
        assertEquals("Author should be pgawade (generated)", "pgawade (generated)", result.getAuthor());
    }
}

package org.akaza.openclinica.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.SubjectDao;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.domain.datamap.Study;
import jakarta.persistence.EntityManager;
import javax.sql.DataSource;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class UnifiedRepositoryTest {

    @Mock private DataSource dataSource;
    @Mock private StudyDao studyDao;
    @Mock private StudySubjectDao studySubjectDao;
    @Mock private SubjectDao subjectDao;
    @Mock private CrfDao crfDao;
    @Mock private EventCrfDao eventCrfDao;
    @Mock private EntityManager entityManager;

    private UnifiedRepository unifiedRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        unifiedRepository = new UnifiedRepository(dataSource);
        unifiedRepository.setStudyDaoHibernate(studyDao);
        unifiedRepository.setStudySubjectDaoHibernate(studySubjectDao);
        unifiedRepository.setSubjectDaoHibernate(subjectDao);
        unifiedRepository.setCrfDaoHibernate(crfDao);
        unifiedRepository.setEventCrfDaoHibernate(eventCrfDao);
        
        unifiedRepository = spy(unifiedRepository);
    }

    @Test
    public void testSaveStudyBean() {
        when(studyDao.getEntityManager()).thenReturn(entityManager);

        Study mockEntity = new Study();
        mockEntity.setStudyId(99);
        when(studyDao.saveOrUpdate(any(Study.class))).thenReturn(mockEntity);
        
        doReturn(mockEntity).when(unifiedRepository).mapToEntity(any(StudyBean.class));

        StudyBean bean = new StudyBean();
        bean.setId(0); // new
        bean.setName("New Study");

        StudyBean result = unifiedRepository.save(bean);

        verify(studyDao).saveOrUpdate(any(Study.class));
        verify(entityManager).flush();
        verify(entityManager).detach(mockEntity);
        assertNotNull(result);
        assertEquals(99, result.getId());
        assertEquals("New Study", result.getName());
    }
}

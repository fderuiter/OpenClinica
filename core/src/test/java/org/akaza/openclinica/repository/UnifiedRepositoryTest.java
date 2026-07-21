package org.akaza.openclinica.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.domain.datamap.Study;
import jakarta.persistence.EntityManager;
import javax.sql.DataSource;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class UnifiedRepositoryTest {

    @Mock private DataSource dataSource;
    @Mock private StudyDao studyDao;
    @Mock private EntityManager entityManager;

    private UnifiedRepository unifiedRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        unifiedRepository = new UnifiedRepository(dataSource, mock(org.akaza.openclinica.dao.managestudy.StudyDAO.class), mock(org.akaza.openclinica.dao.managestudy.StudySubjectDAO.class), mock(org.akaza.openclinica.dao.submit.SubjectDAO.class), mock(org.akaza.openclinica.dao.admin.CRFDAO.class), mock(org.akaza.openclinica.dao.submit.CRFVersionDAO.class), mock(org.akaza.openclinica.dao.managestudy.StudyEventDAO.class), mock(org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO.class), mock(org.akaza.openclinica.dao.submit.ItemDAO.class), mock(org.akaza.openclinica.dao.submit.ItemDataDAO.class), mock(org.akaza.openclinica.dao.submit.ItemFormMetadataDAO.class));
        unifiedRepository.setStudyDaoHibernate(studyDao);
        
        unifiedRepository = spy(unifiedRepository);
    }

    @Test
    public void testSaveStudyBean() {
        when(studyDao.getEntityManager()).thenReturn(entityManager);

        Study mockEntity = new Study();
        mockEntity.setStudyId(99);
        when(studyDao.saveOrUpdate(any(Study.class))).thenReturn(mockEntity);
        
        doReturn(null).when(unifiedRepository).mapToEntity(any(StudyBean.class));

        StudyBean bean = new StudyBean();
        bean.setId(0); // new
        bean.setName("New Study");

        StudyBean result = unifiedRepository.save(bean);

        verify(studyDao).saveOrUpdate(any(Study.class));
        verify(entityManager).flush();
        verify(entityManager).clear();
        assertNotNull(result);
        assertEquals(99, result.getId());
        assertEquals("New Study", result.getName());
    }
}

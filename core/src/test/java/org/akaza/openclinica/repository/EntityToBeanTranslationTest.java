package org.akaza.openclinica.repository;

import org.junit.Test;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.domain.datamap.Study;
import org.springframework.beans.BeanUtils;
import static org.junit.Assert.*;

public class EntityToBeanTranslationTest {

    @Test
    public void testMapBeanToEntity() {
        StudyBean bean = new StudyBean();
        bean.setId(123);
        bean.setName("Test Study Bean");
        bean.setOfficialTitle("Official Title Bean");

        Study entity = new Study();
        BeanUtils.copyProperties(bean, entity, "id");
        entity.setStudyId(bean.getId());

        assertEquals("Test Study Bean", entity.getName());
        assertEquals("Official Title Bean", entity.getOfficialTitle());
        assertEquals(123, entity.getStudyId());
    }
}

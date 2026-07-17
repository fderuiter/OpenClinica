package org.akaza.openclinica.repository;

import org.junit.Test;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.domain.datamap.Study;
import org.springframework.beans.BeanUtils;
import static org.junit.Assert.*;

public class EntityToBeanTranslationTest {

    @Test
    public void testMapEntityToBean() {
        Study entity = new Study();
        entity.setStudyId(123);
        entity.setName("Test Study Entity");
        entity.setOfficialTitle("Official Title Entity");

        StudyBean bean = new StudyBean();
        BeanUtils.copyProperties(entity, bean, "id");
        bean.setId(entity.getStudyId());

        assertEquals("Test Study Entity", bean.getName());
        assertEquals("Official Title Entity", bean.getOfficialTitle());
        assertEquals(123, bean.getId());
    }
}

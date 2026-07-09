/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2010 Akaza Research
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.crfdata.SCDItemMetadataBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class SCDItemMetadataDao extends AbstractDomainDao<SCDItemMetadataBean>{
    
    @Override
    Class<SCDItemMetadataBean> domainClass() {
        return SCDItemMetadataBean.class;
    }
    
    
    @SuppressWarnings("unchecked")
    public ArrayList<SCDItemMetadataBean> findAllBySectionId(Integer sectionId) {
        String query = "select scd.* from scd_item_metadata scd where scd.scd_item_form_metadata_id in ("
            + "select ifm.item_form_metadata_id from item_form_metadata ifm where ifm.section_id = :sectionId)";
        jakarta.persistence.Query q = this.getEntityManager().createNativeQuery(query, this.domainClass());
        q.setParameter("sectionId", sectionId);
        return (ArrayList<SCDItemMetadataBean>) q.getResultList();  
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> findAllSCDItemFormMetadataIdsBySectionId(Integer sectionId) {
        String query = "select scd.scd_item_form_metadata_id from scd_item_metadata scd where scd.scd_item_form_metadata_id in ("
        + "select ifm.item_form_metadata_id from item_form_metadata ifm where ifm.section_id = :sectionId)";
        jakarta.persistence.Query q = this.getEntityManager().createNativeQuery(query);
        q.setParameter("sectionId", sectionId);
        return q.getResultList();
    }
    @SuppressWarnings("unchecked")
    public ArrayList<SCDItemMetadataBean> findAllSCDByItemFormMetadataId(Integer itemFormMetadataId) {
        String query = "select scd.* from scd_item_metadata scd where scd.scd_item_form_metadata_id = :itemFormMetadataId)";
        jakarta.persistence.Query q = this.getEntityManager().createNativeQuery(query);
        q.setParameter("itemFormMetadataId", itemFormMetadataId);
        return (ArrayList<SCDItemMetadataBean>) q.getResultList();
    }
}
package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.datamap.ItemData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfItemTag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class TagDao extends AbstractDomainDao<Tag> {

    @Override
    Class<Tag> domainClass() {
        // TODO Auto-generated method stub
        return Tag.class;
    }

    
}

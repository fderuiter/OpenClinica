package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.datamap.EventCrfFlag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.ItemDataFlag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.ItemData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.ItemDataFlagWorkflow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ItemDataFlagWorkflowDao extends AbstractDomainDao<ItemDataFlagWorkflow> {

    @Override
    Class<ItemDataFlagWorkflow> domainClass() {
        // TODO Auto-generated method stub
        return ItemDataFlagWorkflow.class;
    }


    
    
}

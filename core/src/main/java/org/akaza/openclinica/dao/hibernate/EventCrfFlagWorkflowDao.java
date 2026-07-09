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
import org.akaza.openclinica.domain.datamap.EventCrfFlagWorkflow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfItemTag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.ItemDataFlag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.ItemData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class EventCrfFlagWorkflowDao extends AbstractDomainDao<EventCrfFlagWorkflow> {

    @Override
    Class<EventCrfFlagWorkflow> domainClass() {
        // TODO Auto-generated method stub
        return EventCrfFlagWorkflow.class;
    }


}

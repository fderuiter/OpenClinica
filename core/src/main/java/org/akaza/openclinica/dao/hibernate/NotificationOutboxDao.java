package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.NotificationOutbox;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class NotificationOutboxDao extends AbstractDomainDao<NotificationOutbox> {

    @Override
    public Class<NotificationOutbox> domainClass() {
        return NotificationOutbox.class;
    }

    @Transactional
    public List<NotificationOutbox> findPendingNotifications() {
        return (List<NotificationOutbox>) getEntityManager()
                .createQuery("from NotificationOutbox where status = 'PENDING'")
                .getResultList();
    }
}

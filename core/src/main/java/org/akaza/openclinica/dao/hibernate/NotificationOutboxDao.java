package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.NotificationOutbox;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public class NotificationOutboxDao extends AbstractDomainDao<NotificationOutbox> {

    @Override
    public Class<NotificationOutbox> domainClass() {
        return NotificationOutbox.class;
    }

    @Transactional
    public List<NotificationOutbox> findPendingNotifications() {
        return (List<NotificationOutbox>) getCurrentSession()
                .createQuery("from NotificationOutbox where status = 'PENDING'")
                .list();
    }
}

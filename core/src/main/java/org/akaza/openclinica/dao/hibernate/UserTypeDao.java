package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.user.UserType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class UserTypeDao extends AbstractDomainDao<UserType> {
	
    @Override
    public Class<UserType> domainClass() {
        return UserType.class;
    }
    
    public UserType findByUserTypeId(Integer userTypeId) {
        
        String query = "from " + getDomainClassName() + " do  where do.userTypeId = :user_type_id";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("user_type_id", userTypeId);
        return (UserType) q.getResultList().stream().findFirst().orElse(null);
    }

}

package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.user.UserAccount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class UserAccountDao extends AbstractDomainDao<UserAccount> {
	
    @Override
    public Class<UserAccount> domainClass() {
        return UserAccount.class;
    }
    
    public UserAccount findByUserName(String userName) {
        
        String query = "from " + getDomainClassName() + " do  where do.userName = :user_name";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("user_name", userName);
        return (UserAccount) q.getResultList().stream().findFirst().orElse(null);
    }

    public UserAccount findByUserId(Integer userId) {
        
        String query = "from " + getDomainClassName() + " do  where do.userId = :user_id";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("user_id", userId);
        return (UserAccount) q.getResultList().stream().findFirst().orElse(null);
    }

}

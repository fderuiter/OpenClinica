package org.akaza.openclinica.dao;

import org.akaza.openclinica.dao.hibernate.DatabaseChangeLogDao;
import org.hibernate.SessionFactory;
import org.junit.Test;

public class TestCompile {
    @Test
    public void test() {
        DatabaseChangeLogDao dao = new DatabaseChangeLogDao();
        SessionFactory sf = null;
        dao.setSessionFactory(sf);
    }
}

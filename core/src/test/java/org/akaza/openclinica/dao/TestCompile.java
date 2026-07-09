package org.akaza.openclinica.dao;

import org.akaza.openclinica.dao.hibernate.DatabaseChangeLogDao;
import org.hibernate.SessionFactory;

public class TestCompile {
    public void test() {
        DatabaseChangeLogDao dao = new DatabaseChangeLogDao();
        SessionFactory sf = null;
        dao.setSessionFactory(sf);
    }
}

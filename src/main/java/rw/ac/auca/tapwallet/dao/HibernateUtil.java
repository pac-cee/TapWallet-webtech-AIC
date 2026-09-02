package rw.ac.auca.tapwallet.dao;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * The Class HibernateUtil.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class HibernateUtil {

    public SessionFactory getSessionFactory(){
        Configuration configuration = new Configuration();
        configuration.configure();
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        return sessionFactory;
    }
}

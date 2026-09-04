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

    private static volatile SessionFactory sessionFactory;

    public SessionFactory getSessionFactory(){
        if (sessionFactory == null) {
            synchronized (HibernateUtil.class) {
                if (sessionFactory == null) {
                    Configuration configuration = new Configuration();
                    configuration.configure();
                    sessionFactory = configuration.buildSessionFactory();
                }
            }
        }
        return sessionFactory;
    }
}

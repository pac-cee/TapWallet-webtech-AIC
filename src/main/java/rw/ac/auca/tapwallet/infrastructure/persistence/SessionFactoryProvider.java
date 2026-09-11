package rw.ac.auca.tapwallet.infrastructure.persistence;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Singleton pattern. Building a SessionFactory is expensive, so exactly one is
 * created for the whole application and shared by every repository.
 */
public final class SessionFactoryProvider {

    private static volatile SessionFactory sessionFactory;

    private SessionFactoryProvider() {
    }

    public static SessionFactory get() {
        if (sessionFactory == null) {
            synchronized (SessionFactoryProvider.class) {
                if (sessionFactory == null) {
                    sessionFactory = new Configuration().configure().buildSessionFactory();
                }
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        synchronized (SessionFactoryProvider.class) {
            if (sessionFactory != null) {
                sessionFactory.close();
                sessionFactory = null;
            }
        }
    }
}

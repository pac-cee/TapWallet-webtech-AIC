package rw.ac.auca.tapwallet.infrastructure.persistence;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.function.Supplier;

/**
 * Unit of Work. One use case = one database transaction: everything inside the
 * callback commits together or rolls back together. Repositories join the
 * thread-bound session opened here, so they never manage transactions themselves.
 */
public final class UnitOfWork {

    private UnitOfWork() {
    }

    public static <T> T execute(Supplier<T> work) {
        Session session = currentSession();
        Transaction transaction = session.beginTransaction();
        try {
            T result = work.get();
            transaction.commit();
            return result;
        } catch (RuntimeException failure) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw failure;
        }
    }

    public static void run(Runnable work) {
        execute(() -> {
            work.run();
            return null;
        });
    }

    static Session currentSession() {
        return SessionFactoryProvider.get().getCurrentSession();
    }
}

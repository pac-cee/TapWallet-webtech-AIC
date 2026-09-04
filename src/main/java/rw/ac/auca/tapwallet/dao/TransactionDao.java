package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * The Class TransactionDao.
 *
 * <p>Pure persistence: CRUD plus finder queries. The money-movement
 * use-cases (transfer, reversal) live in
 * {@code rw.ac.auca.tapwallet.service.PaymentService}, which owns the
 * business rules and the transaction boundary around them.</p>
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class TransactionDao {

    HibernateUtil hibernateUtil = new HibernateUtil();

    // CREATE / UPDATE
    public rw.ac.auca.tapwallet.model.Transaction save(rw.ac.auca.tapwallet.model.Transaction theTx){
        // step 1: create session
        Session ss = hibernateUtil.getSessionFactory().openSession();
        // step 2: create transaction
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            // step 3: perform action
            ss.saveOrUpdate(theTx);
            // step 4: commit transaction
            tr.commit();
        } catch (RuntimeException ex) {
            if (tr != null) {
                tr.rollback();
            }
            throw ex;
        } finally {
            // step 5: close session
            ss.close();
        }
        return theTx;
    }

    // READ (all)
    public List<rw.ac.auca.tapwallet.model.Transaction> findAll(){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT t FROM Transaction t ORDER BY t.id DESC",
                    rw.ac.auca.tapwallet.model.Transaction.class).list();
        } finally {
            ss.close();
        }
    }

    // READ (one)
    public rw.ac.auca.tapwallet.model.Transaction findById(Long id){
        if (id == null) {
            return null;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.get(rw.ac.auca.tapwallet.model.Transaction.class, id);
        } finally {
            ss.close();
        }
    }

    // READ (ledger for one wallet, either side)
    public List<rw.ac.auca.tapwallet.model.Transaction> findByWallet(Long walletId){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT t FROM Transaction t WHERE t.senderWallet.id = :wid OR t.receiverWallet.id = :wid ORDER BY t.id DESC",
                    rw.ac.auca.tapwallet.model.Transaction.class)
                    .setParameter("wid", walletId)
                    .list();
        } finally {
            ss.close();
        }
    }

    // DELETE
    public void delete(Long id){
        if (id == null) {
            return;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            rw.ac.auca.tapwallet.model.Transaction tx = ss.get(rw.ac.auca.tapwallet.model.Transaction.class, id);
            if (tx != null){
                ss.delete(tx);
            }
            tr.commit();
        } catch (RuntimeException ex) {
            if (tr != null) {
                tr.rollback();
            }
            throw ex;
        } finally {
            ss.close();
        }
    }
}

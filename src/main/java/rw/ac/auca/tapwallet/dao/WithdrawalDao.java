package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.model.Withdrawal;

import java.util.List;

/**
 * The Class WithdrawalDao.
 *
 * <p>Pure persistence: CRUD plus finder queries. The debit/undo
 * use-cases live in {@code rw.ac.auca.tapwallet.service.WithdrawalService}.</p>
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class WithdrawalDao {

    HibernateUtil hibernateUtil = new HibernateUtil();

    // CREATE / UPDATE
    public Withdrawal save(Withdrawal theWithdrawal){
        // step 1: create session
        Session ss = hibernateUtil.getSessionFactory().openSession();
        // step 2: create transaction
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            // step 3: perform action
            ss.saveOrUpdate(theWithdrawal);
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
        return theWithdrawal;
    }

    // READ (all)
    public List<Withdrawal> findAll(){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT w FROM Withdrawal w ORDER BY w.id DESC", Withdrawal.class).list();
        } finally {
            ss.close();
        }
    }

    // READ (one)
    public Withdrawal findById(Long id){
        if (id == null) {
            return null;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.get(Withdrawal.class, id);
        } finally {
            ss.close();
        }
    }

    // READ (for one wallet)
    public List<Withdrawal> findByWallet(Long walletId){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT w FROM Withdrawal w WHERE w.wallet.id = :wid ORDER BY w.id DESC", Withdrawal.class)
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
            Withdrawal withdrawal = ss.get(Withdrawal.class, id);
            if (withdrawal != null){
                ss.delete(withdrawal);
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

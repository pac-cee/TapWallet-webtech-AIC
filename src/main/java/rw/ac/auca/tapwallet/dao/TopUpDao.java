package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.model.TopUp;
import rw.ac.auca.tapwallet.model.Wallet;

import java.math.BigDecimal;
import java.util.List;

/**
 * The Class TopUpDao.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class TopUpDao {

    HibernateUtil hibernateUtil = new HibernateUtil();

    // CREATE with ledger: credit wallet + persist row, atomically.
    public TopUp credit(TopUp theTopUp){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            Wallet wallet = ss.get(Wallet.class, theTopUp.getWallet().getId());
            if (wallet == null) {
                throw new IllegalArgumentException("Wallet must exist.");
            }
            if (!"ACTIVE".equals(wallet.getStatus())) {
                throw new IllegalStateException("Wallet must be ACTIVE to top up.");
            }
            if (theTopUp.getAmount() == null || theTopUp.getAmount().compareTo(new BigDecimal("0.01")) < 0) {
                throw new IllegalArgumentException("Amount must be at least 0.01.");
            }
            wallet.setBalance(wallet.getBalance().add(theTopUp.getAmount()));
            ss.update(wallet);
            theTopUp.setWallet(wallet);
            ss.save(theTopUp);
            tr.commit();
        } catch (RuntimeException ex) {
            if (tr != null) {
                tr.rollback();
            }
            throw ex;
        } finally {
            ss.close();
        }
        return theTopUp;
    }

    public List<TopUp> findAll(){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT t FROM TopUp t ORDER BY t.id DESC", TopUp.class).list();
        } finally {
            ss.close();
        }
    }

    public TopUp findById(Long id){
        if (id == null) {
            return null;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.get(TopUp.class, id);
        } finally {
            ss.close();
        }
    }

    public List<TopUp> findByWallet(Long walletId){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT t FROM TopUp t WHERE t.wallet.id = :wid ORDER BY t.id DESC", TopUp.class)
                    .setParameter("wid", walletId)
                    .list();
        } finally {
            ss.close();
        }
    }

    // DELETE with reversal: debit wallet, then remove row.
    public void deleteWithReversal(Long id){
        if (id == null) {
            return;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            TopUp topUp = ss.get(TopUp.class, id);
            if (topUp != null) {
                Wallet wallet = ss.get(Wallet.class, topUp.getWallet().getId());
                if (wallet.getBalance().compareTo(topUp.getAmount()) < 0) {
                    throw new IllegalStateException("Wallet balance too low to undo this top-up.");
                }
                wallet.setBalance(wallet.getBalance().subtract(topUp.getAmount()));
                ss.update(wallet);
                ss.delete(topUp);
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

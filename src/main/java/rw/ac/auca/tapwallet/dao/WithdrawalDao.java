package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.model.Wallet;
import rw.ac.auca.tapwallet.model.Withdrawal;

import java.math.BigDecimal;
import java.util.List;

/**
 * The Class WithdrawalDao.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class WithdrawalDao {

    HibernateUtil hibernateUtil = new HibernateUtil();

    // CREATE with ledger: debit wallet + persist row, atomically.
    public Withdrawal debit(Withdrawal theWithdrawal){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            Wallet wallet = ss.get(Wallet.class, theWithdrawal.getWallet().getId());
            if (wallet == null) {
                throw new IllegalArgumentException("Wallet must exist.");
            }
            if (!"ACTIVE".equals(wallet.getStatus())) {
                throw new IllegalStateException("Wallet must be ACTIVE to withdraw.");
            }
            if (theWithdrawal.getAmount() == null || theWithdrawal.getAmount().compareTo(new BigDecimal("0.01")) < 0) {
                throw new IllegalArgumentException("Amount must be at least 0.01.");
            }
            if (wallet.getBalance().compareTo(theWithdrawal.getAmount()) < 0) {
                throw new IllegalStateException("Insufficient wallet balance.");
            }
            wallet.setBalance(wallet.getBalance().subtract(theWithdrawal.getAmount()));
            ss.update(wallet);
            theWithdrawal.setWallet(wallet);
            ss.save(theWithdrawal);
            tr.commit();
        } catch (RuntimeException ex) {
            if (tr != null) {
                tr.rollback();
            }
            throw ex;
        } finally {
            ss.close();
        }
        return theWithdrawal;
    }

    public List<Withdrawal> findAll(){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT w FROM Withdrawal w ORDER BY w.id DESC", Withdrawal.class).list();
        } finally {
            ss.close();
        }
    }

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

    // DELETE with reversal: credit wallet back, then remove row.
    public void deleteWithReversal(Long id){
        if (id == null) {
            return;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            Withdrawal withdrawal = ss.get(Withdrawal.class, id);
            if (withdrawal != null) {
                Wallet wallet = ss.get(Wallet.class, withdrawal.getWallet().getId());
                wallet.setBalance(wallet.getBalance().add(withdrawal.getAmount()));
                ss.update(wallet);
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

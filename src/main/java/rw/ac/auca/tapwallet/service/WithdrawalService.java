package rw.ac.auca.tapwallet.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.dao.HibernateUtil;
import rw.ac.auca.tapwallet.dao.WithdrawalDao;
import rw.ac.auca.tapwallet.model.Wallet;
import rw.ac.auca.tapwallet.model.Withdrawal;

import java.math.BigDecimal;
import java.util.List;

/**
 * The Class WithdrawalService.
 *
 * <p>Owns the withdrawal use-cases: debit the wallet and persist the row
 * atomically. Beans call here; they never touch the ledger directly.</p>
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class WithdrawalService {

    HibernateUtil hibernateUtil = new HibernateUtil();
    WithdrawalDao withdrawalDao = new WithdrawalDao();

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");

    // USE-CASE: withdraw (debit wallet + ledger row, atomically).
    public Withdrawal debit(Withdrawal theWithdrawal){
        // step 1: create session
        Session ss = hibernateUtil.getSessionFactory().openSession();
        // step 2: create transaction
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            // step 3: perform the use-case
            Wallet wallet = ss.get(Wallet.class, theWithdrawal.getWallet().getId());
            if (wallet == null) {
                throw new IllegalArgumentException("Wallet must exist.");
            }
            if (!"ACTIVE".equals(wallet.getStatus())) {
                throw new IllegalStateException("Wallet must be ACTIVE to withdraw.");
            }
            if (theWithdrawal.getAmount() == null || theWithdrawal.getAmount().compareTo(MIN_AMOUNT) < 0) {
                throw new IllegalArgumentException("Amount must be at least 0.01.");
            }
            if (wallet.getBalance().compareTo(theWithdrawal.getAmount()) < 0) {
                throw new IllegalStateException("Insufficient wallet balance.");
            }
            wallet.setBalance(wallet.getBalance().subtract(theWithdrawal.getAmount()));
            ss.update(wallet);
            theWithdrawal.setWallet(wallet);
            ss.save(theWithdrawal);
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

    // USE-CASE: undo a withdrawal (credit wallet back, then remove row).
    public void undo(Long id){
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

    // READ (delegated to the DAO — no rules involved)
    public List<Withdrawal> findAll(){
        return withdrawalDao.findAll();
    }

    public Withdrawal findById(Long id){
        return withdrawalDao.findById(id);
    }

    public List<Withdrawal> findByWallet(Long walletId){
        return withdrawalDao.findByWallet(walletId);
    }
}

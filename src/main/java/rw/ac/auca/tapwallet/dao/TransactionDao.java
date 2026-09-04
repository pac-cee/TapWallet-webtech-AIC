package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.model.Wallet;

import java.math.BigDecimal;
import java.util.List;

/**
 * The Class TransactionDao.
 *
 * <p>Money movement is always applied atomically: debit sender, credit
 * receiver and persist the ledger row inside one Hibernate transaction,
 * so the two balances can never drift apart.</p>
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class TransactionDao {

    HibernateUtil hibernateUtil = new HibernateUtil();

    // CREATE with ledger: debit sender + credit receiver + persist row, atomically.
    public rw.ac.auca.tapwallet.model.Transaction transfer(rw.ac.auca.tapwallet.model.Transaction theTx){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            Wallet sender = ss.get(Wallet.class, theTx.getSenderWallet().getId());
            Wallet receiver = ss.get(Wallet.class, theTx.getReceiverWallet().getId());
            if (sender == null || receiver == null) {
                throw new IllegalArgumentException("Both wallets must exist.");
            }
            if (sender.getId().equals(receiver.getId())) {
                throw new IllegalArgumentException("Sender and receiver must differ.");
            }
            BigDecimal amount = theTx.getAmount();
            if (amount == null || amount.compareTo(new BigDecimal("0.01")) < 0) {
                throw new IllegalArgumentException("Amount must be at least 0.01.");
            }
            if (!"ACTIVE".equals(sender.getStatus()) || !"ACTIVE".equals(receiver.getStatus())) {
                throw new IllegalStateException("Both wallets must be ACTIVE.");
            }
            if (sender.getBalance().compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient sender balance.");
            }
            sender.setBalance(sender.getBalance().subtract(amount));
            receiver.setBalance(receiver.getBalance().add(amount));
            ss.update(sender);
            ss.update(receiver);
            theTx.setSenderWallet(sender);
            theTx.setReceiverWallet(receiver);
            ss.save(theTx);
            tr.commit();
        } catch (RuntimeException ex) {
            if (tr != null) {
                tr.rollback();
            }
            throw ex;
        } finally {
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

    // DELETE with reversal: credit sender back + debit receiver, then remove row.
    public void deleteWithReversal(Long id){
        if (id == null) {
            return;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            rw.ac.auca.tapwallet.model.Transaction tx = ss.get(rw.ac.auca.tapwallet.model.Transaction.class, id);
            if (tx != null) {
                Wallet sender = ss.get(Wallet.class, tx.getSenderWallet().getId());
                Wallet receiver = ss.get(Wallet.class, tx.getReceiverWallet().getId());
                if (receiver.getBalance().compareTo(tx.getAmount()) < 0) {
                    throw new IllegalStateException("Receiver balance too low to reverse this payment.");
                }
                sender.setBalance(sender.getBalance().add(tx.getAmount()));
                receiver.setBalance(receiver.getBalance().subtract(tx.getAmount()));
                ss.update(sender);
                ss.update(receiver);
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

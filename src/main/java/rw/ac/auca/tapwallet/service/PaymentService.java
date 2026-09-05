package rw.ac.auca.tapwallet.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.dao.HibernateUtil;
import rw.ac.auca.tapwallet.dao.TransactionDao;
import rw.ac.auca.tapwallet.model.Wallet;

import java.math.BigDecimal;
import java.util.List;

public class PaymentService {
    HibernateUtil hibernateUtil = new HibernateUtil();
    TransactionDao transactionDao = new TransactionDao();

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");

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
            if (amount == null || amount.compareTo(MIN_AMOUNT) < 0) {
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

    public void reverse(Long id){
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

    public List<rw.ac.auca.tapwallet.model.Transaction> findAll(){
        return transactionDao.findAll();
    }

    public rw.ac.auca.tapwallet.model.Transaction findById(Long id){
        return transactionDao.findById(id);
    }

    public List<rw.ac.auca.tapwallet.model.Transaction> findByWallet(Long walletId){
        return transactionDao.findByWallet(walletId);
    }
}

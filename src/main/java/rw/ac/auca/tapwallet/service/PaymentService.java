package rw.ac.auca.tapwallet.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.dao.HibernateUtil;
import rw.ac.auca.tapwallet.dao.TransactionDao;
import rw.ac.auca.tapwallet.model.Merchant;
import rw.ac.auca.tapwallet.model.Wallet;

import java.math.BigDecimal;
import java.util.List;

public class PaymentService {
    HibernateUtil hibernateUtil = new HibernateUtil();
    TransactionDao transactionDao = new TransactionDao();

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");

    public rw.ac.auca.tapwallet.model.Transaction pay(rw.ac.auca.tapwallet.model.Transaction theTx){
        Session ss = hibernateUtil.getSessionFactory().openSession();

        Transaction tr = null;
        try {
            tr = ss.beginTransaction();

            Wallet sender = ss.get(Wallet.class, theTx.getSenderWallet().getId());
            Merchant merchant = ss.get(Merchant.class, theTx.getReceiverMerchant().getId());
            if (sender == null || merchant == null) {
                throw new IllegalArgumentException("Both the wallet and the merchant must exist.");
            }
            if (sender.getOwner().getId().equals(merchant.getOperator().getId())) {
                throw new IllegalArgumentException("You can't pay your own shop.");
            }
            BigDecimal amount = theTx.getAmount();
            if (amount == null || amount.compareTo(MIN_AMOUNT) < 0) {
                throw new IllegalArgumentException("Amount must be at least 0.01.");
            }
            if (!"ACTIVE".equals(sender.getStatus())) {
                throw new IllegalStateException("The wallet must be ACTIVE.");
            }
            if (!"ACTIVE".equals(merchant.getStatus())) {
                throw new IllegalStateException("The merchant must be ACTIVE.");
            }
            if (sender.getBalance().compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient wallet balance.");
            }
            sender.setBalance(sender.getBalance().subtract(amount));
            merchant.setBalance(merchant.getBalance().add(amount));
            ss.update(sender);
            ss.update(merchant);
            theTx.setSenderWallet(sender);
            theTx.setReceiverMerchant(merchant);
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
                Merchant merchant = ss.get(Merchant.class, tx.getReceiverMerchant().getId());
                if (merchant.getBalance().compareTo(tx.getAmount()) < 0) {
                    throw new IllegalStateException("Merchant balance too low to reverse this payment.");
                }
                sender.setBalance(sender.getBalance().add(tx.getAmount()));
                merchant.setBalance(merchant.getBalance().subtract(tx.getAmount()));
                ss.update(sender);
                ss.update(merchant);
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

    public List<rw.ac.auca.tapwallet.model.Transaction> findByMerchant(Long merchantId){
        return transactionDao.findByMerchant(merchantId);
    }
}

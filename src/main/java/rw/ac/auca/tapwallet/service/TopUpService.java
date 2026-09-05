package rw.ac.auca.tapwallet.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.dao.HibernateUtil;
import rw.ac.auca.tapwallet.dao.TopUpDao;
import rw.ac.auca.tapwallet.model.TopUp;
import rw.ac.auca.tapwallet.model.Wallet;

import java.math.BigDecimal;
import java.util.List;

public class TopUpService {
    HibernateUtil hibernateUtil = new HibernateUtil();
    TopUpDao topUpDao = new TopUpDao();

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");

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
            if (theTopUp.getAmount() == null || theTopUp.getAmount().compareTo(MIN_AMOUNT) < 0) {
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

    public void undo(Long id){
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

    public List<TopUp> findAll(){
        return topUpDao.findAll();
    }

    public TopUp findById(Long id){
        return topUpDao.findById(id);
    }

    public List<TopUp> findByWallet(Long walletId){
        return topUpDao.findByWallet(walletId);
    }
}

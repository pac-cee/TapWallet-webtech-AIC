package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class TransactionDao {
    HibernateUtil hibernateUtil = new HibernateUtil();

    public rw.ac.auca.tapwallet.model.Transaction save(rw.ac.auca.tapwallet.model.Transaction theTx){
        Session ss = hibernateUtil.getSessionFactory().openSession();

        Transaction tr = null;
        try {
            tr = ss.beginTransaction();

            ss.saveOrUpdate(theTx);

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

    public List<rw.ac.auca.tapwallet.model.Transaction> findAll(){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT t FROM Transaction t ORDER BY t.id DESC",
                    rw.ac.auca.tapwallet.model.Transaction.class).list();
        } finally {
            ss.close();
        }
    }

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

    public List<rw.ac.auca.tapwallet.model.Transaction> findByWallet(Long walletId){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT t FROM Transaction t WHERE t.senderWallet.id = :wid ORDER BY t.id DESC",
                    rw.ac.auca.tapwallet.model.Transaction.class)
                    .setParameter("wid", walletId)
                    .list();
        } finally {
            ss.close();
        }
    }

    public List<rw.ac.auca.tapwallet.model.Transaction> findByMerchant(Long merchantId){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT t FROM Transaction t WHERE t.receiverMerchant.id = :mid ORDER BY t.id DESC",
                    rw.ac.auca.tapwallet.model.Transaction.class)
                    .setParameter("mid", merchantId)
                    .list();
        } finally {
            ss.close();
        }
    }

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

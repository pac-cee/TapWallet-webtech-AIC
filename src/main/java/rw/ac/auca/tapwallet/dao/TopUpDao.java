package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.model.TopUp;

import java.util.List;

public class TopUpDao {
    HibernateUtil hibernateUtil = new HibernateUtil();

    public TopUp save(TopUp theTopUp){
        Session ss = hibernateUtil.getSessionFactory().openSession();

        Transaction tr = null;
        try {
            tr = ss.beginTransaction();

            ss.saveOrUpdate(theTopUp);

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

    public void delete(Long id){
        if (id == null) {
            return;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            TopUp topUp = ss.get(TopUp.class, id);
            if (topUp != null){
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

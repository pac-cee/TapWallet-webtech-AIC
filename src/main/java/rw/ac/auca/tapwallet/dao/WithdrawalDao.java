package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.model.Withdrawal;

import java.util.List;

public class WithdrawalDao {
    HibernateUtil hibernateUtil = new HibernateUtil();

    public Withdrawal save(Withdrawal theWithdrawal){
        Session ss = hibernateUtil.getSessionFactory().openSession();

        Transaction tr = null;
        try {
            tr = ss.beginTransaction();

            ss.saveOrUpdate(theWithdrawal);

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

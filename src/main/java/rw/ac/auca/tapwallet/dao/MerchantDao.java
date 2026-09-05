package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.model.Merchant;

import java.util.List;

public class MerchantDao {
    HibernateUtil hibernateUtil = new HibernateUtil();

    public Merchant save(Merchant theMerchant){
        Session ss = hibernateUtil.getSessionFactory().openSession();

        Transaction tr = null;
        try {
            tr = ss.beginTransaction();

            ss.saveOrUpdate(theMerchant);

            tr.commit();
        } catch (RuntimeException ex) {
            if (tr != null) {
                tr.rollback();
            }
            throw ex;
        } finally {
            ss.close();
        }
        return theMerchant;
    }

    public List<Merchant> findAll(){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT m FROM Merchant m ORDER BY m.id", Merchant.class).list();
        } finally {
            ss.close();
        }
    }

    public Merchant findById(Long id){
        if (id == null) {
            return null;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.get(Merchant.class, id);
        } finally {
            ss.close();
        }
    }

    public Merchant findByCode(String code){
        if (code == null) {
            return null;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            List<Merchant> merchants = ss.createQuery("SELECT m FROM Merchant m WHERE m.merchantCode = :code", Merchant.class)
                    .setParameter("code", code)
                    .setMaxResults(1)
                    .list();
            return merchants.isEmpty() ? null : merchants.get(0);
        } finally {
            ss.close();
        }
    }

    public Merchant findByOperator(Long operatorId){
        if (operatorId == null) {
            return null;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            List<Merchant> merchants = ss.createQuery("SELECT m FROM Merchant m WHERE m.operator.id = :operatorId", Merchant.class)
                    .setParameter("operatorId", operatorId)
                    .setMaxResults(1)
                    .list();
            return merchants.isEmpty() ? null : merchants.get(0);
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
            Merchant merchant = ss.get(Merchant.class, id);
            if (merchant != null){
                ss.delete(merchant);
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

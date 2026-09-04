package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.model.Merchant;

import java.util.List;

/**
 * The Class MerchantDao.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class MerchantDao {

    HibernateUtil hibernateUtil = new HibernateUtil();

    // CREATE / UPDATE
    public Merchant save(Merchant theMerchant){
        // step 1: create session
        Session ss = hibernateUtil.getSessionFactory().openSession();
        // step 2: create transaction
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            // step 3: perform action
            ss.saveOrUpdate(theMerchant);
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
        return theMerchant;
    }

    // READ (all)
    public List<Merchant> findAll(){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT m FROM Merchant m ORDER BY m.id", Merchant.class).list();
        } finally {
            ss.close();
        }
    }

    // READ (one)
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

    // READ (by code, parameterized)
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

    // READ (by operator — one shop per user)
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

    // DELETE
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

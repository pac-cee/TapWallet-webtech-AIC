package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.model.TopUp;

import java.util.List;

/**
 * The Class TopUpDao.
 *
 * <p>Pure persistence: CRUD plus finder queries. The credit/undo
 * use-cases live in {@code rw.ac.auca.tapwallet.service.TopUpService}.</p>
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class TopUpDao {

    HibernateUtil hibernateUtil = new HibernateUtil();

    // CREATE / UPDATE
    public TopUp save(TopUp theTopUp){
        // step 1: create session
        Session ss = hibernateUtil.getSessionFactory().openSession();
        // step 2: create transaction
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            // step 3: perform action
            ss.saveOrUpdate(theTopUp);
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
        return theTopUp;
    }

    // READ (all)
    public List<TopUp> findAll(){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT t FROM TopUp t ORDER BY t.id DESC", TopUp.class).list();
        } finally {
            ss.close();
        }
    }

    // READ (one)
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

    // READ (for one wallet)
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

    // DELETE
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

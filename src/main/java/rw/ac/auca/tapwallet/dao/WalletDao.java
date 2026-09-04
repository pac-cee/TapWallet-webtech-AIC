package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.model.Wallet;

import java.util.List;

/**
 * The Class WalletDao.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class WalletDao {

    HibernateUtil hibernateUtil = new HibernateUtil();

    public Wallet save(Wallet theWallet){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            ss.saveOrUpdate(theWallet);
            tr.commit();
        } catch (RuntimeException ex) {
            if (tr != null) {
                tr.rollback();
            }
            throw ex;
        } finally {
            ss.close();
        }
        return theWallet;
    }

    public List<Wallet> findAll(){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT w FROM Wallet w ORDER BY w.id", Wallet.class).list();
        } finally {
            ss.close();
        }
    }

    public Wallet findById(Long id){
        if (id == null) {
            return null;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.get(Wallet.class, id);
        } finally {
            ss.close();
        }
    }

    // READ (by owner — one wallet per user per BR-01)
    public Wallet findByOwner(Long ownerId){
        if (ownerId == null) {
            return null;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            List<Wallet> wallets = ss.createQuery("SELECT w FROM Wallet w WHERE w.owner.id = :ownerId", Wallet.class)
                    .setParameter("ownerId", ownerId)
                    .setMaxResults(1)
                    .list();
            return wallets.isEmpty() ? null : wallets.get(0);
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
            Wallet wallet = ss.get(Wallet.class, id);
            if (wallet != null){
                ss.delete(wallet);
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

package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.model.NfcCard;

import java.util.List;

/**
 * The Class NfcCardDao.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class NfcCardDao {

    HibernateUtil hibernateUtil = new HibernateUtil();

    // CREATE / UPDATE
    public NfcCard save(NfcCard theCard){
        // step 1: create session
        Session ss = hibernateUtil.getSessionFactory().openSession();
        // step 2: create transaction
        Transaction tr = null;
        try {
            tr = ss.beginTransaction();
            // step 3: perform action
            ss.saveOrUpdate(theCard);
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
        return theCard;
    }

    // READ (all)
    public List<NfcCard> findAll(){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.createQuery("SELECT c FROM NfcCard c ORDER BY c.id", NfcCard.class).list();
        } finally {
            ss.close();
        }
    }

    // READ (one)
    public NfcCard findById(Long id){
        if (id == null) {
            return null;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            return ss.get(NfcCard.class, id);
        } finally {
            ss.close();
        }
    }

    // READ (by token, parameterized)
    public NfcCard findByToken(String token){
        if (token == null) {
            return null;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            List<NfcCard> cards = ss.createQuery("SELECT c FROM NfcCard c WHERE c.token = :token", NfcCard.class)
                    .setParameter("token", token)
                    .setMaxResults(1)
                    .list();
            return cards.isEmpty() ? null : cards.get(0);
        } finally {
            ss.close();
        }
    }

    // READ (by wallet — at most one card per wallet)
    public NfcCard findByWallet(Long walletId){
        if (walletId == null) {
            return null;
        }
        Session ss = hibernateUtil.getSessionFactory().openSession();
        try {
            List<NfcCard> cards = ss.createQuery("SELECT c FROM NfcCard c WHERE c.wallet.id = :walletId", NfcCard.class)
                    .setParameter("walletId", walletId)
                    .setMaxResults(1)
                    .list();
            return cards.isEmpty() ? null : cards.get(0);
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
            NfcCard card = ss.get(NfcCard.class, id);
            if (card != null){
                ss.delete(card);
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

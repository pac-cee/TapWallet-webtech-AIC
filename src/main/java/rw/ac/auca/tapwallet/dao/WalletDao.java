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
        Transaction tr = ss.beginTransaction();
        ss.saveOrUpdate(theWallet);
        tr.commit();
        ss.close();
        return theWallet;
    }

    public List<Wallet> findAll(){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        List<Wallet> wallets = ss.createQuery("SELECT w FROM Wallet w", Wallet.class).list();
        ss.close();
        return wallets;
    }

    public Wallet findById(Long id){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        Wallet wallet = ss.get(Wallet.class, id);
        ss.close();
        return wallet;
    }

    public void delete(Long id){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        Transaction tr = ss.beginTransaction();
        Wallet wallet = ss.get(Wallet.class, id);
        if (wallet != null){
            ss.delete(wallet);
        }
        tr.commit();
        ss.close();
    }
}

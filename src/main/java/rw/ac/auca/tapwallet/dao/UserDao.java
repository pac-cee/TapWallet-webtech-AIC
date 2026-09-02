package rw.ac.auca.tapwallet.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.tapwallet.model.User;

import java.util.List;

/**
 * The Class UserDao.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class UserDao {

    HibernateUtil hibernateUtil = new HibernateUtil();

    // CREATE / UPDATE
    public User save(User theUser){
        // step 1: create session
        Session ss = hibernateUtil.getSessionFactory().openSession();
        // step 2: create transaction
        Transaction tr = ss.beginTransaction();
        // step 3: perform action
        ss.saveOrUpdate(theUser);
        // step 4: commit transaction
        tr.commit();
        // step 5: close session
        ss.close();
        return theUser;
    }

    // READ (all)
    public List<User> findAll(){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        List<User> users = ss.createQuery("SELECT u FROM User u", User.class).list();
        ss.close();
        return users;
    }

    // READ (one)
    public User findById(Long id){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        User user = ss.get(User.class, id);
        ss.close();
        return user;
    }

    // DELETE
    public void delete(Long id){
        Session ss = hibernateUtil.getSessionFactory().openSession();
        Transaction tr = ss.beginTransaction();
        User user = ss.get(User.class, id);
        if (user != null){
            ss.delete(user);
        }
        tr.commit();
        ss.close();
    }
}

package rw.ac.auca.tapwallet.dao;

import org.junit.Test;
import rw.ac.auca.tapwallet.model.User;

import java.util.List;

import static org.junit.Assert.*;

public class UserDaoTest {

    private final UserDao userDao = new UserDao();

    @Test
    public void savingAUserAssignsAnId() {
        User user = new User("Test Save User", "save-" + System.nanoTime() + "@example.com",
                "0788123456", "hash", "CUSTOMER", "ACTIVE");
        userDao.save(user);
        assertNotNull(user.getId());
    }

    @Test
    public void findByIdReturnsWhatWasSaved() {
        User user = new User("Find Me", "find-" + System.nanoTime() + "@example.com",
                "0788123456", "hash", "CUSTOMER", "ACTIVE");
        userDao.save(user);

        User found = userDao.findById(user.getId());
        assertNotNull(found);
        assertEquals("Find Me", found.getFullName());
    }

    @Test
    public void savingWithAnExistingIdUpdatesInPlace() {
        User user = new User("Before Update", "update-" + System.nanoTime() + "@example.com",
                "0788123456", "hash", "CUSTOMER", "ACTIVE");
        userDao.save(user);

        user.setFullName("After Update");
        userDao.save(user);

        User found = userDao.findById(user.getId());
        assertEquals("After Update", found.getFullName());
    }

    @Test
    public void deleteRemovesTheUser() {
        User user = new User("Delete Me", "delete-" + System.nanoTime() + "@example.com",
                "0788123456", "hash", "CUSTOMER", "ACTIVE");
        userDao.save(user);
        Long id = user.getId();

        userDao.delete(id);

        assertNull(userDao.findById(id));
    }

    @Test
    public void findAllIncludesASavedUser() {
        User user = new User("List Me", "list-" + System.nanoTime() + "@example.com",
                "0788123456", "hash", "CUSTOMER", "ACTIVE");
        userDao.save(user);

        List<User> all = userDao.findAll();
        assertTrue(all.stream().anyMatch(u -> u.getId().equals(user.getId())));
    }
}

package rw.ac.auca.tapwallet;

import org.junit.Test;
import rw.ac.auca.tapwallet.dao.UserDao;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.util.PasswordUtil;

import static org.junit.Assert.*;

public class UserBeanTest {

    private final UserDao userDao = new UserDao();

    @Test
    public void savingANewUserHashesThePasswordAndPersists() {
        UserBean bean = new UserBean();
        bean.setFullName("New User");
        bean.setEmail("newuser-" + System.nanoTime() + "@example.com");
        bean.setPhone("0788123456");
        bean.setPassword("PlainPassword1");
        bean.setStatus("ACTIVE");

        String outcome = bean.save();

        assertEquals("user-list?faces-redirect=true", outcome);
        User saved = userDao.findAll().stream()
                .filter(u -> u.getEmail().equals(bean.getEmail()))
                .findFirst()
                .orElse(null);
        assertNotNull(saved);
        assertNotEquals("PlainPassword1", saved.getPasswordHash());
        assertTrue(PasswordUtil.verify("PlainPassword1", saved.getPasswordHash()));
    }

    @Test
    public void editingAUserWithABlankPasswordKeepsTheOldHash() {
        User user = new User("Edit Me", "edit-" + System.nanoTime() + "@example.com",
                "0788123456", PasswordUtil.hash("OriginalPassword"), "ACTIVE");
        userDao.save(user);

        UserBean bean = new UserBean();
        bean.setId(user.getId());
        bean.loadForEdit();
        bean.setFullName("Edited Name");
        bean.setPassword("");

        bean.save();

        User reloaded = userDao.findById(user.getId());
        assertEquals("Edited Name", reloaded.getFullName());
        assertTrue(PasswordUtil.verify("OriginalPassword", reloaded.getPasswordHash()));
    }

    @Test
    public void deleteRemovesTheUser() {
        User user = new User("Delete Via Bean", "deletebean-" + System.nanoTime() + "@example.com",
                "0788123456", PasswordUtil.hash("pw"), "ACTIVE");
        userDao.save(user);

        UserBean bean = new UserBean();
        String outcome = bean.delete(user.getId());

        assertEquals("user-list?faces-redirect=true", outcome);
        assertNull(userDao.findById(user.getId()));
    }
}

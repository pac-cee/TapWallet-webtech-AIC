package rw.ac.auca.tapwallet.infrastructure.persistence;

import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.domain.model.user.User;
import rw.ac.auca.tapwallet.domain.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class HibernateUserRepository implements UserRepository {

    @Override
    public User save(User user) {
        UnitOfWork.currentSession().saveOrUpdate(user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(UnitOfWork.currentSession().get(User.class, id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        List<User> matches = UnitOfWork.currentSession()
                .createQuery("SELECT u FROM User u WHERE u.email.value = :email", User.class)
                .setParameter("email", email.trim().toLowerCase())
                .setMaxResults(1)
                .list();
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }

    @Override
    public List<User> findAll() {
        return UnitOfWork.currentSession()
                .createQuery("SELECT u FROM User u ORDER BY u.id", User.class)
                .list();
    }

    @Override
    public List<User> findByRole(Role role) {
        return UnitOfWork.currentSession()
                .createQuery("SELECT u FROM User u WHERE u.role = :role ORDER BY u.fullName", User.class)
                .setParameter("role", role)
                .list();
    }

    @Override
    public void delete(User user) {
        UnitOfWork.currentSession().delete(user);
    }

    @Override
    public long count() {
        return UnitOfWork.currentSession()
                .createQuery("SELECT COUNT(u) FROM User u", Long.class)
                .uniqueResult();
    }
}

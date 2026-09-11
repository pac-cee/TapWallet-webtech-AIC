package rw.ac.auca.tapwallet.domain.repository;

import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.domain.model.user.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository contract owned by the domain. The Hibernate implementation lives in
 * the infrastructure layer, so the domain never depends on persistence details.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    List<User> findByRole(Role role);

    void delete(User user);

    long count();
}

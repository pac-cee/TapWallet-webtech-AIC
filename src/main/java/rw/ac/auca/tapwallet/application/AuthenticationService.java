package rw.ac.auca.tapwallet.application;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.exception.InactiveAccountException;
import rw.ac.auca.tapwallet.domain.model.user.User;
import rw.ac.auca.tapwallet.domain.repository.UserRepository;
import rw.ac.auca.tapwallet.infrastructure.persistence.UnitOfWork;
import rw.ac.auca.tapwallet.infrastructure.security.PasswordHasher;

import java.util.Optional;

/**
 * Application service for signing in. Deliberately gives the same message for an
 * unknown email and a wrong password, so the form cannot be used to discover
 * which accounts exist.
 */
public class AuthenticationService {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public AuthenticationService(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    public User signIn(String email, String rawPassword) {
        if (email == null || email.trim().isEmpty() || rawPassword == null || rawPassword.isEmpty()) {
            throw new DomainException("Enter your email and password.");
        }

        User user = UnitOfWork.execute(() -> {
            Optional<User> found = users.findByEmail(email);
            if (!found.isPresent()) {
                return null;
            }
            User candidate = found.get();
            // Touch the values we need before the session closes.
            candidate.getRole();
            candidate.getEmail().getValue();
            return candidate;
        });

        if (user == null || !passwordHasher.matches(rawPassword, user.getPasswordHash())) {
            throw new DomainException("Email or password is incorrect.");
        }
        if (!user.canSignIn()) {
            throw new InactiveAccountException("This account is frozen. Contact an administrator.");
        }
        return user;
    }
}

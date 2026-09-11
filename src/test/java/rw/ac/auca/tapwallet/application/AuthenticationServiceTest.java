package rw.ac.auca.tapwallet.application;

import org.junit.Test;
import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.exception.InactiveAccountException;
import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.domain.model.user.User;
import rw.ac.auca.tapwallet.infrastructure.config.ServiceRegistry;
import rw.ac.auca.tapwallet.infrastructure.persistence.UnitOfWork;

import static org.junit.Assert.*;

public class AuthenticationServiceTest extends ApplicationServiceTestSupport {

    private final AuthenticationService authentication = ServiceRegistry.authentication();

    @Test
    public void signsInWithTheCorrectPassword() {
        String email = uniqueEmail("signin");
        createUser("Alice Uwase", email, Role.CUSTOMER, "Pass@123");

        User signedIn = authentication.signIn(email, "Pass@123");

        assertEquals(email, signedIn.getEmail().getValue());
        assertEquals(Role.CUSTOMER, signedIn.getRole());
    }

    @Test
    public void refusesTheWrongPassword() {
        String email = uniqueEmail("wrongpass");
        createUser("Alice Uwase", email, Role.CUSTOMER, "Pass@123");

        try {
            authentication.signIn(email, "NotMyPassword");
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void refusesAnUnknownEmailWithTheSameMessageAsAWrongPassword() {
        String email = uniqueEmail("known");
        createUser("Alice Uwase", email, Role.CUSTOMER, "Pass@123");

        String wrongPasswordMessage = messageFrom(email, "NotMyPassword");
        String unknownEmailMessage = messageFrom(uniqueEmail("ghost"), "Pass@123");

        assertEquals(wrongPasswordMessage, unknownEmailMessage);
    }

    @Test
    public void refusesAFrozenAccountEvenWithTheRightPassword() {
        String email = uniqueEmail("frozen");
        User user = createUser("Frozen User", email, Role.CUSTOMER, "Pass@123");
        UnitOfWork.run(() -> {
            User attached = ServiceRegistry.userRepository().findById(user.getId()).orElseThrow(IllegalStateException::new);
            attached.freeze();
            ServiceRegistry.userRepository().save(attached);
        });

        try {
            authentication.signIn(email, "Pass@123");
            fail("Expected an InactiveAccountException");
        } catch (InactiveAccountException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void refusesBlankCredentials() {
        try {
            authentication.signIn("", "");
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    private String messageFrom(String email, String password) {
        try {
            authentication.signIn(email, password);
            fail("Expected a DomainException");
            return null;
        } catch (DomainException expected) {
            return expected.getMessage();
        }
    }
}

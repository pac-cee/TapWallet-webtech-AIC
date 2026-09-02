# TapWallet CRUD Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement full CRUD for `User` and `Wallet` in a Maven/JSF/Hibernate project, matching the instructor's `StockManagementSystem` conventions, backed by an H2 in-memory database, with all 3 required validation types and all 3 CSS inclusion styles.

**Architecture:** Classic layered JSF app — `.xhtml` views bind to `@ManagedBean` beans, which delegate to DAO classes that open a Hibernate `Session`/`Transaction` per call (same shape as the reference project's `ProductDao`). Entities are plain JPA POJOs extending a shared `Audit` superclass.

**Tech Stack:** Java 8, Maven (`war` packaging), JSF 2.3.9 (org.glassfish), Hibernate ORM 5.6.15.Final, H2 2.2.224 (in-memory), Hibernate Validator 6.2.5.Final (Bean Validation), JUnit 4.13.2.

**Spec:** `docs/superpowers/specs/2026-09-02-tapwallet-design.md`

## Global Constraints

- Java 8 source/target; `groupId=rw.ac.auca`; package root `rw.ac.auca.tapwallet`.
- Dependency versions match the reference project exactly: `javax.faces` 2.3.9 (org.glassfish), `weld-servlet-shaded` 3.1.9.Final, `javax.servlet-api` 4.0.1 (provided), `hibernate-core` 5.6.15.Final, `maven-war-plugin` 3.3.2.
- Database is H2 in-memory only: `jdbc:h2:mem:tapwallet;DB_CLOSE_DELAY=-1`, dialect `org.hibernate.dialect.H2Dialect`, `hbm2ddl.auto=update`.
- No plaintext password is ever persisted or redisplayed — only `PasswordUtil`'s salted SHA-256 output.
- All Hibernate access uses `Session.get(...)`, `Session.saveOrUpdate(...)`, or parameterized `createQuery` — never string-concatenated HQL.
- Every form input has a paired `<h:message ... style="color:red"/>`.
- All 3 validation types must appear somewhere in the two forms: standard JSF validators (`f:validateLength` / `f:validateDoubleRange`), the custom `PhoneValidator` (`@FacesValidator`), and Bean Validation (JSR-303) annotations on the entities.
- All 3 CSS inclusion styles must appear: external `resources/css/styles.css`, one internal `<style>` block (on `wallet-list.xhtml`), and inline `style="..."` attributes.
- Tests use JUnit 4.13.2 and run directly against the H2 in-memory database — no servlet container needed to verify DAO/bean/validation logic. Views (`.xhtml`) are verified by `mvn package` succeeding and by manual cross-check of EL expressions against bean/entity member names (a container isn't available in this environment).

---

### Task 1: Project scaffold + `PasswordUtil`

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/rw/ac/auca/tapwallet/util/PasswordUtil.java`
- Test: `src/test/java/rw/ac/auca/tapwallet/util/PasswordUtilTest.java`

**Interfaces:**
- Produces: `PasswordUtil.hash(String plain) -> String`, `PasswordUtil.verify(String plain, String storedHash) -> boolean`. Later tasks (`UserBean`) call `PasswordUtil.hash`.

- [ ] **Step 1: Create `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>rw.ac.auca</groupId>
    <artifactId>TapWallet</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>TapWallet</name>
    <packaging>war</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.target>1.8</maven.compiler.target>
        <maven.compiler.source>1.8</maven.compiler.source>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.glassfish</groupId>
            <artifactId>javax.faces</artifactId>
            <version>2.3.9</version>
        </dependency>
        <dependency>
            <groupId>org.jboss.weld.servlet</groupId>
            <artifactId>weld-servlet-shaded</artifactId>
            <version>3.1.9.Final</version>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>5.6.15.Final</version>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>6.2.5.Final</version>
        </dependency>
        <dependency>
            <groupId>javax.el</groupId>
            <artifactId>javax.el-api</artifactId>
            <version>3.0.0</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish</groupId>
            <artifactId>javax.el</artifactId>
            <version>3.0.1-b12</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Write the failing test for `PasswordUtil`**

```java
package rw.ac.auca.tapwallet.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class PasswordUtilTest {

    @Test
    public void hashProducesSaltColonHashFormat() {
        String hash = PasswordUtil.hash("Secret123");
        assertTrue(hash.contains(":"));
    }

    @Test
    public void verifyAcceptsTheCorrectPassword() {
        String hash = PasswordUtil.hash("Secret123");
        assertTrue(PasswordUtil.verify("Secret123", hash));
    }

    @Test
    public void verifyRejectsTheWrongPassword() {
        String hash = PasswordUtil.hash("Secret123");
        assertFalse(PasswordUtil.verify("WrongPassword", hash));
    }

    @Test
    public void twoHashesOfTheSamePasswordAreDifferent() {
        String hash1 = PasswordUtil.hash("Secret123");
        String hash2 = PasswordUtil.hash("Secret123");
        assertNotEquals(hash1, hash2);
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `mvn -q -Dtest=PasswordUtilTest test`
Expected: FAIL (compile error — `PasswordUtil` does not exist)

- [ ] **Step 4: Implement `PasswordUtil`**

```java
package rw.ac.auca.tapwallet.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * The Class PasswordUtil.
 *
 * Hashes and verifies passwords using salted SHA-256. The salt travels
 * alongside the hash (separated by ':') so verification never needs a
 * second lookup, and a plaintext password is never stored anywhere.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class PasswordUtil {

    private static final int SALT_LENGTH = 16;

    public static String hash(String plainPassword) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hashed = digest.digest(plainPassword.getBytes("UTF-8"));

            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException("Unable to hash password", ex);
        }
    }

    public static boolean verify(String plainPassword, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] actualHash = digest.digest(plainPassword.getBytes("UTF-8"));

            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception ex) {
            return false;
        }
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn -q -Dtest=PasswordUtilTest test`
Expected: PASS (4 tests)

- [ ] **Step 6: Commit**

```bash
git add pom.xml src/main/java/rw/ac/auca/tapwallet/util/PasswordUtil.java src/test/java/rw/ac/auca/tapwallet/util/PasswordUtilTest.java
git commit -m "feat: project scaffold + salted-hash PasswordUtil"
```

---

### Task 2: `Audit` + `User` entity + Hibernate config + `UserDao`

**Files:**
- Create: `src/main/java/rw/ac/auca/tapwallet/model/Audit.java`
- Create: `src/main/java/rw/ac/auca/tapwallet/model/User.java`
- Create: `src/main/resources/hibernate.cfg.xml`
- Create: `src/main/resources/META-INF/persistence.xml`
- Create: `src/main/java/rw/ac/auca/tapwallet/dao/HibernateUtil.java`
- Create: `src/main/java/rw/ac/auca/tapwallet/dao/UserDao.java`
- Test: `src/test/java/rw/ac/auca/tapwallet/dao/UserDaoTest.java`

**Interfaces:**
- Consumes: `PasswordUtil.hash` (Task 1) — used only in the test to build a realistic `passwordHash`.
- Produces: `User` (fields: `id: Long`, `fullName: String`, `email: String`, `phone: String`, `passwordHash: String`, `role: String`, `status: String`, plus inherited `createdAt`/`updatedAt`), `UserDao.save(User) -> User`, `UserDao.findAll() -> List<User>`, `UserDao.findById(Long) -> User`, `UserDao.delete(Long) -> void`, `HibernateUtil.getSessionFactory() -> SessionFactory`. Later tasks (`WalletDao`, `UserBean`, `WalletBean`) depend on all of these exact names.

- [ ] **Step 1: Write the failing `UserDaoTest`**

```java
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=UserDaoTest test`
Expected: FAIL (compile error — `User`, `UserDao`, `HibernateUtil` do not exist)

- [ ] **Step 3: Create `Audit`**

```java
package rw.ac.auca.tapwallet.model;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * The Class Audit.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@MappedSuperclass
public class Audit {
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public Audit() {
    }

    public Audit(LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```

- [ ] **Step 4: Create `User`**

```java
package rw.ac.auca.tapwallet.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * The Class User.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@Entity
@Table(name = "app_user")
public class User extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Column(name = "phone", nullable = false)
    private String phone;

    @NotBlank(message = "Password is required")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", nullable = false)
    private String role = "CUSTOMER";

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    public User() {
    }

    public User(String fullName, String email, String phone, String passwordHash, String role, String status) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
```

- [ ] **Step 5: Create `hibernate.cfg.xml`**

```xml
<?xml version='1.0' encoding='utf-8'?>
<!DOCTYPE hibernate-configuration PUBLIC
        "-//Hibernate/Hibernate Configuration DTD//EN"
        "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">

<hibernate-configuration>

    <!-- a SessionFactory instance listed as /jndi/name -->
    <session-factory
            name="java:hibernate/SessionFactory">

        <!-- properties -->
        <property name="hibernate.connection.driver_class">org.h2.Driver</property>
        <property name="hibernate.connection.url">jdbc:h2:mem:tapwallet;DB_CLOSE_DELAY=-1</property>
        <property name="hibernate.connection.username">sa</property>
        <property name="hibernate.connection.password"></property>
        <property name="dialect">org.hibernate.dialect.H2Dialect</property>
        <property name="show_sql">true</property>
        <property name="hbm2ddl.auto">update</property>

        <!--      Entity mapping-->
        <mapping class="rw.ac.auca.tapwallet.model.User"/>

    </session-factory>

</hibernate-configuration>
```

- [ ] **Step 6: Create `persistence.xml`** (present, unused — matches reference project)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="http://java.sun.com/xml/ns/persistence" version="2.0">
    <persistence-unit name="default">

    </persistence-unit>
</persistence>
```

- [ ] **Step 7: Create `HibernateUtil`**

```java
package rw.ac.auca.tapwallet.dao;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * The Class HibernateUtil.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
public class HibernateUtil {

    public SessionFactory getSessionFactory(){
        Configuration configuration = new Configuration();
        configuration.configure();
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        return sessionFactory;
    }
}
```

- [ ] **Step 8: Create `UserDao`**

```java
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
```

- [ ] **Step 9: Run test to verify it passes**

Run: `mvn -q -Dtest=UserDaoTest test`
Expected: PASS (5 tests)

- [ ] **Step 10: Commit**

```bash
git add src/main/java/rw/ac/auca/tapwallet/model/Audit.java src/main/java/rw/ac/auca/tapwallet/model/User.java src/main/resources/hibernate.cfg.xml src/main/resources/META-INF/persistence.xml src/main/java/rw/ac/auca/tapwallet/dao/HibernateUtil.java src/main/java/rw/ac/auca/tapwallet/dao/UserDao.java src/test/java/rw/ac/auca/tapwallet/dao/UserDaoTest.java
git commit -m "feat: User entity + Hibernate config (H2 in-memory) + UserDao CRUD"
```

---

### Task 3: `Wallet` entity + `WalletDao`

**Files:**
- Create: `src/main/java/rw/ac/auca/tapwallet/model/Wallet.java`
- Modify: `src/main/resources/hibernate.cfg.xml` (add `Wallet` mapping)
- Create: `src/main/java/rw/ac/auca/tapwallet/dao/WalletDao.java`
- Test: `src/test/java/rw/ac/auca/tapwallet/dao/WalletDaoTest.java`

**Interfaces:**
- Consumes: `User` entity + `UserDao.save` (Task 2).
- Produces: `Wallet` (fields: `id: Long`, `owner: User`, `balance: BigDecimal`, `currency: String`, `status: String`, plus inherited `createdAt`/`updatedAt`), `WalletDao.save(Wallet) -> Wallet`, `WalletDao.findAll() -> List<Wallet>`, `WalletDao.findById(Long) -> Wallet`, `WalletDao.delete(Long) -> void`. Later tasks (`WalletBean`) depend on these exact names.

- [ ] **Step 1: Write the failing `WalletDaoTest`**

```java
package rw.ac.auca.tapwallet.dao;

import org.junit.Test;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.model.Wallet;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

public class WalletDaoTest {

    private final UserDao userDao = new UserDao();
    private final WalletDao walletDao = new WalletDao();

    private User newOwner(String label) {
        User owner = new User(label, label.toLowerCase() + "-" + System.nanoTime() + "@example.com",
                "0788123456", "hash", "CUSTOMER", "ACTIVE");
        userDao.save(owner);
        return owner;
    }

    @Test
    public void savingAWalletAssignsAnId() {
        Wallet wallet = new Wallet(newOwner("Wallet Owner 1"), new BigDecimal("1000.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);
        assertNotNull(wallet.getId());
    }

    @Test
    public void findByIdReturnsTheOwnerToo() {
        User owner = newOwner("Wallet Owner 2");
        Wallet wallet = new Wallet(owner, new BigDecimal("500.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);

        Wallet found = walletDao.findById(wallet.getId());
        assertNotNull(found);
        assertEquals(owner.getId(), found.getOwner().getId());
        assertEquals(0, new BigDecimal("500.00").compareTo(found.getBalance()));
    }

    @Test
    public void savingWithAnExistingIdUpdatesTheBalance() {
        Wallet wallet = new Wallet(newOwner("Wallet Owner 3"), new BigDecimal("100.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);

        wallet.setBalance(new BigDecimal("250.00"));
        walletDao.save(wallet);

        Wallet found = walletDao.findById(wallet.getId());
        assertEquals(0, new BigDecimal("250.00").compareTo(found.getBalance()));
    }

    @Test
    public void deleteRemovesTheWallet() {
        Wallet wallet = new Wallet(newOwner("Wallet Owner 4"), new BigDecimal("10.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);
        Long id = wallet.getId();

        walletDao.delete(id);

        assertNull(walletDao.findById(id));
    }

    @Test
    public void findAllIncludesASavedWallet() {
        Wallet wallet = new Wallet(newOwner("Wallet Owner 5"), new BigDecimal("10.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);

        List<Wallet> all = walletDao.findAll();
        assertTrue(all.stream().anyMatch(w -> w.getId().equals(wallet.getId())));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=WalletDaoTest test`
Expected: FAIL (compile error — `Wallet`, `WalletDao` do not exist)

- [ ] **Step 3: Create `Wallet`**

```java
package rw.ac.auca.tapwallet.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * The Class Wallet.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@Entity
@Table(name = "wallet")
public class Wallet extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long id;

    @NotNull(message = "Owner is required")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false)
    private String currency = "RWF";

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    public Wallet() {
    }

    public Wallet(User owner, BigDecimal balance, String currency, String status) {
        this.owner = owner;
        this.balance = balance;
        this.currency = currency;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
```

- [ ] **Step 4: Add the `Wallet` mapping to `hibernate.cfg.xml`**

In `src/main/resources/hibernate.cfg.xml`, change:

```xml
        <!--      Entity mapping-->
        <mapping class="rw.ac.auca.tapwallet.model.User"/>

    </session-factory>
```

to:

```xml
        <!--      Entity mapping-->
        <mapping class="rw.ac.auca.tapwallet.model.User"/>
        <mapping class="rw.ac.auca.tapwallet.model.Wallet"/>

    </session-factory>
```

- [ ] **Step 5: Create `WalletDao`**

```java
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
```

- [ ] **Step 6: Run test to verify it passes**

Run: `mvn -q -Dtest=WalletDaoTest test`
Expected: PASS (5 tests)

- [ ] **Step 7: Commit**

```bash
git add src/main/java/rw/ac/auca/tapwallet/model/Wallet.java src/main/resources/hibernate.cfg.xml src/main/java/rw/ac/auca/tapwallet/dao/WalletDao.java src/test/java/rw/ac/auca/tapwallet/dao/WalletDaoTest.java
git commit -m "feat: Wallet entity (owned by User) + WalletDao CRUD"
```

---

### Task 4: Custom validator — `PhoneValidator`

**Files:**
- Create: `src/main/java/rw/ac/auca/tapwallet/util/PhoneValidator.java`
- Test: `src/test/java/rw/ac/auca/tapwallet/util/PhoneValidatorTest.java`

**Interfaces:**
- Produces: `PhoneValidator.isValidPhone(String) -> boolean` (static, pure), registered JSF validator id `"phoneValidator"`. Task 7's `user-form.xhtml` attaches it via `<f:validator validatorId="phoneValidator"/>`.

- [ ] **Step 1: Write the failing `PhoneValidatorTest`**

```java
package rw.ac.auca.tapwallet.util;

import org.junit.Test;

import javax.faces.validator.ValidatorException;

import static org.junit.Assert.*;

public class PhoneValidatorTest {

    @Test
    public void acceptsAValidMtnNumber() {
        assertTrue(PhoneValidator.isValidPhone("0788123456"));
    }

    @Test
    public void acceptsAValidAirtelNumber() {
        assertTrue(PhoneValidator.isValidPhone("0738123456"));
    }

    @Test
    public void rejectsATooShortNumber() {
        assertFalse(PhoneValidator.isValidPhone("07812345"));
    }

    @Test
    public void rejectsANonRwandanPrefix() {
        assertFalse(PhoneValidator.isValidPhone("0612345678"));
    }

    @Test
    public void rejectsNull() {
        assertFalse(PhoneValidator.isValidPhone(null));
    }

    @Test
    public void validateThrowsForAnInvalidNumber() {
        PhoneValidator validator = new PhoneValidator();
        try {
            validator.validate(null, null, "12345");
            fail("Expected a ValidatorException");
        } catch (ValidatorException ex) {
            assertNotNull(ex.getFacesMessage());
        }
    }

    @Test
    public void validateDoesNotThrowForAValidNumber() {
        PhoneValidator validator = new PhoneValidator();
        validator.validate(null, null, "0788123456");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=PhoneValidatorTest test`
Expected: FAIL (compile error — `PhoneValidator` does not exist)

- [ ] **Step 3: Implement `PhoneValidator`**

```java
package rw.ac.auca.tapwallet.util;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.regex.Pattern;

/**
 * The Class PhoneValidator.
 *
 * Custom JSF validator (validation type 2 of 3) that checks a phone
 * number matches the Rwandan MTN/Airtel mobile format: 07[2389]xxxxxxx.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@FacesValidator("phoneValidator")
public class PhoneValidator implements Validator {

    private static final Pattern RWANDAN_PHONE = Pattern.compile("^07[2389]\\d{7}$");

    public static boolean isValidPhone(String phone) {
        return phone != null && RWANDAN_PHONE.matcher(phone).matches();
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String phone = value == null ? "" : value.toString();
        if (!isValidPhone(phone)) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Invalid phone number", "Phone number must look like 0788123456 (MTN/Airtel format).");
            throw new ValidatorException(message);
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=PhoneValidatorTest test`
Expected: PASS (7 tests)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/rw/ac/auca/tapwallet/util/PhoneValidator.java src/test/java/rw/ac/auca/tapwallet/util/PhoneValidatorTest.java
git commit -m "feat: custom PhoneValidator (validation type 2/3)"
```

---

### Task 5: Bean Validation coverage test

**Files:**
- Test: `src/test/java/rw/ac/auca/tapwallet/model/UserValidationTest.java`
- Test: `src/test/java/rw/ac/auca/tapwallet/model/WalletValidationTest.java`

**Interfaces:**
- Consumes: `User` (Task 2), `Wallet` (Task 3) — their JSR-303 annotations were already added when those classes were created. This task only proves they fire correctly.

- [ ] **Step 1: Write `UserValidationTest`**

```java
package rw.ac.auca.tapwallet.model;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.*;

public class UserValidationTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @Test
    public void aFullyPopulatedUserHasNoViolations() {
        User user = new User("Pacifique Bakundukize", "pacifique@example.com", "0788123456", "hash", "CUSTOMER", "ACTIVE");
        Set<ConstraintViolation<User>> violations = VALIDATOR.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void aBlankFullNameIsRejected() {
        User user = new User("", "pacifique@example.com", "0788123456", "hash", "CUSTOMER", "ACTIVE");
        Set<ConstraintViolation<User>> violations = VALIDATOR.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void anInvalidEmailIsRejected() {
        User user = new User("Pacifique Bakundukize", "not-an-email", "0788123456", "hash", "CUSTOMER", "ACTIVE");
        Set<ConstraintViolation<User>> violations = VALIDATOR.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void aBlankPasswordHashIsRejected() {
        User user = new User("Pacifique Bakundukize", "pacifique@example.com", "0788123456", "", "CUSTOMER", "ACTIVE");
        Set<ConstraintViolation<User>> violations = VALIDATOR.validate(user);
        assertFalse(violations.isEmpty());
    }
}
```

- [ ] **Step 2: Write `WalletValidationTest`**

```java
package rw.ac.auca.tapwallet.model;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;

import static org.junit.Assert.*;

public class WalletValidationTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    private User anyOwner() {
        return new User("Owner", "owner@example.com", "0788123456", "hash", "CUSTOMER", "ACTIVE");
    }

    @Test
    public void aFullyPopulatedWalletHasNoViolations() {
        Wallet wallet = new Wallet(anyOwner(), new BigDecimal("1000.00"), "RWF", "ACTIVE");
        Set<ConstraintViolation<Wallet>> violations = VALIDATOR.validate(wallet);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void aNegativeBalanceIsRejected() {
        Wallet wallet = new Wallet(anyOwner(), new BigDecimal("-5.00"), "RWF", "ACTIVE");
        Set<ConstraintViolation<Wallet>> violations = VALIDATOR.validate(wallet);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void aMissingOwnerIsRejected() {
        Wallet wallet = new Wallet(null, new BigDecimal("10.00"), "RWF", "ACTIVE");
        Set<ConstraintViolation<Wallet>> violations = VALIDATOR.validate(wallet);
        assertFalse(violations.isEmpty());
    }
}
```

- [ ] **Step 3: Run tests to verify they pass**

Run: `mvn -q -Dtest=UserValidationTest,WalletValidationTest test`
Expected: PASS (7 tests) — this proves validation type 3/3 (Bean Validation) works.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/rw/ac/auca/tapwallet/model/UserValidationTest.java src/test/java/rw/ac/auca/tapwallet/model/WalletValidationTest.java
git commit -m "test: prove Bean Validation constraints fire (validation type 3/3)"
```

---

### Task 6: `UserBean` + `WalletBean`

**Files:**
- Create: `src/main/java/rw/ac/auca/tapwallet/UserBean.java`
- Create: `src/main/java/rw/ac/auca/tapwallet/WalletBean.java`
- Test: `src/test/java/rw/ac/auca/tapwallet/UserBeanTest.java`
- Test: `src/test/java/rw/ac/auca/tapwallet/WalletBeanTest.java`

**Interfaces:**
- Consumes: `UserDao`/`WalletDao` (Tasks 2-3), `PasswordUtil.hash` (Task 1).
- Produces: `UserBean` properties `id/fullName/email/phone/password/role/status` + methods `save() -> String`, `loadForEdit() -> void`, `delete(Long) -> String`, `getAllUsers() -> List<User>`. `WalletBean` properties `id/ownerId/balance/currency/status` + methods `save() -> String`, `loadForEdit() -> void`, `delete(Long) -> String`, `getAllWallets() -> List<Wallet>`, `getAllUsersForDropdown() -> List<User>`. Task 7's `.xhtml` pages bind to these exact property/method names.

- [ ] **Step 1: Write the failing `UserBeanTest`**

```java
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
        bean.setRole("CUSTOMER");
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
                "0788123456", PasswordUtil.hash("OriginalPassword"), "CUSTOMER", "ACTIVE");
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
                "0788123456", PasswordUtil.hash("pw"), "CUSTOMER", "ACTIVE");
        userDao.save(user);

        UserBean bean = new UserBean();
        String outcome = bean.delete(user.getId());

        assertEquals("user-list?faces-redirect=true", outcome);
        assertNull(userDao.findById(user.getId()));
    }
}
```

- [ ] **Step 2: Write the failing `WalletBeanTest`**

```java
package rw.ac.auca.tapwallet;

import org.junit.Test;
import rw.ac.auca.tapwallet.dao.UserDao;
import rw.ac.auca.tapwallet.dao.WalletDao;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.model.Wallet;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class WalletBeanTest {

    private final UserDao userDao = new UserDao();
    private final WalletDao walletDao = new WalletDao();

    private User newOwner(String label) {
        User owner = new User(label, label.toLowerCase() + "-" + System.nanoTime() + "@example.com",
                "0788123456", "hash", "CUSTOMER", "ACTIVE");
        userDao.save(owner);
        return owner;
    }

    @Test
    public void savingANewWalletLinksItToTheChosenOwner() {
        User owner = newOwner("Bean Owner 1");

        WalletBean bean = new WalletBean();
        bean.setOwnerId(owner.getId());
        bean.setBalance(new BigDecimal("2000.00"));
        bean.setCurrency("RWF");
        bean.setStatus("ACTIVE");

        String outcome = bean.save();

        assertEquals("wallet-list?faces-redirect=true", outcome);
        Wallet saved = walletDao.findAll().stream()
                .filter(w -> w.getOwner().getId().equals(owner.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(saved);
        assertEquals(0, new BigDecimal("2000.00").compareTo(saved.getBalance()));
    }

    @Test
    public void editingAWalletUpdatesTheBalance() {
        User owner = newOwner("Bean Owner 2");
        Wallet wallet = new Wallet(owner, new BigDecimal("100.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);

        WalletBean bean = new WalletBean();
        bean.setId(wallet.getId());
        bean.loadForEdit();
        bean.setBalance(new BigDecimal("300.00"));

        bean.save();

        Wallet reloaded = walletDao.findById(wallet.getId());
        assertEquals(0, new BigDecimal("300.00").compareTo(reloaded.getBalance()));
    }

    @Test
    public void deleteRemovesTheWallet() {
        Wallet wallet = new Wallet(newOwner("Bean Owner 3"), new BigDecimal("10.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);

        WalletBean bean = new WalletBean();
        String outcome = bean.delete(wallet.getId());

        assertEquals("wallet-list?faces-redirect=true", outcome);
        assertNull(walletDao.findById(wallet.getId()));
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**

Run: `mvn -q -Dtest=UserBeanTest,WalletBeanTest test`
Expected: FAIL (compile error — `UserBean`, `WalletBean` do not exist)

- [ ] **Step 4: Implement `UserBean`**

```java
package rw.ac.auca.tapwallet;

import rw.ac.auca.tapwallet.dao.UserDao;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.util.PasswordUtil;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.util.List;

/**
 * The Class UserBean.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@ManagedBean
public class UserBean {

    private UserDao userDao = new UserDao();

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String role = "CUSTOMER";
    private String status = "ACTIVE";

    public String save() {
        User user;
        if (id != null) {
            user = userDao.findById(id);
            if (user == null) {
                return "user-list?faces-redirect=true";
            }
        } else {
            user = new User();
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(status);

        if (password != null && !password.trim().isEmpty()) {
            user.setPasswordHash(PasswordUtil.hash(password));
        }

        try {
            userDao.save(user);
        } catch (RuntimeException ex) {
            // Never leak a stack trace to the page — show a friendly message and stay put.
            if (FacesContext.getCurrentInstance() != null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Could not save user", "Please check the values (e.g. email must be unique) and try again."));
            }
            return null;
        }
        return "user-list?faces-redirect=true";
    }

    public void loadForEdit() {
        if (id != null) {
            User user = userDao.findById(id);
            if (user != null) {
                fullName = user.getFullName();
                email = user.getEmail();
                phone = user.getPhone();
                role = user.getRole();
                status = user.getStatus();
            }
        }
    }

    public String delete(Long userId) {
        userDao.delete(userId);
        return "user-list?faces-redirect=true";
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
```

- [ ] **Step 5: Implement `WalletBean`**

```java
package rw.ac.auca.tapwallet;

import rw.ac.auca.tapwallet.dao.UserDao;
import rw.ac.auca.tapwallet.dao.WalletDao;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.model.Wallet;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.math.BigDecimal;
import java.util.List;

/**
 * The Class WalletBean.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@ManagedBean
public class WalletBean {

    private WalletDao walletDao = new WalletDao();
    private UserDao userDao = new UserDao();

    private Long id;
    private Long ownerId;
    private BigDecimal balance = BigDecimal.ZERO;
    private String currency = "RWF";
    private String status = "ACTIVE";

    public String save() {
        Wallet wallet;
        if (id != null) {
            wallet = walletDao.findById(id);
            if (wallet == null) {
                return "wallet-list?faces-redirect=true";
            }
        } else {
            wallet = new Wallet();
        }

        User owner = userDao.findById(ownerId);
        wallet.setOwner(owner);
        wallet.setBalance(balance);
        wallet.setCurrency(currency);
        wallet.setStatus(status);

        try {
            walletDao.save(wallet);
        } catch (RuntimeException ex) {
            // Never leak a stack trace to the page — show a friendly message and stay put.
            if (FacesContext.getCurrentInstance() != null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Could not save wallet", "Please check the values and try again."));
            }
            return null;
        }
        return "wallet-list?faces-redirect=true";
    }

    public void loadForEdit() {
        if (id != null) {
            Wallet wallet = walletDao.findById(id);
            if (wallet != null) {
                ownerId = wallet.getOwner().getId();
                balance = wallet.getBalance();
                currency = wallet.getCurrency();
                status = wallet.getStatus();
            }
        }
    }

    public String delete(Long walletId) {
        walletDao.delete(walletId);
        return "wallet-list?faces-redirect=true";
    }

    public List<Wallet> getAllWallets() {
        return walletDao.findAll();
    }

    public List<User> getAllUsersForDropdown() {
        return userDao.findAll();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `mvn -q -Dtest=UserBeanTest,WalletBeanTest test`
Expected: PASS (6 tests)

- [ ] **Step 7: Commit**

```bash
git add src/main/java/rw/ac/auca/tapwallet/UserBean.java src/main/java/rw/ac/auca/tapwallet/WalletBean.java src/test/java/rw/ac/auca/tapwallet/UserBeanTest.java src/test/java/rw/ac/auca/tapwallet/WalletBeanTest.java
git commit -m "feat: UserBean + WalletBean (create/edit/delete/list logic)"
```

---

### Task 7: JSF views, app config, and CSS (3 inclusion styles)

**Files:**
- Create: `src/main/webapp/WEB-INF/web.xml`
- Create: `src/main/webapp/WEB-INF/faces-config.xml`
- Create: `src/main/webapp/WEB-INF/beans.xml`
- Create: `src/main/webapp/resources/css/styles.css`
- Create: `src/main/webapp/index.xhtml`
- Create: `src/main/webapp/user-list.xhtml`
- Create: `src/main/webapp/user-form.xhtml`
- Create: `src/main/webapp/wallet-list.xhtml`
- Create: `src/main/webapp/wallet-form.xhtml`

**Interfaces:**
- Consumes: `#{userBean.*}` / `#{walletBean.*}` EL expressions must match `UserBean`/`WalletBean` (Task 6) property and method names exactly; `validatorId="phoneValidator"` must match `@FacesValidator("phoneValidator")` (Task 4).
- Produces: nothing consumed by later tasks — this is the outermost layer.

- [ ] **Step 1: Create `web.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
    <servlet>
        <servlet-name>Faces Servlet</servlet-name>
        <servlet-class>javax.faces.webapp.FacesServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>Faces Servlet</servlet-name>
        <url-pattern>*.xhtml</url-pattern>
    </servlet-mapping>

    <welcome-file-list>
        <welcome-file>index.xhtml</welcome-file>
    </welcome-file-list>
</web-app>
```

- [ ] **Step 2: Create `faces-config.xml`**

```xml
<?xml version='1.0' encoding='UTF-8'?>
<faces-config version="2.2" xmlns="http://xmlns.jcp.org/xml/ns/javaee"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                                  http://xmlns.jcp.org/xml/ns/javaee/web-facesconfig_2_2.xsd">

</faces-config>
```

- [ ] **Step 3: Create `beans.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://xmlns.jcp.org/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_1_1.xsd"
       bean-discovery-mode="all">
</beans>
```

- [ ] **Step 4: Create the external stylesheet `resources/css/styles.css`** (CSS type 1/3 — external)

```css
body {
    font-family: Arial, Helvetica, sans-serif;
    background-color: #f4f6f8;
    color: #1f2933;
    margin: 0;
    padding: 0;
}

h1 {
    background-color: #123456;
    color: #ffffff;
    padding: 16px 24px;
    margin: 0 0 20px 0;
}

.nav {
    padding: 0 24px;
    margin-bottom: 20px;
}

.nav a {
    margin-right: 16px;
    color: #123456;
    font-weight: bold;
    text-decoration: none;
}

.nav a:hover {
    text-decoration: underline;
}

.form-container, .table-container {
    margin: 0 24px 24px 24px;
    background-color: #ffffff;
    padding: 20px;
    border-radius: 6px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}

table {
    border-collapse: collapse;
    width: 100%;
}

table th {
    background-color: #123456;
    color: #ffffff;
    text-align: left;
    padding: 8px 12px;
}

table td {
    padding: 8px 12px;
    border-bottom: 1px solid #e0e0e0;
}

.btn-link {
    color: #123456;
    font-weight: bold;
    text-decoration: none;
}

.btn-danger {
    color: #b00020;
    font-weight: bold;
    text-decoration: none;
}
```

- [ ] **Step 5: Create `index.xhtml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core">
<h:head>
    <title>TapWallet</title>
    <h:outputStylesheet library="css" name="styles.css"/>
</h:head>
<h:body>
    <f:view>
        <h1>TapWallet - NFC Digital Payment &amp; Wallet System</h1>
        <div class="nav">
            <h:link outcome="user-list" value="Manage Users"/>
            <h:link outcome="wallet-list" value="Manage Wallets"/>
        </div>
    </f:view>
</h:body>
</html>
```

- [ ] **Step 6: Create `user-list.xhtml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core">
<h:head>
    <title>TapWallet - Users</title>
    <h:outputStylesheet library="css" name="styles.css"/>
</h:head>
<h:body>
    <f:view>
        <h1>Users</h1>
        <div class="nav">
            <h:link outcome="index" value="Home"/>
            <h:link outcome="wallet-list" value="Manage Wallets"/>
            <h:link outcome="user-form" value="Add New User"/>
        </div>
        <div class="table-container">
            <h:form>
                <h:dataTable value="#{userBean.allUsers}" var="u" border="1">
                    <h:column>
                        <f:facet name="header">Full Name</f:facet>
                        #{u.fullName}
                    </h:column>
                    <h:column>
                        <f:facet name="header">Email</f:facet>
                        #{u.email}
                    </h:column>
                    <h:column>
                        <f:facet name="header">Phone</f:facet>
                        #{u.phone}
                    </h:column>
                    <h:column>
                        <f:facet name="header">Role</f:facet>
                        #{u.role}
                    </h:column>
                    <h:column>
                        <f:facet name="header">Status</f:facet>
                        <span style="font-weight:bold">#{u.status}</span>
                    </h:column>
                    <h:column>
                        <f:facet name="header">Actions</f:facet>
                        <h:link outcome="user-form" value="Edit" styleClass="btn-link">
                            <f:param name="id" value="#{u.id}"/>
                        </h:link>
                        &#160;|&#160;
                        <h:commandLink value="Delete" action="#{userBean.delete(u.id)}" styleClass="btn-danger"/>
                    </h:column>
                </h:dataTable>
            </h:form>
        </div>
    </f:view>
</h:body>
</html>
```

- [ ] **Step 7: Create `user-form.xhtml`** (validation types 1 and 2 of 3 live here, plus type 3 fires automatically)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core">
<h:head>
    <title>TapWallet - User Form</title>
    <h:outputStylesheet library="css" name="styles.css"/>
</h:head>
<h:body>
    <f:view>
        <f:metadata>
            <f:viewParam name="id" value="#{userBean.id}"/>
            <f:event type="preRenderView" listener="#{userBean.loadForEdit}"/>
        </f:metadata>

        <h1>#{userBean.id == null ? 'Register User' : 'Edit User'}</h1>
        <div class="nav">
            <h:link outcome="user-list" value="Back to Users"/>
        </div>

        <div class="form-container">
            <h:form>
                <h:panelGrid columns="3">
                    <h:outputLabel value="Full Name" for="fullName"/>
                    <h:inputText id="fullName" value="#{userBean.fullName}" required="true" label="Full Name">
                        <f:validateLength minimum="3" maximum="50"/>
                    </h:inputText>
                    <h:message for="fullName" style="color : red"/>

                    <h:outputLabel value="Email" for="email"/>
                    <h:inputText id="email" value="#{userBean.email}" required="true" label="Email"/>
                    <h:message for="email" style="color : red"/>

                    <h:outputLabel value="Phone (07XXXXXXXX)" for="phone"/>
                    <h:inputText id="phone" value="#{userBean.phone}" required="true" label="Phone" validatorId="phoneValidator"/>
                    <h:message for="phone" style="color : red"/>

                    <h:outputLabel value="Password" for="password"/>
                    <h:inputSecret id="password" value="#{userBean.password}" required="#{userBean.id == null}" label="Password"/>
                    <h:message for="password" style="color : red"/>

                    <h:outputLabel value="Role" for="role"/>
                    <h:selectOneMenu id="role" value="#{userBean.role}">
                        <f:selectItem itemValue="CUSTOMER" itemLabel="Customer"/>
                        <f:selectItem itemValue="MERCHANT" itemLabel="Merchant"/>
                        <f:selectItem itemValue="ADMIN" itemLabel="Admin"/>
                    </h:selectOneMenu>
                    <h:message for="role" style="color : red"/>

                    <h:outputLabel value="Status" for="status"/>
                    <h:selectOneMenu id="status" value="#{userBean.status}">
                        <f:selectItem itemValue="ACTIVE" itemLabel="Active"/>
                        <f:selectItem itemValue="FROZEN" itemLabel="Frozen"/>
                    </h:selectOneMenu>
                    <h:message for="status" style="color : red"/>

                    <h:commandButton value="Save" action="#{userBean.save()}"/>
                </h:panelGrid>
            </h:form>
        </div>
    </f:view>
</h:body>
</html>
```

Note: `phone`'s `validatorId="phoneValidator"` is the **custom validator** (type 2/3); `f:validateLength` on `fullName` is a **standard validator** (type 1/3); the `@NotBlank`/`@Email`/`@Size` annotations on `User` are **Bean Validation** (type 3/3) and fire automatically on every field with no extra tag needed.

- [ ] **Step 8: Create `wallet-list.xhtml`** (CSS type 2/3 — internal `<style>` block)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core">
<h:head>
    <title>TapWallet - Wallets</title>
    <h:outputStylesheet library="css" name="styles.css"/>
    <style type="text/css">
        .badge-active {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 10px;
            background-color: #d4edda;
            color: #155724;
            font-size: 12px;
            font-weight: bold;
        }
        .badge-frozen {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 10px;
            background-color: #f8d7da;
            color: #721c24;
            font-size: 12px;
            font-weight: bold;
        }
    </style>
</h:head>
<h:body>
    <f:view>
        <h1>Wallets</h1>
        <div class="nav">
            <h:link outcome="index" value="Home"/>
            <h:link outcome="user-list" value="Manage Users"/>
            <h:link outcome="wallet-form" value="Add New Wallet"/>
        </div>
        <div class="table-container">
            <h:form>
                <h:dataTable value="#{walletBean.allWallets}" var="w" border="1">
                    <h:column>
                        <f:facet name="header">Owner</f:facet>
                        #{w.owner.fullName}
                    </h:column>
                    <h:column>
                        <f:facet name="header">Balance</f:facet>
                        #{w.balance}
                    </h:column>
                    <h:column>
                        <f:facet name="header">Currency</f:facet>
                        #{w.currency}
                    </h:column>
                    <h:column>
                        <f:facet name="header">Status</f:facet>
                        <span class="#{w.status == 'ACTIVE' ? 'badge-active' : 'badge-frozen'}">#{w.status}</span>
                    </h:column>
                    <h:column>
                        <f:facet name="header">Actions</f:facet>
                        <h:link outcome="wallet-form" value="Edit" styleClass="btn-link">
                            <f:param name="id" value="#{w.id}"/>
                        </h:link>
                        &#160;|&#160;
                        <h:commandLink value="Delete" action="#{walletBean.delete(w.id)}" styleClass="btn-danger"/>
                    </h:column>
                </h:dataTable>
            </h:form>
        </div>
    </f:view>
</h:body>
</html>
```

- [ ] **Step 9: Create `wallet-form.xhtml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core">
<h:head>
    <title>TapWallet - Wallet Form</title>
    <h:outputStylesheet library="css" name="styles.css"/>
</h:head>
<h:body>
    <f:view>
        <f:metadata>
            <f:viewParam name="id" value="#{walletBean.id}"/>
            <f:event type="preRenderView" listener="#{walletBean.loadForEdit}"/>
        </f:metadata>

        <h1>#{walletBean.id == null ? 'Create Wallet' : 'Edit Wallet'}</h1>
        <div class="nav">
            <h:link outcome="wallet-list" value="Back to Wallets"/>
        </div>

        <div class="form-container">
            <h:form>
                <h:panelGrid columns="3">
                    <h:outputLabel value="Owner" for="owner"/>
                    <h:selectOneMenu id="owner" value="#{walletBean.ownerId}" required="true" label="Owner">
                        <f:selectItems value="#{walletBean.allUsersForDropdown}" var="u" itemValue="#{u.id}" itemLabel="#{u.fullName}"/>
                    </h:selectOneMenu>
                    <h:message for="owner" style="color : red"/>

                    <h:outputLabel value="Balance" for="balance"/>
                    <h:inputText id="balance" value="#{walletBean.balance}" required="true" label="Balance">
                        <f:validateDoubleRange minimum="0.0"/>
                    </h:inputText>
                    <h:message for="balance" style="color : red"/>

                    <h:outputLabel value="Currency" for="currency"/>
                    <h:inputText id="currency" value="#{walletBean.currency}" required="true" label="Currency">
                        <f:validateLength minimum="3" maximum="3"/>
                    </h:inputText>
                    <h:message for="currency" style="color : red"/>

                    <h:outputLabel value="Status" for="status"/>
                    <h:selectOneMenu id="status" value="#{walletBean.status}">
                        <f:selectItem itemValue="ACTIVE" itemLabel="Active"/>
                        <f:selectItem itemValue="FROZEN" itemLabel="Frozen"/>
                    </h:selectOneMenu>
                    <h:message for="status" style="color : red"/>

                    <h:commandButton value="Save" action="#{walletBean.save()}"/>
                </h:panelGrid>
            </h:form>
        </div>
    </f:view>
</h:body>
</html>
```

- [ ] **Step 10: Verify the whole project compiles and packages**

Run: `mvn -q clean package`
Expected: `BUILD SUCCESS`, and `target/TapWallet-1.0-SNAPSHOT.war` exists.

- [ ] **Step 11: Manually cross-check EL bindings**

Grep every `#{userBean.` / `#{walletBean.` / `#{u.` / `#{w.` expression used above against the getters/setters/methods defined in `UserBean`, `WalletBean`, `User`, `Wallet` (Tasks 2, 3, 6) — confirm every property (`fullName`, `email`, `phone`, `password`, `role`, `status`, `id`, `ownerId`, `balance`, `currency`, `allUsers`, `allWallets`, `allUsersForDropdown`) and method (`save`, `delete`, `loadForEdit`) referenced in the views exists with a matching name. This is the substitute for a full container-based integration test.

- [ ] **Step 12: Commit**

```bash
git add src/main/webapp
git commit -m "feat: JSF views for User/Wallet CRUD, app config, and 3-style CSS"
```

---

### Task 8: README + final verification

**Files:**
- Create: `README.md`

**Interfaces:**
- Consumes: nothing new — this documents the whole project.

- [ ] **Step 1: Create `README.md`**

```markdown
# TapWallet — NFC Digital Payment & Wallet System

A web-based digital wallet and NFC payment concept, implemented for
Web Technology Assignment 3 as full CRUD (Create/Read/Update/Delete) on
two entities — **User** and **Wallet** — using JSF 2.3 and Hibernate 5,
backed by an **H2 in-memory database** (no external DB server needed).

**Author:** Pacifique Bakundukize (26798)

## Tech stack

- Java 8, Maven (`war` packaging)
- JSF 2.3.9 (org.glassfish) + Weld (CDI, via `weld-servlet-shaded`)
- Hibernate ORM 5.6.15.Final
- H2 2.2.224, in-memory (`jdbc:h2:mem:tapwallet`)
- Hibernate Validator 6.2.5.Final (JSR-303 Bean Validation)
- JUnit 4.13.2

## Running the tests

The DAO, bean, and validation layers are covered by JUnit tests that run
directly against the H2 in-memory database — no servlet container needed:

```bash
mvn test
```

## Building the WAR

```bash
mvn clean package
```

This produces `target/TapWallet-1.0-SNAPSHOT.war`.

## Deploying

Deploy the WAR to any Servlet 4.0 container, e.g. Apache Tomcat 9:

1. Copy `target/TapWallet-1.0-SNAPSHOT.war` into `$CATALINA_HOME/webapps/`.
2. Start Tomcat.
3. Open `http://localhost:8080/TapWallet-1.0-SNAPSHOT/`.

The H2 database is created fresh, in memory, each time the app starts —
nothing to install or configure.

## What's implemented

- Full CRUD for **User** (full name, email, phone, hashed password, role,
  status) and **Wallet** (owner, balance, currency, status).
- All 3 required validation types: standard JSF validators
  (`f:validateLength`, `f:validateDoubleRange`), a custom validator
  (`PhoneValidator`), and Bean Validation (JSR-303 annotations).
- All 3 CSS inclusion styles: external stylesheet, one internal
  `<style>` block, and inline `style=` attributes.
- Salted password hashing, parameterized HQL, and server-side-authoritative
  validation.

## What's documented but not implemented

The rest of the TapWallet domain (NFC cards, merchants, transactions,
top-ups/withdrawals) is described in the Phase-1 project documentation as
the intended full system; this repository implements the CRUD slice
required by Assignment 3, req. #2.
```

- [ ] **Step 2: Run the full test suite one more time**

Run: `mvn -q clean test`
Expected: `BUILD SUCCESS`, all tests (PasswordUtilTest, UserDaoTest, WalletDaoTest, PhoneValidatorTest, UserValidationTest, WalletValidationTest, UserBeanTest, WalletBeanTest) pass.

- [ ] **Step 3: Commit**

```bash
git add README.md
git commit -m "docs: add project README (build, test, deploy instructions)"
```

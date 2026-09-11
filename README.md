# TapWallet — Digital Wallet & Merchant Payments

A web-based digital wallet built for Web Technology Assignment 3, using
**JSF 2.3 + Hibernate 5** on an **H2 in-memory database**, organised with a
**Domain-Driven Design** layered architecture and role-based access control.

**Author:** Pacifique Bakundukize (26798)
**Repository:** https://github.com/pac-cee/TapWallet-webtech-AIC
**Video walkthrough:** https://drive.google.com/file/d/1lZD1U0aY5mWQ_xdpl7Zg-5t4imuNkclN/view?usp=sharing

## Run it

```bash
mvn clean package
cp target/TapWallet-1.0-SNAPSHOT.war <tomcat9>/webapps/
<tomcat9>/bin/startup.sh          # JAVA_HOME must point at a JDK
```

Then open <http://localhost:8080/TapWallet-1.0-SNAPSHOT/> and sign in.
**Tomcat 9** specifically — Tomcat 10+ moved to the `jakarta.*` namespace,
which this project does not use. The database is rebuilt in memory on every
start and seeded with the accounts below.

### Demo accounts

| Role | Email | Password |
|---|---|---|
| Administrator | admin@tapwallet.rw | `Admin@123` |
| Customer | alice@tapwallet.rw | `Pass@123` |
| Customer | eric@tapwallet.rw | `Pass@123` |
| Merchant | cafe@tapwallet.rw | `Pass@123` |
| Merchant | shop@tapwallet.rw | `Pass@123` |

## What each role can do

| Role | Can | Cannot |
|---|---|---|
| **Administrator** | Full CRUD on users and merchants, see every payment, reverse payments, freeze accounts | Reach customer or merchant screens |
| **Customer** | See and top up their own wallet, pay any active merchant, read their own payment history | See another customer's wallet or payments |
| **Merchant** | See their own shop, its revenue, and payments received with payer names | See another shop's takings |

Access is enforced **twice**: `SecurityFilter` rejects the request by URL
prefix before a page renders, and every query is keyed on the user id held in
the session rather than on an id from the browser. Hiding a menu link is not
treated as security.

## Architecture (DDD, four layers)

```
domain/          aggregates, value objects, repository interfaces, domain service
  model/user     User (aggregate root), Role, EmailAddress, PhoneNumber
  model/wallet   Wallet — credit()/debit() own the balance invariants
  model/merchant Merchant, MerchantCode
  model/payment  Payment, PaymentStatus
  model/shared   Money, Currency, AccountStatus, AuditableEntity
  repository/    UserRepository, WalletRepository, MerchantRepository, PaymentRepository
  service/       PaymentDomainService — the rule that spans Wallet and Merchant
  exception/     DomainException, InsufficientFunds, InactiveAccount, SelfPayment

application/     one class per actor's use cases, each = one Unit of Work
infrastructure/  Hibernate repositories, SessionFactoryProvider, UnitOfWork,
                 PasswordHasher strategy, ServiceRegistry, DataSeeder
presentation/    JSF beans, SecurityFilter, PhoneValidator, Facelets template
```

Dependencies point inwards only — the domain knows nothing about Hibernate or
JSF, which is why its rules are unit-testable without a server.

### Design patterns

Repository · Aggregate Root · Value Object · Domain Service · Application
Service · Unit of Work · Singleton (`SessionFactoryProvider`) · Factory Method
(`User.register()`, `Wallet.openFor()`, …) · Factory/Composition Root
(`ServiceRegistry`) · Strategy (`PasswordHasher`) · MVC · Composite View
(Facelets template) · Front Controller (`SecurityFilter`).

## Tests

```bash
mvn test
```

**56 tests.** Domain rules run as plain objects (no database); application
services run against a real H2 database, including rollback behaviour and the
cross-role data-isolation rules.

## Assignment requirements

- **Full CRUD on two entities** — Users and Merchants, both administered end to end.
- **Three validation types** — standard JSF validators (`f:validateLength`,
  `f:validateDoubleRange`, `f:validateRegex`), a custom validator
  (`PhoneValidator`), and Bean Validation (JSR-303 annotations on entities and
  value objects). The domain constructors validate a third time, so an invalid
  object cannot exist even if a screen forgets.
- **Three CSS inclusion styles** — external (`resources/css/styles.css`),
  internal (`<style>` block in `customer/wallet.xhtml`), and inline `style=`
  attributes on messages and figures.

## Documentation

Full Phase-1 documentation (abstract, problem statement, scope, AS-IS/TO-BE,
business requirements, DDD architecture, patterns, class diagram, verification
log) is at `docs/phase1/tapwallet-dossier.html` — open it in a browser, or
view it online at:
https://claude.ai/code/artifact/d0320ea9-7dd3-4b5c-b8e3-41bcfdf0e459

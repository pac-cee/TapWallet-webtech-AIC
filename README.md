# TapWallet — NFC Digital Payment & Wallet System

A web-based digital wallet and NFC payment platform, implemented for
Web Technology Assignment 3 using JSF 2.3 and Hibernate 5, backed by an
**H2 in-memory database** (no external DB server needed).

**Author:** Pacifique Bakundukize (26798)

## Tech stack

- Java 8 (source/target), Maven (`war` packaging)
- JSF 2.3.9 (org.glassfish) + Weld (CDI, via `weld-servlet-shaded`)
- Hibernate ORM 5.6.15.Final
- H2 2.2.224, in-memory (`jdbc:h2:mem:tapwallet`)
- Hibernate Validator 6.2.5.Final (JSR-303 Bean Validation)
- JUnit 4.13.2

## Running the tests

The DAO, service, bean, and validation layers are covered by JUnit
tests that run directly against the H2 in-memory database — no servlet
container needed:

```bash
mvn test
```

## Building the WAR

```bash
mvn clean package
```

This produces `target/TapWallet-1.0-SNAPSHOT.war`.

## Deploying

Deploy the WAR to any Servlet 4.0 container — **Tomcat 9** specifically
(not 10+, which moved to the `jakarta.*` namespace this project doesn't use):

1. Copy `target/TapWallet-1.0-SNAPSHOT.war` into `<tomcat>/webapps/`.
2. Start Tomcat (`bin/startup.sh`, with `JAVA_HOME` pointed at a JDK).
3. Open `http://localhost:8080/TapWallet-1.0-SNAPSHOT/`.

The H2 database is created fresh, in memory, each time the app starts —
nothing to install or configure, but all data is lost on restart.

## What's implemented

Full CRUD across the whole domain, not just the two entities the
assignment strictly requires:

| Entity | Fields | Notes |
|---|---|---|
| **User** | full name, email, phone, hashed password, role, status | Email unique; phone validated against MTN/Airtel format |
| **Wallet** | owner, balance, currency, status | One wallet per user (enforced) |
| **Merchant** | business name, merchant code, operator, status | One shop per operator (enforced); code unique |
| **NfcCard** | token, wallet, status | One card per wallet (enforced); token unique |
| **Transaction** (payment) | sender wallet, receiver wallet, amount, type, status | Atomic debit+credit+ledger write; reversible |
| **TopUp** | wallet, amount, method | Atomic credit+ledger write; reversible |
| **Withdrawal** | wallet, amount, method | Atomic debit+ledger write; reversible |

Money-moving operations (`PaymentService`, `TopUpService`,
`WithdrawalService`) run inside a single Hibernate transaction each —
balance changes and the ledger row are committed together or not at
all, so a partial write can never leave two balances out of sync.

**Validation** (all 3 required types): standard JSF validators
(`f:validateLength`, `f:validateDoubleRange`), a custom validator
(`PhoneValidator`, `@FacesValidator`), and Bean Validation (JSR-303
annotations on every entity).

**CSS** (all 3 required inclusion styles): external stylesheet
(`resources/css/styles.css`), an internal `<style>` block
(`wallet-list.xhtml`), and inline `style=` attributes throughout.

**Security**: salted SHA-256 password hashing (passwords are never
stored or redisplayed in plain text), parameterized HQL everywhere (no
string-built queries), server-side validation as the source of truth,
and friendly `FacesMessage`s instead of leaked stack traces on failure.

**Tests**: 48 JUnit tests across DAOs, services, beans, and validation —
`mvn test` runs all of them against the in-memory database.

## Known limitations (flagged, not yet fixed)

A few things work today but aren't realistic yet, and are queued for a
follow-up pass:

- `User.role` (`CUSTOMER`/`MERCHANT`/`ADMIN`) exists but isn't used for
  anything — `Merchant` is already its own table with its own
  `operator` link, so the role flag is redundant.
- A `Wallet` is created independently of its owning `User` (a separate
  "Add New Wallet" form, picking any user from a dropdown) instead of
  automatically when a user registers.
- `Transaction` connects wallet → wallet with a `PAYMENT`/`TRANSFER`
  type, so nothing currently stops one customer sending money straight
  to another customer's wallet — real payments should only ever go
  from a customer's wallet to a registered `Merchant`.
- There's no seed/demo data, so a fresh deploy starts with an empty
  Users/Wallets/Merchants list.

## What's documented but not implemented

Real NFC hardware reading, a licensed payment-gateway integration,
production authentication/session management, and regulatory
(KYC/AML) compliance are described in the Phase-1 documentation as the
intended full system's edges, but are out of scope for this classroom
sandbox.

## Phase-1 documentation

The Assignment-3 Phase-1 document (abstract, problem statement, scope,
AS-IS/TO-BE models, business requirements, software qualities, and the
full-domain class diagram) is at `docs/phase1/tapwallet-dossier.html` —
open it in a browser, or view it live at:
https://claude.ai/code/artifact/d0320ea9-7dd3-4b5c-b8e3-41bcfdf0e459

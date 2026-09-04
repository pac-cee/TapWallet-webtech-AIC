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
  status) and **Wallet** (owner, balance, currency, status) — one wallet
  per user, duplicate emails rejected, guarded deletes.
- Full CRUD for **NfcCard** (token, wallet, status) and **Merchant**
  (business name, code, operator, status) — one card per wallet, one
  shop per operator.
- Money movements with atomic ledger logic: **TopUp** credits,
  **Withdrawal** debits (insufficient funds rejected), **Transaction**
  debits sender + credits receiver in one DB transaction; deleting a
  movement reverses it (fails cleanly if balances no longer allow it).
- All 3 required validation types: standard JSF validators
  (`f:validateLength`, `f:validateDoubleRange`), a custom validator
  (`PhoneValidator`), and Bean Validation (JSR-303 annotations).
- All 3 CSS inclusion styles: external stylesheet, one internal
  `<style>` block, and inline `style=` attributes.
- Salted password hashing, parameterized HQL, and server-side-authoritative
  validation.

## Phase-1 documentation

The Assignment-3 Phase-1 document (abstract, problem statement, scope,
AS-IS/TO-BE models, business requirements, software qualities, and the
full-domain class diagram) is at `docs/phase1/tapwallet-dossier.html` —
open it in a browser, or view it live at:
https://claude.ai/code/artifact/d0320ea9-7dd3-4b5c-b8e3-41bcfdf0e459

## What's documented but not implemented

Nothing — the Phase-1 class diagram is now fully built. `AuditLog` from
the diagram is covered by the shared `Audit` superclass
(`createdAt`/`updatedAt` on every entity) rather than a separate table.

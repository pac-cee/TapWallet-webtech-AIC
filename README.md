# TapWallet — NFC Digital Payment & Wallet System

A web-based digital wallet system, implemented for Web Technology
Assignment 3 using JSF 2.3 and Hibernate 5, backed by an **H2
in-memory database** (no external DB server needed).

Per the assignment's requirement (choose 2 entities, implement full
CRUD), this project implements **User** and **Wallet** — nothing more.

**Author:** Pacifique Bakundukize (26798)
**Repository:** https://github.com/pac-cee/TapWallet-webtech-AIC
**Video walkthrough:** https://drive.google.com/file/d/1lZD1U0aY5mWQ_xdpl7Zg-5t4imuNkclN/view?usp=sharing

## Tech stack

- Java 8 (source/target), Maven (`war` packaging)
- JSF 2.3.9 (org.glassfish) + Weld (CDI, via `weld-servlet-shaded`)
- Hibernate ORM 5.6.15.Final
- H2 2.2.224, in-memory (`jdbc:h2:mem:tapwallet`)
- Hibernate Validator 6.2.5.Final (JSR-303 Bean Validation)
- JUnit 4.13.2

## Running the tests

```bash
mvn test
```

34 JUnit tests cover the DAO, bean, and validation layers, running
directly against the H2 in-memory database — no servlet container
needed.

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

| Entity | Fields | Notes |
|---|---|---|
| **User** | full name, email, phone, hashed password, status | Email unique; phone validated against MTN/Airtel format |
| **Wallet** | owner, balance, currency, status | One wallet per user (enforced) |

Full CRUD (Create/Read/Update/Delete) on both, via `user-list`/`user-form`
and `wallet-list`/`wallet-form`.

**Validation** (all 3 required types): standard JSF validators
(`f:validateLength`, `f:validateDoubleRange`), a custom validator
(`PhoneValidator`, `@FacesValidator`), and Bean Validation (JSR-303
annotations on both entities).

**CSS** (all 3 required inclusion styles): external stylesheet
(`resources/css/styles.css`), an internal `<style>` block
(`wallet-list.xhtml`), and inline `style=` attributes throughout.

**Security**: salted SHA-256 password hashing (passwords are never
stored or redisplayed in plain text), parameterized HQL everywhere (no
string-built queries), server-side validation as the source of truth,
and friendly `FacesMessage`s instead of leaked stack traces on failure.

**Tests**: 34 JUnit tests across DAOs, beans, and validation —
`mvn test` runs all of them against the in-memory database.

## What's documented but not implemented

The wider TapWallet vision (merchants, NFC cards, payments, top-ups,
withdrawals) is described in the Phase-1 documentation as the intended
full system, but this repository deliberately implements only the
CRUD slice the assignment requires — User and Wallet.

## Phase-1 documentation

The Assignment-3 Phase-1 document (abstract, problem statement, scope,
AS-IS/TO-BE models, business requirements, software qualities, and the
full-domain class diagram) is at `docs/phase1/tapwallet-dossier.html` —
open it in a browser, or view it live at:
https://claude.ai/code/artifact/d0320ea9-7dd3-4b5c-b8e3-41bcfdf0e459

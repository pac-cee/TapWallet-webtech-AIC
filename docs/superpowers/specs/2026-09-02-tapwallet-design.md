# TapWallet — NFC Digital Payment & Wallet System — Design Spec

**Author:** Pacifique Bakundukize (26798)
**Date:** 2026-09-02
**Course:** Web Technology — Assignment 3 (Hibernate + JSF)

## 1. Purpose

Implement Assignment-3 requirement #2: pick 2 entities from the TapWallet
domain and implement full CRUD using JSF + Hibernate, applying all three
required validation types and all three CSS inclusion styles, following the
same conventions used in the instructor's reference project
(`StockManagementSystem`).

## 2. Reference project conventions being reused

Observed in `StockManagementSystem` and applied here verbatim unless noted:

- Maven `war` packaging, `groupId=rw.ac.auca`, Java 8 source/target.
- `javax.faces` 2.3.9 (org.glassfish) + `weld-servlet-shaded` 3.1.9.Final +
  `javax.servlet-api` 4.0.1 (provided) + `hibernate-core` 5.6.15.Final +
  `maven-war-plugin` 3.3.2.
- Hibernate configured via `hibernate.cfg.xml` (not `persistence.xml`, which
  is left present-but-empty like the reference project).
- Package layout: `rw.ac.auca.tapwallet` root, with `model`, `dao`, `util`
  sub-packages; `*Bean` managed beans live in the root package.
- Entities are plain `@Entity` POJOs; shared `createdAt`/`updatedAt` live on
  a `@MappedSuperclass Audit` that entities extend (as `Product extends
  Audit` does).
- DAOs open a session, begin a transaction, act, commit, close — with the
  same step-numbered comment style as `ProductDao`.
- Managed beans use `@ManagedBean` (JSF 2.x style, no CDI `@Named`), are
  thin, and delegate to a DAO.
- Views are plain `.xhtml` using `h:panelGrid` forms, `h:message` per field,
  built-in validator tags, and a custom `Converter` where needed.
- Javadoc class header with `@author` / `@version` on every Java class.

Deviations from the reference project (and why):

- **Database**: H2 in-memory (`jdbc:h2:mem:tapwallet;DB_CLOSE_DELAY=-1`,
  `H2Dialect`) instead of Postgres — no external DB server needed, and the
  user asked for "only in memory db". `hbm2ddl.auto=update` is kept for
  consistency even though it behaves like `create` on a fresh in-memory DB.
- **IDs**: auto-generated (`@GeneratedValue(strategy = IDENTITY)`) instead
  of the reference project's manually-typed `productId`, because a user
  shouldn't have to invent their own numeric ID.
- **New dependencies**: `com.h2database:h2`, `org.hibernate.validator:
  hibernate-validator`, `javax.el-api` + `org.glassfish:javax.el` — required
  to make JSR-303 Bean Validation annotations actually fire during JSF
  submits. The reference project only used built-in JSF validators, so it
  never needed these.
- **Real relation**: `Wallet.owner` is a genuine `@ManyToOne User` (with a
  dropdown of existing users in the form), rather than the reference
  project's flat, unrelated single entity — this is a small step up in
  fidelity, still simple to implement and read.

## 3. Scope

**In scope (implemented code):**
- Full CRUD (Create/Read/Update/Delete) for `User` and `Wallet`.
- 3 validation types (standard JSF, custom `Validator`, Bean Validation).
- CSS via external stylesheet, one internal `<style>` block, and inline
  `style=` attributes.
- Basic security: salted password hashing, parameterized HQL, server-side
  validation as the source of truth, no stack traces surfaced to the user.

**Out of scope (documented only, not implemented):** NFC card tap flow,
merchant dashboard, transaction ledger, top-up/withdrawal processing,
admin console, authentication/session login. These appear in the Phase-1
documentation's class diagram and TO-BE model as the intended full system,
per the assignment's own separation between "propose the whole system" and
"implement CRUD on 2 entities from it".

## 4. Data model

### `User`
| field | type | notes |
|---|---|---|
| id | Long | `@Id @GeneratedValue(IDENTITY)` |
| fullName | String | `@NotBlank @Size(min=3,max=50)` |
| email | String | `@NotBlank @Email` |
| phone | String | `@NotBlank`, format checked by custom validator (`07XXXXXXXX`) |
| passwordHash | String | salted SHA-256, set on create, optional on edit |
| role | String | `CUSTOMER` \| `MERCHANT` \| `ADMIN`, dropdown |
| status | String | `ACTIVE` \| `FROZEN`, dropdown |
| createdAt/updatedAt | via `Audit` | |

### `Wallet`
| field | type | notes |
|---|---|---|
| id | Long | `@Id @GeneratedValue(IDENTITY)` |
| owner | `User` | `@ManyToOne @JoinColumn(name="user_id")`, chosen from dropdown |
| balance | BigDecimal | `@NotNull @DecimalMin("0.0")` |
| currency | String | default `"RWF"` |
| status | String | `ACTIVE` \| `FROZEN`, dropdown |
| createdAt/updatedAt | via `Audit` | |

## 5. CRUD flow

Two pages per entity:
- `*-list.xhtml`: `h:dataTable` of all rows + "Edit"/"Delete" links per row
  + "Add New" link.
- `*-form.xhtml`: one form serving both create and edit. Edit is triggered
  by `?id=<id>` (`f:viewParam` + `preRenderView` listener loads the entity
  into the bean); create is the same page with no `id`. Submit calls
  `save()` on the bean, which calls `dao.save()` (insert or update — same
  method, Hibernate decides via presence of an ID) and redirects to the
  list page (`faces-redirect=true`) with a flash success message.

Delete is a link/button on the list page calling `bean.delete(id)`,
followed by a redirect back to the (now refreshed) list.

## 6. Validation (3 required types)

1. **Standard/built-in JSF validators** — `f:validateLength` on
   `fullName`, `f:validateDoubleRange` on `balance`.
2. **Custom validator** — `rw.ac.auca.tapwallet.util.PhoneValidator`
   implements `javax.faces.validator.Validator`, registered with
   `@FacesValidator("phoneValidator")`, attached to the phone field via
   `<f:validator validatorId="phoneValidator"/>`, rejects anything not
   matching `^07[2389]\d{7}$`.
3. **Bean Validation (JSR-303)** — annotations directly on `User`/`Wallet`
   fields (`@NotBlank`, `@Size`, `@Email`, `@DecimalMin`), invoked
   automatically by JSF 2.3's built-in Bean Validation integration once
   `hibernate-validator` is on the classpath.

Each input still gets an `h:message` next to it, styled inline
(`style="color:red"`), matching the reference project.

## 7. CSS (3 required inclusion styles)

- **External**: `webapp/resources/css/styles.css`, pulled in via
  `<h:outputStylesheet library="css" name="styles.css"/>` on every page —
  base layout, nav bar, table styling, buttons.
- **Internal**: a `<style>` block in the `<h:head>` of `wallet-list.xhtml`
  for the balance/status badge look, scoped to that page only.
- **Inline**: `style="color:red"` on validation messages, and a couple of
  small inline touches (e.g. status badge color) — same habit as the
  reference project's `h:message style="color : red"`.

## 8. Security notes

- `PasswordUtil` salted SHA-256 hash; plaintext password is never stored,
  never re-displayed in the edit form (left blank = keep current hash).
- All DAO queries use bound HQL parameters (`:id`, `:email`) — no string
  concatenation, so no HQL/SQL injection surface.
- Server-side validation (Bean Validation + custom validator + `required`)
  is authoritative; nothing depends on client-side JS.
- JSF's default output escaping is left on everywhere (no
  `escape="false"`), so rendered user input can't inject markup/script.
- DAO/bean catch persistence exceptions and surface a plain
  `FacesMessage` ("Could not save wallet — please check the values and try
  again.") instead of leaking a stack trace to the page.

## 9. Documentation deliverable

A single document (published as an artifact + saved under `docs/`)
covering: Abstract, Problem Statement, Scope, AS-IS Model, TO-BE Model,
Business Requirements, Software Qualities (security, usability,
maintainability, reliability, performance, scalability — each explained
for TapWallet specifically), and an Initial Class Diagram (Mermaid)
spanning the full intended domain (User, Wallet, NfcCard, Merchant,
Transaction, TopUp, Withdrawal), with clearly marked placeholders for the
GitHub repo link and the video walkthrough link, and the required
submission filename `26798_Pacifique_Bakundukize_assignment_3.zip`.

## 10. File plan

```
TapWallet-webtech-AIC/
├─ pom.xml
├─ README.md
├─ src/main/resources/
│  ├─ hibernate.cfg.xml
│  └─ META-INF/persistence.xml   (present, unused — matches reference project)
├─ src/main/java/rw/ac/auca/tapwallet/
│  ├─ model/Audit.java
│  ├─ model/User.java
│  ├─ model/Wallet.java
│  ├─ dao/HibernateUtil.java
│  ├─ dao/UserDao.java
│  ├─ dao/WalletDao.java
│  ├─ util/PasswordUtil.java
│  ├─ util/PhoneValidator.java
│  ├─ UserBean.java
│  └─ WalletBean.java
└─ src/main/webapp/
   ├─ WEB-INF/{web.xml,faces-config.xml,beans.xml}
   ├─ resources/css/styles.css
   ├─ index.xhtml
   ├─ user-list.xhtml
   ├─ user-form.xhtml
   ├─ wallet-list.xhtml
   └─ wallet-form.xhtml
```

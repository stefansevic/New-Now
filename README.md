# New-Now

Web application for managing **locations**, **events**, and **reviews**. Users can browse locations, submit ratings and comments, and view events. Administrators and location managers have dedicated areas for approving account requests and managing locations and content.

---

## Features

- **Auth** — Registration, login with JWT, user profile. Tokens are sent via header; the frontend uses an auth interceptor and stores the token in memory/local storage.
- **Locations** — List all locations, view details, add and edit (with permissions). “My locations” shows locations linked to the current user; location managers can be assigned to locations.
- **Reviews** — Users can rate locations and leave comments. Ratings and comments are stored and displayed on location detail pages.
- **Events** — Event listing with basic event data; events can be associated with locations.
- **Admin** — Account request workflow: users request accounts, admins approve or reject. Admin dashboard for overview and management.
- **Upload** — File upload support (e.g. images for locations). Max file size is configurable in `application.properties` (default 25MB).

---

## Tech Stack

| Layer   | Stack |
|--------|--------|
| Backend | Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA, Spring Security, JWT (jjwt), H2 |
| Frontend | Angular 20, TypeScript, RxJS, jwt-decode |
| Build | Maven (builds Angular and copies output to `target/classes/static`) |

- **Database**: H2 file-based (`./data/demo`). Schema is updated automatically (`ddl-auto=update`). The H2 console is enabled — after starting the app, open **http://localhost:8080/h2-console**, use JDBC URL `jdbc:h2:file:./data/demo`, user `sa`, password `password`.
- **API**: REST-style endpoints under `/api` (auth, locations, events, reviews, account-requests, users, upload). Protected routes require a valid JWT in the `Authorization` header.

---

## Prerequisites

- **Java 17** (for running the backend)
- **Node.js** (v20 recommended) and **npm** — only needed if you run or build the frontend separately. The full Maven build can install Node/npm via the frontend-maven-plugin.

---

## How to Run

**Backend only** (uses pre-built frontend from `target/classes/static`):

```bash
./mvnw spring-boot:run
```

App: **http://localhost:8080**

**Frontend dev** (Angular with proxy to backend):

```bash
cd frontend
npm install
npm start
```

Frontend: **http://localhost:4200** (proxies API to port 8080).

**Full build** (frontend + backend):

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Maven will install Node/npm in `target`, run `npm install` and `npm run build` in `frontend`, then copy `frontend/dist/browser` into `target/classes/static`.

---

## Project Layout

**Backend** (`src/main/java/com/example/demo/`):

- `controller/` — REST controllers (Auth, Location, Event, Review, AccountRequest, User, Upload, Spa for SPA fallback)
- `model/` — JPA entities (User, Location, Event, Review, Comment, Rate, Image, AccountRequest, etc.)
- `repository/` — Spring Data JPA repositories
- `service/` — Business logic (e.g. AccountRequestService, LocationService, ReviewService)
- `security/` — JWT filter, JwtService, SecurityConfig, CustomUserDetailsService
- `config/` — WebConfig (CORS etc.), DataInitializer for sample data

**Resources** (`src/main/resources/`):

- `application.properties` — Data source, JPA, JWT settings, server port, file upload limits
- `static/` — Built Angular app (after build)

**Frontend** (`frontend/`):

- `src/app/components/` — Pages/components (login, register, home, locations, location-details, location-form, my-locations, location-managers, events, profile, account-requests, admin-home, navbar)
- `src/app/services/` — Auth, location, event, review, account-request, user services; auth interceptor

---

## Configuration

Main settings in `src/main/resources/application.properties`:

- `server.port=8080`
- `spring.datasource.url`, H2 file path and credentials
- `jwt.secret`, `jwt.expiration` (e.g. 3600000 ms)
- `spring.servlet.multipart.max-file-size=25MB`

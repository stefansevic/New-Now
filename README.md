# New-Now 

Projektni zadatak iz predmeta **Serverske web tehnologije (SVT)** i **Klijentske web tehnologije (KVA)** 

---

## Šta je ovo?

**New-Now** je veb aplikacija za upravljanje lokacijama, događajima i recenzijama. Korisnici mogu da pregledaju lokacije, daju ocene i komentare, a administratori i menadžeri lokacija imaju posebne uloge (odobravanje naloga, upravljanje lokacijama itd.).

---

## Šta je urađeno?

- **Korisnici**: registracija, prijava (JWT), profil.
- **Lokacije**: pregled svih lokacija, detalji lokacije, dodavanje/izmena lokacija, “moje lokacije”, menadžeri lokacija.
- **Recenzije**: ocene i komentari na lokacijama.
- **Događaji**: pregled događaja.
- **Administracija**: stranica za odobravanje zahteva za nalog (account requests), admin početna.
- **Upload**: podrška za upload fajlova (npr. slike).

---

## Tehnologije

| Deo        | Tehnologija |
|-----------|-------------|
| **Backend** | Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA, Spring Security, JWT (jjwt), H2 baza |
| **Frontend** | Angular 20, TypeScript, RxJS, jwt-decode |
| **Build** | Maven (backend + automatski build frontenda u `frontend/dist/browser` i kopiranje u `target/classes/static`) |

Baza se čuva u fajlu (`jdbc:h2:file:./data/demo`), H2 konzola je uključena.

---

## Kako pokrenuti?

1. **Samo backend (sa već izgrađenim frontendom)**  
   U root folderu projekta:
   ```bash
   ./mvnw spring-boot:run
   ```
   Aplikacija: **http://localhost:8080**

2. **Razvoj frontenda (Angular)**  
   U folderu `frontend`:
   ```bash
   npm install
   npm start
   ```
   Frontend: **http://localhost:4200** (proxy ka backendu na 8080).

3. **Full build (frontend + backend)**  
   U root folderu:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```
   Maven prvo gradi Angular aplikaciju, pa je pakuje sa backendom.

---

## Struktura projekta

- **`src/main/java/com/example/demo/`** — Spring Boot aplikacija (kontroleri, modeli, repozitorijumi, servisi, JWT i security konfiguracija).
- **`src/main/resources/`** — `application.properties`, statički fajlovi (build-ovani frontend).
- **`frontend/`** — Angular aplikacija (komponente, rute, servisi za auth, lokacije, događaje, recenzije itd.).

---

## Napomena

JWT secret i ostale osetljive vrednosti u `application.properties` su namenjeni razvoju; u produkciji koristiti sigurnu konfiguraciju (npr. env varijable ili secret manager).

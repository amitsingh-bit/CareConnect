# CareConnect

CareConnect is a healthcare coordination application for patients and care teams. The repository contains a Spring Boot REST API and an Angular 19 frontend.

## Features

- Session based sign in and role specific registration for patients, doctors, nurses, and administrators.
- A role aware workspace for appointments, patient profiles, medical records, diagnoses, prescriptions, lab reports, doctors, nurses, and the medication catalog.
- Create, update, search, and delete actions for resources supported by the API.
- Overview cards for care activity, with shortcuts to appointments and health records.

## Interface direction

The Angular workspace follows the UI/UX Pro Max healthcare guidance: a light, data-dense dashboard with calm teal and green accents, clear surfaces, and Atkinson Hyperlegible typography. Tables and forms use readable text sizing, visible keyboard focus, responsive layouts, and reduced-motion support. The source design guidance is in `ui-ux-pro-max-skill/`.

## Requirements

- Java 17 or later
- Maven 3.9 or the included Maven wrapper
- Node.js 20 or later and npm
- MySQL running locally with a database named `CareConnect`

The backend database connection is configured in `backend/src/main/resources/application.yml`. It defaults to `jdbc:mysql://localhost:3306/CareConnect` with username `root` and password `amit7280`; override these with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for your local MySQL installation. The application uses the existing session cookie authentication. For administrator signup, set `CARECONNECT_ADMIN_SIGNUP_CODE` to the invitation code that new administrators must provide.

## Demo database

Create the database if needed:

```sql
CREATE DATABASE CareConnect;
```

Start Spring Boot once so Hibernate can create or update the tables from the existing JPA entities. Then, from the repository root, load the linked demo records:

```bash
mysql -u root -p CareConnect < database/careconnect_demo_data.sql
```

Restart Spring Boot and sign in with any of these demo accounts:

| Role | Email | Password |
| --- | --- | --- |
| Patient | `patient@careconnect.com` | `Patient@123` |
| Doctor | `doctor@careconnect.com` | `Doctor@123` |
| Nurse | `nurse@careconnect.com` | `Nurse@123` |
| Admin | `admin@careconnect.com` | `Admin@123` |

The seed uses the existing `doctor`, `patients`, `appointments`, `medical_records`, `diagnoses`, `prescriptions`, `lab_reports`, and related table mappings. Vitals and notifications use the added `vitals` and `notifications` entities. The SQL uses explicit stable IDs and can be run again to refresh the demo rows. It is intended for local development and demos; do not use the listed credentials outside that environment.

## Run locally

Open two terminals from the repository root.

Start the backend:

```bash
mvn spring-boot:run
```

Or use the wrapper:

```bash
./mvnw spring-boot:run
```

The backend listens on [http://localhost:8080](http://localhost:8080). The root Maven configuration points to the backend source and resource folders under `backend/src`.

Start the Angular frontend:

```bash
cd frontend
npm install
npm start
```

Open [http://localhost:4200](http://localhost:4200). The Angular development server forwards `/api` requests to the backend through `frontend/proxy.conf.json`; requests use the backend session cookie.

To deploy Angular inside Spring Boot, build it from `frontend`:

```bash
npm run build
```

The production build is written to `backend/src/main/resources/static`. Run or restart Spring Boot and open [http://localhost:8080](http://localhost:8080) to use the combined app.

## Build

Build the frontend:

```bash
cd frontend
npm run build
```

Build the Spring Boot application:

```bash
mvn package
```

## API areas

The backend provides `/api/auth` for session access and REST resources under `/api/appointments`, `/api/patients`, `/api/doctors`, `/api/nurses`, `/api/medical-records`, `/api/diagnoses`, `/api/prescriptions`, `/api/lab-reports`, `/api/medications`, and `/api/admins`.
# CareConnect

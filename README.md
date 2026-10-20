# CareConnect

CareConnect is an existing healthcare coordination application built with a Spring Boot REST API, MySQL, and an Angular 19 frontend. This repository extends the existing application; it is not a replacement project.

## Existing architecture

- **Frontend:** Angular 19, TypeScript, Angular Router, HttpClient, one role-aware workspace component, and session-based API requests.
- **Backend:** Java 17, Spring Boot 4.1.1, Spring Web, Spring Data JPA, Hibernate, and Maven. Sources are under `backend/src`; the root `pom.xml` configures those source directories.
- **Database:** MySQL with the existing patient, doctor, nurse, administrator, appointment, medical record, diagnosis, prescription, medication, lab report, vital, notification, and account entities.
- **Authentication:** Server-side HTTP session. Passwords are hashed by the existing `PasswordHasher`; account roles are `PATIENT`, `DOCTOR`, `NURSE`, and `ADMIN`.

The role-aware dashboard already loads live records from the API. Patients see their profile, appointments, care team, records, diagnoses, prescriptions, lab reports, vitals, and notifications. Doctor and nurse dashboards show their existing scoped work queues. Empty lists are presented without inventing clinical data.

## Security changes

- Session cookies are HTTP-only and use `SameSite=Lax`. Set `SESSION_COOKIE_SECURE=true` behind HTTPS in production.
- Login attempts are throttled to five attempts per 15 minutes per remote-address/account pair. Buckets are held in process memory; use a shared rate-limit store before running multiple backend instances.
- Unsafe API requests require a CSRF token. After login or signup the backend sets the readable `XSRF-TOKEN` cookie; Angular's default XSRF interceptor sends it as `X-XSRF-TOKEN`. The token is stored in the server session and compared before processing writes.
- API activity is written to the `audit_logs` table with account ID and role when available, action, resource type and ID, HTTP method, route, response status, correlation ID, and timestamp. Request bodies and clinical data are not copied into the audit table.
- Clinical record, diagnosis, prescription, lab report, vital, and consultation-note APIs require the patient or an authorized care role; administrator accounts cannot read or change those clinical resources. Doctors can manage records, diagnoses, prescriptions, and lab reports for patients in their care; doctors and nurses can manage vitals within their patient access. Patient relationships are checked by the backend.

The application currently uses Hibernate `ddl-auto: update`; on startup it adds audit and consultation-note tables/columns to the existing schema. No Flyway migration framework is configured in this repository. Back up the database and review production schema changes through your normal change process before deploying.

## Patient and clinical workflows

- The patient dashboard includes interactive 7-day and 30-day trend views for recorded systolic/diastolic blood pressure, heart rate, temperature, oxygen saturation, weight, and height, compared with the preceding period when earlier measurements exist. The dashboard still loads the existing role-scoped vital list and does not expose a paginated historical analytics API.
- Doctors can create and update consultation notes from an appointment with chief complaint, history, examination, assessment, diagnosis, plan, follow-up instructions, and follow-up date. Before changed content is saved, the prior note is copied into `consultation_note_versions`; authorized users can review these snapshots in the note dialog.
- The patient health assistant supports English and Hindi. Its interface language is saved in the browser. Assistant requests require an AI-compatible provider configured with `AI_API_KEY`, `AI_MODEL`, and optionally `AI_BASE_URL`. Without credentials the endpoint returns a service-unavailable error; it does not simulate answers. The assistant does not access patient records, diagnose, prescribe, or replace a clinician. Avoid entering sensitive information into assistant messages because messages are sent to the configured provider.
- English/Hindi preference is saved in browser storage and restored on return. Translation resources cover the public landing page, navigation, authentication, patient/doctor/nurse/admin dashboards, resource tables, patient profiles, common form states, consultation-note history, video dialogs, and common API errors. User-generated clinical content and backend validation messages remain in their original language; those values should not be machine-translated.
- Video consultation now provisions an expiring private Daily room and scoped meeting tokens through a backend-only API key. Only the appointment's patient or assigned doctor can request a token, and access opens 15 minutes before start and closes 2 hours after the scheduled start. Configure `DAILY_API_KEY` to activate it. Provider activation and an end-to-end call still require a Daily account and have not been verified in this environment.
- The administrator audit page lists the latest 200 audit events. Fine-grained clinical events, export, and retention controls remain future work.

## Requirements

- Java 17 or later
- Maven 3.9 or the included Maven wrapper
- Node.js 20 or later and npm
- MySQL with a database named `CareConnect`

## Configuration

The backend reads these environment variables (placeholders only; do not commit real credentials):

| Variable | Default | Purpose |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/CareConnect` | MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `root` | MySQL username |
| `SPRING_DATASOURCE_PASSWORD` | empty | MySQL password |
| `SESSION_COOKIE_SECURE` | `false` | Set `true` when served over HTTPS |
| `CARECONNECT_ADMIN_SIGNUP_CODE` | empty | Invitation code required for admin signup |
| `CARECONNECT_CSRF_ENABLED` | `true` | CSRF protection toggle; keep enabled outside isolated tests |
| `AI_API_KEY` | empty | API key for the configured OpenAI-compatible assistant provider |
| `AI_MODEL` | empty | Model identifier accepted by that provider |
| `AI_BASE_URL` | `https://api.openai.com/v1` | OpenAI-compatible API base URL |
| `DAILY_API_KEY` | empty | Server-side Daily API key; required to provision private video rooms and participant tokens |

Copy `.env.example` as a reference for local environment variables. The backend does not load `.env` files; export the values in your shell or deployment platform. Create the database before starting the backend:

```sql
CREATE DATABASE CareConnect;
```

For example, configure a local environment before starting (replace the sample values locally):

```bash
export SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/CareConnect'
export SPRING_DATASOURCE_USERNAME='root'
export SPRING_DATASOURCE_PASSWORD='your-local-password'
```

There is no `.env` loader in the current backend; export variables in the shell or your deployment platform. Do not use demo credentials or data in a production environment. `database/careconnect_demo_data.sql` is for local development only.

## Run locally

From the repository root, start Spring Boot:

```bash
./mvnw spring-boot:run
```

Or use installed Maven:

```bash
mvn spring-boot:run
```

In another terminal, start Angular:

```bash
cd frontend
npm install
npm start
```

Open `http://localhost:4200`. The Angular development proxy forwards `/api` to `http://localhost:8080`, so browser requests and the session cookie use the same origin. The backend listens on port 8080.

Build Angular for static serving by running `npm run build` from `frontend`; the configured output directory is `backend/src/main/resources/static`. Build the backend with `./mvnw package`.

## Tests

```bash
./mvnw test
cd frontend && npm test -- --watch=false
```

The backend test suite uses an isolated in-memory H2 database for application context startup and unit tests. The frontend test suite uses Karma with Chrome. Production builds can be generated with `./mvnw -DskipTests package` and `cd frontend && npm run build`.

## API areas

The existing API includes `/api/auth`, `/api/appointments`, `/api/patients`, `/api/doctors`, `/api/nurses`, `/api/medical-records`, `/api/diagnoses`, `/api/prescriptions`, `/api/lab-reports`, `/api/medications`, `/api/vitals`, and `/api/notifications`. Consultation notes use `GET` and `PUT /api/appointments/{appointmentId}/consultation-note`, with prior snapshots at `GET /api/appointments/{appointmentId}/consultation-note/versions`. The patient assistant uses `POST /api/assistant/chat`. Eligible appointment participants request a video join token through `POST /api/appointments/{appointmentId}/video/join`. Administrators can read the latest 200 events through `GET /api/admins/audit-logs`. Authentication uses `/api/auth/login`, `/api/auth/signup`, `/api/auth/me`, and `/api/auth/logout`.

## Known limitations

- The repository still uses Hibernate `ddl-auto: update` rather than versioned Flyway migrations. Review a production database backup and schema plan before deploying new entities or columns.
- The dashboards use existing list APIs and client-side chart calculations; a paginated analytics API and database-level time-range queries have not been added.
- Login throttling is process-local, so horizontally scaled deployments need a shared store or gateway limit.
- AI and Daily rooms require valid provider credentials. The endpoints return explicit configuration errors without credentials; live provider calls and a full call workflow were not verified in this environment.
- Localization covers application-owned UI text and common API errors. User-entered medical content, provider responses, and detailed server validation messages are shown as received to avoid altering clinical meaning.
- Consultation notes are versioned when changed, but the existing schema does not have a separate signed/finalized state or a formal amendment reason field.
- Audit events capture API request metadata, but fine-grained access events, tamper-evident archival, retention controls, and export remain future work.

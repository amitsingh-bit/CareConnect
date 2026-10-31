# CareConnect Angular frontend

This Angular 19 application provides account access and role-aware workspaces for patients, doctors, nurses, and administrators. It connects to the Spring Boot API for session authentication and healthcare coordination workflows.

## Run locally

Start the Spring Boot backend on port `8080`, then run the Angular app from this directory:

```bash
npm install
npm start
```

The development server runs at `http://localhost:4200`. `proxy.conf.json` forwards `/api` requests to the backend so the session cookie remains same-origin during local development.

## Build

```bash
npm run build
```

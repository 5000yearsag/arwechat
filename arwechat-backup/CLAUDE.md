# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## High-level Architecture

This repository contains a WeChat Mini Program, a Java Spring Boot backend, and a web admin portal.

- **Frontend (WeChat Mini Program):** The source code is in the root directory. The main configuration files are `project.config.json` and `app.json`. The AppID for the mini program is `wx360d6d845e60562e`.

- **Admin Portal (`arweb/`):** This is a web application built with Ant Design Pro and UmiJS, located in the `arweb/` directory. It serves as the admin portal for managing the application.

- **Backend (Java Spring Boot):** The backend code is located in the `ar-platform/` directory. It is a Maven project and provides APIs for both the mini program and the admin portal. The server is configured to run on port 9091.

## Common Commands

### Frontend (WeChat Mini Program)

To develop and run the frontend, use the WeChat DevTools to open the project at the root of this repository.

### Admin Portal (`arweb/`)

- **Navigate to the admin portal directory:**
  ```bash
  cd arweb
  ```

- **Install dependencies:**
  ```bash
  npm install
  ```

- **Run in development mode:**
  ```bash
  npm run dev
  ```

- **Build the project:**
  ```bash
  npm run build
  ```

- **Run linting:**
  ```bash
  npm run lint
  ```

### Backend (`ar-platform/`)

The backend is a standard Maven project.

- **Navigate to the backend directory:**
  ```bash
  cd ar-platform
  ```

- **Build the project:**
  ```bash
  mvn clean package
  ```

- **Run the application:**
  ```bash
  mvn spring-boot:run
  ```

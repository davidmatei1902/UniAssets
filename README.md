# University Assets Manager

**University Assets Manager** (codenamed `uni-assets`) is a full-stack application designed to efficiently track, manage, and maintain university assets. It features a responsive web-based front-end, a robust Spring Boot back-end, and a SQL Server database.

## Features
- **Asset Management**: Create, update, and delete records for various university assets (IT equipment, furniture, laboratory tools).
- **Web Interface**: User-friendly dashboard rendered with Thymeleaf to view and filter asset data.
- **Database Integration**: Direct and secure interaction with MS SQL Server.
- **Bulk Operations**: Scripts included for initial data insertion and database creation.
- **Safe Operations**: Safeguards for critical actions like database deletion.

## Technologies & Dependencies

### Backend
- **Spring Boot 3 (Spring Web)**: Handles RESTful APIs, business logic, and database connectivity.
- **Java 23**: Utilizes the latest Java features.

### Frontend
- **Thymeleaf**: Server-side Java template engine for rendering dynamic HTML.
- **CSS3**: Custom styling including dashboard layouts (`dashboard.css`, `global.css`).

### Database
- **MS SQL Server**: Primary data storage (`EvidentaDotarilor`).
- **Spring Data JDBC**: For efficient SQL execution and object mapping.

---

# How to Run

This guide explains how to set up and run the **University Assets Manager** locally.

## Prerequisites

Ensure you have the following installed:

- **Java 23** (Ensure `JAVA_HOME` is configured)
- **Maven** (Or use the provided `mvnw` wrapper)
- **MS SQL Server** (Express or Standard edition)
- **SQL Server Authentication** enabled (recommended)

---

### Project Structure

```text
├── HELP.md
├── README.md
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── io
│   │   │       └── github
│   │   │           └── davidmatei1902
│   │   │               └── uni_assets
│   │   │                   ├── UniAssetsApplication.java
│   │   │                   ├── controller
│   │   │                   │   └── AppController.java
│   │   │                   ├── model
│   │   │                   │   ├── Asset.java
│   │   │                   │   └── AssetStatus.java
│   │   │                   ├── repository
│   │   │                   │   └── AssetRepository.java
│   │   │                   └── service
│   │   │                       └── DatabaseService.java
│   │   └── resources
│   │       ├── application.properties
│   │       ├── sql
│   │       │   ├── ClearDatabaseRecords.sql
│   │       │   ├── CreateAssetsDatabase.sql
│   │       │   ├── DeleteDatabase_CAUTION.sql
│   │       │   ├── InsertData.sql
│   │       │   └── QuerryAllTables.sql
│   │       ├── static
│   │       │   ├── css
│   │       │   │   ├── dashboard.css
│   │       │   │   ├── global.css
│   │       │   │   ├── index.css
│   │       │   │   └── login.css
│   │       │   ├── images
│   │       │   │   ├── university_assets_3d_printer.jpg
│   │       │   │   ├── university_assets_destkop_workstation.jpg
│   │       │   │   ├── university_assets_laptop.jpg
│   │       │   │   ├── university_assets_mesh_router.jpg
│   │       │   │   ├── university_assets_multimeter.jpg
│   │       │   │   ├── university_assets_projector.jpg
│   │       │   │   ├── university_assets_robot_arm.jpg
│   │       │   │   ├── university_assets_screen_classroom.jpg
│   │       │   │   ├── university_exterior_1.jpg
│   │       │   │   ├── university_exterior_2.jpg
│   │       │   │   ├── university_faculty_business_management.jpg
│   │       │   │   ├── university_faculty_chemical_engineering.jpg
│   │       │   │   ├── university_faculty_computer_science_1.jpg
│   │       │   │   ├── university_faculty_computer_science_2.jpg
│   │       │   │   ├── university_faculty_computer_science_3.jpg
│   │       │   │   ├── university_faculty_electronic_engineering.jpg
│   │       │   │   ├── university_faculty_mechanical_engineering.jpg
│   │       │   │   ├── university_faculty_transport_engineering.jpg
│   │       │   │   ├── university_interior_chairs_1.jpg
│   │       │   │   ├── university_interior_laptop_1.jpg
│   │       │   │   ├── university_interior_lecture_1.jpg
│   │       │   │   ├── university_interior_lecture_2.jpg
│   │       │   │   ├── university_interior_library_1.jpg
│   │       │   │   └── university_laboratories.jpg
│   │       │   └── js
│   │       └── templates
│   │           ├── dashboard.html
│   │           ├── index.html
│   │           └── login.html
│   └── test
│       └── java
│           └── io
│               └── github
│                   └── davidmatei1902
│                       └── uni_assets
│                           └── UniAssetsApplicationTests.java
└── target
    ├── classes
    │   ├── application.properties
    │   ├── io
    │   │   └── github
    │   │       └── davidmatei1902
    │   │           └── uni_assets
    │   │               ├── UniAssetsApplication.class
    │   │               ├── controller
    │   │               │   └── AppController.class
    │   │               ├── model
    │   │               │   ├── Asset.class
    │   │               │   └── AssetStatus.class
    │   │               ├── repository
    │   │               │   └── AssetRepository.class
    │   │               └── service
    │   │                   └── DatabaseService.class
    │   ├── sql
    │   │   ├── ClearDatabaseRecords.sql
    │   │   ├── CreateAssetsDatabase.sql
    │   │   ├── DeleteDatabase_CAUTION.sql
    │   │   ├── InsertData.sql
    │   │   └── QuerryAllTables.sql
    │   ├── static
    │   │   ├── css
    │   │   │   ├── dashboard.css
    │   │   │   ├── global.css
    │   │   │   ├── index.css
    │   │   │   └── login.css
    │   │   └── images
    │   │       ├── university_assets_3d_printer.jpg
    │   │       ├── university_assets_destkop_workstation.jpg
    │   │       ├── university_assets_laptop.jpg
    │   │       ├── university_assets_mesh_router.jpg
    │   │       ├── university_assets_multimeter.jpg
    │   │       ├── university_assets_projector.jpg
    │   │       ├── university_assets_robot_arm.jpg
    │   │       ├── university_assets_screen_classroom.jpg
    │   │       ├── university_exterior_1.jpg
    │   │       ├── university_exterior_2.jpg
    │   │       ├── university_faculty_business_management.jpg
    │   │       ├── university_faculty_chemical_engineering.jpg
    │   │       ├── university_faculty_computer_science_1.jpg
    │   │       ├── university_faculty_computer_science_2.jpg
    │   │       ├── university_faculty_computer_science_3.jpg
    │   │       ├── university_faculty_electronic_engineering.jpg
    │   │       ├── university_faculty_mechanical_engineering.jpg
    │   │       ├── university_faculty_transport_engineering.jpg
    │   │       ├── university_interior_chairs_1.jpg
    │   │       ├── university_interior_laptop_1.jpg
    │   │       ├── university_interior_lecture_1.jpg
    │   │       ├── university_interior_lecture_2.jpg
    │   │       ├── university_interior_library_1.jpg
    │   │       └── university_laboratories.jpg
    │   └── templates
    │       ├── dashboard.html
    │       ├── index.html
    │       └── login.html
    ├── generated-sources
    │   └── annotations
    └── maven-status
        └── maven-compiler-plugin
            └── compile
                └── default-compile
                    └── inputFiles.lst
```

## Configuration

1.  Create a database named `EvidentaDotarilor` in your SQL Server.
2.  Open `src/main/resources/application.properties`.
3.  Update the database connection details:

```properties
# Database Connection
spring.datasource.url=jdbc:sqlserver://localhost;databaseName=EvidentaDotarilor;encrypt=true;trustServerCertificate=true;
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

# Driver & Dialect
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.jpa.hibernate.ddl-auto=update

# Thymeleaf
spring.thymeleaf.cache=false
```

> **Note:** Replace `YOUR_DB_USERNAME` and `YOUR_DB_PASSWORD` with your actual SQL Server credentials. If using a named instance (like SQLEXPRESS), ensure the URL reflects that (e.g., `jdbc:sqlserver://localhost\\SQLEXPRESS;...`).

---

# Running the Application

### Using Maven Wrapper (Recommended)

Open a terminal in the project root and run:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

### Using an IDE (IntelliJ / Eclipse / VS Code)
1.  Import the folder as a **Maven Project**.
2.  Reload Maven dependencies.
3.  Locate `src/main/java/.../UniAssetsApplication.java`.
4.  Run the `main` method.

---

# Accessing the Interface

Once the application logs show `Started UniAssetsApplication`, open your browser:

**http://localhost:8080**

- **Home**: Landing page.
- **Dashboard**: View and manage assets.
- **Login**: Admin authentication (if configured).

---

# Common Issues

| Issue | Possible Solution |
| :--- | :--- |
| **Dependencies Not Found** | Run `./mvnw clean install` to force download dependencies. |
| **Connection Refused (SQL)** | Ensure SQL Server is running and **TCP/IP** protocol is enabled in *SQL Server Configuration Manager*. |
| **Port 8080 in use** | Change the port in `application.properties`: `server.port=8081`. |
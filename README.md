# UniAssets

UniAssets is a full-stack University Assets Management application, featuring a web-based front-end, a Spring Boot back-end, and a SQL Server database to efficiently track, manage, and maintain university assets.
## Features
- **Database Management**: Create, update, and delete records for university assets.
- **Data Insertion**: Add new assets easily into the database.
- **Record Viewing**: Retrieve and display detailed asset information via a web interface.
- **Safe Operations**: Includes safeguards for critical actions like database deletion.

## Technologies & Dependencies

### Backend
- **Spring Boot (Spring Web)**: Handles the backend logic, RESTful APIs, and connects to the database. Uses embedded Apache Tomcat.

### Frontend
- **Thymeleaf**: Server-side template engine that renders dynamic HTML pages with data from Spring Boot.

### Database
- **MS SQL Server Driver**: JDBC driver for connecting Spring Boot to SQL Server (`EvidentaDotarilor` database).
- **JDBC API**: Java standard API for executing SQL queries and interacting with the database.
---

# How to Run UniAssets

This guide explains how to run the **UniAssets** University Assets Management system locally.

## Prerequisites

Make sure you have:

- **Java 23** installed and `JAVA_HOME` set
- **Maven** installed (or use the included `mvnw` wrapper)
- **SQL Server** with the `EvidentaDotarilor` database created
- Internet connection (for downloading dependencies)

---

### Project Structure
```text
├── HELP.md
├── README.md
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── io
│   │   │       └── github
│   │   │           └── davidmatei1902
│   │   │               └── uni_assets
│   │   │                   └── UniAssetsApplication.java
│   │   └── resources
│   │       ├── application.properties
│   │       ├── static
│   │       └── templates
│   │           └── index.html
│   └── test
│       └── java
│           └── io
│               └── github
│                   └── davidmatei1902
│                       └── uni_assets
│                           └── UniAssetsApplicationTests.java
└── target
    ├── classes
    └── generated-sources
```

- `src/main/java` → Java backend (Spring Boot)
- `src/main/resources` → configuration, static files, templates
- `pom.xml` → Maven dependencies
- `mvnw` / `mvnw.cmd` → Maven wrapper
## Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=EvidentaDotarilor
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.jpa.hibernate.ddl-auto=update
spring.thymeleaf.cache=false
```

Replace *YOUR_DB_USERNAME* and *YOUR_DB_PASSWORD* with your SQL Server credentials.

---

# Running the Application

## Using Mavem Wrapper

```bash
    ./mvnw spring-boot:run    # Linux / macOS
    mvnw.cmd spring-boot:run  # Windows
```
## Using IDE
- Import the project as a Maven project.
- Set Java 23 as the SDK.
- Run UniAssetsApplication.java main method.

---

# Accessing the Web Interface

Open your browser at:

```html
    http://localhost:8080
```
- The homepage is rendered using Thymeleaf.
- You can view, add, and manage university assets.
---

# Common Issues

- **Dependencies Not Found**
    - Run `./mvnw clean install` (Linux/macOS) or `mvnw.cmd clean install` (Windows).
- *TBA* 
    - Additional known issues will be documented here as they are discovered.
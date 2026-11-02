# Smart Emergency Assistance System

Java Swing desktop academic demonstration using MySQL. The project combines the supplied interface and emergency workflows with database persistence.

## Project team

- Anushka Anoop Arakkal
- Safreen Fathima S
- Shabin S
- Thejas Vinod

Project faculty: Ms. Reshma N Pillai  
Institution and department details follow the supplied sample report: Marian Engineering College, Department of Computer Science and Engineering.

## Requirements

- JDK 21 (already installed on the student's system)
- IntelliJ IDEA or another Java IDE (optional, recommended)
- MySQL Server (already installed)
- IntelliJ can load Maven from `pom.xml` and fetch MySQL Connector/J; no separate Maven installation is needed for that workflow.

## Set up MySQL

1. Open MySQL Workbench or the MySQL command-line client and connect to your local server.
2. For a new database, run `database/schema.sql`. If you already ran the original SQL from the chat, back up `emergency_db` and run `database/upgrade-existing.sql` once instead.
3. Set the database environment variables in Windows before starting the app:

```powershell
$env:SEAS_DB_URL = "jdbc:mysql://localhost:3306/emergency_db"
$env:SEAS_DB_USER = "root"
$env:SEAS_DB_PASSWORD = "your_mysql_password"
```

Replace the username and password with the account you use for MySQL. Keep the password out of source files.

## Open and run

1. In IntelliJ choose **Open** and select this folder (the one containing `pom.xml`).
2. Allow IntelliJ to load the Maven project and download MySQL Connector/J.
3. Run `src/main/java/com/seas/Main.java`.

The first dashboard refresh checks the database connection. If it fails, verify that MySQL Server is running, that the schema script completed, and that the connection variables match your MySQL account.

## Main functions

- Register a user and record an emergency.
- Add and view ambulances and hospitals.
- Dispatch the first available demonstration ambulance to a selected hospital.
- Record vitals and update an emergency's status.

This prototype does not provide GPS routing, real dispatch, SMS/email alerts, or clinical decision support. Use it only as an academic demonstration, not to contact emergency services.

## Database note

`database/schema.sql` is for a fresh setup. `database/upgrade-existing.sql` adds the fields this version needs to the earlier schema. Back up the database before running the upgrade. The Java password field stores a salted PBKDF2 hash, not the entered password.

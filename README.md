
# TinyDB - A Lightweight Database Management System

TinyDB is a simplified database management system built as part of a project for CSCI 5408. The project involves creating core database components, implementing key database functionalities, and ensuring security through custom solutions. The system handles basic database operations and transaction processing with an emphasis on modular design.

---

## Features and Modules

### 1. **Database Design**
   - **Data Structures:** Utilizes custom linear data structures for query and data processing.
   - **Custom File Format:** Data is stored in a persistent custom file format (non-JSON/XML/CSV/Serialized/binary).
   - **Metadata Management:** Maintains a data dictionary in the same custom format for metadata storage.

### 2. **Query Implementation**
   - Implements and validates standard SQL commands:
     - `CREATE DATABASE`, `USE DATABASE`, `CREATE TABLE`, `INSERT INTO`, `SELECT FROM`, `UPDATE`, `DELETE`, `DROP TABLE`.
   - Supports single `WHERE` conditions for queries (logical operators like AND/OR are excluded).

### 3. **Transaction Processing**
   - Identifies and distinguishes transactions from normal queries.
   - Ensures ACID compliance for all transactions through in-memory operations.
   - Transactions are processed using logs and written to persistent storage only after validation.

### 4. **Log Management**
   - **General Logs:** Tracks query execution time and database state (e.g., number of tables, records).
   - **Event Logs:** Captures database changes, concurrent transactions, and crash reports.
   - **Query Logs:** Records user queries along with timestamps.

### 5. **Data Modeling and Reverse Engineering**
   - Generates a text-based Entity-Relationship Diagram (ERD) based on current database state.
   - ERD includes tables, columns, relationships, and cardinality derived from metadata.

### 6. **Exporting Data**
   - Provides SQL-format exports of database structure and values.
   - Captures the current state of the database, including any recent updates.

### 7. **User Interface and Security**
   - **Console-Based UI:** Simple and menu-driven.
   - **User Authentication:** Features registration and login with:
     - UserID and password hashing (e.g., MD5, SHA-1).
     - Security questions for added authentication.
   - Offers the following options post-login:
     - Execute queries (normal and transactional).
     - Export database structure and data.
     - Generate ERD.
     - Exit the application.

---

## Technical Details

### **Programming Environment**
- Language: **Java** (no third-party libraries).
- Runtime: Standard Java Runtime Environment.
- IDE: Any standard code editor for writing and compiling Java code.

### **Security**
- Hashing algorithms (e.g., MD5, SHA-1) for password storage.
- Custom file format to prevent common vulnerabilities.

---

## Deliverables

### **Sprints**
1. **Sprint 1:**
   - Implementation of at least 3 modules with functional testing.
   - Deliverables: Background research, architecture diagram, pseudocode, Git repository, test cases.
2. **Sprint 2:**
   - Additional 3 modules with functional testing.
   - Deliverables: Enhanced pseudocode, Git repository updates, evidence of testing.
3. **Sprint 3:**
   - Final module implementation and modifications.
   - Deliverables: Final product presentation, Git repository, final test results.

---

## Installation and Usage

1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/tinydb.git
   cd tinydb
   ```

2. Compile the Java files:
   ```bash
   javac Main.java
   ```

3. Run the application:
   ```bash
   java Main
   ```

---

## Team

- **Team Leader:** Axata Darji
- **Team Members:** [Add team members' names]

---

## License

This project is protected under the Dalhousie University Academic Integrity policy. Unauthorized sharing or reproduction is prohibited.

---

## Contact

For queries, reach out to:
- **Email:** [team-email@example.com]

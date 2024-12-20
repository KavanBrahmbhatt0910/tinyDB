
# TinyDB - A Lightweight Database Management System

TinyDB is a simplified database management system developed as part of the CSCI 5408 course. This project was divided into three sprints, each focusing on core DBMS functionalities, implementing critical modules, and ensuring secure, modular, and user-friendly operations.

---

## Features and Modules

### **Sprint 1**
1. **Database Design and Query Implementation**:
   - Data is stored in structured text files with metadata maintained for each database and table.
   - Supported SQL commands: 
     - `CREATE DATABASE`, `USE DATABASE`, `CREATE TABLE`, `INSERT INTO`, `SELECT`, `UPDATE`, `DELETE`, `DROP TABLE`.
   - Simple data types supported: `int`, `string`, `double`.

2. **Transaction Management**:
   - Implemented `START TRANSACTION`, `COMMIT`, and `ROLLBACK`.
   - Ensures ACID compliance in a single-user environment.

3. **Testing and Evidence**:
   - Developed and validated test cases using `mvn test` for implemented modules.

---

### **Sprint 2**
1. **Transaction Enhancements**:
   - Transactions include multiple queries with proper rollback and commit functionalities.
   - Support for parallel transactions is under development.

2. **Export Database Structure and Data**:
   - Generates SQL dump files with both structure and values, maintaining data integrity.

3. **User Interface and Security**:
   - **Registration**:
     - Email validation and hashed passwords.
     - Security questions for additional verification.
   - **Login**:
     - Secure login with hashed password verification.
     - User options:
       1. Execute Queries
       2. Export Data and Structure
       3. Generate ERD
       4. Exit

4. **Testing and Evidence**:
   - Verified user management and export functionalities.
   - Captured test results for all modules.

---

### **Sprint 3**
1. **Reverse Engineering (Data Modeling)**:
   - Automatically generates an Entity-Relationship Diagram (ERD) for a selected database.
   - Captures table relationships, column structures, and metadata.

2. **Enhanced User Menu**:
   - Options to:
     - Write queries.
     - Export data and structure.
     - Generate ERD.
     - Exit.

3. **Testing and Evidence**:
   - Verified ERD generation with sample databases.
   - Captured test results for each feature, demonstrating file structures, tables, and the generated ERD.

---

## Technical Details

### **Programming Environment**
- Language: **Java** (no third-party libraries).
- Runtime: Standard Java Runtime Environment.
- IDE: Any standard code editor for writing and compiling Java code.

### **Security**
- User credentials hashed using MD5 for secure storage.
- Security questions for enhanced verification.

---

## Installation and Usage

1. Clone the repository:
   ```bash
   git clone https://github.com/KavanBrahmbhatt0910/tinyDB.git
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

## License

This project is protected under the Dalhousie University Academic Integrity policy. Unauthorized sharing or reproduction is prohibited.

---

## Contact

For any queries or support, feel free to reach out to:

**Kavankumar Brahmbhatt**  
[Email](mailto:kavanbrahmbhatt0910@gmail.com) | [GitHub](https://github.com/KavanBrahmbhatt0910)

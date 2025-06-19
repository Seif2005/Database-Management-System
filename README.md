# Database Management System

A lightweight, extensible Database Management System (DBMS) built in Java for educational purposes. This project demonstrates the fundamental design and implementation of a DBMS, supporting basic relational operations, data paging, and bitmap indexing.

![Java](https://img.shields.io/badge/language-Java-orange)
![Status](https://img.shields.io/badge/status-experimental-yellow)
![License](https://img.shields.io/github/license/Seif2005/Database-Management-System)

---

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgements](#acknowledgements)

---

## Features

- **Table Creation**: Define tables with arbitrary columns.
- **Data Insertion**: Insert records into tables using arrays of values.
- **Selection (Querying)**:
  - Select all records from a table.
  - Select records by page and record number (data paging).
  - Select records using column-value conditions.
- **Bitmap Indexing**: Support for bitmap indexes on columns for efficient querying.
- **Data Validation & Recovery**:
  - Validate records and recover missing data pages.
  - Generate full trace logs for operations and table state.
- **Trace Logging**: Retrieve trace logs of all operations for debugging and audit.
- **Test Suite**: Includes comprehensive JUnit tests covering table creation, insertion, querying, and trace features.

---

## Getting Started

### Prerequisites

- Java JDK 8 or above
- (Optional for building) [JUnit 4](https://junit.org/junit4/)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Seif2005/Database-Management-System.git
   cd Database-Management-System
   ```

2. **Compile the project:**
   ```bash
   javac DBApp.java
   ```

3. **Run the main application:**
   ```bash
   java DBApp
   ```

---

## Usage

### Example: Creating a Table and Inserting Records

```java
String[] columns = {"id", "name", "major", "semester", "gpa"};
DBApp.createTable("student", columns);

String[] r1 = {"1", "stud1", "CS", "5", "0.9"};
DBApp.insert("student", r1);

String[] r2 = {"2", "stud2", "BI", "7", "1.2"};
DBApp.insert("student", r2);

// Selecting all records
ArrayList<String[]> allRecords = DBApp.select("student");

// Selecting with conditions
String[] condCols = {"major"};
String[] condVals = {"CS"};
ArrayList<String[]> csStudents = DBApp.select("student", condCols, condVals);

// Paging
ArrayList<String[]> page = DBApp.select("student", 0, 1);

// Get operation trace
String trace = DBApp.getFullTrace("student");
```

### Running Tests

JUnit test cases are available under `Tests/MS1 Tests/`. To run all tests:

```bash
javac -cp .:junit-4.12.jar Tests/MS1\ Tests/DBAppTests17.java
java -cp .:junit-4.12.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore Tests.MS1\ Tests.DBAppTests17
```

---

## Project Structure

```
Database-Management-System/
├── DBApp.java         # Main API for the DBMS
├── Table.java         # Table data structure and logic
├── FileManager.java   # Handles persistence and storage (not shown)
├── BitmapIndex.java   # Bitmap indexing for columns (not shown)
├── Page.java          # Data page abstraction (not shown)
└── Tests/
    └── MS1 Tests/
        └── DBAppTests17.java   # JUnit test suite
```

---

## Contributing

Contributions are welcome! Please open an issue to discuss any feature or bug, or submit a pull request.

1. Fork the repo
2. Create your feature branch (`git checkout -b feature/YourFeature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin feature/YourFeature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Acknowledgements

- Inspired by classic database textbooks and university assignments.
- Thanks to all contributors and testers.

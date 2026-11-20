# Chocoholics Anonymous (ChocAn) — CS-200 Final Project

**Team:** CodeJutsu (Team 15)  
**Members:** Simeon, Daniel, Cameron, Jackson, Teal'c

## Project Mission
**Chocoholics Anonymous (ChocAn)** is an organization dedicated to helping people addicted to chocolate in all its glorious forms. For a monthly fee, members receive unlimited consultations and treatments with health care professionals (dietitians, internists, and exercise experts) using a nine-digit member card at provider terminals.

The mission of this project is to develop a reliable, efficient software system and terminal interface for ChocAn—supporting both **Local Persistent Storage Mode** and **Neon PostgreSQL Cloud Database Mode**—to support the organization's core operations:
- **Provider Services:** Validating 9-digit member cards (`Validated`, `Invalid number`, `Member suspended`), recording health care consultations/services with 6-digit service code verification and fee lookup, and requesting/saving the alphabetical Provider Directory.
- **Operator Management:** Maintaining accurate member and provider records (adding, updating, and deleting profiles with ChocAn field length validations).
- **Accounting Procedure & Managerial Reporting:** Running the Friday Midnight Main Accounting Procedure and generating individual Member Reports (`<MemberName>_<Date>.txt`), Provider Reports (`<ProviderName>_<Date>.txt`), Accounts Payable Summary Reports (`Summary_Report_<Date>.txt`), and Electronic Funds Transfer (`EFT_Data_<Date>.txt`) files.

---

## Team Roles & Class Author Attribution
- **Simeon (Backend & Database):** `ChocAN_DB.java`, `AuthService.java`, backend integration, and persistent data storage (Local & Neon PostgreSQL Cloud DB).
- **Daniel (Frontend & UML):** `ChocAnApp.java`, `ChocAnGUI.java`, `User.java`, `Role.java`, terminal interface design, and final UML Class Diagram.
- **Jackson (Provider Features):** `ProviderDirectory.java`, `ServiceEntry.java`, `ServiceRecord.java`, member validation, and provider billing workflows.
- **Teal'c (Operator Features):** `Operator.java`, `Member.java`, `Provider.java`, member and provider record management.
- **Cameron (Manager Features):** `Manager.java`, `ReportService.java`, `ManagerTest.java`, Main Accounting Procedure, Member Reports, Provider Reports, Accounts Payable Summary Report, and EFT data generation.

---

## How to Compile and Run

### Prerequisites
- **Java Development Kit (JDK):** Java 17 or higher
- **PostgreSQL JDBC Driver:** Included at `lib/postgresql-42.7.13.jar`

### 1. Compile the Project
From the project root directory (`CS-200 Final Project`):

**Windows (PowerShell / Command Prompt):**
```powershell
javac -cp "lib/postgresql-42.7.13.jar;src" -d "out/production/CS-200 Final Project" src/*.java
```

**macOS / Linux:**
```bash
javac -cp "lib/postgresql-42.7.13.jar:src" -d "out/production/CS-200 Final Project" src/*.java
```

### 2. Run the Command-Line Terminal (`ChocAnApp`)
```powershell
java -cp "lib/postgresql-42.7.13.jar;out/production/CS-200 Final Project" ChocAnApp
```

### 3. Run the Graphical User Interface (`ChocAnGUI` — Optional Extra Credit)
You can type `gui` at the command-line login prompt, or launch the GUI directly:
```powershell
java -cp "lib/postgresql-42.7.13.jar;out/production/CS-200 Final Project" ChocAnGUI
```

### Test Credentials
| Role | Username | Password | Associated ID / Notes |
| :--- | :--- | :--- | :--- |
| **Operator** | `operator1` | `1984` | Full Member & Provider CRUD |
| **Manager** | `manager1` | `fishes` | Main Accounting Procedure, Member/Provider/Summary/EFT Reports |
| **Provider** | `provider1` | `HotPotato` | Active Provider `111222333` (Dr. Sweet Tooth) |
| **Provider** | `provider2` | `CocoaCare` | Active Provider `444555666` (Choc Care Clinic) |
| **Provider ID Login** | `111222333` | `111222333` | Direct 9-digit Provider Number login |

---

## Final UML Class Diagram

```mermaid
classDiagram
    class Role {
        <<enumeration>>
        PROVIDER
        MANAGER
        OPERATOR
    }

    class User {
        -String username
        -String password
        -Role role
        -String providerId
        +getUsername() String
        +checkPassword(String) boolean
        +getRole() Role
        +getProviderId() String
        +setProviderId(String) void
    }

    class AuthService {
        -ArrayList~User~ users
        +login(String, String) User
    }

    class Member {
        -String memberId
        -String name
        -String address
        -String city
        -String state
        -String zip
        -boolean suspended
        +getMemberId() String
        +getName() String
        +getAddress() String
        +getCity() String
        +getState() String
        +getZip() String
        +isSuspended() boolean
    }

    class Provider {
        -String providerId
        -String name
        -String address
        -String city
        -String state
        -String zip
        +getProviderId() String
        +getName() String
        +getAddress() String
        +getCity() String
        +getState() String
        +getZip() String
    }

    class ServiceEntry {
        -String serviceCode
        -String serviceName
        -double fee
        +getServiceCode() String
        +getServiceName() String
        +getFee() double
    }

    class ProviderDirectory {
        -List~ServiceEntry~ directory$
        +getAllServices()$ List~ServiceEntry~
        +findServiceByCode(String)$ ServiceEntry
        +formatDirectory()$ String
        +writeDirectoryToFile()$ File
    }

    class ServiceRecord {
        -String currentDateTime
        -String serviceDate
        -String providerId
        -String memberId
        -String serviceCode
        -String comments
        +getCurrentDateTime() String
        +getServiceDate() String
        +getProviderId() String
        +getMemberId() String
        +getServiceCode() String
        +getComments() String
        +getServiceName() String
        +getFee() double
    }

    class Operator {
        -boolean useDatabase$
        -List~Member~ localMembers$
        -List~Provider~ localProviders$
        -List~ServiceRecord~ localServiceRecords$
        +validateMemberCard(String)$ String
        +addMember(Member)$ boolean
        +updateMember(Member)$ boolean
        +deleteMember(String)$ boolean
        +addProvider(Provider)$ boolean
        +updateProvider(Provider)$ boolean
        +deleteProvider(String)$ boolean
        +addServiceRecord(ServiceRecord)$ boolean
        +getServiceRecords()$ List~ServiceRecord~
        +billChocAn(Scanner, String)$ void
    }

    class Manager {
        +getServiceRecords()$ List~ServiceRecord~
        +addServiceRecord(ServiceRecord)$ void
        +generateMemberReport(String)$ String
        +generateProviderReport(String)$ String
        +generateSummaryReport()$ String
        +searchProvidersToPay(String)$ String
        +managerMenu(Scanner)$ void
    }

    class ReportService {
        +runMainAccountingProcedure()$ String
        +generateMemberReport(Member)$ String
        +generateAllMemberReports(boolean)$ String
        +generateProviderReport(Provider)$ String
        +generateAllProviderReports(boolean)$ String
        +generateSummaryReport()$ String
        +generateEftReport()$ String
    }

    class ChocAN_DB {
        -String dbUrl$
        +initializeDatabase()$ boolean
        +addMember(Member)$ boolean
        +getAllMembers()$ List~Member~
        +updateMember(Member)$ boolean
        +deleteMember(String)$ boolean
        +addProvider(Provider)$ boolean
        +getAllProviders()$ List~Provider~
        +updateProvider(Provider)$ boolean
        +deleteProvider(String)$ boolean
        +addServiceRecord(ServiceRecord)$ boolean
        +getAllServiceRecords()$ List~ServiceRecord~
    }

    class ChocAnApp {
        +main(String[] args)$ void
        +selectMode(Scanner)$ void
        +operatorMenu(Scanner)$ void
        +managerMenu(Scanner)$ void
        +providerMenu(Scanner, User)$ void
    }

    class ChocAnGUI {
        +main(String[] args)$ void
    }

    User --> Role
    AuthService --> User
    AuthService --> Operator
    ChocAnApp --> AuthService
    ChocAnApp --> Operator
    ChocAnApp --> Manager
    ChocAnApp --> ProviderDirectory
    ChocAnApp --> ReportService
    ChocAnGUI --> AuthService
    ChocAnGUI --> Operator
    ChocAnGUI --> Manager
    ChocAnGUI --> ProviderDirectory
    ChocAnGUI --> ReportService
    Manager --> ReportService
    Manager --> Operator
    ProviderDirectory "1" *-- "*" ServiceEntry
    ServiceRecord --> ProviderDirectory
    Operator --> ChocAN_DB
    Operator "1" o-- "*" Member
    Operator "1" o-- "*" Provider
    Operator "1" o-- "*" ServiceRecord
    ReportService --> Operator
```

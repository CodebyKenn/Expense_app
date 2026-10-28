# Pockit App - System & Data Architecture Diagrams

This document contains the Entity-Relationship Diagram (ERD), Architecture Diagram, and Data Flow Diagrams for the Pockit Personal Finance Android application.

---

## 1. Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    users {
        String userId PK "User Identifier (e.g. email)"
        String displayName "Display Name"
    }

    accounts {
        Long id PK "Auto-generated ID"
        String userId FK "Belongs to user"
        String name "Account name (e.g. M-Pesa, Cash, Bank)"
        String iconKey "Icon key for UI"
        Double openingBalance "Initial account balance"
    }

    categories {
        Long id PK "Auto-generated ID"
        String name "Category name (e.g. Food, Salary)"
        String type "EXPENSE or INCOME"
        String iconKey "Icon key for UI"
    }

    records {
        Long id PK "Auto-generated ID"
        String userId FK "Belongs to user"
        String type "EXPENSE, INCOME, or TRANSFER"
        Double amount "Transaction amount"
        Long accountId FK "Source account ID"
        Long toAccountId FK "Destination account ID (Transfers only)"
        Long categoryId FK "Category ID (Expense & Income only)"
        String note "Transaction description / note"
        Long timestamp "Epoch milliseconds date/time"
    }

    budgets {
        Long id PK "Auto-generated ID"
        String userId FK "Belongs to user"
        String month "Format YYYY-MM"
        Long categoryId FK "Expense Category ID"
        Double limitAmount "Monthly spending limit"
    }

    users ||--o{ accounts : "owns"
    users ||--o{ records : "creates"
    users ||--o{ budgets : "sets"
    accounts ||--o{ records : "source for"
    accounts ||--o{ records : "destination for"
    categories ||--o{ records : "categorizes"
    categories ||--o{ budgets : "budgeted under"
```

---

## 2. System Architecture Diagram

```mermaid
graph TD
    subgraph UI_Layer["UI Layer (Views & Fragments)"]
        A1["DashboardFragment"]
        A2["AddExpenseFragment"]
        A3["SummaryFragment"]
        A4["BudgetFragment"]
        A5["WalletFragment"]
    end

    subgraph ViewModel_Layer["ViewModel Layer"]
        VM1["DashboardViewModel"]
        VM2["RecordViewModel"]
        VM3["AnalysisViewModel"]
        VM4["BudgetViewModel"]
        VM5["AccountsViewModel"]
    end

    subgraph Data_Layer["Data Layer (Single Source of Truth)"]
        S["Session (Active UserId & DisplayName)"]
        R["PockitRepository"]
        
        subgraph DAOs["Room Data Access Objects (DAOs)"]
            D1["AccountDao"]
            D2["CategoryDao"]
            D3["RecordDao"]
            D4["BudgetDao"]
        end

        DB[("AppDatabase (SQLite / Room)")]
    end

    A1 --> VM1
    A2 --> VM2
    A3 --> VM3
    A4 --> VM4
    A5 --> VM5

    VM1 --> R
    VM2 --> R
    VM3 --> R
    VM4 --> R
    VM5 --> R

    R --> S
    R --> D1
    R --> D2
    R --> D3
    R --> D4

    D1 --> DB
    D2 --> DB
    D3 --> DB
    D4 --> DB
```

---

## 3. Data Flow Diagram (DFD)

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant UI as AddExpenseFragment
    participant VM as RecordViewModel / Repo
    participant DAO as RecordDao / AccountDao
    participant DB as Room Database (pockit.db)
    participant Dialog as SuccessDialog

    User->>UI: Selects Type (Expense/Income/Transfer), Amount, Account & Category
    User->>UI: Taps "SAVE TRANSACTION"
    UI->>VM: saveRecord(RecordInput)
    VM->>VM: Validate RecordInput (Amount > 0, Valid Accounts & Category)
    
    alt Validation Valid
        VM->>DAO: insert(MoneyRecord)
        DAO->>DB: SQL INSERT INTO records
        DB-->>DAO: Insert Success
        DAO-->>VM: Record Saved
        VM-->>UI: SaveResult.Success
        UI->>Dialog: Show SuccessDialog Popup ("Transaction Saved!")
        Dialog-->>User: Displays Checkmark Dialog with 'X' & Close options
    else Validation Invalid
        VM-->>UI: SaveResult.Invalid(RecordError)
        UI-->>User: Displays Inline Input Error
    end
```

# 🌐 Android Development for Web Developers: Pockit Codebase Guide

Welcome! If you come from a web background (React, HTML/CSS, Node.js, Express, SQL), Android development in Kotlin can initially seem different. This guide maps familiar web development concepts directly to the Pockit codebase so you can navigate and understand the code instantly.

---

## 🔄 Concept Mapping: Web vs Android (Pockit)

| Web Concept (React / Node.js) | Android Equivalent in Pockit | Pockit Code Location |
| :--- | :--- | :--- |
| **HTML / JSX** | XML Layout Files | `app/src/main/res/layout/*.xml` |
| **CSS Flexbox / Styling** | `LinearLayout`, `ConstraintLayout`, `colors.xml`, `drawables` | `res/values/colors.xml`, `res/drawable/*.xml` |
| **React Page / Component** | `Fragment` / `Activity` | `ui/expenses/AddExpenseFragment.kt` |
| **React State (`useState`)** | `LiveData` / `StateFlow` inside `ViewModel` | `ui/dashboard/DashboardViewModel.kt` |
| **React Router (`<Routes>`)** | Jetpack Navigation Component | `res/navigation/nav_graph.xml` |
| **Express.js API Routes** | `PockitRepository.kt` | `data/PockitRepository.kt` |
| **SQL Database / Prisma ORM** | Room SQLite Database & DAOs | `data/AppDatabase.kt`, `data/dao/*.kt` |
| **Async / Promises / `async-await`** | Kotlin Coroutines & `suspend` functions | `viewModelScope.launch { ... }` |
| **Global State (Redux / Context)** | `Session.kt` (User ID state) & `Repository` | `data/Session.kt` |

---

## 🏛️ How the Codebase is Structured

Pockit uses **MVVM (Model-View-ViewModel)** architecture. Think of it like a full-stack web app running locally on the phone:

```
[ XML Layouts + Fragments ] <---> [ ViewModel (State Holder) ] <---> [ PockitRepository ] <---> [ Room SQLite DB ]
  (HTML/CSS + React Component)          (React Custom Hook / State)          (Express Controller / Service)      (PostgreSQL / Prisma)
```

---

## 🔍 File-by-File Breakdown

### 1. UI Layer (HTML / React equivalents)
- **`MainActivity.kt`**: The single main entry window (like `index.html` + root `<App />` container in React).
- **`res/navigation/nav_graph.xml`**: Defines all app screens and transitions (like `react-router-dom` routes).
- **Fragments (`ui/*/*Fragment.kt`)**: Individual screen views (like page components in React):
  - `DashboardFragment.kt`: Home screen showing total remaining budget and recent transactions.
  - `AddExpenseFragment.kt`: Transaction entry form for Expense, Income, and Transfers.
  - `SummaryFragment.kt`: Financial analytics screen with Donut Chart and category breakdown.
  - `BudgetFragment.kt`: Category budget setup and progress bars.
  - `WalletFragment.kt`: Account balance management, reset data, and demo data loader.

### 2. ViewModel Layer (State & Event Handlers)
In web development, when a user types into an input or clicks a button, you update state (`useState`). In Android:
- ViewModels hold `LiveData` or `StateFlow` state variables.
- When data changes in ViewModel, the Fragment observes it and updates the UI automatically.
- Examples:
  - `DashboardViewModel.kt`: Computes overview spent/remaining and recent transaction items.
  - `AccountsViewModel.kt`: Manages account balances and reset/demo data triggers.
  - `AnalysisViewModel.kt`: Controls period filters (Day/Week/Month) and Expense/Income/Transfer chart breakdowns.

### 3. Data Layer (Backend API & Database)
- **`AppDatabase.kt`**: Creates and initializes the local SQLite database file (`pockit.db`).
- **`PockitRepository.kt`**: The central controller service. It validates user inputs before saving, calculates balances, and coordinates database operations.
- **DAOs (`data/dao/*Dao.kt`)**: Data Access Objects containing SQL queries (like SQL query files or ORM models):
  - `AccountDao.kt`: SQL queries for account balances.
  - `RecordDao.kt`: SQL queries for transaction records and category breakdown calculations.
  - `BudgetDao.kt`: SQL queries for category limits and monthly spent amounts.

---

## 🏃 Lifecycle of a User Action (Step-by-Step)

Here is what happens under the hood when a user adds a **New Expense**:

1. **User Action:** The user fills the amount, account, and category in `AddExpenseFragment.kt` and taps **SAVE TRANSACTION**.
2. **ViewModel / Repo Call:** `AddExpenseFragment` calls `PockitRepository.saveRecord(input)`.
3. **Validation (Backend Rules):** `RecordInput.validate()` checks that `amount > 0` and that valid `accountId` and `categoryId` exist.
4. **Database Query:** `RecordDao.insert(record)` inserts a row into SQLite table `records`.
5. **Reactive Flow Emission:** Room automatically detects a change in `records` table and emits a new list via `Flow`.
6. **UI Auto-Refresh:** ViewModels observing the Flow re-emit updated data to `DashboardFragment` and `WalletFragment`.
7. **Success Popup:** `AddExpenseFragment` displays `SuccessDialog.show(...)` with the transaction summary and navigates back.

---

## 💡 Quick Tips for Web Developers Modifying Code
1. **Changing Text / Colors / Spacing:** Modify `res/values/colors.xml` or `res/values/strings.xml`.
2. **Adding a New Database Column:** Modify the Data Entity in `data/model/`, increment database `version` in `AppDatabase.kt`, or recreate the database.
3. **Adding a New Screen:** Create a new `Fragment`, add a `<fragment>` destination in `nav_graph.xml`, and add a ViewModel if state management is needed.

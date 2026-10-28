# 💳 Pockit - Personal Expense & Budget Tracker

Pockit is a modern, intuitive, offline-first Android personal finance application built with Kotlin, Jetpack Navigation, Room SQLite Database, ViewModel, and Kotlin Coroutines/Flows. It enables users to seamlessly manage accounts, track income and expense transactions, manage transfers, set category budgets, and analyze spending habits with interactive charts.

---

## 🌟 Key Features

### 📊 1. Home Dashboard (`DashboardFragment`)
- **Dynamic Balance Overview**: Displays current monthly budget remaining, spent amount, total limit, and percentage used with an interactive progress indicator.
- **Time Window Filters**: Switch between Day, Week, and Month filters.
- **Recent Transactions**: Real-time listing of recent transactions with category icons, account tags, and timestamps.
- **Quick Action**: Direct button to record new transactions or log out.

### 💰 2. Wallet & Account Management (`WalletFragment`)
- **Total Balance Overview**: Displays total aggregated net balance across all accounts.
- **Multi-Account Support**: Manage accounts such as M-Pesa, Cash, Bank, PayPal, or custom user accounts.
- **Account Operations**:
  - Add new accounts with custom opening balances.
  - Edit account names and update balances.
  - Delete accounts and associated records.
- **Data Controls**:
  - **LOAD DUMMY DATA**: Instantly seeds sample transactions, accounts, and budgets for demo testing.
  - **RESET / CLEAR DATA**: Clears user data and restores default empty accounts with KSh 0 balance.

### ➕ 3. Transaction Entry (`AddExpenseFragment`)
- **Multi-Type Support**: Seamlessly log **Expense**, **Income**, or **Transfer** transactions.
- **Interactive Numeric Keypad**: Custom soft keypad with backspace, decimal point, and amount formatting.
- **Dynamic Dropdowns**:
  - Automatically loads accounts and categories from Room DB with high-contrast UI styling.
  - Shows "To Account" selection automatically when **Transfer** mode is selected.
- **Date & Time Selectors**: Date picker dialog and real-time clock time tag.
- **Custom Success Dialog (`SuccessDialog`)**: Replaces standard Toast messages with a dismissible popup dialog featuring a header 'X' close button, checkmark icon, and tap-outside-to-dismiss behavior.

### 📈 4. Financial Analysis & Summary (`SummaryFragment`)
- **Interactive Donut Chart (`DonutChartView`)**: Custom-rendered pie chart showing percentage share per category/account.
- **Multi-Category Analysis**: Toggle between **Expense**, **Income**, and **Transfer** analyses.
- **Period Navigation**: Filter analyses by Day, Week, or Month with date range navigation (`< Range >`).
- **Interactive Legend & Breakdown List**: Color-coded breakdown with exact percentage shares and amounts.

### 🎯 5. Category Budget Management (`BudgetFragment`)
- **Category Spending Progress**: Displays spent vs limit progress bars for each expense category.
- **Set & Edit Limits**: Quick limit setup per category for the current month.
- **Create Custom Budget Categories**: Button to create new custom expense categories (e.g. *Subscriptions*) with initial monthly limits.
- **Status Tags**: Clear indicators for "Left to spend", "Near limit", or "Over budget".

### 🔐 6. User Isolation & Authentication
- **Multi-User Data Isolation**: `Session.userId` isolates DB records per user email address, ensuring each user maintains their own private dataset.
- **Authentication**: Email/Password login and registration workflows.

---

## 🛠️ Technology Stack & Libraries

| Layer / Aspect | Technology / Library |
| :--- | :--- |
| **Language** | Kotlin 1.9+ |
| **Architecture** | MVVM (Model-View-ViewModel) + Repository Pattern |
| **UI Framework** | Android XML Layouts, ViewBinding, Material Components 3 |
| **Navigation** | Jetpack Navigation Component (`NavHostFragment`) |
| **Database** | Room Database (SQLite ORM) with KSP compiler |
| **Asynchrony** | Kotlin Coroutines (`viewModelScope`, `lifecycleScope`) & Kotlin `Flow` |
| **State Management** | `LiveData`, `MediatorLiveData`, `StateFlow` |
| **Testing** | JUnit, AndroidX Test Runner, Espresso |

---

## 📐 Architecture & Database Schema

Pockit follows Google's recommended Android app architecture guidelines:

```
[ UI Layer (Fragments) ] 
       │
       ▼ (Observes LiveData/Flow)
[ ViewModel Layer ]
       │
       ▼ (Coroutines / Async)
[ PockitRepository (Single Source of Truth) ]
       │
       ├───────────────────────┐
       ▼                       ▼
[ Room DAOs ]            [ Session ]
       │
       ▼
[ AppDatabase (pockit.db) ]
```

Detailed ERD and System Diagrams are documented in [`docs/SYSTEM_DIAGRAMS.md`](docs/SYSTEM_DIAGRAMS.md).

---

## 📁 Project Structure

```
app/src/main/java/com/group/pockit/
├── MainActivity.kt               # Main shell activity hosting NavHostFragment and BottomNav
├── data/
│   ├── AppDatabase.kt            # Room database configuration & category seed callback
│   ├── PockitRepository.kt       # Repository managing DAOs, validation, and demo seeding
│   ├── Session.kt                # Active user session holder
│   ├── dao/
│   │   ├── AccountDao.kt         # Account CRUD & balance calculations
│   │   ├── BudgetDao.kt          # Category budget limits & status queries
│   │   ├── CategoryDao.kt        # Category queries & inserts
│   │   └── RecordDao.kt          # MoneyRecord CRUD, totals & breakdown queries
│   └── model/                    # Data models & entity definitions (Account, Budget, Category, MoneyRecord)
├── ui/
│   ├── accounts/                 # AccountsViewModel
│   ├── analysis/                 # AnalysisViewModel
│   ├── auth/                     # LoginFragment, RegisterFragment, SplashFragment, WelcomeFragment
│   ├── budget/                   # BudgetFragment, BudgetViewModel
│   ├── dashboard/                # DashboardFragment, SummaryFragment, DonutChartView, TransactionAdapter
│   ├── expenses/                 # AddExpenseFragment
│   ├── record/                   # RecordViewModel
│   └── wallet/                   # WalletFragment
└── util/
    ├── DateRanges.kt             # Date range utilities & period calculations
    ├── Money.kt                  # Currency formatting helpers
    └── SuccessDialog.kt          # Custom dismissible popup success dialog
```

---

## 🚀 Building & Running the Project

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Android SDK 34/35 (Min SDK 26)

### Build Debug APK
Run the following Gradle task to compile and generate the testable APK:
```bash
./gradlew app:assembleDebug
```
The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Run Unit Tests
To execute local unit tests:
```bash
./gradlew test
```

---

## 📄 Documentation Links
- 📐 [System & Architecture Diagrams (ERD, DFD)](docs/SYSTEM_DIAGRAMS.md)

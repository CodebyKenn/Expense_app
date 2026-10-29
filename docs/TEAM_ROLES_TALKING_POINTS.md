# 👥 Team Roles & Presentation Codebase Guide

This document assigns specific codebase sections, files to present, and **4+ technical talking points** for every member of the 9-person project team during presentation and defense.

---

## 👑 1. Team Leader (You)
**Role Overview:** Architecture lead, overall project coordination, repository pattern, multi-user session management, and build integration.

### Files to Present & Open in IDE:
- [`PockitRepository.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/data/PockitRepository.kt)
- [`Session.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/data/Session.kt)
- [`MainActivity.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/MainActivity.kt)
- [`nav_graph.xml`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/res/navigation/nav_graph.xml)

### 4 Technical Talking Points:
1. **Architecture & MVVM Pattern:** Established the overall MVVM architecture and Repository pattern where `PockitRepository` acts as the single source of truth between Room SQLite DAOs and UI ViewModels.
2. **Session Management & User Isolation:** Designed `Session.kt` to dynamically bind `userId` from user logins, ensuring all SQL queries filter per-user data so logins are strictly isolated.
3. **Data Control & Reset Strategy:** Implemented `clearUserData()` and `forceSeedDemoData()` in the repository to support clean data wiping (restoring zero-balance default accounts) and manual dummy data seeding.
4. **Shell Activity & Navigation Architecture:** Configured `MainActivity.kt` with `NavHostFragment` and Jetpack Navigation Graph (`nav_graph.xml`) to manage global screen transitions and bottom bar visibility.

---

## 🛠️ 2. Backend Developer 1 (Database & Room Setup)
**Role Overview:** Room ORM configuration, AppDatabase singleton, Account Entity & DAO.

### Files to Present & Open in IDE:
- [`AppDatabase.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/data/AppDatabase.kt)
- [`AccountDao.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/data/dao/AccountDao.kt)
- [`Account.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/data/model/Account.kt)

### 4 Technical Talking Points:
1. **Room Database Singleton:** Built `AppDatabase.kt` using Room's `@Database` annotation and thread-safe double-checked locking singleton pattern.
2. **Database Seeding Callback:** Created the `SeedCategories` callback that automatically populates default Expense and Income categories upon initial SQLite creation.
3. **Dynamic Account Balance Calculation:** Authored the SQL query in `AccountDao.observeBalances` calculating balances dynamically: $\text{Opening Balance} + \text{Income} - \text{Expenses} - \text{Transfers Out} + \text{Transfers In}$.
4. **Account Operations:** Implemented CRUD queries in `AccountDao` including `insert`, `updateAccount` (editing balance and name), and user-scoped account deletion.

---

## 🛠️ 3. Backend Developer 2 (Record DAO & Validations)
**Role Overview:** MoneyRecord Entity, Record DAO, SQL aggregations, and transaction input validation rules.

### Files to Present & Open in IDE:
- [`RecordDao.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/data/dao/RecordDao.kt)
- [`MoneyRecord.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/data/model/MoneyRecord.kt)
- [`RecordInput.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/data/model/RecordInput.kt)

### 4 Technical Talking Points:
1. **Unified MoneyRecord Entity:** Designed `MoneyRecord` with support for `RecordType.EXPENSE`, `RecordType.INCOME`, and `RecordType.TRANSFER`.
2. **SQL Time-Window Aggregations:** Authored `observeTotal` query in `RecordDao` computing total income, expense, or transfers across dynamic epoch millisecond ranges.
3. **Category & Transfer Breakdown Queries:** Authored `observeTotalsByCategory` and `observeTransferTotals` using SQL `GROUP BY` and `JOIN` statements to generate slice percentages for the donut chart.
4. **Input Validation Logic:** Created `RecordInput.validate()` to enforce business constraints (e.g. amount > 0, required categories, and ensuring Transfer `fromAccount` and `toAccount` are different).

---

## 🛠️ 4. Backend Developer 3 (Budget DAO & Reactive Streams)
**Role Overview:** Budget Entity, Budget DAO, Kotlin Flow reactive streams, and date range shift utilities.

### Files to Present & Open in IDE:
- [`BudgetDao.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/data/dao/BudgetDao.kt)
- [`Budget.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/data/model/Budget.kt)
- [`DateRanges.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/util/DateRanges.kt)

### 4 Technical Talking Points:
1. **Budget Entity & Upsert Query:** Created `Budget` entity indexed by `userId`, `month` (`YYYY-MM`), and `categoryId`, using `@Insert(onConflict = REPLACE)` to handle budget updates.
2. **Complex Budget Status JOIN Query:** Developed `BudgetDao.observeStatus` query joining `categories` with `budgets` and subqueries on `records` to produce spent-vs-limit status per category in one query.
3. **Reactive Kotlin Flow Integration:** Integrated cold `Flow` streams across DAOs so Room automatically emits new data to ViewModels when database rows change.
4. **Date Range Shifting Utilities:** Built `DateRanges.kt` providing start/end timestamp calculations and date navigation (`shift`) for Day, Week, and Month filters.

---

## 📱 5. Frontend Developer 1 (Dashboard & Nav Bar)
**Role Overview:** Dashboard UI Fragment, Recent Transactions RecyclerView, ViewModel MediatorLiveData, and Navigation Bar styling.

### Files to Present & Open in IDE:
- [`DashboardFragment.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/ui/dashboard/DashboardFragment.kt)
- [`DashboardViewModel.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/ui/dashboard/DashboardViewModel.kt)
- [`TransactionAdapter.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/ui/dashboard/TransactionAdapter.kt)

### 4 Technical Talking Points:
1. **Dashboard Fragment & ViewBinding:** Implemented `DashboardFragment` utilizing ViewBinding for safe, null-free view references.
2. **MediatorLiveData State Combination:** Used `MediatorLiveData` in `DashboardViewModel` to combine income, expense, and recent records streams into a single `DashboardState`.
3. **Transaction RecyclerView Adapter:** Built `TransactionAdapter` binding transaction items with formatted amounts, account badges, and date strings.
4. **Single Active Nav Bar Indicator:** Resolved dual-background styling in BottomNavigationView by setting `isItemActiveIndicatorEnabled = false` and configuring custom active pill shapes.

---

## 📱 6. Frontend Developer 2 (Add Transaction & Custom Keypad)
**Role Overview:** Add Expense/Income/Transfer form, soft numeric keypad, dynamic To-Account toggles, high-contrast Spinners.

### Files to Present & Open in IDE:
- [`AddExpenseFragment.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/ui/expenses/AddExpenseFragment.kt)
- [`fragment_add_expense.xml`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/res/layout/fragment_add_expense.xml)
- [`spinner_item.xml`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/res/layout/spinner_item.xml)

### 4 Technical Talking Points:
1. **Multi-Type Transaction Form:** Implemented `AddExpenseFragment` handling Expense, Income, and Transfer modes seamlessly.
2. **Custom Soft Keypad Logic:** Developed custom keypad event handling (digits 0-9, single decimal check, backspace) without bringing up system soft keyboard.
3. **Dynamic To-Account Visibility:** Implemented UI switching for Transfer mode: automatically hides Category dropdown and shows "To Account" spinner.
4. **High-Contrast Spinner Items:** Solved black-on-black dropdown text issues by creating `spinner_item.xml` and `spinner_dropdown_item.xml` with explicit white backgrounds (`#FFFFFF`) and dark text (`#46044B`).

---

## 📱 7. Frontend Developer 3 (Donut Chart & Budget UI)
**Role Overview:** Summary/Analysis screen, custom DonutChartView canvas drawing, Expense/Income/Transfer analysis tabs, Budget screen UI.

### Files to Present & Open in IDE:
- [`SummaryFragment.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/ui/dashboard/SummaryFragment.kt)
- [`DonutCHartView.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/ui/dashboard/DonutCHartView.kt)
- [`BudgetFragment.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/ui/budget/BudgetFragment.kt)

### 4 Technical Talking Points:
1. **Custom Canvas Donut Chart:** Implemented `DonutChartView` extending Android `View` and drawing arc slices on Canvas for percentage distributions.
2. **Multi-Type Analysis Tabs:** Integrated Expense, Income, and Transfer selector tabs updating chart data, legends, and list adapters dynamically.
3. **Budget Progress Bars & Color-Coding:** Developed `BudgetAdapter` with progress bars dynamically colored Red (Over limit), Amber (Near limit), or Purple (Normal).
4. **Add Budget Category Dialog:** Built "+ ADD BUDGET CATEGORY" workflow allowing users to create new custom categories with limits.

---

## 🎨 8. UI/UX Designer
**Role Overview:** Design tokens, Pockit color theme palette, custom XML shape drawables, and SuccessDialog layout.

### Files to Present & Open in IDE:
- [`colors.xml`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/res/values/colors.xml)
- [`dialog_success.xml`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/res/layout/dialog_success.xml)
- [`SuccessDialog.kt`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/util/SuccessDialog.kt)
- [`p2_bg_purple_card.xml`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/res/drawable/p2_bg_purple_card.xml)

### 4 Technical Talking Points:
1. **Cohesive Color Palette:** Defined centralized design tokens in `colors.xml` featuring Cream (`#F8F1DC`), Deep Purple (`#46044B`), and Lavender (`#E3D3F2`).
2. **Custom Shape Drawables:** Designed rounded corner XML drawables for keypads, total cards, input fields, and category pills in `res/drawable/`.
3. **Custom Success Popup UI:** Designed `dialog_success.xml` featuring white rounded container, checkmark icon, top-right 'X' close button, and dim background overlay.
4. **Accessibility & Contrast Verification:** Verified contrast ratios across text, buttons, and active bottom navigation indicators to meet accessibility standards.

---

## 📚 9. Documentation Lead
**Role Overview:** Codebase documentation, technical specifications, presentation Q&A guide, system ERD diagrams.

### Files to Present & Open in IDE:
- [`README.md`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/README.md)
- [`docs/SYSTEM_DIAGRAMS.md`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/docs/SYSTEM_DIAGRAMS.md)
- [`docs/PRESENTATION_QA.md`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/docs/PRESENTATION_QA.md)
- [`docs/ANDROID_FOR_WEB_DEVS.md`](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/docs/ANDROID_FOR_WEB_DEVS.md)

### 4 Technical Talking Points:
1. **Comprehensive Project README:** Authored root `README.md` detailing app features, technology stack table, project structure, and build instructions.
2. **System Architecture & ERD Diagrams:** Created `docs/SYSTEM_DIAGRAMS.md` with interactive Mermaid diagrams for ERD, Architecture, and Data Flow.
3. **Web-to-Android Onboarding Guide:** Created `docs/ANDROID_FOR_WEB_DEVS.md` mapping web concepts (JSX, React state, Redux, Express, SQL) to Android Kotlin equivalents.
4. **Presentation Defense Q&A Preparation:** Developed `docs/PRESENTATION_QA.md` covering expected technical defense questions, design decisions, and solutions.

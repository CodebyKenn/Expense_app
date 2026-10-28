# 🎓 Pockit Presentation - Expected Questions & Answers Guide

This document prepares you for your project presentation and defense. It covers likely technical, architectural, database, and design questions with clear, expert answers.

---

## 🏗️ Section 1: Architecture & Design Patterns

### Q1: What architecture did you use for Pockit, and why?
**Answer:**  
We used the **MVVM (Model-View-ViewModel)** pattern with a **Repository** as recommended by Google for modern Android development.
- **View (Fragments & Layouts):** Handles UI rendering and user interactions.
- **ViewModel:** Preserves UI state during lifecycle events (like screen rotation) and executes business logic without holding context references.
- **Repository (`PockitRepository`):** Serves as the single source of truth, managing database operations, business validation, and default data seeding.

### Q2: Why use a Repository pattern instead of calling DAOs directly from Fragments or ViewModels?
**Answer:**  
The Repository abstracts the data layer from the UI layer. It allows us to centralize data validation, apply business rules (like checking account balance updates or validating transfer constraints), and easily change data sources (e.g., adding a remote REST API or Firebase backend in the future) without modifying any UI code.

### Q3: What is the purpose of `Session.kt`?
**Answer:**  
`Session` is a lightweight singleton object holding the currently logged-in `userId` and `displayName`. Every Room SQL query filters by `userId` (e.g., `WHERE userId = :userId`), enabling multi-user data isolation on the device so different user logins see only their own data.

---

## 💾 Section 2: Database & Offline-First Storage

### Q4: How is data stored on the phone?
**Answer:**  
Pockit uses **Room**, an object-relational mapping (ORM) library built on top of native **SQLite**. The database file (`pockit.db`) lives locally on the phone's internal storage, making the app 100% offline-first.

### Q5: How do account balances update automatically when a transaction is added or deleted?
**Answer:**  
We use a dynamic SQL query in `AccountDao.kt` (`observeBalances`). Instead of storing a stale hardcoded balance, Room calculates each account's current balance on the fly:
$$\text{Current Balance} = \text{Opening Balance} + \text{Income} - \text{Expenses} - \text{Transfers Out} + \text{Transfers In}$$
Because `observeBalances()` returns a Kotlin `Flow<List<AccountBalance>>`, Room automatically recalculates and emits updated balances to all observing ViewModels whenever any record changes.

### Q6: How does the "Reset / Clear Data" feature work without breaking default accounts?
**Answer:**  
In `PockitRepository.clearUserData()`, we execute deletion queries for the active user across `records`, `budgets`, and `accounts` tables. Immediately after, we call `ensureDefaultAccounts()`, which recreates clean default accounts (**M-Pesa**, **Cash**, **Bank**) with a KSh 0 opening balance.

---

## ⚡ Section 3: Concurrency & Reactive Streams

### Q7: What are Coroutines and Flow, and why did you use them?
**Answer:**  
- **Coroutines:** Lightweight Kotlin threads used to perform background operations (like database reads/writes) without freezing the main UI thread. Functions marked `suspend` run asynchronously.
- **Kotlin Flow:** A cold asynchronous data stream that continuously emits updated database query results whenever the underlying SQLite tables change.

### Q8: What is the difference between `LiveData` and `Flow` in your app?
**Answer:**  
- **Flow** is used in the data and repository layers to stream continuous database updates.
- **LiveData** is lifecycle-aware and used in the ViewModels to expose data to Fragments (`asLiveData()`). This prevents memory leaks and crashes by only updating the UI when the Fragment is active on screen.

---

## 📱 Section 4: User Interface & User Experience

### Q9: How did you implement navigation between screens?
**Answer:**  
We used **Jetpack Navigation Component**. All screen destinations are defined in a single navigation graph XML (`nav_graph.xml`). `MainActivity` hosts a single `NavHostFragment` and wires `BottomNavigationView` with `setupWithNavController()`.

### Q10: How does the Add screen handle Transfers vs Expense/Income?
**Answer:**  
When the user toggles to **Transfer**:
1. The Category dropdown is hidden (`View.GONE`) because transfers do not belong to expense/income categories.
2. The **"To Account"** dropdown appears (`View.VISIBLE`).
3. Validation ensures `fromAccount` and `toAccount` are different before saving a `RecordType.TRANSFER` record with `toAccountId`.

### Q11: How did you replace Toast notifications with custom popups?
**Answer:**  
We created a custom `SuccessDialog` object ([SuccessDialog.kt](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/java/com/group/pockit/util/SuccessDialog.kt)) inflated from [dialog_success.xml](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/res/layout/dialog_success.xml). It displays an 'X' button in the top right, a checkmark icon, action summary text, and a "Close" button, and can be dismissed by tapping outside.

---

## 🛠️ Section 5: Technical Challenges & Solutions

### Q12: What was a tough technical challenge you encountered during development, and how did you solve it?
**Answer:**  
One key challenge was **UI contrast in Spinner dropdown lists** and **BottomNavigationView styling**:
- Default Android Spinners rendered black text on dark popup backgrounds in certain themes. We resolved this by creating custom Spinner item layouts ([spinner_item.xml](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/res/layout/spinner_item.xml) and [spinner_dropdown_item.xml](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/res/layout/spinner_dropdown_item.xml)) with explicit white card backgrounds (`#FFFFFF`) and dark purple text (`#46044B`).
- Material 3 `BottomNavigationView` rendered overlapping dual active indicators. We solved this by disabling the built-in active indicator (`isItemActiveIndicatorEnabled = false`) and applying a clean custom inset lavender pill background ([bg_nav_item_selected.xml](file:///C:/Users/Matt/Documents/KCA UNIVERSITY DOCS/Bsc.IT docs/Year2Trim2/Mobile_Applications_Development/Project/app/src/main/res/drawable/bg_nav_item_selected.xml)).

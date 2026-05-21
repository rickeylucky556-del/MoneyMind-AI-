package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FinanceDatabase
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FinanceDatabase.getDatabase(application)
    private val repository = FinanceRepository(
        database.transactionDao(),
        database.budgetDao(),
        database.goalDao(),
        database.tipDao(),
        database.reminderDao()
    )

    // Mode: false = Personal, true = Small Business
    private val _isBusinessMode = MutableStateFlow(false)
    val isBusinessMode: StateFlow<Boolean> = _isBusinessMode.asStateFlow()

    // Observe streams reactively based on current business mode
    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = _isBusinessMode
        .flatMapLatest { isBiz -> repository.getTransactions(isBiz) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val budgets: StateFlow<List<Budget>> = _isBusinessMode
        .flatMapLatest { isBiz -> repository.getBudgets(isBiz) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val goals: StateFlow<List<FinancialGoal>> = _isBusinessMode
        .flatMapLatest { isBiz -> repository.getGoals(isBiz) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val reminders: StateFlow<List<Reminder>> = _isBusinessMode
        .flatMapLatest { isBiz -> repository.getReminders(isBiz) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tips: StateFlow<List<PersonalTip>> = _isBusinessMode
        .flatMapLatest { isBiz -> repository.getTips(isBiz) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGeneratingTips = MutableStateFlow(false)
    val isGeneratingTips: StateFlow<Boolean> = _isGeneratingTips.asStateFlow()

    init {
        // Run pre-population on first launch if empty
        viewModelScope.launch {
            repository.getTransactions(false).first().let { list ->
                if (list.isEmpty()) {
                    prepopulateDatabase()
                }
            }
        }
    }

    fun setBusinessMode(isBusiness: Boolean) {
        _isBusinessMode.value = isBusiness
    }

    // Transactions API
    fun addTransaction(amount: Double, type: String, category: String, description: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    amount = amount,
                    type = type,
                    category = category,
                    description = description,
                    timestamp = System.currentTimeMillis(),
                    isBusiness = _isBusinessMode.value
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Budgets API
    fun addBudget(category: String, limitAmount: Double) {
        viewModelScope.launch {
            repository.insertBudget(
                Budget(
                    category = category,
                    limitAmount = limitAmount,
                    isBusiness = _isBusinessMode.value
                )
            )
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // Goals API
    fun addGoal(title: String, targetAmount: Double, currentAmount: Double, deadlineDays: Int) {
        viewModelScope.launch {
            val deadlineTimestamp = System.currentTimeMillis() + (deadlineDays * 24L * 60 * 60 * 1000)
            repository.insertGoal(
                FinancialGoal(
                    title = title,
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    deadlineTimestamp = deadlineTimestamp,
                    isBusiness = _isBusinessMode.value
                )
            )
        }
    }

    fun updateGoalProgress(goal: FinancialGoal, addedAmount: Double) {
        viewModelScope.launch {
            val newAmount = (goal.currentAmount + addedAmount).coerceAtMost(goal.targetAmount)
            repository.insertGoal(goal.copy(currentAmount = newAmount))
        }
    }

    fun deleteGoal(goal: FinancialGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // Reminders API
    fun addReminder(title: String, message: String, delayDays: Int) {
        viewModelScope.launch {
            val triggerTime = System.currentTimeMillis() + (delayDays * 24L * 60 * 60 * 1000)
            repository.insertReminder(
                Reminder(
                    title = title,
                    message = message,
                    triggerTime = triggerTime,
                    isBusiness = _isBusinessMode.value
                )
            )
        }
    }

    fun toggleReminderCompleted(reminder: Reminder) {
        viewModelScope.launch {
            repository.insertReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    // Gemini API Advice trigger
    fun generateAITips() {
        viewModelScope.launch {
            _isGeneratingTips.value = true
            try {
                // Fetch current list elements safely
                val currentTransactions = transactions.value
                val currentBudgets = budgets.value
                val currentGoals = goals.value
                val currentMode = _isBusinessMode.value
                
                repository.generateTipsAndSave(
                    transactions = currentTransactions,
                    budgets = currentBudgets,
                    goals = currentGoals,
                    isBusiness = currentMode
                )
            } finally {
                _isGeneratingTips.value = false
            }
        }
    }

    private suspend fun prepopulateDatabase() {
        val now = System.currentTimeMillis()
        val oneDay = 24L * 60 * 60 * 1000

        // PERSONAL SEED DATA
        // Transactions
        val personalTx = listOf(
            Transaction(amount = 4200.0, type = "INCOME", category = "Salary", description = "Monthly corporate payroll", timestamp = now - 5 * oneDay, isBusiness = false),
            Transaction(amount = 1200.0, type = "EXPENSE", category = "Housing", description = "Downtown apartment monthly lease", timestamp = now - 4 * oneDay, isBusiness = false),
            Transaction(amount = 145.5, type = "EXPENSE", category = "Groceries", description = "Whole Foods weekly supply", timestamp = now - 3 * oneDay, isBusiness = false),
            Transaction(amount = 75.0, type = "EXPENSE", category = "Dining", description = "Sushi with friends", timestamp = now - 2 * oneDay, isBusiness = false),
            Transaction(amount = 500.0, type = "INVESTMENT", category = "Equities", description = "Automated S&P 500 index buy", timestamp = now - 2 * oneDay, isBusiness = false),
            Transaction(amount = 250.0, type = "SAVING", category = "Emergency Fund", description = "Savings vault deposit", timestamp = now - oneDay, isBusiness = false)
        )
        personalTx.forEach { repository.insertTransaction(it) }

        // Budgets
        val personalBudgets = listOf(
            Budget(category = "Groceries", limitAmount = 400.0, isBusiness = false),
            Budget(category = "Dining", limitAmount = 250.0, isBusiness = false),
            Budget(category = "Housing", limitAmount = 1300.0, isBusiness = false),
            Budget(category = "Shopping", limitAmount = 200.0, isBusiness = false)
        )
        personalBudgets.forEach { repository.insertBudget(it) }

        // Goals
        val personalGoals = listOf(
            FinancialGoal(title = "Emergency Vault", targetAmount = 10000.0, currentAmount = 4500.0, deadlineTimestamp = now + 180 * oneDay, isBusiness = false),
            FinancialGoal(title = "Japan Trip 2026", targetAmount = 4000.0, currentAmount = 1500.0, deadlineTimestamp = now + 120 * oneDay, isBusiness = false)
        )
        personalGoals.forEach { repository.insertGoal(it) }

        // Reminders
        val personalReminders = listOf(
            Reminder(title = "Apartment Rent Due", message = "Automated Rent transfer is scheduled in 4 days.", triggerTime = now + 4 * oneDay, isBusiness = false),
            Reminder(title = "Credit Card Statement", message = "Pay off full outstanding statement balance to avoid interest.", triggerTime = now + 12 * oneDay, isBusiness = false)
        )
        personalReminders.forEach { repository.insertReminder(it) }

        // MOCK Tips (First boot)
        val personalTip = PersonalTip(
            title = "SmartAI Wealth Blueprint",
            content = """
                ### Welcome to your Personal Finance Board!
                *   **High rent ratio**: Your rent eats up around **28%** of your active earnings. This is perfectly safe (below the 30% rule), but any increase could trim your potential savings.
                *   **Consistency is Key**: Your auto-deposit into the S&P 500 represents **11.9%** of your income. Automate this to build a long-term compound loop.
                *   **Unspent eating allocations**: You have utilized less than **30%** of your dining out budget. Savings can be automatically routed to your Japan Trip goal.
            """.trimIndent(),
            timestamp = now,
            isBusiness = false,
            category = "BUDGETING"
        )
        repository.insertTip(personalTip)


        // SMALL BUSINESS SEED DATA
        // Transactions
        val businessTx = listOf(
            Transaction(amount = 8900.0, type = "INCOME", category = "Invoicing", description = "App Development Milestone 1 Payment", timestamp = now - 6 * oneDay, isBusiness = true),
            Transaction(amount = 3200.0, type = "INCOME", category = "Invoicing", description = "UX Consultant Retainer Fee", timestamp = now - 5 * oneDay, isBusiness = true),
            Transaction(amount = 850.0, type = "EXPENSE", category = "Hosting", description = "Monthly AWS & GCP bills", timestamp = now - 3 * oneDay, isBusiness = true),
            Transaction(amount = 1200.0, type = "EXPENSE", category = "Payroll", description = "Freelance UI designer payments", timestamp = now - 2 * oneDay, isBusiness = true),
            Transaction(amount = 1500.0, type = "SAVING", category = "Tax Buffer", description = "Sinking tax reserve sweep", timestamp = now - 2 * oneDay, isBusiness = true),
            Transaction(amount = 1000.0, type = "INVESTMENT", category = "R&D", description = "Hiring consulting advisor", timestamp = now - oneDay, isBusiness = true)
        )
        businessTx.forEach { repository.insertTransaction(it) }

        // Budgets
        val businessBudgets = listOf(
            Budget(category = "Hosting", limitAmount = 1200.0, isBusiness = true),
            Budget(category = "Payroll", limitAmount = 5000.0, isBusiness = true),
            Budget(category = "Marketing", limitAmount = 2000.0, isBusiness = true)
        )
        businessBudgets.forEach { repository.insertBudget(it) }

        // Goals
        val businessGoals = listOf(
            FinancialGoal(title = "Annual Tax Reserve", targetAmount = 25000.0, currentAmount = 8500.0, deadlineTimestamp = now + 90 * oneDay, isBusiness = true),
            FinancialGoal(title = "Next-gen AI Server Hardware", targetAmount = 15000.0, currentAmount = 4000.0, deadlineTimestamp = now + 150 * oneDay, isBusiness = true)
        )
        businessGoals.forEach { repository.insertGoal(it) }

        // Reminders
        val businessReminders = listOf(
            Reminder(title = "AWS Sinking Invoices", message = "Cloud infrastructure charges post in 3 days.", triggerTime = now + 3 * oneDay, isBusiness = true),
            Reminder(title = "VAT/Sales Tax Filing Due", message = "Submit the quarterly reports and pay the calculated VAT balance.", triggerTime = now + 10 * oneDay, isBusiness = true)
        )
        businessReminders.forEach { repository.insertReminder(it) }

        // MOCK Tips (First boot)
        val businessTip = PersonalTip(
            title = "SmartAI Business Growth Analysis",
            content = """
                ### Small Business Diagnostics & Runway
                *   **Sensational gross margins**: Your net operating margins are above **60%** for this billing cycle. This represents highly optimized agency throughput.
                *   **Build a healthy working capital buffer**: Maintain a reserve of 3-6 months ($15k - $30k) before increasing active R&D hiring.
                *   **Infrastructure spikes**: Hosting costs currently represent **7%** of revenues. Consider server reservations or annual upfront discounting to trim bills.
            """.trimIndent(),
            timestamp = now,
            isBusiness = true,
            category = "BUSINESS"
        )
        repository.insertTip(businessTip)
    }
}

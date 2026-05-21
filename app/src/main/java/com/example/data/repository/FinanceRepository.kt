package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.*
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val goalDao: GoalDao,
    private val tipDao: TipDao,
    private val reminderDao: ReminderDao,
    private val apiService: GeminiApiService = RetrofitClient.apiService
) {

    // Transactions
    fun getTransactions(isBusiness: Boolean): Flow<List<Transaction>> =
        transactionDao.getTransactions(isBusiness)

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Int) {
        transactionDao.deleteTransactionById(id)
    }

    // Budgets
    fun getBudgets(isBusiness: Boolean): Flow<List<Budget>> =
        budgetDao.getBudgets(isBusiness)

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun deleteBudgetById(id: Int) {
        budgetDao.deleteBudgetById(id)
    }

    // Goals
    fun getGoals(isBusiness: Boolean): Flow<List<FinancialGoal>> =
        goalDao.getGoals(isBusiness)

    suspend fun insertGoal(goal: FinancialGoal) {
        goalDao.insertGoal(goal)
    }

    suspend fun deleteGoal(goal: FinancialGoal) {
        goalDao.deleteGoal(goal)
    }

    suspend fun deleteGoalById(id: Int) {
        goalDao.deleteGoalById(id)
    }

    // Reminders
    fun getReminders(isBusiness: Boolean): Flow<List<Reminder>> =
        reminderDao.getReminders(isBusiness)

    suspend fun insertReminder(reminder: Reminder) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun deleteReminderById(id: Int) {
        reminderDao.deleteReminderById(id)
    }

    // Tips
    fun getTips(isBusiness: Boolean): Flow<List<PersonalTip>> =
        tipDao.getTips(isBusiness)

    suspend fun insertTip(tip: PersonalTip) {
        tipDao.insertTip(tip)
    }

    suspend fun generateTipsAndSave(
        transactions: List<Transaction>,
        budgets: List<Budget>,
        goals: List<FinancialGoal>,
        isBusiness: Boolean
    ): String = withContext(Dispatchers.IO) {
        val mode = if (isBusiness) "Small Business" else "Personal Finance"
        
        // Build overview
        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val totalSavings = transactions.filter { it.type == "SAVING" }.sumOf { it.amount }
        val totalInvestments = transactions.filter { it.type == "INVESTMENT" }.sumOf { it.amount }
        
        val transactionsSummary = transactions.take(10).joinToString("\n") {
            "- ${it.category}: $${it.amount} (${it.type}) - ${it.description}"
        }

        val budgetsSummary = budgets.joinToString("\n") {
            "- ${it.category}: limit $${it.limitAmount}"
        }

        val goalsSummary = goals.joinToString("\n") {
            "- ${it.title}: target $${it.targetAmount}, saved $${it.currentAmount}"
        }

        val prompt = """
            You are a Personal Finance & Small Business Budgeting Co-Pilot called SmartFinance AI.
            Context: $mode
            Here is the user's current financial snapshot:
            Incomes: $$totalIncome
            Expenses: $$totalExpenses
            Savings: $$totalSavings
            Investments: $$totalInvestments

            Recent Transactions:
            $transactionsSummary

            Active Budgets:
            $budgetsSummary

            Financial Goals:
            $goalsSummary

            Please write 3 customized, precise, and actionable financial tips/reminders for this user to help them optimize their cash flow, reduce expenses, or improve their business operations.
            Format the response clearly using clean and simple markdown bullet points suitable for a mobile UI. Keep paragraphs brief and highly readable.
        """.trimIndent()

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("FinanceRepository", "Gemini API Key is a placeholder, generating elegant offline tips.")
            return@withContext saveOfflineTips(isBusiness)
        }

        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = "You are a professional, motivating financial coach. Be concise, direct, helpful, and highly analytical."))
            )
        )

        try {
            val response = apiService.generateTips(apiKey, requestBody)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrBlank()) {
                // Clear old tips and save this one
                tipDao.clearAllTips()
                tipDao.insertTip(
                    PersonalTip(
                        title = "SmartAI Financial Analysis",
                        content = responseText,
                        timestamp = System.currentTimeMillis(),
                        isBusiness = isBusiness,
                        category = if (isBusiness) "BUSINESS" else "BUDGETING"
                    )
                )
                responseText
            } else {
                saveOfflineTips(isBusiness)
            }
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error contacting Gemini API: ${e.message}", e)
            saveOfflineTips(isBusiness)
        }
    }

    private suspend fun saveOfflineTips(isBusiness: Boolean): String {
        tipDao.clearAllTips()
        
        val content = if (isBusiness) {
            """
                ### Core Growth Strategies
                *   **Cash Flow Optimization**: Your business accounts are growing! Make sure you maintain a 3-month working capital buffer before reinvesting.
                *   **Tax Reserves Allocation**: It is advised to set aside 20-30% of your incoming gross revenues into a dedicated high-yield savings vault for tax obligations.
                *   **Operating Ratio Control**: Audit your miscellaneous recurring software SaaS bills. Trimming unnecessary tools can expand your net profit margins by 2-5%.
            """.trimIndent()
        } else {
            """
                ### Wealth-Building Guidelines
                *   **The 50/30/20 Rule**: Direct 50% of your income to needs, 30% to wants, and automate 20% directly into high-growth savings or passive index investments.
                *   **Emergency Buffer First**: Prioritize saving room for an emergency fund equivalent to 3 to 6 months of fixed living costs before scaling active stock investments.
                *   **Avoid Lifestyle Creep**: Whenever you receive a raise or bonus, instantly route at least half of the increase to your wealth goals. This keeps your discretionary baseline secure.
            """.trimIndent()
        }

        tipDao.insertTip(
            PersonalTip(
                title = "SmartFinance Financial Insights",
                content = content,
                timestamp = System.currentTimeMillis(),
                isBusiness = isBusiness,
                category = if (isBusiness) "BUSINESS" else "BUDGETING"
            )
        )
        return content
    }
}

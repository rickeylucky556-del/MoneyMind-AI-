package com.example.data.local

import androidx.room.*
import com.example.data.model.Budget
import com.example.data.model.FinancialGoal
import com.example.data.model.PersonalTip
import com.example.data.model.Reminder
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isBusiness = :isBusiness ORDER BY timestamp DESC")
    fun getTransactions(isBusiness: Boolean): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE isBusiness = :isBusiness")
    fun getBudgets(isBusiness: Boolean): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)
    
    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: Int)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE isBusiness = :isBusiness ORDER BY deadlineTimestamp ASC")
    fun getGoals(isBusiness: Boolean): Flow<List<FinancialGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: FinancialGoal)

    @Delete
    suspend fun deleteGoal(goal: FinancialGoal)
    
    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Int)
}

@Dao
interface TipDao {
    @Query("SELECT * FROM tips WHERE isBusiness = :isBusiness ORDER BY timestamp DESC")
    fun getTips(isBusiness: Boolean): Flow<List<PersonalTip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTip(tip: PersonalTip)

    @Query("DELETE FROM tips")
    suspend fun clearAllTips()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isBusiness = :isBusiness ORDER BY triggerTime ASC")
    fun getReminders(isBusiness: Boolean): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
    
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Int)
}

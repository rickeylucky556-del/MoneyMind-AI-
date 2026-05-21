package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "EXPENSE", "INCOME", "SAVING", "INVESTMENT"
    val category: String, // e.g., "Food", "Rent", "Salary", "Dividends", "Equities"
    val description: String,
    val timestamp: Long,
    val isBusiness: Boolean // Allows switching between Personal and Small Business views
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val limitAmount: Double,
    val period: String = "MONTHLY",
    val isBusiness: Boolean
)

@Entity(tableName = "goals")
data class FinancialGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadlineTimestamp: Long,
    val isBusiness: Boolean
)

@Entity(tableName = "tips")
data class PersonalTip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long,
    val isBusiness: Boolean,
    val category: String // "BUDGETING", "SAVINGS", "INVESTMENT", "BUSINESS"
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val triggerTime: Long,
    val isCompleted: Boolean = false,
    val isBusiness: Boolean
)

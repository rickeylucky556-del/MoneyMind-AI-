package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Reminder
import com.example.data.model.Transaction
import com.example.ui.components.FinanceDonutChart
import com.example.ui.components.ColorExpense
import com.example.ui.components.ColorIncome
import com.example.ui.components.ColorSaving
import com.example.ui.components.ColorInvestment
import com.example.ui.components.InteractiveTrendLineChart
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBusinessMode by viewModel.isBusinessMode.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val goals by viewModel.goals.collectAsState()

    // Calculate financials
    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val totalSavings = transactions.filter { it.type == "SAVING" }.sumOf { it.amount }
    val totalInvestments = transactions.filter { it.type == "INVESTMENT" }.sumOf { it.amount }
    
    val netCashFlow = totalIncome - totalExpense - totalSavings - totalInvestments

    // Trend lines calculation (e.g., aggregate past 5 days of expense metrics)
    val trendPoints = remember(transactions) {
        val expenseLast6 = transactions.filter { it.type == "EXPENSE" }
            .sortedBy { it.timestamp }
            .takeLast(6)
            .map { it.amount }
        if (expenseLast6.size < 2) {
            listOf(120.0, 450.0, 320.0, 680.0, 150.0, 520.0) // Realistic trend fallback
        } else {
            expenseLast6
        }
    }
    
    val trendLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and business toggle header
        FinanceHeaderSection(
            isBusinessMode = isBusinessMode,
            onToggleBusiness = { viewModel.setBusinessMode(it) }
        )

        // Financial KPI Cards
        FlowSummaryCards(
            income = totalIncome,
            expense = totalExpense,
            netFlow = netCashFlow
        )

        // Interactive Donut Chart Section
        Text(
            text = "Portfolio Distribution",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
        FinanceDonutChart(
            income = totalIncome,
            expense = totalExpense,
            saving = totalSavings,
            investment = totalInvestments,
            modifier = Modifier.fillMaxWidth()
        )

        // Dynamic Interactive Trend-line
        InteractiveTrendLineChart(
            dataPoints = trendPoints,
            labels = trendLabels,
            modifier = Modifier.fillMaxWidth()
        )

        // Reminders & Pushes Section (reminders list with checkbox toggle)
        RemindersNotificationSection(
            reminders = reminders.take(3),
            onToggleComplete = { viewModel.toggleReminderCompleted(it) }
        )

        // Quick Recent Transactions
        RecentTransactionsOverview(
            transactions = transactions.take(4),
            onSeeAllClick = onNavigateToTransactions
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FinanceHeaderSection(
    isBusinessMode: Boolean,
    onToggleBusiness: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBusinessMode) "Enterprise Hub" else "Personal Dashboard",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isBusinessMode) "Corporate Wealth" else "My Smart Vault",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // Account Context Switcher Mode
                Switch(
                    checked = isBusinessMode,
                    onCheckedChange = { onToggleBusiness(it) },
                    thumbContent = {
                        Icon(
                            imageVector = if (isBusinessMode) Icons.Default.BusinessCenter else Icons.Default.Person,
                            contentDescription = "Switch profile Mode",
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                )
            }
            Text(
                text = if (isBusinessMode) 
                    "Currently viewing small business operational balances, corporate expense reports, and client billing logs." 
                    else "Currently tracking household budgets, private investment portfolios, and leisure travel savings goals.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FlowSummaryCards(
    income: Double,
    expense: Double,
    netFlow: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryMiniCard(
            title = "Cash Inflow",
            amount = "$${String.format("%,.0f", income)}",
            icon = Icons.Default.ArrowDownward,
            iconColor = Color(0xFF7CA685),
            modifier = Modifier.weight(1f)
        )
        SummaryMiniCard(
            title = "Cash Expense",
            amount = "$${String.format("%,.0f", expense)}",
            icon = Icons.Default.ArrowUpward,
            iconColor = Color(0xFFC97C78),
            modifier = Modifier.weight(1f)
        )
        SummaryMiniCard(
            title = "Net Flow",
            amount = "$${String.format("%,.0f", netFlow)}",
            icon = Icons.Default.AccountBalanceWallet,
            iconColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryMiniCard(
    title: String,
    amount: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = amount,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RemindersNotificationSection(
    reminders: List<Reminder>,
    onToggleComplete: (Reminder) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notifications & Reminders",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "Alerts",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        if (reminders.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier.padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No pending payments or schedules. All safe!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            reminders.forEach { reminder ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (reminder.isCompleted) 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) 
                            else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(reminder.triggerTime))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = reminder.isCompleted,
                                onCheckedChange = { onToggleComplete(reminder) }
                            )
                            Column {
                                Text(
                                    text = reminder.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (reminder.isCompleted) 
                                        MaterialTheme.colorScheme.onSurfaceVariant 
                                        else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = reminder.message,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Alert date: $dateStr",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsOverview(
    transactions: List<Transaction>,
    onSeeAllClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Cash Flow",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onSeeAllClick) {
                Text("See All", fontWeight = FontWeight.Bold)
            }
        }

        if (transactions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier.padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent transactions logged yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            transactions.forEach { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val tintColor = when (tx.type) {
                                "INCOME" -> ColorIncome
                                "EXPENSE" -> ColorExpense
                                "SAVING" -> ColorSaving
                                else -> ColorInvestment
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(tintColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val txIcon = when (tx.type) {
                                    "INCOME" -> Icons.Default.TrendingUp
                                    "EXPENSE" -> Icons.Default.TrendingDown
                                    "SAVING" -> Icons.Default.Savings
                                    else -> Icons.Default.Assessment
                                }
                                Icon(
                                    imageVector = txIcon,
                                    contentDescription = null,
                                    tint = tintColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = tx.description,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = tx.category,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val prefix = when (tx.type) {
                            "INCOME" -> "+"
                            "EXPENSE" -> "-"
                            "SAVING" -> "•"
                            else -> "⚡"
                        }
                        val tintAmount = when (tx.type) {
                            "INCOME" -> ColorIncome
                            "EXPENSE" -> ColorExpense
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        Text(
                            text = "$prefix$${String.format("%,.2f", tx.amount)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = tintAmount
                        )
                    }
                }
            }
        }
    }
}

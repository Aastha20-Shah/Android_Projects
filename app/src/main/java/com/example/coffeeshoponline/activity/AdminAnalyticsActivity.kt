package com.example.coffeeshoponline.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.coffeeshoponline.R
import com.example.coffeeshoponline.databinding.ActivityAdminAnalyticsBinding
import com.example.coffeeshoponline.model.OrderModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AdminAnalyticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminAnalyticsBinding
    private val database = FirebaseDatabase.getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/").reference
    private var allOrders = mutableListOf<OrderModel>()
    private var allUsersCount = 0L
    private var allItemsCount = 0L

    private enum class MetricType { REVENUE, SALES, USERS, PRODUCTS }
    private var currentMetric = MetricType.REVENUE
    private var currentFilter = "Week"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { finish() }
        
        setupChart()
        fetchData()
        setupBottomNavigation()
        setupMetricClicks()

        binding.btnWeek.setOnClickListener { 
            currentFilter = "Week"
            updateFilter() 
        }
        binding.btnMonth.setOnClickListener { 
            currentFilter = "Month"
            updateFilter() 
        }
        binding.btnYear.setOnClickListener { 
            currentFilter = "Year"
            updateFilter() 
        }
    }

    private fun setupMetricClicks() {
        binding.cardRevenue.setOnClickListener {
            currentMetric = MetricType.REVENUE
            updateChartUI()
        }
        binding.cardSales.setOnClickListener {
            currentMetric = MetricType.SALES
            updateChartUI()
        }
        binding.cardUsers.setOnClickListener {
            currentMetric = MetricType.USERS
            updateChartUI()
        }
        binding.cardProducts.setOnClickListener {
            currentMetric = MetricType.PRODUCTS
            updateChartUI()
        }
    }

    private fun updateChartUI() {
        binding.tvChartTitle.text = when(currentMetric) {
            MetricType.REVENUE -> "Revenue Overview"
            MetricType.SALES -> "Sales Overview"
            MetricType.USERS -> "Users Growth"
            MetricType.PRODUCTS -> "Product Inventory"
        }
        updateFilter()
    }

    private fun setupChart() {
        binding.revenueChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = Color.parseColor("#220c01")
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                textColor = Color.parseColor("#220c01")
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun fetchData() {
        database.child("orders").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allOrders.clear()
                var totalRevenue = 0.0
                for (child in snapshot.children) {
                    val order = child.getValue(OrderModel::class.java)
                    if (order != null) {
                        allOrders.add(order)
                        if (order.status.equals("Success", ignoreCase = true) || order.status.equals("Delivered", ignoreCase = true)) {
                            totalRevenue += order.totalAmount
                        }
                    }
                }
                binding.tvTotalRevenue.text = "₹%.0f".format(totalRevenue)
                binding.tvTotalSales.text = allOrders.size.toString()
                updateFilter() 
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allUsersCount = snapshot.childrenCount
                binding.tvTotalUsers.text = allUsersCount.toString()
                if (currentMetric == MetricType.USERS) updateFilter()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.child("items").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allItemsCount = snapshot.childrenCount
                binding.tvTotalProducts.text = allItemsCount.toString()
                if (currentMetric == MetricType.PRODUCTS) updateFilter()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateFilter() {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        
        binding.btnWeek.backgroundTintList = ContextCompat.getColorStateList(this, R.color.brown)
        binding.btnMonth.backgroundTintList = ContextCompat.getColorStateList(this, R.color.brown)
        binding.btnYear.backgroundTintList = ContextCompat.getColorStateList(this, R.color.brown)

        val activeBtn = when(currentFilter) {
            "Week" -> binding.btnWeek
            "Month" -> binding.btnMonth
            else -> binding.btnYear
        }
        activeBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.darkBrown)

        val iterations = when(currentFilter) {
            "Week" -> 6
            "Month" -> 3
            "Year" -> 5
            else -> 6
        }

        for (i in iterations downTo 0) {
            calendar.time = Date()
            val value: Float
            val label: String

            when (currentFilter) {
                "Week" -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -i)
                    val dayStart = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
                    val dayEnd = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                    
                    value = calculateMetricForPeriod(dayStart, dayEnd)
                    label = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
                }
                "Month" -> {
                    calendar.add(Calendar.WEEK_OF_YEAR, -i)
                    val weekStart = calendar.apply { set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
                    val weekEnd = weekStart + (7 * 24 * 60 * 60 * 1000)
                    
                    value = calculateMetricForPeriod(weekStart, weekEnd)
                    label = "W${4-i}"
                }
                "Year" -> {
                    calendar.add(Calendar.MONTH, -i)
                    val month = calendar.get(Calendar.MONTH)
                    val year = calendar.get(Calendar.YEAR)
                    
                    value = calculateMetricForMonth(month, year)
                    label = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
                }
                else -> { value = 0f; label = "" }
            }
            
            entries.add(BarEntry((iterations - i).toFloat(), value))
            labels.add(label)
        }

        val dataSet = BarDataSet(entries, currentMetric.name)
        dataSet.color = Color.parseColor("#f69724")
        dataSet.valueTextColor = Color.parseColor("#220c01")
        dataSet.valueTextSize = 10f

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f
        
        binding.revenueChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            animateY(1000)
            invalidate()
        }
    }

    private fun calculateMetricForPeriod(start: Long, end: Long): Float {
        return when (currentMetric) {
            MetricType.REVENUE -> allOrders.filter { it.timestamp in start..end && (it.status.equals("Success", ignoreCase = true) || it.status.equals("Delivered", ignoreCase = true)) }.sumOf { it.totalAmount }.toFloat()
            MetricType.SALES -> allOrders.count { it.timestamp in start..end }.toFloat()
            MetricType.USERS -> allUsersCount.toFloat() // Simplified for demo, usually growth over time
            MetricType.PRODUCTS -> allItemsCount.toFloat()
        }
    }

    private fun calculateMetricForMonth(month: Int, year: Int): Float {
        return when (currentMetric) {
            MetricType.REVENUE -> allOrders.filter { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year && (it.status.equals("Success", ignoreCase = true) || it.status.equals("Delivered", ignoreCase = true))
            }.sumOf { it.totalAmount }.toFloat()
            MetricType.SALES -> allOrders.count { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
            }.toFloat()
            MetricType.USERS -> allUsersCount.toFloat()
            MetricType.PRODUCTS -> allItemsCount.toFloat()
        }
    }

    private fun setupBottomNavigation() {
        binding.adminBottomNav.navHome.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
            finish()
        }
        binding.adminBottomNav.navOrders.setOnClickListener {
            startActivity(Intent(this, AdminManageOrdersActivity::class.java))
            finish()
        }
        binding.adminBottomNav.navUsers.setOnClickListener {
            startActivity(Intent(this, AdminManageUsersActivity::class.java))
            finish()
        }
        binding.adminBottomNav.navMenu.setOnClickListener {
            startActivity(Intent(this, AdminMenuActivity::class.java))
            finish()
        }
    }
}
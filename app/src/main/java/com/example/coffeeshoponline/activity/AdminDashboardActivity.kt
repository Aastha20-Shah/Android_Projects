package com.example.coffeeshoponline.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshoponline.Adapter.AdminItemAdapter
import com.example.coffeeshoponline.Adapter.AdminOrderAdapter
import com.example.coffeeshoponline.Adapter.AdminUserAdapter
import com.example.coffeeshoponline.R
import com.example.coffeeshoponline.databinding.ActivityAdminDashboardBinding
import com.example.coffeeshoponline.model.ItemModel
import com.example.coffeeshoponline.model.OrderModel
import com.example.coffeeshoponline.model.UserModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var database: DatabaseReference
    
    private val allOrders = mutableListOf<OrderModel>()
    private val allProducts = mutableListOf<ItemModel>()
    private val allUsers = mutableListOf<UserModel>()
    
    private enum class DashboardType { PRODUCTS, ORDERS, CLIENTS, REVENUE, TODAY_ORDERS }
    private var currentSelection = DashboardType.TODAY_ORDERS 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        database = FirebaseDatabase.getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/").reference
        
        setupBottomNavigation()
        setupChart()
        loadAllData()
        setupClickListeners()
        setupLogout()
        
        updateCardUI(DashboardType.TODAY_ORDERS)
    }

    private fun setupChart() {
        binding.dashboardRevenueChart.apply {
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
    
    private fun setupClickListeners() {
        binding.cardProducts.setOnClickListener {
            currentSelection = DashboardType.PRODUCTS
            updateCardUI(DashboardType.PRODUCTS)
            showProductsList()
            scrollToList()
        }
        
        binding.cardOrders.setOnClickListener {
            currentSelection = DashboardType.ORDERS
            updateCardUI(DashboardType.ORDERS)
            showAllOrdersList()
            scrollToList()
        }
        
        binding.cardClients.setOnClickListener {
            currentSelection = DashboardType.CLIENTS
            updateCardUI(DashboardType.CLIENTS)
            showUsersList()
            scrollToList()
        }
        
        binding.cardRevenue.setOnClickListener {
            currentSelection = DashboardType.REVENUE
            updateCardUI(DashboardType.REVENUE)
            showAllOrdersList() 
            scrollToList()
        }
        
        binding.tvAdminName.setOnClickListener {
            currentSelection = DashboardType.TODAY_ORDERS
            updateCardUI(DashboardType.TODAY_ORDERS)
            showTodayOrdersList()
        }
    }

    private fun updateCardUI(selected: DashboardType) {
        resetCardStyle(binding.cardProducts, binding.tvTotalProductsCount, binding.tvProductsLabel)
        resetCardStyle(binding.cardOrders, binding.tvTotalOrders, binding.tvOrdersLabel)
        resetCardStyle(binding.cardClients, binding.tvTotalUsersCount, binding.tvClientsLabel)
        resetCardStyle(binding.cardRevenue, binding.tvTotalRevenue, binding.tvRevenueLabel)

        when (selected) {
            DashboardType.PRODUCTS -> {
                highlightCardStyle(binding.cardProducts, binding.tvTotalProductsCount, binding.tvProductsLabel)
                binding.tvDashboardListLabel.text = "All Products"
            }
            DashboardType.ORDERS -> {
                highlightCardStyle(binding.cardOrders, binding.tvTotalOrders, binding.tvOrdersLabel)
                binding.tvDashboardListLabel.text = "All Orders"
            }
            DashboardType.CLIENTS -> {
                highlightCardStyle(binding.cardClients, binding.tvTotalUsersCount, binding.tvClientsLabel)
                binding.tvDashboardListLabel.text = "All Clients"
            }
            DashboardType.REVENUE -> {
                highlightCardStyle(binding.cardRevenue, binding.tvTotalRevenue, binding.tvRevenueLabel)
                binding.tvDashboardListLabel.text = "Revenue Details (Total)"
            }
            DashboardType.TODAY_ORDERS -> {
                binding.tvDashboardListLabel.text = "Recent Orders (Today)"
            }
        }
    }

    private fun highlightCardStyle(card: CardView, count: TextView, label: TextView) {
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.darkBrown))
        count.setTextColor(Color.WHITE)
        label.setTextColor(Color.WHITE)
        label.alpha = 0.8f
    }

    private fun resetCardStyle(card: CardView, count: TextView, label: TextView) {
        card.setCardBackgroundColor(Color.WHITE)
        count.setTextColor(ContextCompat.getColor(this, R.color.darkBrown))
        label.setTextColor(ContextCompat.getColor(this, R.color.darkBrown))
        label.alpha = 0.7f
    }

    private fun scrollToList() {
        binding.dashboardScrollView.post {
            binding.dashboardScrollView.smoothScrollTo(0, binding.tvDashboardListLabel.top)
        }
    }

    private fun loadAllData() {
        binding.rvDashboardList.layoutManager = LinearLayoutManager(this)
        
        database.child("items").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allProducts.clear()
                for (child in snapshot.children) {
                    child.getValue(ItemModel::class.java)?.let { allProducts.add(it) }
                }
                binding.tvTotalProductsCount.text = allProducts.size.toString()
                if (currentSelection == DashboardType.PRODUCTS) showProductsList()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allUsers.clear()
                for (child in snapshot.children) {
                    val user = child.getValue(UserModel::class.java)
                    if (user != null) {
                        user.id = child.key ?: ""
                        allUsers.add(user)
                    }
                }
                binding.tvTotalUsersCount.text = allUsers.size.toString()
                if (currentSelection == DashboardType.CLIENTS) showUsersList()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.child("orders").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allOrders.clear()
                var revenue = 0.0
                for (child in snapshot.children) {
                    val order = child.getValue(OrderModel::class.java)
                    if (order != null) {
                        allOrders.add(order)
                        if (order.status.equals("Success", ignoreCase = true) || order.status.equals("Delivered", ignoreCase = true)) {
                            revenue += order.totalAmount
                        }
                    }
                }
                binding.tvTotalOrders.text = allOrders.size.toString()
                binding.tvTotalRevenue.text = "₹%.0f".format(revenue)
                
                updateChartData()
                
                when (currentSelection) {
                    DashboardType.TODAY_ORDERS -> showTodayOrdersList()
                    DashboardType.ORDERS, DashboardType.REVENUE -> showAllOrdersList()
                    else -> {}
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateChartData() {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dayStart = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
            val dayEnd = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
            
            val dayRevenue = allOrders.filter { 
                it.timestamp in dayStart..dayEnd && (it.status.equals("Success", ignoreCase = true) || it.status.equals("Delivered", ignoreCase = true))
            }.sumOf { it.totalAmount }
            
            entries.add(BarEntry((6 - i).toFloat(), dayRevenue.toFloat()))
            labels.add(SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time))
        }

        val dataSet = BarDataSet(entries, "Weekly Revenue")
        dataSet.color = Color.parseColor("#f69724")
        dataSet.valueTextColor = Color.parseColor("#220c01")
        dataSet.valueTextSize = 10f

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f
        
        binding.dashboardRevenueChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            animateY(1000)
            invalidate()
        }
    }

    private fun showTodayOrdersList() {
        val today = Calendar.getInstance()
        val todayOrders = allOrders.filter { order ->
            val orderDate = Calendar.getInstance()
            orderDate.timeInMillis = order.timestamp
            orderDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            orderDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }.sortedByDescending { it.timestamp }
        
        binding.rvDashboardList.adapter = AdminOrderAdapter(todayOrders)
    }

    private fun showAllOrdersList() {
        binding.rvDashboardList.adapter = AdminOrderAdapter(allOrders.sortedByDescending { it.timestamp })
    }

    private fun showProductsList() {
        binding.rvDashboardList.adapter = AdminItemAdapter(allProducts.toMutableList(), 
            onEditClick = { navigateToMenu() },
            onDeleteClick = { navigateToMenu() }
        )
    }

    private fun showUsersList() {
        binding.rvDashboardList.adapter = AdminUserAdapter(allUsers)
    }

    private fun navigateToMenu() {
        startActivity(Intent(this, AdminMenuActivity::class.java))
    }

    private fun setupLogout() {
        binding.btnSettings.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, AdminLoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
    
    private fun setupBottomNavigation() {
        binding.adminBottomNav.navHome.setOnClickListener { }
        binding.adminBottomNav.navOrders.setOnClickListener {
            startActivity(Intent(this, AdminManageOrdersActivity::class.java))
        }
        binding.adminBottomNav.navUsers.setOnClickListener {
            startActivity(Intent(this, AdminManageUsersActivity::class.java))
        }
        binding.adminBottomNav.navMenu.setOnClickListener {
            navigateToMenu()
        }
        binding.adminBottomNav.navCoupons.setOnClickListener {
            startActivity(Intent(this, AdminAnalyticsActivity::class.java))
        }
    }
}
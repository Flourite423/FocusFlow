package com.focusflow.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.focusflow.ui.dashboard.DashboardScreen
import com.focusflow.ui.dashboard.DailyReviewScreen
import com.focusflow.ui.plan.PlanDetailScreen
import com.focusflow.ui.plan.PlanListScreen
import com.focusflow.ui.review.ReviewScreen
import com.focusflow.ui.settings.SettingsScreen
import com.focusflow.ui.timer.TimerScreen

@Composable
fun MainNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.createRoute(),
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable(Screen.Dashboard.createRoute()) { DashboardScreen(navController = navController) }
            composable(Screen.DailyReview.createRoute()) { DailyReviewScreen(navController = navController) }
            composable(Screen.PlanList.createRoute()) { PlanListScreen(navController = navController) }
            composable(
                route = "plan/{planId}",
                arguments = listOf(navArgument("planId") { type = NavType.StringType })
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString("planId") ?: ""
                PlanDetailScreen(planId = planId, navController = navController)
            }
            composable(Screen.Timer.createRoute()) { TimerScreen(navController = navController) }
            composable(Screen.Review.createRoute()) { ReviewScreen(navController = navController) }
            composable(Screen.Settings.createRoute()) { SettingsScreen(navController = navController) }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items = listOf(
        NavItem(Screen.Dashboard.createRoute(), Icons.Default.Home, "仪表盘"),
        NavItem(Screen.PlanList.createRoute(), Icons.Default.List, "计划"),
        NavItem(Screen.Timer.createRoute(), Icons.Default.PlayArrow, "计时"),
        NavItem(Screen.Review.createRoute(), Icons.Default.DateRange, "复习"),
        NavItem(Screen.Settings.createRoute(), Icons.Default.Person, "我的")
    )
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(text = item.label, fontSize = 10.sp) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class NavItem(val route: String, val icon: ImageVector, val label: String)

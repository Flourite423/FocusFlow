package com.focusflow.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
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
import androidx.navigation.compose.NavHostController
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.focusflow.ui.dashboard.DashboardScreen
import com.focusflow.ui.dashboard.DailyReviewScreen
import com.focusflow.ui.plan.PlanDetailScreen
import com.focusflow.ui.plan.PlanListScreen
import com.focusflow.ui.review.ReviewScreen
import com.focusflow.ui.settings.SettingsScreen
import com.focusflow.ui.timer.TimerScreen

@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Dashboard.createRoute()
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(route = Screen.Dashboard.createRoute()) {
                DashboardScreen(navController = navController)
            }
            composable(route = Screen.DailyReview.createRoute()) {
                DailyReviewScreen(navController = navController)
            }
            composable(route = Screen.PlanList.createRoute()) {
                PlanListScreen(navController = navController)
            }
            composable(
                route = "plan/{planId}",
                arguments = listOf(navArgument("planId") { type = NavType.StringType })
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString("planId") ?: ""
                PlanDetailScreen(planId = planId, navController = navController)
            }
            composable(route = Screen.Timer.createRoute()) {
                TimerScreen(navController = navController)
            }
            composable(route = Screen.Review.createRoute()) {
                ReviewScreen(navController = navController)
            }
            composable(route = Screen.Settings.createRoute()) {
                SettingsScreen(navController = navController)
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        NavItem(
            route = Screen.Dashboard.createRoute(),
            icon = Icons.Default.Home,
            label = "仪表盘"
        ),
        NavItem(
            route = Screen.PlanList.createRoute(),
            icon = Icons.Default.List,
            label = "计划"
        ),
        NavItem(
            route = Screen.Timer.createRoute(),
            icon = Icons.Default.Timer,
            label = "计时"
        ),
        NavItem(
            route = Screen.Review.createRoute(),
            icon = Icons.Default.DateRange,
            label = "复习"
        ),
        NavItem(
            route = Screen.Settings.createRoute(),
            icon = Icons.Default.Person,
            label = "我的"
        )
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(text = item.label, fontSize = 10.sp) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                alwaysShowLabel = true,
            )
        }
    }
}

data class NavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

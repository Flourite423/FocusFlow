package com.focusflow.navigation

sealed interface Screen {
    @Suppress("UNUSED_PARAMETER")
    fun createRoute(): String

    object Dashboard : Screen {
        override fun createRoute() = "dashboard"
    }

    object DailyReview : Screen {
        override fun createRoute() = "daily_review"
    }

    object PlanList : Screen {
        override fun createRoute() = "plan_list"
    }

    data class PlanDetail(val planId: String) : Screen {
        override fun createRoute() = "plan/$planId"
    }

    object WeeklyPlan : Screen {
        override fun createRoute() = "weekly_plan"
    }

    object DailyPlan : Screen {
        override fun createRoute() = "daily_plan"
    }

    object Timer : Screen {
        override fun createRoute() = "timer"
    }

    object Review : Screen {
        override fun createRoute() = "review"
    }

    object Settings : Screen {
        override fun createRoute() = "settings"
    }
}

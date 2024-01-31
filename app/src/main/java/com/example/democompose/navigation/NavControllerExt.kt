package com.example.democompose.navigation

import androidx.navigation.NavController

sealed class NavEvent {
    data class Navigate(val route: String): NavEvent()
    data object NavigateUp: NavEvent()
}

fun NavController.navigate(route: NavEvent.Navigate) {
    this.navigate(route.route)
}
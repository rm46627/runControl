package com.example.runcontrol.extensions

import androidx.navigation.NavController
import androidx.navigation.NavDirections

object NavController {

    fun NavController.safeNavigate(direction: NavDirections) {
        currentDestination?.getAction(direction.actionId)?.run { navigate(direction) }
    }

}
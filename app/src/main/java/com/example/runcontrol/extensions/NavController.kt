package com.example.runcontrol.extensions

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions

object NavController {

    fun NavController.safeNavigate(direction: NavDirections) {
        currentDestination?.getAction(direction.actionId)?.run { navigate(direction) }
    }

    fun NavController.safeNavigate(direction: NavDirections, options: NavOptions) {
        currentDestination?.getAction(direction.actionId)?.run { navigate(direction, options) }
    }

}
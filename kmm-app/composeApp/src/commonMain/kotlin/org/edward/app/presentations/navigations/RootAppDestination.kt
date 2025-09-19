package org.edward.app.presentations.navigations

import cafe.adriel.voyager.navigator.Navigator
import org.edward.app.presentations.screens.auth.login.LoginScreen

sealed interface RootAppDestination {
    object Login : RootAppDestination
    object MainNav : RootAppDestination
}

fun Navigator.navigateTo(destination: RootAppDestination) {
    when (destination) {
        RootAppDestination.Login -> this.replace(LoginScreen())
        RootAppDestination.MainNav -> this.replace(FancyDrawerNav())
    }
}

fun Navigator.replaceAll(destination: RootAppDestination) {
    when (destination) {
        RootAppDestination.Login -> this.replaceAll(LoginScreen())
        RootAppDestination.MainNav -> this.replaceAll(FancyDrawerNav())
    }
}
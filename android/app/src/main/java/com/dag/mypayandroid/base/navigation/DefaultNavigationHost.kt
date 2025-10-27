package com.dag.mypayandroid.base.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.dag.mypayandroid.feature.splash.SplashView
import com.dag.mypayandroid.feature.home.presentation.HomeView
import com.dag.mypayandroid.base.extensions.ObserveAsEvents
import com.dag.mypayandroid.feature.jarvis.presentation.JarvisView
import com.dag.mypayandroid.feature.login.LoginView
import com.dag.mypayandroid.feature.settings.presentation.SettingsView

@Composable
fun DefaultNavigationHost(
    modifier: Modifier = Modifier,
    startDestination: Destination = Destination.Splash,
    navigator: DefaultNavigator,
    navBackStackEntryState: (NavBackStackEntry) -> Unit,
) {
    val navController = rememberNavController()
    ObserveAsEvents(flow = navigator.navigationActions) { action ->
        when (action) {
            is NavigationAction.Navigate -> navController.navigate(action.destination){
                action.navOptions(this)
            }
            NavigationAction.NavigateUp -> navController.navigateUp()
        }
    }
    ObserveAsEvents(flow = navController.currentBackStackEntryFlow){
        navBackStackEntryState(it)
    }
    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
            startDestination = startDestination
        ) {
            splashComposable<Destination.Splash> {
                SplashView(
                    navController = navController
                )
            }

            composableWithAnimations<Destination.HomeScreen> {
                HomeView()
            }

            composableWithAnimations<Destination.LoginScreen> {
                LoginView(
                    navController = navController
                )
            }

            composableWithAnimations<Destination.SettingsScreen> {
                SettingsView()
            }

            composableWithAnimations<Destination.JarvisScreen> {
                JarvisView()
            }
        }
    }
}
package de.aploi.spettrobyeyed.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.aploi.spettrobyeyed.ui.screens.ChatScreen
import de.aploi.spettrobyeyed.ui.screens.ConnectionScreen
import de.aploi.spettrobyeyed.ui.viewmodel.ConnectionState
import de.aploi.spettrobyeyed.ui.viewmodel.SpettroViewModel

@Composable
fun NavGraph(viewModel: SpettroViewModel) {
    val navController = rememberNavController()
    val connectionState by viewModel.connectionState.collectAsState()

    LaunchedEffect(connectionState) {
        val route = navController.currentDestination?.route
        when {
            connectionState is ConnectionState.Connected && route == "connection" ->
                navController.navigate("chat") {
                    popUpTo("connection") { inclusive = true }
                }
            connectionState is ConnectionState.Disconnected && route == "chat" ->
                navController.navigate("connection") {
                    popUpTo("chat") { inclusive = true }
                }
        }
    }

    NavHost(navController = navController, startDestination = "connection") {
        composable("connection") {
            ConnectionScreen(
                viewModel = viewModel,
                onConnected = {
                    navController.navigate("chat") {
                        popUpTo("connection") { inclusive = true }
                    }
                }
            )
        }
        composable("chat") {
            ChatScreen(
                viewModel = viewModel,
                onDisconnect = {
                    viewModel.disconnect()
                    navController.navigate("connection") {
                        popUpTo("chat") { inclusive = true }
                    }
                }
            )
        }
    }
}

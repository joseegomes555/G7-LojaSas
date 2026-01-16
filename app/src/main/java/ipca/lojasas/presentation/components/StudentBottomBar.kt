package ipca.lojasas.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import ipca.lojasas.Routes
import ipca.lojasas.ui.theme.IPCAGreen

@Composable
fun StudentBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.White) {

        // 1. INÍCIO (Dashboard)
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
            label = { Text("Início") },
            selected = currentRoute == Routes.STUDENT_DASHBOARD,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IPCAGreen,
                selectedTextColor = IPCAGreen,
                indicatorColor = IPCAGreen.copy(alpha = 0.1f)
            ),
            onClick = {
                if (currentRoute != Routes.STUDENT_DASHBOARD) {
                    navController.navigate(Routes.STUDENT_DASHBOARD) {
                        popUpTo(Routes.STUDENT_DASHBOARD) { inclusive = true }
                    }
                }
            }
        )

        // 2. FAZER PEDIDO
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Pedir") },
            label = { Text("Pedir") },
            selected = currentRoute == Routes.STUDENT_ORDER,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IPCAGreen,
                selectedTextColor = IPCAGreen,
                indicatorColor = IPCAGreen.copy(alpha = 0.1f)
            ),
            onClick = {
                if (currentRoute != Routes.STUDENT_ORDER) {
                    navController.navigate(Routes.STUDENT_ORDER) {
                        popUpTo(Routes.STUDENT_DASHBOARD) { saveState = true }
                    }
                }
            }
        )

        // 3. PERFIL
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            selected = currentRoute == Routes.STUDENT_PROFILE,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IPCAGreen,
                selectedTextColor = IPCAGreen,
                indicatorColor = IPCAGreen.copy(alpha = 0.1f)
            ),
            onClick = {
                if (currentRoute != Routes.STUDENT_PROFILE) {
                    navController.navigate(Routes.STUDENT_PROFILE) {
                        popUpTo(Routes.STUDENT_DASHBOARD) { saveState = true }
                    }
                }
            }
        )
    }
}
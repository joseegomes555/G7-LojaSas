package ipca.lojasas.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
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
fun BottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.White) {
        // 1. INÍCIO
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
            label = { Text("Início") },
            selected = currentRoute == Routes.STAFF_DASHBOARD,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen, selectedTextColor = IPCAGreen, indicatorColor = IPCAGreen.copy(alpha = 0.1f)),
            onClick = {
                if (currentRoute != Routes.STAFF_DASHBOARD) {
                    navController.navigate(Routes.STAFF_DASHBOARD) {
                        popUpTo(Routes.STAFF_DASHBOARD) { inclusive = true }
                    }
                }
            }
        )

        // 2. STOCK
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Stock") },
            label = { Text("Stock") },
            selected = currentRoute == Routes.STAFF_STOCK,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen, selectedTextColor = IPCAGreen, indicatorColor = IPCAGreen.copy(alpha = 0.1f)),
            onClick = {
                if (currentRoute != Routes.STAFF_STOCK) {
                    navController.navigate(Routes.STAFF_STOCK) {
                        popUpTo(Routes.STAFF_DASHBOARD) { saveState = true }
                    }
                }
            }
        )

        // 3. CANDIDATURAS
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Pedidos") },
            label = { Text("Pedidos") },
            selected = currentRoute == Routes.STAFF_CANDIDATURA, // Vamos criar esta rota já a seguir
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen, selectedTextColor = IPCAGreen, indicatorColor = IPCAGreen.copy(alpha = 0.1f)),
            onClick = {
                if (currentRoute != Routes.STAFF_CANDIDATURA) {
                    navController.navigate(Routes.STAFF_CANDIDATURA) {
                        popUpTo(Routes.STAFF_DASHBOARD) { saveState = true }
                    }
                }
            }
        )

        // 4. BENEFICIÁRIOS
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Benefic.") },
            label = { Text("Benefic.") },
            selected = currentRoute == Routes.STAFF_BENEFICIARIOS,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen, selectedTextColor = IPCAGreen, indicatorColor = IPCAGreen.copy(alpha = 0.1f)),
            onClick = {
                if (currentRoute != Routes.STAFF_BENEFICIARIOS) {
                    navController.navigate(Routes.STAFF_BENEFICIARIOS) {
                        popUpTo(Routes.STAFF_DASHBOARD) { saveState = true }
                    }
                }
            }
        )
    }
}
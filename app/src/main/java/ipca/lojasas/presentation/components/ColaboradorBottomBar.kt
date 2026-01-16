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
fun ColaboradorBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.White) {
        // 1. INÍCIO
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
            label = { Text("Início") },
            selected = currentRoute == Routes.COLABORADOR_DASHBOARD,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen, selectedTextColor = IPCAGreen, indicatorColor = IPCAGreen.copy(alpha = 0.1f)),
            onClick = {
                if (currentRoute != Routes.COLABORADOR_DASHBOARD) {
                    navController.navigate(Routes.COLABORADOR_DASHBOARD) {
                        popUpTo(Routes.COLABORADOR_DASHBOARD) { inclusive = true }
                    }
                }
            }
        )

        // 2. STOCK
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Stock") },
            label = { Text("Stock") },
            selected = currentRoute == Routes.COLABORADOR_STOCK,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen, selectedTextColor = IPCAGreen, indicatorColor = IPCAGreen.copy(alpha = 0.1f)),
            onClick = {
                if (currentRoute != Routes.COLABORADOR_STOCK) {
                    navController.navigate(Routes.COLABORADOR_STOCK) {
                        popUpTo(Routes.COLABORADOR_DASHBOARD) { saveState = true }
                    }
                }
            }
        )

        // 3. CANDIDATURAS
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Pedidos") },
            label = { Text("Candidaturas") },
            selected = currentRoute == Routes.COLABORADOR_CANDIDATURA, // Vamos criar esta rota já a seguir
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen, selectedTextColor = IPCAGreen, indicatorColor = IPCAGreen.copy(alpha = 0.1f)),
            onClick = {
                if (currentRoute != Routes.COLABORADOR_CANDIDATURA) {
                    navController.navigate(Routes.COLABORADOR_CANDIDATURA) {
                        popUpTo(Routes.COLABORADOR_DASHBOARD) { saveState = true }
                    }
                }
            }
        )

        // 4. BENEFICIÁRIOS
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Benefic.") },
            label = { Text("Beneficiários") },
            selected = currentRoute == Routes.COLABORADOR_BENEFICIARIOS,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen, selectedTextColor = IPCAGreen, indicatorColor = IPCAGreen.copy(alpha = 0.1f)),
            onClick = {
                if (currentRoute != Routes.COLABORADOR_BENEFICIARIOS) {
                    navController.navigate(Routes.COLABORADOR_BENEFICIARIOS) {
                        popUpTo(Routes.COLABORADOR_DASHBOARD) { saveState = true }
                    }
                }
            }
        )
    }
}
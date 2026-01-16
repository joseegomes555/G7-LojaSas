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
fun BeneficiarioBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.White) {

        // 1. INÍCIO
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
            label = { Text("Início") },
            selected = currentRoute == Routes.BENEFICIARIO_DASHBOARD,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IPCAGreen,
                selectedTextColor = IPCAGreen,
                indicatorColor = IPCAGreen.copy(alpha = 0.1f)
            ),
            onClick = {
                if (currentRoute != Routes.BENEFICIARIO_DASHBOARD) {
                    navController.navigate(Routes.BENEFICIARIO_DASHBOARD) {
                        popUpTo(Routes.BENEFICIARIO_DASHBOARD) { inclusive = true }
                    }
                }
            }
        )

        // 2. FAZER PEDIDO
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Pedir") },
            label = { Text("Pedir") },
            selected = currentRoute == Routes.BENEFICIARIO_ORDER,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IPCAGreen,
                selectedTextColor = IPCAGreen,
                indicatorColor = IPCAGreen.copy(alpha = 0.1f)
            ),
            onClick = {
                if (currentRoute != Routes.BENEFICIARIO_ORDER) {
                    navController.navigate(Routes.BENEFICIARIO_ORDER) {
                        popUpTo(Routes.BENEFICIARIO_ORDER) { saveState = true }
                    }
                }
            }
        )

        // 3. PERFIL
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            selected = currentRoute == Routes.BENEFICIARIO_PROFILE,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IPCAGreen,
                selectedTextColor = IPCAGreen,
                indicatorColor = IPCAGreen.copy(alpha = 0.1f)
            ),
            onClick = {
                if (currentRoute != Routes.BENEFICIARIO_PROFILE) {
                    navController.navigate(Routes.BENEFICIARIO_PROFILE) {
                        popUpTo(Routes.BENEFICIARIO_DASHBOARD) { saveState = true }
                    }
                }
            }
        )
    }
}
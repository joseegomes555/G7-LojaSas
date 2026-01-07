package ipca.lojasas.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import ipca.lojasas.Routes
import ipca.lojasas.ui.theme.IPCAGreen

@Composable
fun AppBottomBar(navController: NavController, currentRoute: String) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = currentRoute == Routes.STAFF_DASHBOARD,
            onClick = {
                if (currentRoute != Routes.STAFF_DASHBOARD) {
                    navController.navigate(Routes.STAFF_DASHBOARD)
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
            label = { Text("Início") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen)
        )
        NavigationBarItem(
            selected = currentRoute == Routes.STAFF_STOCK,
            onClick = {
                if (currentRoute != Routes.STAFF_STOCK) {
                    navController.navigate(Routes.STAFF_STOCK)
                }
            },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Stock") },
            label = { Text("Stock") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen)
        )
        NavigationBarItem(
            selected = currentRoute == Routes.STAFF_BENEFICIARIOS,
            onClick = {
                if (currentRoute != Routes.STAFF_BENEFICIARIOS) {
                    navController.navigate(Routes.STAFF_BENEFICIARIOS)
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Benefic.") },
            label = { Text("Benefic.") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen)
        )
    }
}
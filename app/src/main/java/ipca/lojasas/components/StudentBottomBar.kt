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
fun StudentBottomBar(navController: NavController, currentRoute: String) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = currentRoute == Routes.STUDENT_DASHBOARD,
            onClick = { navController.navigate(Routes.STUDENT_DASHBOARD) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
            label = { Text("Início") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen)
        )
        NavigationBarItem(
            selected = currentRoute == Routes.STUDENT_ORDER,
            onClick = { navController.navigate(Routes.STUDENT_ORDER) },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Pedir") },
            label = { Text("Pedir") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen)
        )
        NavigationBarItem(
            selected = currentRoute == Routes.STUDENT_PROFILE,
            onClick = { navController.navigate(Routes.STUDENT_PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = IPCAGreen)
        )
    }
}
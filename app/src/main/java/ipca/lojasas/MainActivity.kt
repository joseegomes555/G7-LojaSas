package ipca.lojasas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ipca.lojasas.presentation.screens.AuthChoiceScreen
import ipca.lojasas.presentation.screens.Beneficiarios.BeneficiarioDetailScreen
import ipca.lojasas.presentation.screens.Beneficiarios.BeneficiariosScreen
import ipca.lojasas.presentation.screens.Staff.StaffDashboardScreen
import ipca.lojasas.presentation.screens.Staff.StaffLoginScreen
import ipca.lojasas.presentation.screens.Staff.StaffOrderDetailScreen
import ipca.lojasas.presentation.screens.StockScreen
import ipca.lojasas.presentation.screens.Students.StudentDashboardScreen
import ipca.lojasas.presentation.screens.Students.StudentLoginScreen
import ipca.lojasas.presentation.screens.Students.StudentOrderScreen
import ipca.lojasas.presentation.screens.Students.StudentProfileScreen
import ipca.lojasas.ui.theme.LojaSasTheme

// Definição de todas as rotas da aplicação
object Routes {
    // Autenticação
    const val CHOICE = "choice"
    const val STUDENT_LOGIN = "student_login"
    const val STAFF_LOGIN = "staff_login"

    // Área do Estudante
    const val STUDENT_DASHBOARD = "student_dashboard"
    const val STUDENT_ORDER = "student_order"
    const val STUDENT_PROFILE = "student_profile"

    // Área do Staff
    const val STAFF_DASHBOARD = "staff_dashboard"
    const val STAFF_STOCK = "staff_stock"
    const val STAFF_BENEFICIARIOS = "staff_beneficiarios"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LojaSasTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Routes.CHOICE
                    ) {

                        // 1. ECRÃS DE ENTRADA

                        // Ecrã de Escolha (Aluno vs Staff)
                        composable(Routes.CHOICE) {
                            AuthChoiceScreen(
                                onStudentClick = { navController.navigate(Routes.STUDENT_LOGIN) },
                                onStaffClick = { navController.navigate(Routes.STAFF_LOGIN) }
                            )
                        }

                        // Login Aluno
                        composable(Routes.STUDENT_LOGIN) {
                            StudentLoginScreen(
                                onBack = { navController.popBackStack() },
                                onLoginSuccess = {
                                    // Navega para o Dashboard do Aluno e limpa o login da história
                                    navController.navigate(Routes.STUDENT_DASHBOARD) {
                                        popUpTo(Routes.CHOICE) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Login Staff
                        composable(Routes.STAFF_LOGIN) {
                            StaffLoginScreen(
                                onBack = { navController.popBackStack() },
                                onLoginSuccess = {
                                    // Navega para o Dashboard de Staff e limpa o login da história
                                    navController.navigate(Routes.STAFF_DASHBOARD) {
                                        popUpTo(Routes.CHOICE) { inclusive = true }
                                    }
                                }
                            )
                        }


                        // 2. ÁREA DO ALUNO

                        // Dashboard Aluno
                        composable(Routes.STUDENT_DASHBOARD) {
                            StudentDashboardScreen(navController = navController)
                        }

                        // Ecrã de Pedidos
                        composable(Routes.STUDENT_ORDER) {
                            StudentOrderScreen(navController = navController)
                        }

                        // Ecrã de Perfil
                        composable(Routes.STUDENT_PROFILE) {
                            StudentProfileScreen(navController = navController)
                        }


                        // 3. ÁREA DO STAFF

                        // Dashboard Principal (Alertas e Entregas)
                        composable(Routes.STAFF_DASHBOARD) {
                            StaffDashboardScreen(
                                navController = navController,
                                onLogout = {
                                    // Volta ao início e limpa a pilha de navegação
                                    navController.navigate(Routes.CHOICE) {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }

                        // Gestão de Stock
                        composable(Routes.STAFF_STOCK) {
                            StockScreen(navController = navController)
                        }

                        // Gestão de Beneficiários
                        composable(Routes.STAFF_BENEFICIARIOS) {
                            BeneficiariosScreen(navController = navController)
                        }

                        composable(
                            route = "staff_beneficiario_detail/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""
                            BeneficiarioDetailScreen(navController = navController, userId = userId)
                        }
                        composable(
                            route = "staff_order_detail/{pedidoId}",
                            arguments = listOf(androidx.navigation.navArgument("pedidoId") { type = androidx.navigation.NavType.StringType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("pedidoId") ?: ""
                            StaffOrderDetailScreen(navController = navController, pedidoId = id)
                        }
                    }
                }
            }
        }
    }
}
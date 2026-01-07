package ipca.lojasas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ipca.lojasas.screens.*
import ipca.lojasas.ui.theme.LojaSasTheme

// Definição de todas as rotas da aplicação
object Routes {
    // Autenticação
    const val CHOICE = "choice"
    const val STUDENT_LOGIN = "student_login"
    const val STAFF_LOGIN = "staff_login"

    // Área do Estudante
    const val STUDENT_DASHBOARD = "student_dashboard"

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

                        composable(Routes.STUDENT_DASHBOARD) {
                            StudentDashboardScreen(
                                navController = navController
                            )
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
                    }
                }
            }
        }
    }
}
package ipca.lojasas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp

// IMPORTS
import ipca.lojasas.presentation.screens.Beneficiarios.BeneficiarioDetailScreen
import ipca.lojasas.presentation.screens.Beneficiarios.BeneficiariosScreen
import ipca.lojasas.presentation.screens.CandidaturaScreen
import ipca.lojasas.presentation.screens.Staff.StaffDashboardScreen
import ipca.lojasas.presentation.screens.Staff.StaffLoginScreen
import ipca.lojasas.presentation.screens.Staff.StaffOrderDetailScreen
import ipca.lojasas.presentation.screens.Staff.StaffCandidaturaScreen
import ipca.lojasas.presentation.screens.StockScreen
import ipca.lojasas.presentation.screens.AuthChoiceScreen
import ipca.lojasas.presentation.screens.Students.StudentDashboardScreen
import ipca.lojasas.presentation.screens.Students.StudentLoginScreen
import ipca.lojasas.presentation.screens.Students.StudentOrderDetailScreen
import ipca.lojasas.presentation.screens.Students.StudentOrderScreen
import ipca.lojasas.presentation.screens.Students.StudentProfileScreen
import ipca.lojasas.ui.theme.LojaSasTheme

object Routes {
    const val CHOICE = "choice"
    const val STUDENT_LOGIN = "student_login"
    const val STAFF_LOGIN = "staff_login"

    // Rotas Aluno
    const val STUDENT_DASHBOARD = "student_dashboard"
    const val STUDENT_ORDER = "student_order"
    const val STUDENT_PROFILE = "student_profile"

    // Rotas Staff
    const val STAFF_DASHBOARD = "staff_dashboard"
    const val STAFF_STOCK = "staff_stock"            // <--- CORRIGIDO
    const val STAFF_BENEFICIARIOS = "staff_beneficiarios" // <--- CORRIGIDO
    const val STAFF_CANDIDATURA = "staff_candidatura"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            LojaSasTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Routes.CHOICE) {

                    // --- GERAL ---
                    composable(Routes.CHOICE) { AuthChoiceScreen(navController) }
                    composable("candidacy") { CandidaturaScreen(navController) }

                    composable(Routes.STUDENT_LOGIN) { StudentLoginScreen(navController) }
                    composable(Routes.STAFF_LOGIN) { StaffLoginScreen(navController) }

                    // --- ALUNO ---
                    composable(Routes.STUDENT_DASHBOARD) { StudentDashboardScreen(navController) }
                    composable(Routes.STUDENT_ORDER) { StudentOrderScreen(navController) }
                    composable(Routes.STUDENT_PROFILE) { StudentProfileScreen(navController) }

                    composable(
                        route = "student_order_detail/{pedidoId}",
                        arguments = listOf(navArgument("pedidoId") { type = NavType.StringType })
                    ) { back -> StudentOrderDetailScreen(navController, back.arguments?.getString("pedidoId") ?: "") }

                    composable(
                        route = "beneficiario_detail/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { back -> BeneficiarioDetailScreen(navController, back.arguments?.getString("userId") ?: "") }

                    composable("student_register") { ipca.lojasas.presentation.screens.Students.StudentRegisterScreen(navController) }

                    // --- STAFF ---

                    composable(Routes.STAFF_DASHBOARD) {
                        StaffDashboardScreen(
                            navController = navController,
                            onLogout = {
                                navController.navigate(Routes.CHOICE) { popUpTo(0) }
                            }
                        )
                    }

                    composable(Routes.STAFF_STOCK) { StockScreen(navController) }
                    composable(Routes.STAFF_BENEFICIARIOS) { BeneficiariosScreen(navController) }
                    composable(Routes.STAFF_CANDIDATURA) { StaffCandidaturaScreen(navController) }

                    composable(
                        route = "staff_order_detail/{pedidoId}",
                        arguments = listOf(navArgument("pedidoId") { type = NavType.StringType })
                    ) { back -> StaffOrderDetailScreen(navController, back.arguments?.getString("pedidoId") ?: "") }
                }
            }
        }
    }
}
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
import ipca.lojasas.ui.theme.LojaSasTheme

// IMPORTS DOS ECRÃS
import ipca.lojasas.presentation.screens.AuthChoiceScreen
import ipca.lojasas.presentation.screens.CandidaturaScreen
import ipca.lojasas.presentation.screens.Beneficiario.BeneficiarioLoginScreen
import ipca.lojasas.presentation.screens.Beneficiario.BeneficiarioRegisterScreen
import ipca.lojasas.presentation.screens.Colaborador.ColaboradorLoginScreen

import ipca.lojasas.presentation.screens.Beneficiario.BeneficiarioDashboardScreen
import ipca.lojasas.presentation.screens.Beneficiario.BeneficiarioOrderScreen
import ipca.lojasas.presentation.screens.Beneficiario.BeneficiarioOrderDetailScreen
import ipca.lojasas.presentation.screens.Beneficiario.BeneficiarioProfileScreen

import ipca.lojasas.presentation.screens.Colaborador.ColaboradorDashboardScreen
import ipca.lojasas.presentation.screens.Colaborador.ColaboradorOrderDetailScreen
import ipca.lojasas.presentation.screens.Colaborador.StockScreen
import ipca.lojasas.presentation.screens.Beneficiario.BeneficiariosScreen
import ipca.lojasas.presentation.screens.Beneficiario.BeneficiarioDetailScreen
import ipca.lojasas.presentation.screens.Colaborador.ColaboradorCandidaturasScreen

object Routes {
    const val CHOICE = "choice"
    const val CANDIDACY = "candidacy" // A rota que estava a faltar

    // Auth
    const val BENEFICIARIO_LOGIN = "beneficiario_login"
    const val BENEFICIARIO_REGISTER = "beneficiario_register"
    const val COLABORADOR_LOGIN = "colaborador_login"

    // Beneficiário
    const val BENEFICIARIO_DASHBOARD = "beneficiario_dashboard"
    const val BENEFICIARIO_ORDER = "beneficiario_order"
    const val BENEFICIARIO_PROFILE = "beneficiario_profile"

    // Colaborador
    const val COLABORADOR_DASHBOARD = "colaborador_dashboard"
    const val COLABORADOR_STOCK = "colaborador_stock"
    const val COLABORADOR_BENEFICIARIOS = "colaborador_beneficiarios"
    const val COLABORADOR_CANDIDATURA = "colaborador_candidatura"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            LojaSasTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Routes.CHOICE) {

                    // ECRÃS DE ENTRADA
                    composable(Routes.CHOICE) { AuthChoiceScreen(navController) }

                    composable(Routes.CANDIDACY) { CandidaturaScreen(navController) }

                    composable(Routes.BENEFICIARIO_LOGIN) { BeneficiarioLoginScreen(navController) }

                    composable(Routes.BENEFICIARIO_REGISTER) { BeneficiarioRegisterScreen(navController) }

                    composable(Routes.COLABORADOR_LOGIN) { ColaboradorLoginScreen(navController) }

                    // ECRÃS DO BENEFICIÁRIO
                    composable(Routes.BENEFICIARIO_DASHBOARD) { BeneficiarioDashboardScreen(navController) }
                    composable(Routes.BENEFICIARIO_ORDER) { BeneficiarioOrderScreen(navController) }
                    composable(Routes.BENEFICIARIO_PROFILE) { BeneficiarioProfileScreen(navController) }

                    composable(
                        route = "beneficiario_order_detail/{pedidoId}",
                        arguments = listOf(navArgument("pedidoId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("pedidoId") ?: ""
                        BeneficiarioOrderDetailScreen(navController, id)
                    }

                    // ECRÃS DO COLABORADOR
                    composable(Routes.COLABORADOR_DASHBOARD) {
                        ColaboradorDashboardScreen(
                            navController = navController,
                            onLogout = { navController.navigate(Routes.CHOICE) { popUpTo(0) } }
                        )
                    }

                    composable(Routes.COLABORADOR_STOCK) { StockScreen(navController) }
                    composable(Routes.COLABORADOR_BENEFICIARIOS) { BeneficiariosScreen(navController) }

                    composable(Routes.COLABORADOR_CANDIDATURA) { ColaboradorCandidaturasScreen(navController) }

                    composable(
                        route = "colaborador_order_detail/{pedidoId}",
                        arguments = listOf(navArgument("pedidoId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("pedidoId") ?: ""
                        ColaboradorOrderDetailScreen(navController, id)
                    }

                    composable(
                        route = "beneficiario_detail/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("userId") ?: ""
                        BeneficiarioDetailScreen(navController, id)
                    }
                }
            }
        }
    }
}
package ipca.lojasas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ipca.lojasas.screens.AuthChoiceScreen
import ipca.lojasas.screens.StudentLoginScreen
import ipca.lojasas.screens.StaffLoginScreen
import ipca.lojasas.ui.theme.LojaSasTheme

object Routes {
    const val CHOICE = "choice"
    const val STUDENT_LOGIN = "student_login"
    const val STAFF_LOGIN = "staff_login"
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
                        composable(Routes.CHOICE) {
                            AuthChoiceScreen(
                                onStudentClick = {
                                    navController.navigate(Routes.STUDENT_LOGIN)
                                },
                                onStaffClick = {
                                    navController.navigate(Routes.STAFF_LOGIN)
                                }
                            )
                        }

                        composable(Routes.STUDENT_LOGIN) {
                            StudentLoginScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.STAFF_LOGIN) {
                            StaffLoginScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

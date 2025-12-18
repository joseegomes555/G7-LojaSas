package ipca.lojasas.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AuthChoiceScreen(
    onStudentClick: () -> Unit,
    onStaffClick: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Loja Social — SAS IPCA",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text("Escolhe o tipo de acesso")

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onStudentClick,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Login Aluno")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onStaffClick,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Login Funcionário (SAS)")
            }
        }
    }
}


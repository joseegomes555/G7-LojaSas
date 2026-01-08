package ipca.lojasas.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ipca.lojasas.ui.theme.IPCAGreen

@Composable
fun AuthChoiceScreen(
    onStudentClick: () -> Unit,
    onStaffClick: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título / Logo
            Text(
                text = "Loja Social",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = IPCAGreen
            )
            Text(
                text = "SAS IPCA",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )

            Spacer(Modifier.height(48.dp))

            // Botão Aluno
            Button(
                onClick = onStudentClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Sou Aluno")
            }

            Spacer(Modifier.height(16.dp))

            // Botão Staff
            OutlinedButton(
                onClick = onStaffClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = IPCAGreen),
                border = androidx.compose.foundation.BorderStroke(1.dp, IPCAGreen),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Sou Colaborador (SAS)")
            }
        }
    }
}
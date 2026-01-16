package ipca.lojasas.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ipca.lojasas.R
import ipca.lojasas.Routes
import ipca.lojasas.ui.theme.IPCAGreen

@Composable
fun AuthChoiceScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Loja Social SAS",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = IPCAGreen,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Bem-vindo",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(48.dp))

            // BOTÃO LOGIN BENEFICIÁRIO
            Button(
                onClick = { navController.navigate(Routes.BENEFICIARIO_LOGIN) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Beneficiário", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTÃO LOGIN COLABORADOR
            OutlinedButton(
                onClick = { navController.navigate(Routes.COLABORADOR_LOGIN) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = IPCAGreen),
                border = BorderStroke(2.dp, IPCAGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Colaborador", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(48.dp))

            // BOTÃO CANDIDATURA
            Text("Ainda não tens acesso?", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate("candidacy") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Fazer Candidatura")
            }
        }
    }
}
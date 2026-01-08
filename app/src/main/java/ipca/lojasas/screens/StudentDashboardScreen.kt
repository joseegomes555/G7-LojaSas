package ipca.lojasas.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ipca.lojasas.Routes
import ipca.lojasas.model.Pedido
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.viewmodel.StudentViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    navController: NavController,
    viewModel: StudentViewModel = viewModel()
) {
    val proximo = viewModel.proximoLevantamento.firstOrNull()
    val historico = viewModel.meusPedidos

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Área do Estudante", color = IPCAGreen, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        navController.navigate(Routes.CHOICE) { popUpTo(0) }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sair", tint = IPCAGreen)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // ESTADO
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IPCAGreen),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Estado do Beneficiário", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ATIVO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tens acesso à Loja Social válido até 30/06/2026.", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LEVANTAMENTO
            Text("Próximo Levantamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (proximo != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IPCAGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Agendado", color = IPCAGreen, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = IPCAGreen)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val dataStr = proximo.dataPedido?.toDate()?.let { sdf.format(it) } ?: "Data N/A"

                        Text(dataStr, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Pedido #${proximo.id.take(6)}", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Sem levantamentos agendados.", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // HISTÓRICO
            Text("Histórico Recente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(historico) { pedido ->
                    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                    val data = pedido.dataPedido?.toDate()?.let { sdf.format(it) } ?: "-"

                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Levantamento $data", fontSize = 14.sp)
                        }
                        Text("Entregue", color = IPCAGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
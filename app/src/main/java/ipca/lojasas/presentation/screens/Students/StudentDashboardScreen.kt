package ipca.lojasas.presentation.screens.Students

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ipca.lojasas.Routes
import ipca.lojasas.presentation.components.StudentBottomBar
import ipca.lojasas.di.AppModule
import ipca.lojasas.presentation.viewmodel.StudentViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    navController: NavController,
    // CLEAN ARCH: Usamos a Factory para criar o ViewModel
    viewModel: StudentViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    // CLEAN ARCH: Observamos o StateFlow
    val pedidos by viewModel.meusPedidos.collectAsState()

    val pedidoAtivo = pedidos.firstOrNull { it.estado != "Entregue" && it.estado != "Cancelado" }
    val historico = pedidos.filter { it.estado == "Entregue" || it.estado == "Cancelado" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Área do Beneficiário", color = IPCAGreen, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        navController.navigate(Routes.CHOICE) { popUpTo(0) }
                    }) { Icon(Icons.Default.ExitToApp, contentDescription = "Sair", tint = IPCAGreen) }
                }
            )
        },
        bottomBar = { StudentBottomBar(navController, Routes.STUDENT_DASHBOARD) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IPCAGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Face, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Bem-vindo(a)!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Conta Ativa", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Estado do Pedido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (pedidoAtivo != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    border = BorderStroke(1.dp, IPCAGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Pedido #${pedidoAtivo.id.take(4)}", color = Color.Gray, fontSize = 12.sp)
                            val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                            val dataStr = pedidoAtivo.dataPedido?.toDate()?.let { sdf.format(it) } ?: "--"
                            Text(dataStr, color = Color.Gray, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, null, tint = IPCAGreen, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(pedidoAtivo.estado.uppercase(), color = IPCAGreen, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text("Aguarde atualização.", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Não tens pedidos ativos.", color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Histórico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(historico) { pedido ->
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val data = pedido.dataPedido?.toDate()?.let { sdf.format(it) } ?: "-"
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Concluído", fontWeight = FontWeight.Bold)
                        Text(data, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
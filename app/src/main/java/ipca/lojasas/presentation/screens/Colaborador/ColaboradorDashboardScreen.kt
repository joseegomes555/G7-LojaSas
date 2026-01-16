package ipca.lojasas.presentation.screens.Colaborador

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
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
import com.google.firebase.auth.FirebaseAuth
import ipca.lojasas.di.AppModule
import ipca.lojasas.presentation.components.ColaboradorBottomBar
import ipca.lojasas.presentation.viewmodel.ColaboradorViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColaboradorDashboardScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: ColaboradorViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    val todosPedidos by viewModel.todosPedidos.collectAsState()
    val pedidosPendentes = todosPedidos.filter { it.estado == "Pendente" || it.estado == "Reagendamento" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Painel Colaborador", color = Color.White) },
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    }) { Icon(Icons.Default.ExitToApp, "Sair", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        },
        bottomBar = { ColaboradorBottomBar(navController) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), modifier = Modifier.weight(1f).height(100.dp)) {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${pedidosPendentes.size}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                        Text("Pendentes", fontSize = 14.sp, color = Color(0xFF1565C0))
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), modifier = Modifier.weight(1f).height(100.dp)) {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        val entregues = todosPedidos.count { it.estado == "Entregue" }
                        Text("$entregues", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = IPCAGreen)
                        Text("Entregues", fontSize = 14.sp, color = IPCAGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Pedidos Pendentes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (pedidosPendentes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Tudo limpo! Sem pedidos pendentes.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(pedidosPendentes) { pedido ->
                        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                        val dataStr = pedido.dataPedido?.toDate()?.let { sdf.format(it) } ?: "Data desc."

                        Card(
                            // CORREÇÃO: Rota correta para detalhes
                            onClick = { navController.navigate("colaborador_order_detail/${pedido.id}") },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(pedido.nomeBeneficiario, fontWeight = FontWeight.Bold)
                                    Text(dataStr, fontSize = 12.sp, color = Color.Gray)
                                    Text("${pedido.itens.size} itens", fontSize = 12.sp, color = Color.Gray)
                                }
                                if (pedido.urgencia == "Urgente") {
                                    Surface(color = IPCARed, shape = MaterialTheme.shapes.small) {
                                        Text("URGENTE", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                } else {
                                    Text(pedido.estado, fontSize = 12.sp, color = IPCAGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
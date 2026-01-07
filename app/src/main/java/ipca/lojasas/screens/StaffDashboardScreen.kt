package ipca.lojasas.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ipca.lojasas.Routes
import ipca.lojasas.components.AppBottomBar
import ipca.lojasas.model.Lote
import ipca.lojasas.model.Pedido
import ipca.lojasas.ui.theme.IPCABackground
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed
import ipca.lojasas.viewmodel.StaffViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: StaffViewModel = viewModel()
) {
    val alertas = viewModel.alertasValidade
    val pedidos = viewModel.pedidosDoDia

    Scaffold(
        containerColor = IPCABackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Olá, Colaborador", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text("SAS - Loja Social", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen),
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sair", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            AppBottomBar(navController, Routes.STAFF_DASHBOARD)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ALERTAS
            if (alertas.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.border(1.dp, IPCARed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = IPCARed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Validade Crítica", color = IPCARed, fontWeight = FontWeight.Bold)
                            }
                            Text("Estes lotes expiram em breve.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

                            alertas.forEach { lote ->
                                AlertItemRow(lote)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // PEDIDOS
            item {
                Text("Pedidos Pendentes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (pedidos.isEmpty()) {
                item {
                    Text("Não há pedidos pendentes para hoje.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(pedidos) { pedido ->
                    DeliveryCard(pedido, onEntregar = {
                        viewModel.processarEntrega(pedido.id)
                    })
                }
            }
        }
    }
}

@Composable
fun AlertItemRow(lote: Lote) {
    val dias = lote.diasParaExpirar()
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF0F0), RoundedCornerShape(8.dp)).padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(lote.nomeProduto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("Qtd: ${lote.quantidade}", fontSize = 11.sp, color = Color.Gray)
        }
        Text(if(dias <= 0) "Expirou!" else "Expira em $dias dias", color = IPCARed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
fun DeliveryCard(pedido: Pedido, onEntregar: () -> Unit) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val hora = pedido.dataPedido?.toDate()?.let { sdf.format(it) } ?: "--:--"

    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Surface(color = IPCAGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text(hora, color = IPCAGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Text("ID: ${pedido.id.take(4)}", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(pedido.nomeAluno, fontWeight = FontWeight.Bold)
            Text("Nº ${pedido.numAluno}", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onEntregar,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen)
            ) { Text("Confirmar Entrega") }
        }
    }
}
package ipca.lojasas.screens.Staff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import ipca.lojasas.di.AppModule
import ipca.lojasas.domain.model.Lote
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.presentation.viewmodel.StaffViewModel
import ipca.lojasas.ui.theme.IPCABackground
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: StaffViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    val pedidos by viewModel.pedidosPendentes.collectAsState()
    val alertas by viewModel.alertasValidade.collectAsState()

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
        bottomBar = { AppBottomBar(navController, Routes.STAFF_DASHBOARD) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (alertas.isNotEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, IPCARed.copy(0.3f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Validade Crítica", color = IPCARed, fontWeight = FontWeight.Bold)
                            alertas.forEach { AlertItemRow(it); Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }
            }
            item { Text("Pedidos Pendentes", fontWeight = FontWeight.Bold) }
            if (pedidos.isEmpty()) item { Text("Sem pedidos.") }
            else items(pedidos) { p ->
                DeliveryCard(p, { viewModel.processarEntrega(p.id) }, { navController.navigate("staff_order_detail/${p.id}") })
            }
        }
    }
}

@Composable
fun AlertItemRow(lote: Lote) {
    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF0F0), RoundedCornerShape(8.dp)).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(lote.nomeProduto, fontWeight = FontWeight.SemiBold)
        Text("Expira em ${lote.diasParaExpirar()} dias", color = IPCARed)
    }
}

@Composable
fun DeliveryCard(pedido: Pedido, onEntregar: () -> Unit, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp), modifier = Modifier.clickable { onClick() }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("PEDIDO", color = IPCAGreen, fontWeight = FontWeight.Bold)
                Text("ID: ${pedido.id.take(4)}", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(pedido.nomeAluno, fontWeight = FontWeight.Bold)
                    Text("Nº ${pedido.numAluno}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onEntregar, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen)) { Text("Confirmar Entrega") }
        }
    }
}
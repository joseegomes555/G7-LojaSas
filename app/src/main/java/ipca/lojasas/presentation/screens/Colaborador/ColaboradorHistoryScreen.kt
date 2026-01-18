package ipca.lojasas.presentation.screens.Colaborador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import ipca.lojasas.di.AppModule
import ipca.lojasas.presentation.viewmodel.ColaboradorViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColaboradorHistoryScreen(
    navController: NavController,
    viewModel: ColaboradorViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    val todosPedidos by viewModel.todosPedidos.collectAsState()

    // Filtra apenas os pedidos fechados (Entregues ou Cancelados)
    val historicoPedidos = todosPedidos.filter {
        it.estado == "Entregue" || it.estado == "Cancelado"
    }.sortedByDescending { it.dataPedido }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Entregas", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            if (historicoPedidos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ainda não há histórico de entregas.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(historicoPedidos) { pedido ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val dataStr = pedido.dataPedido?.toDate()?.let { sdf.format(it) } ?: "Data desc."

                        Card(
                            onClick = {
                                // Navega para o detalhe (onde puseste o botão de enviar email)
                                navController.navigate("colaborador_order_detail/${pedido.id}")
                            },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(pedido.nomeBeneficiario, fontWeight = FontWeight.Bold)
                                    Text(dataStr, fontSize = 12.sp, color = Color.Gray)
                                    Text("${pedido.itens.size} itens", fontSize = 12.sp, color = Color.Gray)
                                }

                                Surface(
                                    color = if (pedido.estado == "Entregue") IPCAGreen else IPCARed,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = pedido.estado.uppercase(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
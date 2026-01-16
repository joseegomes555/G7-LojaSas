package ipca.lojasas.presentation.screens.Students

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import ipca.lojasas.presentation.viewmodel.StudentViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentOrderDetailScreen(
    navController: NavController,
    pedidoId: String,
    viewModel: StudentViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    // Busca os pedidos da lista que já está carregada no ViewModel
    val pedidos by viewModel.meusPedidos.collectAsState()
    val pedido = pedidos.find { it.id == pedidoId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Pedido", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        }
    ) { padding ->
        if (pedido == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IPCAGreen)
            }
        } else {
            Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {

                // --- CABEÇALHO ---
                Text("Pedido #${pedido.id.take(4).uppercase()}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = IPCAGreen)
                Spacer(modifier = Modifier.height(8.dp))

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dataEntrega = pedido.dataLevantamento?.toDate()?.let { sdf.format(it) } ?: "A definir"

                Text("Data Recolha: $dataEntrega")
                Text("Estado: ${pedido.estado}", fontWeight = FontWeight.Bold, color = if(pedido.estado == "Cancelado") IPCARed else IPCAGreen)

                Spacer(modifier = Modifier.height(24.dp))

                // --- ALERTA DE REAGENDAMENTO (Se existir) ---
                if (pedido.estado == "Reagendamento" || pedido.propostaReagendamento != null) {
                    val novaData = pedido.propostaReagendamento?.toDate()?.let { sdf.format(it) } ?: "?"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, tint = Color(0xFFE65100))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Proposta de Reagendamento", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("O Staff sugeriu uma nova data: $novaData")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (pedido.propostaReagendamento != null) {
                                        viewModel.aceitarReagendamento(pedido.id, pedido.propostaReagendamento!!)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                            ) {
                                Text("Aceitar Nova Data")
                            }
                        }
                    }
                }

                // --- ALERTA DE CANCELAMENTO ---
                if (pedido.estado == "Cancelado" && pedido.motivoCancelamento.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = IPCARed.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Motivo do Cancelamento:", fontWeight = FontWeight.Bold, color = IPCARed)
                            Text(pedido.motivoCancelamento)
                        }
                    }
                }

                // --- LISTA DE ITENS ---
                Text("Itens do Pedido", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pedido.itens) { item ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.nome)
                                Text("x${item.quantidade}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // --- BOTÃO CANCELAR ---
                if (pedido.estado == "Pendente") {
                    Button(
                        onClick = {
                            viewModel.cancelarPedido(pedido.id)
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IPCARed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar Pedido")
                    }
                }
            }
        }
    }
}
package ipca.lojasas.presentation.screens.Staff

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ipca.lojasas.di.AppModule
import ipca.lojasas.presentation.viewmodel.StaffViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffOrderDetailScreen(
    navController: NavController,
    pedidoId: String,
    viewModel: StaffViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    val todosPedidos by viewModel.todosPedidos.collectAsState()
    val pedido = todosPedidos.find { it.id == pedidoId }
    val context = LocalContext.current

    // Estados para Reagendamento
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val data = datePickerState.selectedDateMillis
                    if (data != null) {
                        viewModel.reagendarPedido(pedidoId, data)
                        Toast.makeText(context, "Proposta enviada!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                    showDatePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhe do Pedido", color = Color.White) },
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

                Text("Aluno: ${pedido.nomeAluno}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Nº: ${pedido.numAluno}", color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))

                Text("Produtos:", fontWeight = FontWeight.Bold)
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pedido.itens) { item ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(item.nome)
                                Text("x${item.quantidade}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (pedido.estado == "Pendente") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = {
                                    viewModel.atualizarEstadoPedido(pedido.id, "Cancelado")
                                    navController.popBackStack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IPCARed),
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancelar") }

                            Button(
                                onClick = {
                                    viewModel.atualizarEstadoPedido(pedido.id, "Entregue")
                                    navController.popBackStack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen),
                                modifier = Modifier.weight(1f)
                            ) { Text("Entregar") }
                        }

                        // Botão Reagendar
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100))
                        ) {
                            Icon(Icons.Default.DateRange, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Sugerir Nova Data (Reagendar)")
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if(pedido.estado == "Cancelado") IPCARed else IPCAGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Estado: ${pedido.estado.uppercase()}",
                            color = Color.White, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp), fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}
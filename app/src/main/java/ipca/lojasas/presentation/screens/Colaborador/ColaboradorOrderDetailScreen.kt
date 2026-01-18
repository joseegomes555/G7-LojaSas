package ipca.lojasas.presentation.screens.Colaborador

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email // Import para o ícone de Email
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
import com.google.firebase.auth.FirebaseAuth
import ipca.lojasas.di.AppModule
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.presentation.viewmodel.ColaboradorViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColaboradorOrderDetailScreen(
    navController: NavController,
    pedidoId: String,
    viewModel: ColaboradorViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    val todosPedidos by viewModel.todosPedidos.collectAsState()

    val pedido = remember(todosPedidos, pedidoId) {
        todosPedidos.find { it.id == pedidoId }
    }

    val context = LocalContext.current

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
                if (todosPedidos.isEmpty()) {
                    CircularProgressIndicator(color = IPCAGreen)
                } else {
                    Text("Pedido não encontrado.")
                }
            }
        } else {
            Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {

                Text("Beneficiário: ${pedido.nomeBeneficiario}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Nº: ${pedido.numBeneficiario}", color = Color.Gray)
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

                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100))
                        ) {
                            Icon(Icons.Default.DateRange, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Sugerir Nova Data")
                        }
                    }
                } else {
                    // ESTADO NÃO PENDENTE (Entregue, Cancelado, etc)
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

                    // --- BOTÃO DE RELATÓRIO (APENAS SE ENTREGUE) ---
                    if (pedido.estado == "Entregue") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                // Chama a função que está definida lá em baixo
                                enviarRelatorioPorEmail(context, pedido)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar Relatório por Email")
                        }
                    }
                    // -----------------------------------------------
                }
            }
        }
    }
}

// --- FUNÇÃO AUXILIAR (FORA DA CLASSE) ---
fun enviarRelatorioPorEmail(context: Context, pedido: Pedido) {
    val emailColaborador = FirebaseAuth.getInstance().currentUser?.email ?: ""

    val assunto = "Relatório de Entrega - Pedido #${pedido.id.take(4).uppercase()}"

    val itensTexto = pedido.itens.joinToString(separator = "\n") { "- ${it.nome}: ${it.quantidade} un" }

    val corpoEmail = """
        RELATÓRIO DE ENTREGA - LOJA SOCIAL SAS
        --------------------------------------
        ID Pedido: ${pedido.id}
        Data do Pedido: ${pedido.dataPedido?.toDate() ?: "N/A"}
        
        DADOS DO BENEFICIÁRIO
        Nome: ${pedido.nomeBeneficiario}
        Nº Mecanográfico: ${pedido.numBeneficiario}
        Email: ${pedido.email}
        
        DETALHES DA ENTREGA
        Estado Atual: ${pedido.estado}
        Entregue por: $emailColaborador
        
        LISTA DE BENS:
        $itensTexto
        
        --------------------------------------
        Processado pela App Loja Social SAS
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:") // Garante que abre apenas apps de email
        putExtra(Intent.EXTRA_EMAIL, arrayOf(emailColaborador)) // Envia para si mesmo
        putExtra(Intent.EXTRA_SUBJECT, assunto)
        putExtra(Intent.EXTRA_TEXT, corpoEmail)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Nenhuma app de email encontrada.", Toast.LENGTH_SHORT).show()
    }
}
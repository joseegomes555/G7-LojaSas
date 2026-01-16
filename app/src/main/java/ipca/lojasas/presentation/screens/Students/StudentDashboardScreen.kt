package ipca.lojasas.presentation.screens.Students

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    viewModel: StudentViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    // Observa a lista de pedidos do ViewModel
    val pedidos by viewModel.meusPedidos.collectAsState()

    // Lógica para encontrar o Pedido Ativo:
    // Qualquer pedido que NÃO esteja "Entregue" nem "Cancelado" (ou seja: Pendente, Reagendamento, Cancelamento Solicitado, Pronto)
    val pedidoAtivo = pedidos.firstOrNull {
        it.estado != "Entregue" && it.estado != "Cancelado"
    }

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
        bottomBar = { StudentBottomBar(navController) }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)
        ) {

            // --- CARTÃO DE BOAS VINDAS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IPCAGreen),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
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

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECÇÃO: PEDIDO ATUAL ---
            Text("O Teu Pedido Atual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))

            if (pedidoAtivo != null) {
                // Formatar a data de recolha (apenas Dia/Mês/Ano)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dataEntrega = pedidoAtivo.dataLevantamento?.toDate()?.let { sdf.format(it) } ?: "A aguardar agendamento"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Navega para os detalhes ao clicar no cartão
                            navController.navigate("student_order_detail/${pedidoAtivo.id}")
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp),
                    border = BorderStroke(1.dp, IPCAGreen.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        // Linha Superior: ID e Data
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pedido #${pedidoAtivo.id.take(4).uppercase()}", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            // Data de Recolha em destaque
                            Surface(
                                color = IPCAGreen.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, null, tint = IPCAGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(dataEntrega, fontWeight = FontWeight.Bold, color = IPCAGreen, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Linha Central: Estado Grande
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if(pedidoAtivo.estado == "Pendente") Icons.Default.Refresh else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if(pedidoAtivo.estado == "Reagendamento") Color(0xFFE65100) else IPCAGreen,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = pedidoAtivo.estado.uppercase(),
                                    color = if(pedidoAtivo.estado == "Reagendamento") Color(0xFFE65100) else IPCAGreen,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "Clica para ver detalhes ou gerir",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            } else {
                // --- SE NÃO HOUVER PEDIDOS ATIVOS ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ShoppingCart, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Sem pedidos ativos no momento.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate(Routes.STUDENT_ORDER) },
                            colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen)
                        ) {
                            Text("Fazer Novo Pedido")
                        }
                    }
                }
            }
        }
    }
}
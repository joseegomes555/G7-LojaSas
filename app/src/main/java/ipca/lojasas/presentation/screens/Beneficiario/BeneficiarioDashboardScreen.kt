package ipca.lojasas.presentation.screens.Beneficiario

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // <--- IMPORTANTE
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
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
import ipca.lojasas.Routes
import ipca.lojasas.di.AppModule
import ipca.lojasas.presentation.components.BeneficiarioBottomBar
import ipca.lojasas.presentation.viewmodel.BeneficiarioViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiarioDashboardScreen(
    navController: NavController,
    viewModel: BeneficiarioViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    val pedidos by viewModel.meusPedidos.collectAsState()
    val perfil by viewModel.perfilUser.collectAsState()

    val pedidoAtual = pedidos.firstOrNull { it.estado != "Entregue" && it.estado != "Cancelado" }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Área do Beneficiário", color = IPCAGreen, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = { BeneficiarioBottomBar(navController) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            // Card Bem-Vindo
            Card(
                colors = CardDefaults.cardColors(containerColor = IPCAGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(100.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(8.dp)) {
                        Icon(Icons.Default.Face, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        val nome = perfil?.get("nome") as? String ?: "..."
                        val ativo = perfil?.get("ativo") as? Boolean ?: true
                        Text("Bem-vindo(a)!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(if(ativo) "Conta Ativa" else "Conta Suspensa", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("O Teu Pedido Atual", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (pedidoAtual != null) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), modifier = Modifier.fillMaxWidth().clickable { navController.navigate("beneficiario_order_detail/${pedidoAtual.id}") }) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingCart, null, tint = IPCAGreen, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(pedidoAtual.estado.uppercase(), color = IPCAGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${pedidoAtual.itens.size} itens • Levantar: ${formatData(pedidoAtual.dataLevantamento)}", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingCart, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Sem pedidos ativos.", color = Color.Gray, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { navController.navigate(Routes.BENEFICIARIO_ORDER) }, colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen)) {
                            Text("Fazer Novo Pedido")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Últimos Pedidos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pedidos.take(5)) { pedido ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp), onClick = { navController.navigate("beneficiario_order_detail/${pedido.id}") }) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(formatData(pedido.dataPedido), fontWeight = FontWeight.Bold)
                                Text("${pedido.itens.size} itens • ${pedido.estado}", fontSize = 12.sp, color = Color.Gray)
                            }
                            IconButton(onClick = {
                                val added = viewModel.repetirPedido(pedido)
                                if (added) {
                                    Toast.makeText(context, "Adicionado ao carrinho!", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Routes.BENEFICIARIO_ORDER)
                                } else {
                                    Toast.makeText(context, "Stock insuficiente.", Toast.LENGTH_SHORT).show()
                                }
                            }) { Icon(Icons.Default.Refresh, contentDescription = "Repetir", tint = IPCAGreen) }
                        }
                    }
                }
            }
        }
    }
}

fun formatData(ts: com.google.firebase.Timestamp?): String {
    if (ts == null) return "--/--"
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(ts.toDate())
}
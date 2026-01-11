package ipca.lojasas.presentation.screens.Students

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import ipca.lojasas.presentation.components.StudentBottomBar
import ipca.lojasas.di.AppModule
import ipca.lojasas.domain.model.ItemPedido
import ipca.lojasas.presentation.viewmodel.StudentViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentOrderScreen(
    navController: NavController,
    viewModel: StudentViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    val produtos by viewModel.produtos.collectAsState()
    val stockMap by viewModel.stockMap.collectAsState()
    val carrinho = remember { mutableStateMapOf<String, Int>() }
    var urgenciaExpanded by remember { mutableStateOf(false) }
    var urgenciaSelecionada by remember { mutableStateOf("Sem Urgência") }
    val opcoesUrgencia = listOf("Sem Urgência", "Urgente (24h)", "Muito Urgente")
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Fazer Pedido", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen))
        },
        bottomBar = { StudentBottomBar(navController, Routes.STUDENT_ORDER) },
        floatingActionButton = {
            if (carrinho.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val listaItens = carrinho.map { ItemPedido(it.key, it.value) }
                        viewModel.submeterPedido(listaItens, urgenciaSelecionada) { success ->
                            if (success) {
                                Toast.makeText(context, "Enviado!", Toast.LENGTH_SHORT).show()
                                navController.navigate(Routes.STUDENT_DASHBOARD)
                            }
                        }
                    },
                    containerColor = IPCAGreen, contentColor = Color.White, icon = { Icon(Icons.Default.Check, null) }, text = { Text("Finalizar") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = urgenciaExpanded, onExpandedChange = { urgenciaExpanded = !urgenciaExpanded }, modifier = Modifier.fillMaxWidth().padding(bottom=16.dp)
            ) {
                OutlinedTextField(value = urgenciaSelecionada, onValueChange = {}, readOnly = true, label = { Text("Urgência") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = urgenciaExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = urgenciaExpanded, onDismissRequest = { urgenciaExpanded = false }) {
                    opcoesUrgencia.forEach { op -> DropdownMenuItem(text = { Text(op) }, onClick = { urgenciaSelecionada = op; urgenciaExpanded = false }) }
                }
            }
            Text("Produtos Disponíveis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(produtos) { p ->
                    val stock = stockMap[p.nome] ?: 0
                    val noCarrinho = carrinho[p.nome] ?: 0
                    val esgotado = stock == 0
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(p.nome, fontWeight = FontWeight.Bold)
                                if (esgotado) Text("ESGOTADO", color = IPCARed, fontSize = 12.sp) else Text("Stock: $stock", color = IPCAGreen, fontSize = 12.sp)
                            }
                            if (!esgotado) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { if (noCarrinho > 0) if (noCarrinho == 1) carrinho.remove(p.nome) else carrinho[p.nome] = noCarrinho - 1 }) { Text("-") }
                                    Text("$noCarrinho", fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { if (noCarrinho < minOf(3, stock)) carrinho[p.nome] = noCarrinho + 1 }) { Text("+") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
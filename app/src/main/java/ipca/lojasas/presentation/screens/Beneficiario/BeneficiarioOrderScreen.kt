package ipca.lojasas.presentation.screens.Beneficiario

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
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
import com.google.firebase.firestore.FirebaseFirestore
import ipca.lojasas.Routes
import ipca.lojasas.di.AppModule
import ipca.lojasas.domain.model.ItemPedido
import ipca.lojasas.presentation.components.BeneficiarioBottomBar
import ipca.lojasas.presentation.viewmodel.BeneficiarioViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiarioOrderScreen(
    navController: NavController,
    viewModel: BeneficiarioViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    val produtos by viewModel.produtos.collectAsState()
    val stockMap by viewModel.stockMap.collectAsState()

    // Accessing cart from ViewModel
    val carrinho = viewModel.carrinho

    var isContaAtiva by remember { mutableStateOf(true) }
    val diasDisponiveis = remember { gerarDiasDisponiveis() }
    var diaSelecionado by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection("utilizadores")
                .whereEqualTo("uid", user.uid).get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        isContaAtiva = docs.documents[0].getBoolean("ativo") ?: true
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fazer Pedido", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        },
        bottomBar = { BeneficiarioBottomBar(navController) },
        floatingActionButton = {
            if (carrinho.isNotEmpty() && isContaAtiva) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (diaSelecionado.isEmpty()) {
                            Toast.makeText(context, "Escolhe um dia para recolha.", Toast.LENGTH_LONG).show()
                        } else {
                            val listaItens = carrinho.map { ItemPedido(it.key, it.value) }
                            val dataFinal = converterDiaParaData(diaSelecionado)

                            viewModel.submeterPedido(listaItens, "Normal", dataFinal) { success ->
                                if (success) {
                                    Toast.makeText(context, "Pedido Registado!", Toast.LENGTH_LONG).show()
                                    navController.navigate(Routes.BENEFICIARIO_DASHBOARD)
                                } else {
                                    Toast.makeText(context, "Erro ao enviar.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    containerColor = IPCAGreen,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Check, null) },
                    text = { Text("Finalizar") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            if (!isContaAtiva) {
                Card(colors = CardDefaults.cardColors(containerColor = IPCARed), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Conta Suspensa. Contacta o SAS.", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (isContaAtiva) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, tint = IPCAGreen)
                            Spacer(Modifier.width(8.dp))
                            Text("Data de Recolha", fontWeight = FontWeight.Bold, color = IPCAGreen)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            diasDisponiveis.forEach { dia ->
                                val isSelected = (dia == diaSelecionado)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { diaSelecionado = dia },
                                    label = { Text(dia) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = IPCAGreen, selectedLabelColor = Color.White)
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                items(produtos) { p ->
                    val stock = stockMap[p.nome] ?: 0
                    val noCarrinho = carrinho[p.nome] ?: 0
                    val esgotado = stock == 0

                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.nome, fontWeight = FontWeight.Bold)
                                if (esgotado) Text("ESGOTADO", color = IPCARed, fontSize = 12.sp)
                                else Text("Stock: $stock", color = Color.Gray, fontSize = 12.sp)
                            }
                            if (!esgotado && isContaAtiva) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.removerDoCarrinho(p.nome) }) {
                                        Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = IPCAGreen)
                                    }
                                    Box(modifier = Modifier.background(IPCAGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                        Text("$noCarrinho", fontWeight = FontWeight.Bold, color = IPCAGreen)
                                    }
                                    IconButton(onClick = { viewModel.adicionarAoCarrinho(p.nome, stock) }) {
                                        Icon(Icons.Default.Add, null, tint = IPCAGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun gerarDiasDisponiveis(): List<String> {
    val dias = mutableListOf<String>()
    val sdf = SimpleDateFormat("dd/MM", Locale("pt", "PT"))
    val cal = Calendar.getInstance()
    for (i in 1..3) {
        cal.add(Calendar.DAY_OF_YEAR, 1)
        dias.add(sdf.format(cal.time))
    }
    return dias
}

fun converterDiaParaData(diaString: String): Date {
    try {
        val diaMes = diaString.split(" ")[0]
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.parse("$diaMes/$anoAtual") ?: Date()
    } catch (e: Exception) { return Date() }
}
package ipca.lojasas.presentation.screens.Students

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
import ipca.lojasas.presentation.components.StudentBottomBar
import ipca.lojasas.di.AppModule
import ipca.lojasas.domain.model.ItemPedido
import ipca.lojasas.presentation.viewmodel.StudentViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentOrderScreen(
    navController: NavController,
    viewModel: StudentViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    val produtos by viewModel.produtos.collectAsState()
    val stockMap by viewModel.stockMap.collectAsState()
    val carrinho = remember { mutableStateMapOf<String, Int>() }

    // --- ESTADO DA CONTA ---
    var isContaAtiva by remember { mutableStateOf(true) }

    // --- LÓGICA DE AGENDAMENTO (DIÁRIO) ---
    val diasDisponiveis = remember { gerarDiasDisponiveis() }
    var diaSelecionado by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Verificar se a conta está ativa ao entrar
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
        bottomBar = { StudentBottomBar(navController) }, // Garante que usas o menu certo
        floatingActionButton = {
            // SÓ MOSTRA O BOTÃO SE A CONTA ESTIVER ATIVA
            if (carrinho.isNotEmpty() && isContaAtiva) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (diaSelecionado.isEmpty()) {
                            Toast.makeText(context, "Por favor, escolhe um dia para recolha.", Toast.LENGTH_LONG).show()
                        } else {
                            val listaItens = carrinho.map { ItemPedido(it.key, it.value) }
                            val dataFinal = converterDiaParaData(diaSelecionado)

                            viewModel.submeterPedido(listaItens, "Normal", dataFinal) { success ->
                                if (success) {
                                    Toast.makeText(context, "Pedido Registado com Sucesso!", Toast.LENGTH_LONG).show()
                                    navController.navigate(Routes.STUDENT_DASHBOARD)
                                } else {
                                    Toast.makeText(context, "Erro ao enviar pedido.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    containerColor = IPCAGreen,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Check, null) },
                    text = { Text("Finalizar Pedido") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            // --- AVISO DE CONTA BLOQUEADA ---
            if (!isContaAtiva) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = IPCARed),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Conta Suspensa", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Contacta o SAS para regularizar a situação.", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- BLOCO DE ESCOLHA DA DATA ---
            if (isContaAtiva) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = IPCAGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Quando queres levantar?", fontWeight = FontWeight.Bold, color = IPCAGreen)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Lista Horizontal de Dias
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            diasDisponiveis.forEach { dia ->
                                val isSelected = (dia == diaSelecionado)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { diaSelecionado = dia },
                                    label = { Text(dia) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IPCAGreen,
                                        selectedLabelColor = Color.White,
                                        labelColor = Color.Black
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,        // <--- CORREÇÃO AQUI
                                        selected = isSelected, // <--- CORREÇÃO AQUI
                                        borderColor = Color.Gray,
                                        selectedBorderColor = IPCAGreen
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Text("Catálogo de Produtos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // --- LISTA DE PRODUTOS ---
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(produtos) { p ->
                    val stock = stockMap[p.nome] ?: 0
                    val noCarrinho = carrinho[p.nome] ?: 0
                    val esgotado = stock == 0

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (esgotado) {
                                    Text("ESGOTADO", color = IPCARed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Stock: $stock un.", color = Color.Gray, fontSize = 12.sp)
                                }
                            }

                            if (!esgotado && isContaAtiva) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (noCarrinho > 0) {
                                                if (noCarrinho == 1) carrinho.remove(p.nome)
                                                else carrinho[p.nome] = noCarrinho - 1
                                            }
                                        }
                                    ) {
                                        Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = IPCAGreen)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(IPCAGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("$noCarrinho", fontWeight = FontWeight.Bold, color = IPCAGreen)
                                    }

                                    IconButton(
                                        onClick = {
                                            if (noCarrinho < stock && noCarrinho < 5) {
                                                carrinho[p.nome] = noCarrinho + 1
                                            } else {
                                                Toast.makeText(context, "Limite atingido", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
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

// --- FUNÇÕES AUXILIARES ---

fun gerarDiasDisponiveis(): List<String> {
    val dias = mutableListOf<String>()
    val sdf = SimpleDateFormat("dd/MM (EEE)", Locale("pt", "PT"))
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
    } catch (e: Exception) {
        return Date()
    }
}
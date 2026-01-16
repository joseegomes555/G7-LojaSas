package ipca.lojasas.presentation.screens.Colaborador

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.* // <--- Importante para getValue e setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import ipca.lojasas.di.AppModule
import ipca.lojasas.domain.model.Lote
import ipca.lojasas.domain.model.Produto
import ipca.lojasas.presentation.components.ColaboradorBottomBar
import ipca.lojasas.presentation.viewmodel.ColaboradorViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
    navController: NavController,
    viewModel: ColaboradorViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    val lotes by viewModel.lotes.collectAsState()
    val produtosCatalogo by viewModel.produtosCatalogo.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var loteParaEliminar by remember { mutableStateOf<Lote?>(null) }
    var motivoEliminacao by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestão de Stock", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        },
        bottomBar = { ColaboradorBottomBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = IPCAGreen, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Lote")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (lotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sem lotes registados.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(lotes) { lote ->
                        val dias = calcularDias(lote.dataValidade)
                        val cor = if(dias <= 5) IPCARed else IPCAGreen
                        val texto = if (dias < 0) "Expirado" else "$dias dias"

                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(lote.nomeProduto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(lote.categoria, fontSize = 12.sp, color = Color.Gray)
                                    Text("Validade: $texto", fontSize = 12.sp, color = cor, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${lote.quantidade}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    IconButton(onClick = {
                                        loteParaEliminar = lote
                                        motivoEliminacao = ""
                                        showDeleteDialog = true
                                    }) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddLoteAvancadoDialog(
                produtosDisponiveis = produtosCatalogo,
                onDismiss = { showAddDialog = false },
                onConfirm = { nome, cat, qtd, data ->
                    viewModel.adicionarLote(nome, cat, qtd, data)
                    showAddDialog = false
                }
            )
        }

        if (showDeleteDialog && loteParaEliminar != null) {
            val safeLote = loteParaEliminar!!
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Eliminar Lote") },
                text = {
                    Column {
                        Text("É obrigatório indicar o motivo.")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = motivoEliminacao, onValueChange = { motivoEliminacao = it }, label = { Text("Motivo") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { if (motivoEliminacao.isNotBlank()) { viewModel.eliminarLote(safeLote.id, motivoEliminacao, safeLote); showDeleteDialog = false } },
                        colors = ButtonDefaults.buttonColors(containerColor = IPCARed),
                        enabled = motivoEliminacao.isNotBlank()
                    ) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoteAvancadoDialog(produtosDisponiveis: List<Produto>, onDismiss: () -> Unit, onConfirm: (String, String, Int, Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedProduto by remember { mutableStateOf<Produto?>(null) }
    var quantidade by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { selectedDateMillis = datePickerState.selectedDateMillis; showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Lote") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedProduto?.nome ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Produto") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        produtosDisponiveis.forEach { item -> DropdownMenuItem(text = { Text(item.nome) }, onClick = { selectedProduto = item; expanded = false }) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = quantidade, onValueChange = { quantidade = it }, label = { Text("Qtd") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if(selectedDateMillis != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDateMillis!!)) else "",
                        onValueChange = {},
                        label = { Text("Validade") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.DateRange, null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = Color.Gray, disabledTrailingIconColor = Color.Black)
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val qtdInt = quantidade.toIntOrNull() ?: 0
                if (selectedProduto != null && qtdInt > 0 && selectedDateMillis != null) {
                    onConfirm(selectedProduto!!.nome, selectedProduto!!.categoria, qtdInt, selectedDateMillis!!)
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen)) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

fun calcularDias(timestamp: Timestamp?): Long {
    if (timestamp == null) return 0
    val hoje = Date().time
    val validade = timestamp.toDate().time
    return TimeUnit.MILLISECONDS.toDays(validade - hoje)
}
package ipca.lojasas.presentation.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ipca.lojasas.Routes
import ipca.lojasas.presentation.components.AppBottomBar
import ipca.lojasas.di.AppModule
import ipca.lojasas.domain.model.Lote
import ipca.lojasas.domain.model.Produto
import ipca.lojasas.presentation.viewmodel.StaffViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
    navController: NavController,
    // CLEAN ARCH: Factory Injetada
    viewModel: StaffViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    // CLEAN ARCH: Ler os Flows
    val lotes by viewModel.lotesList.collectAsState()
    val produtosMestres by viewModel.produtosMestres.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var loteParaRemover by remember { mutableStateOf<Lote?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestão de Stock", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        },
        bottomBar = { AppBottomBar(navController, Routes.STAFF_STOCK) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = IPCAGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Lote")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            if (lotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sem stock registado.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(lotes) { lote ->
                        StockCard(
                            lote = lote,
                            onRemoveClick = { loteParaRemover = lote }
                        )
                    }
                }
            }
        }

        // --- DIÁLOGOS ---

        if (showAddDialog) {
            AddStockDialog(
                produtosDisponiveis = produtosMestres,
                onDismiss = { showAddDialog = false },
                onConfirm = { nome, qtd, data, cat, origem ->
                    // Usa a função helper do ViewModel
                    viewModel.criarLoteParaAdicionar(nome, qtd, data, cat, origem)
                    showAddDialog = false
                }
            )
        }

        if (loteParaRemover != null) {
            RemoveStockDialog(
                lote = loteParaRemover!!,
                onDismiss = { loteParaRemover = null },
                onConfirm = { qtd, motivo ->
                    viewModel.removerStock(loteParaRemover!!, qtd, motivo) { success ->
                        if (!success) { /* Opcional: Mostrar erro */ }
                    }
                    loteParaRemover = null
                }
            )
        }
    }
}

@Composable
fun StockCard(lote: Lote, onRemoveClick: () -> Unit) {
    val dias = lote.diasParaExpirar()
    val corValidade = if (dias <= 5) IPCARed else IPCAGreen

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(lote.nomeProduto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Categ: ${lote.categoria} | ${lote.origem}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (dias < 0) "EXPIRADO" else "Validade: $dias dias",
                    color = corValidade,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${lote.quantidade}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Unid.", fontSize = 10.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color.Gray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockDialog(
    produtosDisponiveis: List<Produto>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Date, String, String) -> Unit
) {
    var nomeSelecionado by remember { mutableStateOf("") }
    var quantidadeStr by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var origem by remember { mutableStateOf("") }
    var dataValidade by remember { mutableStateOf(Date()) }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            dataValidade = calendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("Entrada de Stock", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = IPCAGreen)
                Spacer(modifier = Modifier.height(16.dp))

                // DROPDOWN
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = nomeSelecionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selecionar Produto") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (produtosDisponiveis.isEmpty()) {
                            DropdownMenuItem(text = { Text("Nenhum produto configurado") }, onClick = { expanded = false })
                        } else {
                            produtosDisponiveis.forEach { produto ->
                                DropdownMenuItem(
                                    text = { Text(produto.nome) },
                                    onClick = {
                                        nomeSelecionado = produto.nome
                                        categoria = produto.categoria
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoria") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = origem, onValueChange = { origem = it }, label = { Text("Origem") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantidadeStr,
                    onValueChange = { if(it.all { c -> c.isDigit() }) quantidadeStr = it },
                    label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dataValidade),
                    onValueChange = {},
                    label = { Text("Validade") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) { Icon(Icons.Default.DateRange, null) }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = {
                            val qtd = quantidadeStr.toIntOrNull() ?: 0
                            if (nomeSelecionado.isNotEmpty() && qtd > 0) {
                                onConfirm(nomeSelecionado, qtd, dataValidade, categoria, origem)
                            } else {
                                Toast.makeText(context, "Preencha os dados", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen)
                    ) { Text("Confirmar") }
                }
            }
        }
    }
}

@Composable
fun RemoveStockDialog(lote: Lote, onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var quantidadeStr by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("Saída de Stock", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = IPCARed)
                Text(lote.nomeProduto, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = quantidadeStr,
                    onValueChange = { if(it.all { c -> c.isDigit() }) quantidadeStr = it },
                    label = { Text("Quantidade a remover") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = motivo, onValueChange = { motivo = it }, label = { Text("Motivo") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = {
                            val qtd = quantidadeStr.toIntOrNull() ?: 0
                            if (qtd > 0 && qtd <= lote.quantidade) onConfirm(qtd, motivo)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IPCARed)
                    ) { Text("Remover") }
                }
            }
        }
    }
}
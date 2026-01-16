package ipca.lojasas.presentation.screens.Beneficiario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import ipca.lojasas.presentation.components.ColaboradorBottomBar
import ipca.lojasas.ui.theme.IPCAGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiariosScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    var listaBeneficiarios by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // Carrega dados da BD
    LaunchedEffect(Unit) {
        db.collection("utilizadores")
            .whereEqualTo("tipo", "Beneficiario")
            .addSnapshotListener { docs, _ ->
                if (docs != null) {
                    listaBeneficiarios = docs.documents.map { doc ->
                        val data = doc.data ?: mutableMapOf()
                        val mapa = data.toMutableMap()
                        mapa["id"] = doc.id
                        mapa
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestão de Beneficiários", color = Color.White) },
                // Seta de voltar removida intencionalmente
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        },
        bottomBar = { ColaboradorBottomBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = IPCAGreen, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (listaBeneficiarios.isEmpty()) {
                // Mensagem quando não há ninguém
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("A lista está vazia.\nClica no botão + para adicionar.", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(listaBeneficiarios) { user ->
                        val nome = user["nome"] as? String ?: "Sem Nome"
                        val email = user["email"] as? String ?: ""
                        val tipo = user["tipo_vinculo"] as? String ?: "Aluno"

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            onClick = {
                                val userId = user["id"] as? String ?: ""
                                if (userId.isNotEmpty()) navController.navigate("beneficiario_detail/$userId")
                            },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = IPCAGreen)
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(nome, fontWeight = FontWeight.Bold)
                                    Text(email, fontSize = 12.sp, color = Color.Gray)
                                    Text(tipo, fontSize = 12.sp, color = IPCAGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddComunidadeDialog(
                onDismiss = { showDialog = false },
                onConfirm = { nome, email, tipo ->
                    val novoUser = hashMapOf(
                        "nome" to nome,
                        "email" to email,
                        "tipo_vinculo" to tipo,
                        "tipo" to "Beneficiario",
                        "ativo" to true,
                        "dataCriacao" to com.google.firebase.Timestamp.now()
                    )
                    db.collection("utilizadores").add(novoUser)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun AddComunidadeDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Docente") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registar Novo") },
        text = {
            Column {
                OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                Spacer(Modifier.height(8.dp))
                Row {
                    RadioButton(selected = tipo == "Docente", onClick = { tipo = "Docente" })
                    Text("Docente", modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(Modifier.width(8.dp))
                    RadioButton(selected = tipo == "Funcionário", onClick = { tipo = "Funcionário" })
                    Text("Funcionário", modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        },
        confirmButton = { Button(onClick = { if(nome.isNotEmpty()) onConfirm(nome, email, tipo) }, colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen)) { Text("Adicionar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
package ipca.lojasas.presentation.screens.Staff

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import ipca.lojasas.presentation.components.BottomBar
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffCandidaturaScreen(navController: NavController) {
    var candidaturas by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Carregar Candidaturas Pendentes
    LaunchedEffect(Unit) {
        db.collection("candidaturas")
            .whereEqualTo("estado", "Pendente")
            .addSnapshotListener { docs, _ ->
                if (docs != null) {
                    candidaturas = docs.documents.map {
                        val data = it.data ?: mutableMapOf()
                        val mapa = data.toMutableMap()
                        mapa["id"] = it.id
                        mapa
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novas Candidaturas", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        },
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (candidaturas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Não há candidaturas pendentes.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(candidaturas) { cand ->
                        val id = cand["id"] as String
                        val nome = cand["nome"] as? String ?: "Sem Nome"
                        val email = cand["email"] as? String ?: ""
                        val tipo = cand["tipoCandidato"] as? String ?: "Aluno"

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(tipo, color = IPCAGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(email, color = Color.Gray, fontSize = 14.sp)

                                Spacer(modifier = Modifier.height(8.dp))

                                // Detalhes extra (se existirem)
                                val numMec = cand["numEstudante"] as? String
                                if (numMec != null) Text("Nº: $numMec", fontSize = 12.sp)

                                val isBolseiro = cand["isBolseiro"] as? Boolean == true
                                if (isBolseiro) Text("⚠️ Bolseiro", color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                Spacer(modifier = Modifier.height(16.dp))

                                // Botões de Ação
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            // Lógica de Rejeitar
                                            db.collection("candidaturas").document(id).update("estado", "Rejeitado")
                                            Toast.makeText(context, "Candidatura Rejeitada", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IPCARed),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Close, null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Rejeitar")
                                    }

                                    Button(
                                        onClick = {
                                            // Lógica de Aprovar: Cria user na coleção final
                                            val novoBeneficiario = hashMapOf(
                                                "nome" to nome,
                                                "email" to email,
                                                "tipo" to "Beneficiario",
                                                "tipo_vinculo" to tipo,
                                                "numEstudante" to (cand["numEstudante"] ?: ""),
                                                "telemovel" to (cand["telemovel"] ?: ""),
                                                "curso" to (cand["curso"] ?: ""),
                                                "ativo" to true,
                                                "dataCriacao" to Timestamp.now()
                                            )

                                            // 1. Cria utilizador
                                            db.collection("utilizadores").add(novoBeneficiario)
                                                .addOnSuccessListener {
                                                    // 2. Marca candidatura como Aprovada
                                                    db.collection("candidaturas").document(id).update("estado", "Aprovado")
                                                    Toast.makeText(context, "Aprovado! Adicionado aos Beneficiários.", Toast.LENGTH_SHORT).show()
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Aprovar")
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
package ipca.lojasas.presentation.screens.Colaborador

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
import ipca.lojasas.presentation.components.ColaboradorBottomBar
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColaboradorCandidaturasScreen(navController: NavController) {
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
                title = { Text("Candidaturas", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        },
        bottomBar = { ColaboradorBottomBar(navController) }
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

                                val numMec = cand["numEstudante"] as? String
                                if (numMec != null) Text("Nº: $numMec", fontSize = 12.sp)

                                val isBolseiro = cand["isBolseiro"] as? Boolean == true
                                if (isBolseiro) Text("⚠️ Bolseiro", color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            db.collection("candidaturas").document(id).update("estado", "Rejeitado")
                                            Toast.makeText(context, "Rejeitada", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IPCARed),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Close, null)
                                        Text("Rejeitar")
                                    }

                                    Button(
                                        onClick = {
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

                                            db.collection("utilizadores").add(novoBeneficiario)
                                                .addOnSuccessListener {
                                                    db.collection("candidaturas").document(id).update("estado", "Aprovado")
                                                    Toast.makeText(context, "Aprovado!", Toast.LENGTH_SHORT).show()
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, null)
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
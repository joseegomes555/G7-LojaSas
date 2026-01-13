package ipca.lojasas.presentation.screens.Beneficiarios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import ipca.lojasas.Routes
import ipca.lojasas.presentation.components.AppBottomBar
import ipca.lojasas.ui.theme.IPCAGreen

// Modelo atualizado com mais dados
data class Beneficiario(
    var id: String = "", // ID do documento no Firestore
    val email: String = "",
    val nome: String = "Benefeciário IPCA",
    val nif: String = "--",
    val dataNascimento: String = "--",
    val tipo: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiariosScreen(navController: NavController) {
    var lista by remember { mutableStateOf(listOf<Beneficiario>()) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        db.collection("utilizadores")
            .whereEqualTo("tipo", "aluno")
            .get()
            .addOnSuccessListener { result ->
                lista = result.map { doc ->
                    doc.toObject(Beneficiario::class.java).apply { id = doc.id }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beneficiários", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        },
        bottomBar = { AppBottomBar(navController, Routes.STAFF_BENEFICIARIOS) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Total: ${lista.size} beneficiários ativos", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            items(lista) { aluno ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Ação de Clique: Navega para o detalhe enviando o ID do aluno
                        .clickable {
                            navController.navigate("staff_beneficiario_detail/${aluno.id}")
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp).background(Color(0xFFE0E0E0), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(aluno.nome, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(aluno.email, color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Ver detalhe", tint = Color.Gray)
                    }
                }
            }
        }
    }
}
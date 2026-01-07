package ipca.lojasas.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import ipca.lojasas.Routes
import ipca.lojasas.components.AppBottomBar
import ipca.lojasas.model.Lote
import ipca.lojasas.ui.theme.IPCAGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(navController: NavController) {
    var lotes by remember { mutableStateOf(listOf<Lote>()) }
    val db = FirebaseFirestore.getInstance()

    // Ler dados da BD
    LaunchedEffect(Unit) {
        db.collection("lotes").get().addOnSuccessListener { result ->
            lotes = result.map { doc -> doc.toObject(Lote::class.java).apply { id = doc.id } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestão de Stock", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        },
        bottomBar = { AppBottomBar(navController, Routes.STAFF_STOCK) },
        floatingActionButton = {
            FloatingActionButton(onClick = {}, containerColor = IPCAGreen, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(lotes) { lote ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = lote.nomeProduto, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Quantidade: ${lote.quantidade}", color = Color.Gray)

                        val dias = lote.diasParaExpirar()
                        val cor = if(dias < 5) Color.Red else IPCAGreen
                        Text(text = "Validade: $dias dias restantes", color = cor)
                    }
                }
            }
        }
    }
}
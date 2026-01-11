package ipca.lojasas.screens.Staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import ipca.lojasas.di.AppModule
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.presentation.viewmodel.StaffViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffOrderDetailScreen(
    navController: NavController,
    pedidoId: String,
    viewModel: StaffViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    var pedido by remember { mutableStateOf<Pedido?>(null) }
    LaunchedEffect(pedidoId) {
        FirebaseFirestore.getInstance().collection("pedidos").document(pedidoId).get()
            .addOnSuccessListener { doc -> if (doc.exists()) pedido = doc.toObject(Pedido::class.java)?.apply { id = doc.id } }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Detalhes", color = Color.White) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen))
        }
    ) { padding ->
        if (pedido == null) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(pedido!!.nomeAluno, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Urgência: ${pedido!!.urgencia}", fontWeight = FontWeight.Bold, color = if (pedido!!.urgencia.contains("Urgente")) IPCARed else IPCAGreen)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Itens", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyColumn {
                    items(pedido!!.itens) { item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical=4.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.nome)
                            Text("${item.quantidade} un.", fontWeight = FontWeight.Bold, color = IPCAGreen)
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                if (pedido!!.estado == "Pendente") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.cancelarPedido(pedido!!); navController.popBackStack() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = IPCARed)) { Text("Recusar") }
                        Button(onClick = { viewModel.processarEntrega(pedidoId); navController.popBackStack() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen)) { Text("Entregar") }
                    }
                }
            }
        }
    }
}
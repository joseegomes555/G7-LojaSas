package ipca.lojasas.presentation.screens.Students

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ipca.lojasas.Routes
import ipca.lojasas.presentation.components.StudentBottomBar
import ipca.lojasas.di.AppModule
import ipca.lojasas.presentation.viewmodel.StudentViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    navController: NavController,
    // CLEAN ARCH: Factory
    viewModel: StudentViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    val pedidos by viewModel.meusPedidos.collectAsState()

    // Estado local para o perfil
    var nomeAluno by remember { mutableStateOf("Carregando...") }
    var emailAluno by remember { mutableStateOf("") }
    var nifAluno by remember { mutableStateOf("--") }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            emailAluno = user.email ?: ""
            FirebaseFirestore.getInstance().collection("utilizadores").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        nomeAluno = doc.getString("nome") ?: "Estudante"
                        nifAluno = doc.getString("nif") ?: "--"
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen),
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        navController.navigate(Routes.CHOICE) { popUpTo(0) }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sair", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = { StudentBottomBar(navController, Routes.STUDENT_PROFILE) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(80.dp).background(IPCAGreen.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = IPCAGreen, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(nomeAluno, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(emailAluno, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))
            Surface(color = IPCAGreen.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
                Text("NIF: $nifAluno", modifier = Modifier.padding(8.dp), color = IPCAGreen, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Text("Histórico de Pedidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pedidos) { pedido ->
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val data = pedido.dataPedido?.toDate()?.let { sdf.format(it) } ?: "-"

                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Pedido #${pedido.id.take(4)}", fontWeight = FontWeight.Bold)
                                Text(data, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            val corStatus = if(pedido.estado == "Entregue") IPCAGreen else Color(0xFFE65100)
                            Text(pedido.estado, color = corStatus, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
package ipca.lojasas.presentation.screens.Colaborador

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import ipca.lojasas.di.AppModule
import ipca.lojasas.presentation.components.ColaboradorBottomBar
import ipca.lojasas.presentation.viewmodel.ColaboradorViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed
import ipca.lojasas.worker.StockNotificationWorker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColaboradorDashboardScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: ColaboradorViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    val todosPedidos by viewModel.todosPedidos.collectAsState()
    val pedidosPendentes = todosPedidos.filter { it.estado == "Pendente" || it.estado == "Reagendamento" }

    val context = LocalContext.current

    // --- LÓGICA DE NOTIFICAÇÕES E POPUP ---
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(Unit) {
        // 1. Verificar Permissões (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val temPermissao = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!temPermissao) {
                showPermissionDialog = true
            }
        }

        // --- AGENDAMENTO PARA AS 10:00 DA MANHÃ ---
        val currentTime = Calendar.getInstance()
        val dueTime = Calendar.getInstance()

        // Definir hora alvo: 10:00:00
        dueTime.set(Calendar.HOUR_OF_DAY, 10)
        dueTime.set(Calendar.MINUTE, 0)
        dueTime.set(Calendar.SECOND, 0)

        // Se já passaram as 10h de hoje, agendar para amanhã
        if (dueTime.before(currentTime)) {
            dueTime.add(Calendar.HOUR_OF_DAY, 24)
        }

        val timeDiff = dueTime.timeInMillis - currentTime.timeInMillis
        // ------------------------------------------

        val workRequest = PeriodicWorkRequestBuilder<StockNotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag("stock_check")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "VerificacaoDiariaValidade",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Painel Colaborador", color = Color.White) },
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Sair", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        },
        bottomBar = { ColaboradorBottomBar(navController) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            // Cartões de Estatísticas
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Pendentes
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), modifier = Modifier.weight(1f).height(100.dp)) {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${pedidosPendentes.size}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                        Text("Pendentes", fontSize = 14.sp, color = Color(0xFF1565C0))
                    }
                }

                // Histórico (Entregues) - CLICÁVEL
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.weight(1f).height(100.dp).clickable { navController.navigate("colaborador_history") }
                ) {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        val entregues = todosPedidos.count { it.estado == "Entregue" }
                        Text("$entregues", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = IPCAGreen)
                        Text("Histórico", fontSize = 14.sp, color = IPCAGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Pedidos Pendentes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Lista de Pedidos
            if (pedidosPendentes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Tudo limpo! Sem pedidos pendentes.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(pedidosPendentes) { pedido ->
                        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                        val dataStr = pedido.dataPedido?.toDate()?.let { sdf.format(it) } ?: "Data desc."

                        Card(
                            onClick = { navController.navigate("colaborador_order_detail/${pedido.id}") },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(pedido.nomeBeneficiario, fontWeight = FontWeight.Bold)
                                    Text(dataStr, fontSize = 12.sp, color = Color.Gray)
                                    Text("${pedido.itens.size} itens", fontSize = 12.sp, color = Color.Gray)
                                }
                                if (pedido.urgencia == "Urgente") {
                                    Surface(color = IPCARed, shape = MaterialTheme.shapes.small) {
                                        Text("URGENTE", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                } else {
                                    Text(pedido.estado, fontSize = 12.sp, color = IPCAGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- POPUP EXPLICATIVO ---
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("⚠️ Alertas de Validade") },
                text = {
                    Text("Para avisá-lo de que o stock está a acabar ou a expirar, precisamos de ativar as notificações no seu telemóvel.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionDialog = false
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen)
                    ) {
                        Text("Ativar Notificações")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Agora não")
                    }
                },
                containerColor = Color.White
            )
        }
    }
}
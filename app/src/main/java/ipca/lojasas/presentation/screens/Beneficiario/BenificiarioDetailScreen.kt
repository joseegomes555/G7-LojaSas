package ipca.lojasas.presentation.screens.Beneficiario

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiarioDetailScreen(navController: NavController, userId: String) {
    var user by remember { mutableStateOf<Map<String, Any>?>(null) }
    var pedidos by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        db.collection("utilizadores").document(userId).addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                user = doc.data
                val uid = doc.getString("uid")
                val email = doc.getString("email")

                if (email != null) {
                    db.collection("pedidos").whereEqualTo("email", email)
                        .orderBy("dataPedido", Query.Direction.DESCENDING)
                        .addSnapshotListener { snaps, _ ->
                            if (snaps != null) pedidos = snaps.toObjects(Pedido::class.java)
                            loading = false
                        }
                } else if (uid != null) {
                    db.collection("pedidos").whereEqualTo("uid", uid)
                        .orderBy("dataPedido", Query.Direction.DESCENDING)
                        .addSnapshotListener { snaps, _ ->
                            if (snaps != null) pedidos = snaps.toObjects(Pedido::class.java)
                            loading = false
                        }
                } else { loading = false }
            } else { loading = false }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes Beneficiário", color = Color.White) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        }
    ) { padding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = IPCAGreen) }
        } else if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Erro ao carregar.") }
        } else {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {

                // CABEÇALHO
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {

                    val fotoBase64 = user!!["fotoPerfil"] as? String

                    // Processa a imagem fora da UI tree
                    val imageBitmap = remember(fotoBase64) {
                        try {
                            if (fotoBase64 != null) {
                                val decodedBytes = Base64.decode(fotoBase64, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
                            } else null
                        } catch (e: Exception) { null }
                    }

                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Foto",
                            modifier = Modifier.size(60.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.size(60.dp).background(Color.LightGray, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user!!["nome"] as? String ?: "Sem Nome", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(user!!["tipo_vinculo"] as? String ?: "Aluno", color = Color.Gray)
                    }

                    val isAtivo = user!!["ativo"] as? Boolean ?: true
                    Column(horizontalAlignment = Alignment.End) {
                        Switch(
                            checked = isAtivo,
                            onCheckedChange = { novo ->
                                db.collection("utilizadores").document(userId).update("ativo", novo)
                                Toast.makeText(context, if(novo) "Ativado" else "Suspenso", Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = IPCAGreen, checkedTrackColor = IPCAGreen.copy(alpha = 0.5f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Dados de Contacto
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        val email = user!!["email"] as? String ?: "-"
                        val telemovel = user!!["telemovel"] as? String ?: "-"

                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                            context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:$email") })
                        }, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(email, fontSize = 14.sp)
                        }

                        if (telemovel.isNotEmpty() && telemovel != "-") {
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(telemovel, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Histórico Recente", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(pedidos) { p ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val d = p.dataPedido?.toDate()?.let { sdf.format(it) } ?: "-"
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(d, fontWeight = FontWeight.Bold)
                                Text(p.estado, color = if(p.estado=="Entregue") IPCAGreen else Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
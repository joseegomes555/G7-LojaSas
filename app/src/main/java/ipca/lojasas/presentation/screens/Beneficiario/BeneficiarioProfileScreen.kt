package ipca.lojasas.presentation.screens.Beneficiario

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ipca.lojasas.Routes
import ipca.lojasas.di.AppModule
import ipca.lojasas.presentation.components.BeneficiarioBottomBar
import ipca.lojasas.presentation.viewmodel.BeneficiarioViewModel
import ipca.lojasas.ui.theme.IPCAGreen
import ipca.lojasas.ui.theme.IPCARed
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiarioProfileScreen(
    navController: NavController,
    viewModel: BeneficiarioViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    // 1. DADOS
    val perfil by viewModel.perfilUser.collectAsState(initial = null)
    val context = LocalContext.current

    // 2. LÓGICA DE UPLOAD DE FOTO
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                Toast.makeText(context, "A guardar foto...", Toast.LENGTH_SHORT).show()
                viewModel.uploadFoto(bitmap) { success ->
                    if(success) Toast.makeText(context, "Foto atualizada!", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(context, "Erro ao guardar foto.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // 3. NOVA LÓGICA
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH) // 0=Jan, 11=Dez
    val currentYear = calendar.get(Calendar.YEAR)

    // Calcular
    val anoLetivo = if (currentMonth >= Calendar.SEPTEMBER) {
        "$currentYear/${(currentYear + 1).toString().takeLast(2)}"
    } else {
        "${currentYear - 1}/${currentYear.toString().takeLast(2)}"
    }

    val isContaValida = currentMonth != Calendar.JULY && currentMonth != Calendar.AUGUST

    val cardColor = if (isContaValida) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val iconColor = if (isContaValida) IPCAGreen else IPCARed
    val statusIcon = if (isContaValida) Icons.Default.CheckCircle else Icons.Default.Error
    val statusTexto = if (isContaValida) "CONTA VÁLIDA" else "FORA DO PERÍODO"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("O Meu Perfil", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen),
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        navController.navigate(Routes.CHOICE) { popUpTo(0) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.White)
                    }
                }
            )
        },
        bottomBar = { BeneficiarioBottomBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- FOTO DE PERFIL ---
            Box(contentAlignment = Alignment.BottomEnd) {
                if (perfil != null && perfil!!["fotoPerfil"] != null) {
                    val base64Str = perfil!!["fotoPerfil"] as String
                    val imageBitmap = remember(base64Str) {
                        try {
                            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size).asImageBitmap()
                        } catch (e: Exception) { null }
                    }

                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap, contentDescription = null,
                            modifier = Modifier.size(120.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else { AvatarPlaceholder() }
                } else { AvatarPlaceholder() }

                Box(
                    modifier = Modifier.size(36.dp).background(IPCAGreen, CircleShape).clickable { launcher.launch("image/*") }.padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (perfil == null) {
                CircularProgressIndicator(color = IPCAGreen)
            } else {
                val dados = perfil!!
                val nome = dados["nome"] as? String ?: "Sem Nome"
                val email = dados["email"] as? String ?: ""
                val numAluno = (dados["numBeneficiario"] ?: dados["numEstudante"]) as? String ?: "N/A"
                val curso = dados["curso"] as? String ?: "N/A"

                Text(nome, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(email, color = Color.Gray)

                Spacer(modifier = Modifier.height(32.dp))

                // --- CARTÃO DE STATUS (LÓGICA DO ANO LETIVO) ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = statusIcon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = statusTexto, fontWeight = FontWeight.Bold, color = iconColor)
                            Text("Ano Letivo: $anoLetivo", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- DETALHES ---
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        ProfileRow("Nº Estudante", numAluno)
                        Divider(Modifier.padding(vertical = 8.dp))
                        // Voltei a colocar o Curso em vez da Candidatura
                        ProfileRow("Curso", curso)
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarPlaceholder() {
    Box(modifier = Modifier.size(120.dp).background(Color.LightGray, CircleShape), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(60.dp))
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiarioProfileScreen(
    navController: NavController,
    viewModel: BeneficiarioViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    val perfil by viewModel.perfilUser.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                Toast.makeText(context, "A guardar foto...", Toast.LENGTH_SHORT).show()
                // FIX: Renamed from uploadProfilePhoto to uploadFoto to match ViewModel
                viewModel.uploadFoto(bitmap) { success ->
                    if(success) Toast.makeText(context, "Foto atualizada!", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(context, "Erro ao guardar foto.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
                        Icon(Icons.Default.ExitToApp, null, tint = Color.White)
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
            Spacer(modifier = Modifier.height(32.dp))

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
                            bitmap = imageBitmap,
                            contentDescription = "Foto Perfil",
                            modifier = Modifier.size(120.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AvatarPlaceholder()
                    }
                } else {
                    AvatarPlaceholder()
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(IPCAGreen, CircleShape)
                        .clickable { launcher.launch("image/*") }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (perfil == null) {
                CircularProgressIndicator(color = IPCAGreen)
                Text("A carregar dados...", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
            } else {
                val nome = perfil!!["nome"] as? String ?: "Sem Nome"
                val email = perfil!!["email"] as? String ?: ""
                val numAluno = perfil!!["numEstudante"] as? String ?: ""
                val curso = perfil!!["curso"] as? String ?: ""
                val ativo = perfil!!["ativo"] as? Boolean ?: true

                Text(nome, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(email, color = Color.Gray)

                Spacer(modifier = Modifier.height(32.dp))

                Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        ProfileRow("Nº Estudante", numAluno)
                        Divider(Modifier.padding(vertical = 8.dp))
                        ProfileRow("Curso", curso)
                        Divider(Modifier.padding(vertical = 8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Estado da Conta", color = Color.Gray)
                            Surface(color = if (ativo) IPCAGreen else IPCARed, shape = RoundedCornerShape(4.dp)) {
                                Text(if (ativo) "ATIVO" else "SUSPENSO", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
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
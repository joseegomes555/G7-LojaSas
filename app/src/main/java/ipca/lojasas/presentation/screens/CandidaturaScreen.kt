package ipca.lojasas.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ipca.lojasas.di.AppModule
import ipca.lojasas.presentation.viewmodel.AuthViewModel
import ipca.lojasas.ui.theme.IPCAGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidaturaScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(factory = AppModule.viewModelFactory)
) {
    // Campos do formulário
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telemovel by remember { mutableStateOf("") }
    var cc by remember { mutableStateOf("") }     // Cartão Cidadão
    var nif by remember { mutableStateOf("") }    // NIF
    var morada by remember { mutableStateOf("") } // Morada
    var curso by remember { mutableStateOf("") }
    var isBolseiro by remember { mutableStateOf(false) }
    val tipo by remember { mutableStateOf("Aluno") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidatura SAS", color = Color.White) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Preenche os teus dados", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = IPCAGreen)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome Completo") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Institucional") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = telemovel, onValueChange = { telemovel = it }, label = { Text("Telemóvel") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = cc, onValueChange = { cc = it }, label = { Text("Cartão de Cidadão") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = nif, onValueChange = { nif = it }, label = { Text("NIF") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = morada, onValueChange = { morada = it }, label = { Text("Morada") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = curso, onValueChange = { curso = it }, label = { Text("Curso") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isBolseiro, onCheckedChange = { isBolseiro = it }, colors = CheckboxDefaults.colors(checkedColor = IPCAGreen))
                Text("Sou Bolseiro SAS")
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (nome.isNotEmpty() && email.isNotEmpty() && cc.isNotEmpty() && nif.isNotEmpty()) {
                        viewModel.submeterCandidatura(nome, email, telemovel, cc, nif, morada, curso, tipo, isBolseiro) { success, msg ->
                            if (success) {
                                Toast.makeText(context, "Candidatura enviada! Aguarde aprovação.", Toast.LENGTH_LONG).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Erro: $msg", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Preenche os campos obrigatórios", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen)
            ) {
                Text("Enviar Candidatura")
            }
        }
    }
}
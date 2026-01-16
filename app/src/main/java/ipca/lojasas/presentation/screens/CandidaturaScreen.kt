package ipca.lojasas.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import ipca.lojasas.ui.theme.IPCAGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidaturaScreen(navController: NavController) {
    // --- DADOS PESSOAIS [PDF: Identificação] ---
    var nome by remember { mutableStateOf("") }
    var dataNascimento by remember { mutableStateOf("") }
    var cc by remember { mutableStateOf("") } // Cartão Cidadão/Passaporte
    var telemovel by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // --- DADOS ACADÉMICOS [PDF: Dados académicos] ---
    var numEstudante by remember { mutableStateOf("") }
    var curso by remember { mutableStateOf("") }
    var grau by remember { mutableStateOf("Licenciatura") } // Licenciatura, Mestrado, CTeSP

    // --- CONDIÇÃO SOCIOECONÓMICA [PDF: Apoios] ---
    var isFaes by remember { mutableStateOf(false) } // Apoio FAES?
    var isBolseiro by remember { mutableStateOf(false) } // Tem bolsa?
    var valorBolsa by remember { mutableStateOf("") }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Requerimento Loja Social", color = Color.White, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IPCAGreen)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Este formulário destina-se apenas a alunos.", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))

            // 1. Identificação
            Text("1. Identificação", fontWeight = FontWeight.Bold, color = IPCAGreen)
            OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome Completo") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = dataNascimento, onValueChange = { dataNascimento = it }, label = { Text("Data Nasc.") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = cc, onValueChange = { cc = it }, label = { Text("CC / Passaporte") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Institucional") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = telemovel, onValueChange = { telemovel = it }, label = { Text("Telemóvel") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(24.dp))

            // 2. Dados Académicos
            Text("2. Dados Académicos", fontWeight = FontWeight.Bold, color = IPCAGreen)
            OutlinedTextField(value = numEstudante, onValueChange = { numEstudante = it }, label = { Text("Nº Estudante") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = curso, onValueChange = { curso = it }, label = { Text("Curso") }, modifier = Modifier.fillMaxWidth())

            // Seletor de Grau (Simplificado)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Grau:", modifier = Modifier.padding(end = 8.dp))
                listOf("CTeSP", "Licenciatura", "Mestrado").forEach { tipo ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (grau == tipo), onClick = { grau = tipo }, colors = RadioButtonDefaults.colors(selectedColor = IPCAGreen))
                        Text(tipo, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 3. Apoios Sociais
            Text("3. Apoios", fontWeight = FontWeight.Bold, color = IPCAGreen)

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = isFaes, onCheckedChange = { isFaes = it }, colors = CheckboxDefaults.colors(checkedColor = IPCAGreen))
                Text("Apoiado pelo FAES (Emergência Social)?")
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = isBolseiro, onCheckedChange = { isBolseiro = it }, colors = CheckboxDefaults.colors(checkedColor = IPCAGreen))
                Text("Beneficiário de Bolsa de Estudo?")
            }

            if (isBolseiro) {
                OutlinedTextField(value = valorBolsa, onValueChange = { valorBolsa = it }, label = { Text("Entidade e Valor") }, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (nome.isEmpty() || numEstudante.isEmpty() || email.isEmpty()) {
                        Toast.makeText(context, "Preencha os dados obrigatórios", Toast.LENGTH_SHORT).show()
                    } else {
                        val candidatura = hashMapOf(
                            "nome" to nome,
                            "dataNascimento" to dataNascimento,
                            "cc" to cc,
                            "email" to email,
                            "telemovel" to telemovel,
                            "numEstudante" to numEstudante,
                            "curso" to curso,
                            "grau" to grau,
                            "isFaes" to isFaes,
                            "isBolseiro" to isBolseiro,
                            "valorBolsa" to valorBolsa,
                            "tipoCandidato" to "Aluno", // Fixo, pois só alunos usam a app
                            "estado" to "Pendente",
                            "dataSubmissao" to com.google.firebase.Timestamp.now()
                        )

                        db.collection("candidaturas").add(candidatura)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Requerimento enviado! Aguarde contacto.", Toast.LENGTH_LONG).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener { Toast.makeText(context, "Erro ao enviar.", Toast.LENGTH_SHORT).show() }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IPCAGreen)
            ) {
                Text("Submeter Candidatura")
            }
            Spacer(Modifier.height(50.dp))
        }
    }
}
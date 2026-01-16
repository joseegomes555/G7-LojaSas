package ipca.lojasas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // CANDIDATURA
    fun submeterCandidatura(
        nome: String, email: String, telemovel: String,
        cc: String, nif: String, morada: String,
        curso: String, tipo: String, isBolseiro: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        val candidatura = hashMapOf(
            "nome" to nome,
            "email" to email,
            "telemovel" to telemovel,
            "cartaoCidadao" to cc,
            "nif" to nif,
            "morada" to morada,
            "curso" to curso,
            "tipoCandidato" to tipo,
            "isBolseiro" to isBolseiro,
            "estado" to "Pendente",
            "dataCandidatura" to Timestamp.now()
        )

        db.collection("candidaturas").add(candidatura)
            .addOnSuccessListener { onResult(true, "") }
            .addOnFailureListener { onResult(false, it.message ?: "Erro desconhecido") }
    }

    // CRIAR CONTA / DEFINIR PASSWORD
    fun registarPassword(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        db.collection("utilizadores").whereEqualTo("email", email).get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    onResult(false, "Este email não tem candidatura aprovada.")
                } else {
                    val userDocId = docs.documents[0].id

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { authResult ->
                            val uid = authResult.user?.uid
                            db.collection("utilizadores").document(userDocId).update("uid", uid)
                                .addOnSuccessListener { onResult(true, "") }
                                .addOnFailureListener { onResult(false, "Erro ao vincular conta.") }
                        }
                        .addOnFailureListener {
                            onResult(false, it.message ?: "Erro ao criar utilizador. Verifica se o email já existe.")
                        }
                }
            }
            .addOnFailureListener { onResult(false, "Erro de conexão.") }
    }
}
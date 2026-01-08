package ipca.lojasas.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import ipca.lojasas.model.Pedido

class StudentViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var meusPedidos = mutableStateListOf<Pedido>()
    var proximoLevantamento = mutableStateListOf<Pedido>()

    init {
        fetchMeusDados()
    }

    private fun fetchMeusDados() {
        val email = auth.currentUser?.email ?: return

        db.collection("pedidos")
            .orderBy("dataPedido", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                meusPedidos.clear()
                proximoLevantamento.clear()

                for (document in result) {
                    val p = document.toObject(Pedido::class.java).apply { id = document.id }

                    if (p.estado == "Pendente") {
                        proximoLevantamento.add(p)
                    } else {
                        meusPedidos.add(p)
                    }
                }
            }
    }

    fun logout() {
        auth.signOut()
    }
}
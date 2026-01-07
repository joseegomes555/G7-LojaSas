package ipca.lojasas.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import ipca.lojasas.model.Lote
import ipca.lojasas.model.Pedido

class StaffViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var alertasValidade = mutableStateListOf<Lote>()
        private set

    var pedidosDoDia = mutableStateListOf<Pedido>()
        private set

    var isLoading = mutableStateListOf<Boolean>()

    init {
        fetchLotesCriticos()
        fetchPedidosHoje()
    }

    // 1. Vai à BD buscar produtos que expiram em breve
    fun fetchLotesCriticos() {
        db.collection("lotes")
            .get()
            .addOnSuccessListener { result ->
                alertasValidade.clear()
                for (document in result) {
                    val lote = document.toObject(Lote::class.java)
                    lote.id = document.id

                    // Filtra apenas o que expira em menos de 5 dias
                    if (lote.diasParaExpirar() <= 5) {
                        alertasValidade.add(lote)
                    }
                }
                // Ordenar - o que expira primeiro apareceem cima
                alertasValidade.sortBy { it.diasParaExpirar() }
            }
    }

    // 2. Vai à BD buscar pedidos com estado "Pendente"
    fun fetchPedidosHoje() {
        db.collection("pedidos")
            .whereEqualTo("estado", "Pendente")
            .orderBy("dataPedido", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                pedidosDoDia.clear()
                for (document in result) {
                    val pedido = document.toObject(Pedido::class.java)
                    pedido.id = document.id
                    pedidosDoDia.add(pedido)
                }
            }
    }

    // 3. Função para dar Baixa de Stock
    fun processarEntrega(pedidoId: String) {
        db.collection("pedidos").document(pedidoId)
            .update("estado", "Entregue")
            .addOnSuccessListener {
                // Atualiza a lista localmente para o utilizador ver logo
                pedidosDoDia.removeIf { it.id == pedidoId }
            }
    }

    fun logout() {
        auth.signOut()
    }
}
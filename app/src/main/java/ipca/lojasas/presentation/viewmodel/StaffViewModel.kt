package ipca.lojasas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import ipca.lojasas.domain.model.Lote
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.domain.model.Produto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class StaffViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // Estados
    private val _todosPedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val todosPedidos: StateFlow<List<Pedido>> = _todosPedidos.asStateFlow()

    private val _lotes = MutableStateFlow<List<Lote>>(emptyList())
    val lotes: StateFlow<List<Lote>> = _lotes.asStateFlow()

    // Lista de Produtos (Catálogo) para o Dropdown
    private val _produtosCatalogo = MutableStateFlow<List<Produto>>(emptyList())
    val produtosCatalogo: StateFlow<List<Produto>> = _produtosCatalogo.asStateFlow()

    private var listeners = mutableListOf<ListenerRegistration>()

    init {
        startPedidosListener()
        startStockListener()
        startCatalogoListener()
    }

    private fun startPedidosListener() {
        val l = db.collection("pedidos").orderBy("dataPedido", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val list = value?.documents?.mapNotNull { doc ->
                    val p = doc.toObject(Pedido::class.java)
                    p?.id = doc.id
                    p
                } ?: emptyList()
                _todosPedidos.value = list
            }
        listeners.add(l)
    }

    private fun startStockListener() {
        val l = db.collection("lotes").orderBy("dataValidade")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val list = value?.documents?.mapNotNull { doc ->
                    val lot = doc.toObject(Lote::class.java)
                    lot?.id = doc.id
                    lot
                } ?: emptyList()
                _lotes.value = list
            }
        listeners.add(l)
    }

    private fun startCatalogoListener() {
        val l = db.collection("produtos").orderBy("nome")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val list = value?.toObjects(Produto::class.java) ?: emptyList()
                _produtosCatalogo.value = list
            }
        listeners.add(l)
    }

    fun atualizarEstadoPedido(pedidoId: String, novoEstado: String) {
        db.collection("pedidos").document(pedidoId).update("estado", novoEstado)
    }

    // --- REAGENDAR PEDIDO ---
    fun reagendarPedido(pedidoId: String, novaData: Long) {
        val timestamp = Timestamp(Date(novaData))
        db.collection("pedidos").document(pedidoId).update(
            mapOf(
                "estado" to "Reagendamento",
                "propostaReagendamento" to timestamp,
                "autorReagendamento" to "Staff"
            )
        )
    }

    fun adicionarLote(nomeProduto: String, categoria: String, quantidade: Int, dataValidade: Long) {
        val validadeTs = Timestamp(Date(dataValidade))
        val lote = hashMapOf(
            "nomeProduto" to nomeProduto,
            "categoria" to categoria,
            "quantidade" to quantidade,
            "origem" to "Manual",
            "dataEntrada" to Timestamp.now(),
            "dataValidade" to validadeTs
        )
        db.collection("lotes").add(lote)
    }

    fun eliminarLote(loteId: String) {
        db.collection("lotes").document(loteId).delete()
    }

    fun eliminarProduto(produtoId: String) {
        // Função opcional se quiseres apagar do catalogo, mas no stockScreen usamos apagar lote
        db.collection("produtos").document(produtoId).delete()
    }

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { it.remove() }
    }
}
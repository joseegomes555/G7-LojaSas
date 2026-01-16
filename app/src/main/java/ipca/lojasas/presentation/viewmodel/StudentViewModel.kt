package ipca.lojasas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import ipca.lojasas.domain.model.ItemPedido
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.domain.model.Produto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class StudentViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Catálogo e Stock
    private val _produtos = MutableStateFlow<List<Produto>>(emptyList())
    val produtos: StateFlow<List<Produto>> = _produtos.asStateFlow()

    private val _stockMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val stockMap: StateFlow<Map<String, Int>> = _stockMap.asStateFlow()

    // Dados do Aluno
    private val _meusPedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val meusPedidos: StateFlow<List<Pedido>> = _meusPedidos.asStateFlow()

    // PERFIL
    private val _perfilUser = MutableStateFlow<Map<String, Any>?>(null)
    val perfilUser: StateFlow<Map<String, Any>?> = _perfilUser.asStateFlow()

    // Variáveis internas
    private var nomeDoUtilizador: String = "Beneficiário"
    private var numeroDoUtilizador: String = ""

    private var listeners = mutableListOf<ListenerRegistration>()

    init {
        fetchUserData()
        startProdutosListener()
        startStockFromLotesListener()
        startMeusPedidosListener()
    }

    private fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return
        val listener = db.collection("utilizadores").whereEqualTo("uid", uid).limit(1)
            .addSnapshotListener { docs, _ ->
                if (docs != null && !docs.isEmpty) {
                    val doc = docs.documents[0]
                    val data = doc.data
                    nomeDoUtilizador = doc.getString("nome") ?: "Beneficiário"
                    numeroDoUtilizador = doc.getString("numEstudante") ?: doc.getString("email") ?: ""
                    _perfilUser.value = data
                }
            }
        listeners.add(listener)
    }

    private fun startProdutosListener() {
        val listener = db.collection("produtos").orderBy("nome").addSnapshotListener { value, error ->
            if (error != null) return@addSnapshotListener
            val list = value?.toObjects(Produto::class.java) ?: emptyList()
            _produtos.value = list
        }
        listeners.add(listener)
    }

    private fun startStockFromLotesListener() {
        val listener = db.collection("lotes").addSnapshotListener { value, error ->
            if (error != null) return@addSnapshotListener
            val mapTemp = mutableMapOf<String, Int>()
            value?.documents?.forEach { doc ->
                val nome = doc.getString("nomeProduto") ?: return@forEach
                val qtd = doc.getLong("quantidade")?.toInt() ?: 0
                val atual = mapTemp[nome] ?: 0
                mapTemp[nome] = atual + qtd
            }
            _stockMap.value = mapTemp
        }
        listeners.add(listener)
    }

    private fun startMeusPedidosListener() {
        val user = auth.currentUser ?: return
        val listener = db.collection("pedidos")
            .whereEqualTo("uid", user.uid)
            .orderBy("dataPedido", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val list = value?.documents?.mapNotNull { doc ->
                    val p = doc.toObject(Pedido::class.java)
                    p?.id = doc.id
                    p
                } ?: emptyList()
                _meusPedidos.value = list
            }
        listeners.add(listener)
    }

    fun submeterPedido(itens: List<ItemPedido>, urgencia: String, dataLevantamento: Date, onResult: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return

        if (nomeDoUtilizador == "Beneficiário") {
            val nomeAuth = user.displayName
            if (!nomeAuth.isNullOrEmpty()) nomeDoUtilizador = nomeAuth
        }

        val pedido = Pedido(
            uid = user.uid,
            email = user.email ?: "",
            nomeAluno = nomeDoUtilizador,
            numAluno = numeroDoUtilizador,
            itens = itens,
            urgencia = urgencia,
            dataPedido = Timestamp.now(),
            dataLevantamento = Timestamp(dataLevantamento),
            estado = "Pendente"
        )

        db.collection("pedidos").add(pedido)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun cancelarPedido(pedidoId: String) {
        db.collection("pedidos").document(pedidoId)
            .update("estado", "Cancelado", "motivoCancelamento", "Cancelado pelo Aluno")
    }

    fun aceitarReagendamento(pedidoId: String, novaData: Timestamp) {
        db.collection("pedidos").document(pedidoId).update(
            mapOf(
                "estado" to "Pendente",
                "dataLevantamento" to novaData,
                "propostaReagendamento" to null,
                "autorReagendamento" to ""
            )
        )
    }

    fun logout() {
        auth.signOut()
        listeners.forEach { it.remove() }
    }

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { it.remove() }
    }
}
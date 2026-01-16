package ipca.lojasas.domain.model

import com.google.firebase.Timestamp
import java.util.Date

data class Beneficiario(
    var uid: String = "",
    var nome: String = "",
    var email: String = "",
    var nif: String = "",
    var telefone: String = "",
    var fotoUrl: String = ""
)

// 1. Modelo de LOTE
data class Lote(
    var id: String = "",
    val nomeProduto: String = "",
    val categoria: String = "",
    val quantidade: Int = 0,
    val dataValidade: Timestamp? = null,
    val origem: String = "Manual",
    val dataEntrada: Timestamp? = null
)

// 2. Modelo de Produto
data class Produto(
    var id: String = "",
    val nome: String = "",
    val categoria: String = ""
)

// 3. Item
data class ItemPedido(
    val nome: String = "",
    val quantidade: Int = 0
)

// 4. PEDIDO
data class Pedido(
    var id: String = "",
    val uid: String = "",
    val email: String = "",
    val nomeAluno: String = "",
    val numAluno: String = "",
    val itens: List<ItemPedido> = emptyList(),
    val urgencia: String = "Normal",
    val estado: String = "Pendente",
    val dataPedido: Timestamp? = null,
    val dataLevantamento: Timestamp? = null,
    val autorReagendamento: String = "",   // "Staff" ou "Aluno"
    val propostaReagendamento: Timestamp? = null,
    val motivoCancelamento: String = ""
)
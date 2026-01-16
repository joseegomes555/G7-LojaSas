package ipca.lojasas.domain.model

import com.google.firebase.Timestamp

// Modelo de LOTE (Stock real)
data class Lote(
    var id: String = "",
    val nomeProduto: String = "",
    val categoria: String = "",
    val quantidade: Int = 0,
    val dataValidade: Timestamp? = null,
    val origem: String = "Manual",
    val dataEntrada: Timestamp? = null
)

// Modelo de Produto (Catálogo)
data class Produto(
    var id: String = "",
    val nome: String = "",
    val categoria: String = ""
)

// Item do Pedido
data class ItemPedido(
    val nome: String = "",
    val quantidade: Int = 0
)

// PEDIDO
data class Pedido(
    var id: String = "",
    val uid: String = "",
    val email: String = "",
    val nomeBeneficiario: String = "",
    val numBeneficiario: String = "",
    val itens: List<ItemPedido> = emptyList(),
    val urgencia: String = "Normal",
    val estado: String = "Pendente",
    val dataPedido: Timestamp? = null,
    val dataLevantamento: Timestamp? = null,

    // Campos de Gestão
    val autorReagendamento: String = "",
    val propostaReagendamento: Timestamp? = null,
    val motivoCancelamento: String = ""
)
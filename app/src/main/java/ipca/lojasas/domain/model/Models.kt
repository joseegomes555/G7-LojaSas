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

data class Produto(
    var id: String = "",
    var nome: String = "",
    var categoria: String = ""
)

data class Lote(
    var id: String = "",
    var nomeProduto: String = "",
    var quantidade: Int = 0,
    var dataValidade: Timestamp? = null,
    var categoria: String = "",
    var origem: String = "",
    var dataEntrada: Timestamp? = null
) {
    fun diasParaExpirar(): Long {
        if (dataValidade == null) return 0
        val diff = dataValidade!!.toDate().time - Date().time
        return diff / (1000 * 60 * 60 * 24)
    }
}

data class Pedido(
    var id: String = "",
    var uid: String = "",
    var email: String = "",
    var nomeAluno: String = "",
    var numAluno: String = "",
    var itens: List<ItemPedido> = emptyList(),
    var urgencia: String = "",
    var dataPedido: Timestamp? = null,
    var estado: String = ""
)

data class ItemPedido(
    var nome: String = "",
    var quantidade: Int = 0
)
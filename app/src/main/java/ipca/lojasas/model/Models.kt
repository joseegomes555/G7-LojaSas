package ipca.lojasas.model

import com.google.firebase.Timestamp
import java.util.Date

// Representa um produto físico no armazém (Lote)
data class Lote(
    var id: String = "",           // ID do documento no Firestore
    val nomeProduto: String = "",
    val codigoBarras: String = "",
    val quantidade: Int = 0,
    val dataValidade: Timestamp? = null
) {
    // Função auxiliar para saber se está a expirar (Lógica de Negócio)
    fun diasParaExpirar(): Long {
        if (dataValidade == null) return 999
        val hoje = Date().time
        val validade = dataValidade.toDate().time
        val diff = validade - hoje
        return diff / (1000 * 60 * 60 * 24) // Converter milissegundos em dias
    }
}

// Representa um pedido de um aluno
data class Pedido(
    var id: String = "",
    val nomeAluno: String = "",
    val numAluno: String = "",
    val dataPedido: Timestamp? = null,
    val estado: String = "Pendente" // Pendente, Entregue, Cancelado
)
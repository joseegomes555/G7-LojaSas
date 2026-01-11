package ipca.lojasas.domain.repository

import ipca.lojasas.domain.model.*
import kotlinx.coroutines.flow.Flow

interface LojaRepository {
    fun getProdutos(): Flow<List<Produto>>
    fun getLotes(): Flow<List<Lote>>
    fun getPedidosPorAluno(uid: String): Flow<List<Pedido>>
    fun getPedidosPendentes(): Flow<List<Pedido>>

    // Ações
    suspend fun criarPedido(pedido: Pedido): Result<Boolean>
    suspend fun abaterStock(itens: List<ItemPedido>): Result<Boolean>
    suspend fun cancelarPedido(pedido: Pedido): Result<Boolean>
    suspend fun processarEntrega(pedidoId: String): Result<Boolean>
    suspend fun adicionarLote(lote: Lote): Result<Boolean>
    suspend fun removerStockManual(loteId: String, qtdAtual: Int, qtdRemover: Int): Result<Boolean>
    suspend fun getPerfilUsuario(uid: String): ipca.lojasas.domain.model.Beneficiario?
}

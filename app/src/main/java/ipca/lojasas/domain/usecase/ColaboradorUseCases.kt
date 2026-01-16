package ipca.lojasas.domain.usecase

import ipca.lojasas.domain.model.Lote
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.domain.repository.LojaRepository
import kotlinx.coroutines.flow.Flow

class GetTodosPedidosUseCase(private val repository: LojaRepository) {
    operator fun invoke(): Flow<List<Pedido>> = repository.getTodosPedidos()
}

class GerirLoteUseCase(private val repository: LojaRepository) {
    suspend fun adicionar(lote: Lote) = repository.adicionarLote(lote)
    suspend fun eliminar(id: String, motivo: String, lote: Lote) = repository.eliminarLote(id, motivo, lote)
}

class GerirPedidoUseCase(private val repository: LojaRepository) {
    suspend fun atualizarEstado(id: String, estado: String) = repository.atualizarEstadoPedido(id, estado)
    suspend fun reagendar(id: String, data: Long) = repository.reagendarPedido(id, data)
}

data class ColaboradorUseCases(
    val getTodosPedidos: GetTodosPedidosUseCase,
    val gerirLote: GerirLoteUseCase,
    val gerirPedido: GerirPedidoUseCase,
    val getStock: GetStockUseCase,
    val getCatalogo: GetCatalogoUseCase
)
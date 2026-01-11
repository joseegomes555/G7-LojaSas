package ipca.lojasas.domain.usecase

import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.domain.repository.LojaRepository

class CancelarPedidoUseCase(private val repository: LojaRepository) {
    suspend operator fun invoke(pedido: Pedido): Result<Boolean> {
        return repository.cancelarPedido(pedido)
    }
}
package ipca.lojasas.domain.usecase

import ipca.lojasas.domain.model.ItemPedido
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.domain.repository.LojaRepository

class FazerPedidoUseCase(private val repository: LojaRepository) {
    suspend operator fun invoke(pedido: Pedido): Result<Boolean> {
        // 1. Criar o pedido
        val resultadoPedido = repository.criarPedido(pedido)

        // 2. Se correu bem, abater stock
        if (resultadoPedido.isSuccess) {
            repository.abaterStock(pedido.itens)
        }

        return resultadoPedido
    }
}
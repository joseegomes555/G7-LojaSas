package ipca.lojasas.domain.usecase

import ipca.lojasas.domain.repository.LojaRepository

class GetCatalogoUseCase(private val repository: LojaRepository) {
    operator fun invoke() = repository.getProdutosCatalogo()
}

class GetStockUseCase(private val repository: LojaRepository) {
    operator fun invoke() = repository.getLotesStock()
}

class GetPerfilUseCase(private val repository: LojaRepository) {
    operator fun invoke(uid: String) = repository.getPerfilUsuario(uid)
}

class CancelarPedidoUseCase(private val repository: LojaRepository) {
    suspend operator fun invoke(id: String, motivo: String) = repository.cancelarPedido(id, motivo)
}
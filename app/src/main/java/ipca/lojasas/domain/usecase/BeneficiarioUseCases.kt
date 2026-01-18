package ipca.lojasas.domain.usecase

import android.graphics.Bitmap
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.domain.repository.LojaRepository
import kotlinx.coroutines.flow.Flow
import com.google.firebase.Timestamp

// --- USE CASES ESPECÍFICOS DO BENEFICIÁRIO (Que não estão no Shared) ---

class GetMeusPedidosUseCase(private val repository: LojaRepository) {
    operator fun invoke(uid: String): Flow<List<Pedido>> = repository.getMeusPedidos(uid)
}

class FazerPedidoUseCase(private val repository: LojaRepository) {
    suspend operator fun invoke(pedido: Pedido) = repository.fazerPedido(pedido)
}

class UploadFotoUseCase(private val repository: LojaRepository) {
    operator fun invoke(bitmap: Bitmap, uid: String, onComplete: (Boolean) -> Unit) =
        repository.uploadFotoPerfil(bitmap, uid, onComplete)
}

class AceitarReagendamentoUseCase(private val repository: LojaRepository) {
    suspend operator fun invoke(id: String, novaData: Timestamp) = repository.aceitarReagendamento(id, novaData)
}

class GetEstadoCandidaturaUseCase(private val repository: LojaRepository) {
    operator fun invoke(uid: String) = repository.getEstadoCandidatura(uid)
}

// --- DATA CLASS AGREGADORA ---
data class BeneficiarioUseCases(
    val getMeusPedidos: GetMeusPedidosUseCase,
    val fazerPedido: FazerPedidoUseCase,
    val cancelarPedido: CancelarPedidoUseCase,
    val uploadFoto: UploadFotoUseCase,
    val getPerfil: GetPerfilUseCase,
    val getCatalogo: GetCatalogoUseCase,
    val getStock: GetStockUseCase,
    val aceitarReagendamento: AceitarReagendamentoUseCase,
    val getEstadoCandidatura: GetEstadoCandidaturaUseCase
)
package ipca.lojasas.domain.repository

import android.graphics.Bitmap
import ipca.lojasas.domain.model.Lote
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.domain.model.Produto
import kotlinx.coroutines.flow.Flow
import com.google.firebase.Timestamp

interface LojaRepository {
    // GERAL
    fun getProdutosCatalogo(): Flow<List<Produto>>
    fun getLotesStock(): Flow<List<Lote>>
    fun getPerfilUsuario(uid: String): Flow<Map<String, Any>?>
    suspend fun logout()

    // BENEFICIARIO
    fun getMeusPedidos(uid: String): Flow<List<Pedido>>
    suspend fun fazerPedido(pedido: Pedido): Boolean
    suspend fun cancelarPedido(pedidoId: String, motivo: String)
    suspend fun aceitarReagendamento(pedidoId: String, novaData: Timestamp)
    fun uploadFotoPerfil(bitmap: Bitmap, uid: String, onComplete: (Boolean) -> Unit)

    // COLABORADOR
    fun getTodosPedidos(): Flow<List<Pedido>>
    suspend fun adicionarLote(lote: Lote)
    suspend fun eliminarLote(loteId: String, motivo: String, loteInfo: Lote)
    suspend fun atualizarEstadoPedido(pedidoId: String, novoEstado: String)
    suspend fun reagendarPedido(pedidoId: String, novaData: Long)
}
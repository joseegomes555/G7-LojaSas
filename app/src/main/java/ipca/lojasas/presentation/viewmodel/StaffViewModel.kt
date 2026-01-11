package ipca.lojasas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import ipca.lojasas.domain.model.Lote
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.domain.repository.LojaRepository
import ipca.lojasas.domain.usecase.CancelarPedidoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StaffViewModel(
    private val repository: LojaRepository,
    private val cancelarPedidoUseCase: CancelarPedidoUseCase
) : ViewModel() {

    val pedidosPendentes = repository.getPedidosPendentes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val produtosMestres = repository.getProdutos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _lotesList = MutableStateFlow<List<Lote>>(emptyList())
    val lotesList = _lotesList.asStateFlow()

    private val _alertasValidade = MutableStateFlow<List<Lote>>(emptyList())
    val alertasValidade = _alertasValidade.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getLotes().collect { lista ->
                _lotesList.value = lista
                _alertasValidade.value = lista.filter { it.diasParaExpirar() <= 5 }
                    .sortedBy { it.diasParaExpirar() }
            }
        }
    }

    fun processarEntrega(pedidoId: String) {
        viewModelScope.launch {
            repository.processarEntrega(pedidoId)
        }
    }

    fun cancelarPedido(pedido: Pedido) {
        viewModelScope.launch {
            cancelarPedidoUseCase(pedido)
        }
    }

    fun adicionarNovoLote(lote: Lote, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = repository.adicionarLote(lote)
            onResult(res.isSuccess)
        }
    }

    fun criarLoteParaAdicionar(nome: String, qtd: Int, validade: java.util.Date, cat: String, orig: String) {
        val novoLote = Lote(
            nomeProduto = nome,
            quantidade = qtd,
            dataValidade = com.google.firebase.Timestamp(validade),
            categoria = cat,
            origem = orig
        )
        adicionarNovoLote(novoLote) {}
    }

    fun removerStock(lote: Lote, qtd: Int, motivo: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = repository.removerStockManual(lote.id, lote.quantidade, qtd)
            onResult(res.isSuccess)
        }
    }

    fun logout() { FirebaseAuth.getInstance().signOut() }
}
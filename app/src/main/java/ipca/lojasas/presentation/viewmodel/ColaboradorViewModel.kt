package ipca.lojasas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import ipca.lojasas.domain.model.Lote
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.domain.model.Produto
import ipca.lojasas.domain.usecase.ColaboradorUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class ColaboradorViewModel(
    private val useCases: ColaboradorUseCases
) : ViewModel() {

    private val _todosPedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val todosPedidos = _todosPedidos.asStateFlow()

    private val _lotes = MutableStateFlow<List<Lote>>(emptyList())
    val lotes = _lotes.asStateFlow()

    private val _produtosCatalogo = MutableStateFlow<List<Produto>>(emptyList())
    val produtosCatalogo = _produtosCatalogo.asStateFlow()

    init {
        viewModelScope.launch {
            useCases.getTodosPedidos().collect { _todosPedidos.value = it }
        }
        viewModelScope.launch {
            useCases.getStock().collect { _lotes.value = it }
        }
        viewModelScope.launch {
            useCases.getCatalogo().collect { _produtosCatalogo.value = it }
        }
    }

    fun atualizarEstadoPedido(id: String, estado: String) {
        viewModelScope.launch { useCases.gerirPedido.atualizarEstado(id, estado) }
    }

    fun reagendarPedido(id: String, data: Long) {
        viewModelScope.launch { useCases.gerirPedido.reagendar(id, data) }
    }

    fun adicionarLote(nome: String, cat: String, qtd: Int, validade: Long) {
        val lote = Lote(
            nomeProduto = nome,
            categoria = cat,
            quantidade = qtd,
            dataValidade = Timestamp(Date(validade)),
            dataEntrada = Timestamp.now()
        )
        viewModelScope.launch { useCases.gerirLote.adicionar(lote) }
    }

    fun eliminarLote(id: String, motivo: String, lote: Lote) {
        viewModelScope.launch { useCases.gerirLote.eliminar(id, motivo, lote) }
    }
}
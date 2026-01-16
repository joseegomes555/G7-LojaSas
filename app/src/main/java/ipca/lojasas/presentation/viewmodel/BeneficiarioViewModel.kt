package ipca.lojasas.presentation.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import ipca.lojasas.domain.model.ItemPedido
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.domain.model.Produto
import ipca.lojasas.domain.usecase.BeneficiarioUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.min

class BeneficiarioViewModel(
    private val useCases: BeneficiarioUseCases
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _produtos = MutableStateFlow<List<Produto>>(emptyList())
    val produtos = _produtos.asStateFlow()

    private val _stockMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val stockMap = _stockMap.asStateFlow()

    private val _meusPedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val meusPedidos = _meusPedidos.asStateFlow()

    private val _perfilUser = MutableStateFlow<Map<String, Any>?>(null)
    val perfilUser = _perfilUser.asStateFlow()

    private val _carrinho = mutableStateMapOf<String, Int>()
    val carrinho: Map<String, Int> get() = _carrinho

    init {
        carregarDados()
    }

    private fun carregarDados() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch { useCases.getPerfil(uid).collect { _perfilUser.value = it } }
        viewModelScope.launch { useCases.getMeusPedidos(uid).collect { _meusPedidos.value = it } }
        viewModelScope.launch { useCases.getCatalogo().collect { _produtos.value = it } }
        viewModelScope.launch {
            useCases.getStock().collect { lotes ->
                val mapTemp = mutableMapOf<String, Int>()
                lotes.forEach { lote ->
                    val atual = mapTemp[lote.nomeProduto] ?: 0
                    mapTemp[lote.nomeProduto] = atual + lote.quantidade
                }
                _stockMap.value = mapTemp
            }
        }
    }

    fun adicionarAoCarrinho(nome: String, max: Int) {
        val qtd = _carrinho[nome] ?: 0
        if (qtd < max && qtd < 5) _carrinho[nome] = qtd + 1
    }

    fun removerDoCarrinho(nome: String) {
        val qtd = _carrinho[nome] ?: 0
        if (qtd > 1) _carrinho[nome] = qtd - 1 else _carrinho.remove(nome)
    }

    fun repetirPedido(pedido: Pedido): Boolean {
        var added = false
        _carrinho.clear()
        pedido.itens.forEach { item ->
            val stock = _stockMap.value[item.nome] ?: 0
            if (stock > 0) {
                _carrinho[item.nome] = min(item.quantidade, stock)
                added = true
            }
        }
        return added
    }

    fun submeterPedido(itens: List<ItemPedido>, urgencia: String, dataLev: Date, onRes: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val perfil = _perfilUser.value
        val nome = perfil?.get("nome") as? String ?: "Beneficiário"
        val num = perfil?.get("numEstudante") as? String ?: ""

        val pedido = Pedido(
            uid = uid,
            email = auth.currentUser?.email ?: "",
            nomeBeneficiario = nome,
            numBeneficiario = num,
            itens = itens,
            urgencia = urgencia,
            dataPedido = Timestamp.now(),
            dataLevantamento = Timestamp(dataLev),
            estado = "Pendente"
        )

        viewModelScope.launch {
            val sucesso = useCases.fazerPedido(pedido)
            if(sucesso) _carrinho.clear()
            onRes(sucesso)
        }
    }

    fun cancelarPedido(id: String, motivo: String) {
        viewModelScope.launch {
            useCases.cancelarPedido(id, motivo)
        }
    }

    fun aceitarReagendamento(pedidoId: String, novaData: Timestamp) {
        viewModelScope.launch {
            useCases.aceitarReagendamento(pedidoId, novaData)
        }
    }

    // FUNÇÃO PARA FOTO
    fun uploadFoto(bitmap: Bitmap, onRes: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        useCases.uploadFoto(bitmap, uid, onRes)
    }

    fun logout() { auth.signOut() }
}
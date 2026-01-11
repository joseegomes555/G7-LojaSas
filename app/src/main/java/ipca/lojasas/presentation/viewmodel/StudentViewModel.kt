package ipca.lojasas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import ipca.lojasas.domain.model.Beneficiario
import ipca.lojasas.domain.model.ItemPedido
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.domain.repository.LojaRepository
import ipca.lojasas.domain.usecase.FazerPedidoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudentViewModel(
    private val repository: LojaRepository,
    private val fazerPedidoUseCase: FazerPedidoUseCase
) : ViewModel() {

    val produtos = repository.getProdutos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _meusPedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val meusPedidos = _meusPedidos.asStateFlow()

    private val _stockMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val stockMap = _stockMap.asStateFlow()

    private var perfilUsuario: Beneficiario? = null

    init {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // 1. Carregar Pedidos
            viewModelScope.launch {
                repository.getPedidosPorAluno(user.uid).collect { _meusPedidos.value = it }
            }
            // 2. Carregar o Perfil
            viewModelScope.launch {
                perfilUsuario = repository.getPerfilUsuario(user.uid)
            }
        }

        // 3. Calcular Stock
        viewModelScope.launch {
            repository.getLotes().collect { lotes ->
                val mapa = mutableMapOf<String, Int>()
                lotes.forEach { lote ->
                    if (lote.diasParaExpirar() > 0) {
                        val atual = mapa.getOrDefault(lote.nomeProduto, 0)
                        mapa[lote.nomeProduto] = atual + lote.quantidade
                    }
                }
                _stockMap.value = mapa
            }
        }
    }

    fun submeterPedido(itens: List<ItemPedido>, urgencia: String, onResult: (Boolean) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        // Usa o nome do perfil carregado. Se ainda não carregou, usa "Aluno"
        val nomeReal = perfilUsuario?.nome ?: "Aluno"
        val numReal = user.email?.substringBefore("@") ?: "00000"

        val pedido = Pedido(
            uid = user.uid,
            email = user.email ?: "",
            nomeAluno = nomeReal,
            numAluno = numReal,
            itens = itens,
            urgencia = urgencia,
            dataPedido = Timestamp.now(),
            estado = "Pendente"
        )

        viewModelScope.launch {
            val result = fazerPedidoUseCase(pedido)
            onResult(result.isSuccess)
        }
    }

    fun logout() { FirebaseAuth.getInstance().signOut() }
}
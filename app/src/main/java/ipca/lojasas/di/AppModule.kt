package ipca.lojasas.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ipca.lojasas.data.repository.LojaRepositoryImpl
import ipca.lojasas.domain.repository.LojaRepository
import ipca.lojasas.domain.usecase.*
import ipca.lojasas.presentation.viewmodel.AuthViewModel
import ipca.lojasas.presentation.viewmodel.ColaboradorViewModel
import ipca.lojasas.presentation.viewmodel.BeneficiarioViewModel

object AppModule {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val repository: LojaRepository by lazy {
        LojaRepositoryImpl(db, auth)
    }

    private val beneficiarioUseCases by lazy {
        BeneficiarioUseCases(
            getMeusPedidos = GetMeusPedidosUseCase(repository),
            fazerPedido = FazerPedidoUseCase(repository),
            cancelarPedido = CancelarPedidoUseCase(repository),
            uploadFoto = UploadFotoUseCase(repository),
            getPerfil = GetPerfilUseCase(repository),
            getCatalogo = GetCatalogoUseCase(repository),
            getStock = GetStockUseCase(repository),
            aceitarReagendamento = AceitarReagendamentoUseCase(repository),
            getEstadoCandidatura = GetEstadoCandidaturaUseCase(repository)
        )
    }

    private val colaboradorUseCases by lazy {
        ColaboradorUseCases(
            getTodosPedidos = GetTodosPedidosUseCase(repository),
            gerirLote = GerirLoteUseCase(repository),
            gerirPedido = GerirPedidoUseCase(repository),
            getStock = GetStockUseCase(repository),
            getCatalogo = GetCatalogoUseCase(repository)
        )
    }

    val viewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return when {
                modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel() as T
                modelClass.isAssignableFrom(BeneficiarioViewModel::class.java) -> BeneficiarioViewModel(beneficiarioUseCases) as T
                modelClass.isAssignableFrom(ColaboradorViewModel::class.java) -> ColaboradorViewModel(colaboradorUseCases) as T
                else -> throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
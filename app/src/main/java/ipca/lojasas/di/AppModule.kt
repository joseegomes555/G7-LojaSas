package ipca.lojasas.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.firestore.FirebaseFirestore
import ipca.lojasas.data.repository.LojaRepositoryImpl
import ipca.lojasas.domain.repository.LojaRepository
import ipca.lojasas.domain.usecase.CancelarPedidoUseCase
import ipca.lojasas.domain.usecase.FazerPedidoUseCase
import ipca.lojasas.presentation.viewmodel.StaffViewModel
import ipca.lojasas.presentation.viewmodel.StudentViewModel

object AppModule {
    // 1. Instância Única do Firestore
    private val db by lazy { FirebaseFirestore.getInstance() }

    // 2. Repositório (Injeta o Firestore)
    val lojaRepository: LojaRepository by lazy {
        LojaRepositoryImpl(db)
    }

    // 3. Use Cases (Injetam o Repositório)
    val fazerPedidoUseCase by lazy { FazerPedidoUseCase(lojaRepository) }
    val cancelarPedidoUseCase by lazy { CancelarPedidoUseCase(lojaRepository) }

    // 4. ViewModel Factory
    val viewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return when {
                modelClass.isAssignableFrom(StudentViewModel::class.java) -> {
                    StudentViewModel(lojaRepository, fazerPedidoUseCase) as T
                }
                modelClass.isAssignableFrom(StaffViewModel::class.java) -> {
                    StaffViewModel(lojaRepository, cancelarPedidoUseCase) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
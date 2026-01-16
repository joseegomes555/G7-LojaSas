package ipca.lojasas.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ipca.lojasas.presentation.viewmodel.StaffViewModel
import ipca.lojasas.presentation.viewmodel.StudentViewModel

object AppModule {

    @Suppress("UNCHECKED_CAST")
    val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(StudentViewModel::class.java) -> {
                    StudentViewModel() as T
                }
                modelClass.isAssignableFrom(StaffViewModel::class.java) -> {
                    StaffViewModel() as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
package otus.homework.flowcats

import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CatsViewModel(
    private val catsRepository: CatsRepository
) : ViewModel() {

    private val _catsStateFlow = MutableStateFlow<Result<Fact>>(Result.Loading)
    val catsStateFlow: StateFlow<Result<Fact>> = _catsStateFlow

    init {
        viewModelScope.launch {
            try {
                catsRepository.listenForCatFacts()
                    .catch { exception ->
                        _catsStateFlow.value = Result.Error(exception)
                    }
                    .collect { fact ->
                        _catsStateFlow.value = Result.Success(fact)
                    }
            } catch (exception: Exception) {
                _catsStateFlow.value = Result.Error(exception)
            }
        }
    }
}

sealed class Result<out T> {
    data object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}

class CatsViewModelFactory(private val catsRepository: CatsRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CatsViewModel::class.java)) {
            CatsViewModel(catsRepository) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

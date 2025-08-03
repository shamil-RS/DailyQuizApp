package com.example.dailyquiz.ui.theme.screen.online_quiz_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquiz.data.repository.MainRepository
import com.example.dailyquiz.model.QuizQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUIState(
    val isLoading: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val error: String? = null,
    val isOffline: Boolean = false
)

sealed class MainIntent {
    data class LoadMain(
        val amount: Int = 5,
        val category: Int? = null,
        val difficulty: String? = null,
        val type: String = "multiple"
    ) : MainIntent()
}

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainUIState(isLoading = true))
    val state: StateFlow<MainUIState> = _state.asStateFlow()

    private var loadJob: Job? = null

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.LoadMain -> loadTrivia(
                amount = intent.amount,
                category = intent.category,
                difficulty = intent.difficulty,
                type = intent.type
            )
        }
    }

    private fun loadTrivia(
        amount: Int,
        category: Int?,
        difficulty: String?,
        type: String
    ) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isOffline = false) }

            repository.getQuizQuestions(amount, category, difficulty, type)
                .catch { e ->
                    // В случае ошибки — показываем ошибку и offline, если есть кеш
                    _state.update { currentState ->
                        currentState.copy(
                            error = e.message ?: "Unknown error",
                            isLoading = false,
                            isOffline = true
                        )
                    }
                }
                .collect { questions ->
                    _state.update {
                        it.copy(
                            questions = questions,
                            isLoading = false,
                            error = null,
                            isOffline = false
                        )
                    }
                }
        }
    }
}
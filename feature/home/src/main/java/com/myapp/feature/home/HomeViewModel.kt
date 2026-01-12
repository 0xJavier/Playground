package com.myapp.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.core.domain.GetUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getUserDataUseCase: GetUserDataUseCase,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getUserDataUseCase()
        .map { userData ->
            HomeUiState.Success(
                userName = userData.userName ?: "User",
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val userName: String,
    ) : HomeUiState
}

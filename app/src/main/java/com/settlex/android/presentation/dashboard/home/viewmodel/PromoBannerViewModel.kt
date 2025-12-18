package com.settlex.android.presentation.dashboard.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.settlex.android.data.exception.ApiException
import com.settlex.android.domain.repository.PromoBannerRepository
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.dashboard.home.model.PromoBannerUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PromoBannerViewModel @Inject constructor(
    private val bannerRepository: PromoBannerRepository,
    private val apiException: ApiException
) : ViewModel() {

    private val _banners = MutableStateFlow<UiState<List<PromoBannerUiModel>>>(UiState.Loading)
    val banners = _banners.asStateFlow()

    init {
        fetchPromotionalBanners()
    }

    private fun fetchPromotionalBanners() {
        viewModelScope.launch {
            try {
                val cachedBanners = bannerRepository.getPromotionalBanners()
                _banners.value = UiState.Success(cachedBanners)
            } catch (e: Exception) {
                _banners.value = UiState.Failure(apiException.map(e))
            }
        }
    }
}
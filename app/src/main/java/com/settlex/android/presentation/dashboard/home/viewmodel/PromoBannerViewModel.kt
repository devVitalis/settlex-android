package com.settlex.android.presentation.dashboard.home.viewmodel

import androidx.lifecycle.ViewModel
import com.settlex.android.domain.repository.PromoBannerRepository
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.dashboard.home.model.PromoBannerUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class PromoBannerViewModel @Inject constructor(
    private val bannerRepository: PromoBannerRepository
) : ViewModel() {

    private val _banners =
        MutableStateFlow<UiState<MutableList<PromoBannerUiModel>>>(UiState.Loading)
    val banners = _banners.asStateFlow()

    init {
        fetchPromotionalBanners()
    }

    private fun fetchPromotionalBanners() {
        bannerRepository.fetchPromotionalBanners {
            _banners.value = UiState.Success(it)
        }
    }
}
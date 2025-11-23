package com.settlex.android.ui.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import com.settlex.android.data.repository.BannerRepository
import com.settlex.android.ui.common.state.UiState
import com.settlex.android.ui.dashboard.model.BannerUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class PromoBannerViewModel @Inject constructor(private val bannerRepository: BannerRepository) :
    ViewModel() {
    private val _bannerState =
        MutableStateFlow<UiState<MutableList<BannerUiModel>>>(UiState.Loading)
    val bannerState = _bannerState.asStateFlow()

    init {
        fetchPromoBanners()
    }

    private fun fetchPromoBanners() {
        bannerRepository.fetchPromotionalBanners { bannerUiModel: MutableList<BannerUiModel> ->
            _bannerState.value = UiState.Success(bannerUiModel)
        }
    }
}

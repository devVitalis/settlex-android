package com.settlex.android.ui.dashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.repository.PromoBannerRepository;
import com.settlex.android.ui.dashboard.model.PromoBannerUiModel;
import com.settlex.android.util.event.UiState;

import java.util.List;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class PromoBannerViewModel extends ViewModel {
    private final PromoBannerRepository promoBannerRepo;
    private final MutableLiveData<UiState<List<PromoBannerUiModel>>> promoBannersLiveData = new MutableLiveData<>();

    @Inject
    public PromoBannerViewModel(PromoBannerRepository promoBannerRepo) {
        this.promoBannerRepo = promoBannerRepo;
        fetchPromoBanners();
    }

    // Getters
    public LiveData<UiState<List<PromoBannerUiModel>>> getPromoBanners() {
        return promoBannersLiveData;
    }

    private void fetchPromoBanners() {
        promoBannersLiveData.postValue(UiState.loading());
        promoBannerRepo.fetchPromotionalBanners(bannerUiModel -> promoBannersLiveData.postValue(UiState.success(bannerUiModel)));
    }
}

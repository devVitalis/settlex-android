package com.settlex.android.ui.dashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.repository.PromoBannerRepository;
import com.settlex.android.ui.dashboard.model.PromoBannerUiModel;

import java.util.List;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class PromoBannerViewModel extends ViewModel {
    private final PromoBannerRepository promoBannerRepo;
    private final MutableLiveData<List<PromoBannerUiModel>> promoBannersLiveData = new MutableLiveData<>();

    @Inject
    public PromoBannerViewModel(PromoBannerRepository promoBannerRepo) {
        this.promoBannerRepo = promoBannerRepo;
        fetchPromoBanners();
    }

    // Getters
    public LiveData<List<PromoBannerUiModel>> getPromoBanners() {
        return promoBannersLiveData;
    }

    private void fetchPromoBanners(){
        promoBannerRepo.fetchPromotionalBanners(promoBannersLiveData::postValue);
    }
}

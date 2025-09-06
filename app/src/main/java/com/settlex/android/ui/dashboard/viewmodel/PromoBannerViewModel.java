package com.settlex.android.ui.dashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.repository.PromoBannerRepository;
import com.settlex.android.ui.dashboard.model.PromoBannerUiModel;

import java.util.List;

public class PromoBannerViewModel extends ViewModel {
    private final PromoBannerRepository promoBannerRepo;
    private final MutableLiveData<List<PromoBannerUiModel>> promoBannersLiveData = new MutableLiveData<>();

    // Contructor
    public PromoBannerViewModel() {
        promoBannerRepo = new PromoBannerRepository();
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

package com.settlex.android.onboarding.fragment;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.settlex.android.R;
import com.settlex.android.databinding.FragmentOnboardingPage2Binding;

public class OnboardingPage2Fragment extends Fragment {
    private FragmentOnboardingPage2Binding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingPage2Binding.inflate(inflater, container, false);

        styleHeaderTxt();
        loadOptimizedImage();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void styleHeaderTxt() {
        String htmlText = "Pay <font color='#0044CC'>Bills</font> <br/>Without Stress";
        binding.header.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
    }

    private void loadOptimizedImage() {
        Glide.with(this)
                .load(R.drawable.img_intro_slide_2)
                .centerCrop()
                .into(binding.ImgIntroSlide2);
    }

}
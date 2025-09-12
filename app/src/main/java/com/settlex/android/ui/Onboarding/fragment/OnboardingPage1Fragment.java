package com.settlex.android.ui.Onboarding.fragment;

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
import com.settlex.android.databinding.FragmentOnboardingPage1Binding;

public class OnboardingPage1Fragment extends Fragment {
    private FragmentOnboardingPage1Binding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingPage1Binding.inflate(inflater, container, false);

        styleHeaderTxt();
        loadOptimizedImage();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void styleHeaderTxt(){
        String htmlText = "PAY <font color='#0044CC'>BILLS</font> WITHOUT <font color='#0044CC'>STRESS</font>";
        binding.header.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
    }

    private void loadOptimizedImage() {
        Glide.with(this)
                .load(R.drawable.img_intro_slide_1)
                .centerCrop()
                .into(binding.ImgIntroSlide1);
    }
}

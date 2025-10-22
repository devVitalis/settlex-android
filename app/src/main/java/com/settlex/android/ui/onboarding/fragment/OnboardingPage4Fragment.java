package com.settlex.android.ui.onboarding.fragment;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.settlex.android.R;
import com.settlex.android.databinding.FragmentOnboardingPage4Binding;


public class OnboardingPage4Fragment extends Fragment {
    private FragmentOnboardingPage4Binding binding;

    public OnboardingPage4Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboardingPage4Binding.inflate(inflater, container, false);

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
        String htmlText = "<font color='#0044CC'>Secure</font> <br/>By Design";
        binding.header.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
    }


    private void loadOptimizedImage() {
        Glide.with(this)
                .load(R.drawable.img_intro_slide_4)
                .centerCrop()
                .into(binding.ImgIntroSlide4);
    }
}
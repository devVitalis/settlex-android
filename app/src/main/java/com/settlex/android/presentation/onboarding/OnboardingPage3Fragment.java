package com.settlex.android.presentation.onboarding;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.settlex.android.R;
import com.settlex.android.databinding.FragmentOnboardingPage3Binding;

public class OnboardingPage3Fragment extends Fragment {
    private FragmentOnboardingPage3Binding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingPage3Binding.inflate(inflater, container, false);

        highlightWordInHeader();
        loadOptimizedImage();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void highlightWordInHeader() {
        String fullText = "Never Miss\nYour Shows";
        String textToHighlight = "Shows";
        int startIndex = fullText.indexOf(textToHighlight);
        int endIndex = startIndex + textToHighlight.length();

        SpannableString header = new SpannableString(fullText);
        header.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue_500)),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        binding.header.setText(header);
    }

    private void loadOptimizedImage() {
        Glide.with(this)
                .load(R.drawable.img_intro_slide_3)
                .centerCrop()
                .into(binding.imgIntroSlide3);
    }
}
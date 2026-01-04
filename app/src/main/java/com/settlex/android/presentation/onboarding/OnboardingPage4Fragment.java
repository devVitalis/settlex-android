package com.settlex.android.presentation.onboarding;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.settlex.android.R;
import com.settlex.android.databinding.FragmentOnboardingPage4Binding;

public class OnboardingPage4Fragment extends Fragment {
    private FragmentOnboardingPage4Binding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboardingPage4Binding.inflate(inflater, container, false);

        highlightWordInHeader();
        setImage();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void highlightWordInHeader() {
        String fullText = "Secure By\nDesign";
        String textToHighlight = "Design";
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

    private void setImage() {
        Glide.with(requireContext())
                .load(R.drawable.img_intro_slide_4)
                .centerCrop()
                .into(binding.ivIntroSlide4);
    }
}
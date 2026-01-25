package com.settlex.android.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.settlex.android.R
import com.settlex.android.databinding.FragmentOnboardingPage3Binding
import com.settlex.android.presentation.common.util.SpannableTextFormatter

class OnboardingPage3Fragment : Fragment() {
    private var _binding: FragmentOnboardingPage3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPage3Binding.inflate(inflater, container, false)

        highlightWordInHeader()
        setImage()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun highlightWordInHeader() {
        binding.header.text = SpannableTextFormatter(
            requireContext(),
            "Never Miss\nYour Shows",
            "Shows",
            R.attr.colorPrimary,
        )
    }

    private fun setImage() {
        Glide.with(this)
            .load(R.drawable.img_intro_slide_3)
            .centerCrop()
            .into(binding.ivIntroSlide3)
    }
}
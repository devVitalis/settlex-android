package com.settlex.android.presentation.dashboard.rewards

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.settlex.android.R
import com.settlex.android.databinding.FragmentDashboardRewardsBinding
import com.settlex.android.presentation.common.extensions.copyToClipboard
import com.settlex.android.presentation.common.extensions.getColorRes
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.extensions.toNairaString
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RewardsDashboardFragment : Fragment() {
    private var _binding: FragmentDashboardRewardsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RewardsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardRewardsBinding.inflate(inflater, container, false)

        initViews()
        observeUserSession()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        StatusBar.setColor(requireActivity(), R.color.colorRewardsBackground)
        applyReferralInfoStyle()

        with(binding) {
            ivCopyReferral.setOnClickListener { tvReferralCode.copyToClipboard("Referral Code") }
        }
    }

    private fun observeUserSession() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.userSession.collect {
//                    when (it) {
//                        is UiState.Success -> {
//                            if (it.data.authUid == null) {
//                                showLoggedOutView()
//                                return@collect
//                            }
//                            binding.loggedOutState.visibility = View.GONE
//                            binding.loggedInState.visibility = View.VISIBLE
//
//                            displayRewardsData(it.data.user as RewardsUiModel)
//                        }
//
//                        else -> Unit
//                    }
//                }
            }
        }
    }

    private fun displayRewardsData(user: RewardsUiModel) {
        with(binding) {
            tvReferralCode.text = user.paymentId ?: "Get Referral Code"
            tvTotalReferralEarning.text = user.referralBalance.toNairaString()
            tvReferralsShared.text = "0"
        }
    }

    private fun showLoggedOutView() = with(binding) {
        root.setBackgroundColor(requireContext().getColorRes(R.color.colorSurface))

        viewAuthenticatedUiState.gone()
        viewUnauthenticatedUiState.show()
    }

    private fun applyReferralInfoStyle() {
        val htmlText =
            "Get 1% commission on every transaction your referrals make, for a lifetime. Start sharing and watch your rewards grow!"
        binding.tvReferralInfo.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
    }
}
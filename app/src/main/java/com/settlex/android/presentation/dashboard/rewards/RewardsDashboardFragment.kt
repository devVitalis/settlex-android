package com.settlex.android.presentation.dashboard.rewards

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.settlex.android.R
import com.settlex.android.databinding.FragmentDashboardRewardsBinding
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.wallet.CommissionWithdrawalActivity
import com.settlex.android.util.string.CurrencyFormatter
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RewardsDashboardFragment : Fragment() {
    private var binding: FragmentDashboardRewardsBinding? = null
    private val viewModel: RewardsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardRewardsBinding.inflate(inflater, container, false)

        setupUi()
        observeUserState()
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setupUi() {
        StatusBar.setColor(requireActivity(), R.color.blue_400)
        applyReferralInfoStyle()

        binding!!.btnCopy.setOnClickListener {
            StringFormatter.copyToClipboard(
                requireContext(),
                "Referral Code",
                binding!!.referralCode.text.toString(),
                true
            )
        }
        binding!!.btnShareInvitationLink.setOnClickListener {
            StringFormatter.showNotImplementedToast(
                requireContext()
            )
        }

        binding!!.btnViewCommissionBalance.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    CommissionWithdrawalActivity::class.java
                )
            )
        }
    }

    private fun observeUserState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.userSessionState.collect {
//                    when (it) {
//                        is UiState.Success -> {
//                            if (it.data.authUid == null) {
//                                showLoggedOutView()
//                                return@collect
//                            }
//                            binding!!.loggedOutState.visibility = View.GONE
//                            binding!!.loggedInState.visibility = View.VISIBLE
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
        binding!!.commissionBalance.text = CurrencyFormatter.formatToNaira(user.commissionBalance)
        binding!!.referralCode.text = user.paymentId ?: "Get Referral Code"
        binding!!.totalReferralEarning.text = CurrencyFormatter.formatToNaira(user.referralBalance)
    }

    private fun showLoggedOutView() {
        binding!!.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding!!.headerTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        binding!!.loggedInState.visibility = View.GONE
        binding!!.loggedOutState.visibility = View.VISIBLE
    }

    private fun applyReferralInfoStyle() {
        val htmlText =
            "Get <font color='#0044CC'><b>1% commission</b></font> on every transaction your referrals make, for " +
                    "<font color='#0044CC'><b>a lifetime</b></font>. Start sharing and watch your rewards grow!"
        binding!!.txtReferralInfo.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
    }
}
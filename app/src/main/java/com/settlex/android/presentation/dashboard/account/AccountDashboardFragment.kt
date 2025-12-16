package com.settlex.android.presentation.dashboard.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.settlex.android.R
import com.settlex.android.data.remote.profile.ProfileService
import com.settlex.android.databinding.FragmentDashboardAccountBinding
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.dashboard.account.model.ProfileUiModel
import com.settlex.android.presentation.dashboard.account.viewmodel.ProfileViewModel
import com.settlex.android.presentation.settings.SettingsActivity
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountDashboardFragment : Fragment() {
    private var binding: FragmentDashboardAccountBinding? = null
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardAccountBinding.inflate(inflater, container, false)

        initViews()
        observeUserState()
        return binding!!.getRoot()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun initViews() {
        StatusBar.setColor(requireActivity(), R.color.blue_200)

        binding!!.ivProfilePhoto.setOnClickListener {
            startActivity(
                ProfileActivity::class.java
            )
        }
        binding!!.btnSettings.setOnClickListener {
            startActivity(
                SettingsActivity::class.java
            )
        }
        binding!!.btnSettingsIcon.setOnClickListener {
            startActivity(
                SettingsActivity::class.java
            )
        }
        binding!!.btnTransactions.setOnClickListener {
            StringFormatter.showNotImplementedToast(
                requireContext()
            )
        }
        binding!!.btnAbout.setOnClickListener {
            startActivity(
                AboutActivity::class.java
            )
        }
        binding!!.btnEarnRewards.setOnClickListener {
            toFragment(
                R.id.rewards_fragment
            )
        }
    }

    private fun observeUserState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                profileViewModel.userSession.collect {
////                    when (it) {
////                        is UiState.Success -> {
////                            val user = it.data.user as ProfileUiModel
////                            ProfileService.loadProfilePic(user.photoUrl, binding!!.btnProfilePic)
////                            binding!!.tvFullName.text = user.fullName
////                        }
////
////                        else -> Unit
////                    }
//                }
            }
        }
    }

    private fun startActivity(activityClass: Class<out Activity>) {
        startActivity(Intent(requireContext(), activityClass))
    }

    private fun toFragment(@IdRes navId: Int) {
        val navController = NavHostFragment.findNavController(this)
        navController.navigate(navId)
    }
}
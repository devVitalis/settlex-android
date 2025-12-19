package com.settlex.android.presentation.dashboard.account

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.Timestamp
import com.settlex.android.R
import com.settlex.android.data.remote.profile.ProfileService
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.databinding.ActivityProfileBinding
import com.settlex.android.presentation.common.extensions.addAtPrefix
import com.settlex.android.presentation.common.extensions.maskEmail
import com.settlex.android.presentation.common.extensions.maskPhoneNumber
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.presentation.dashboard.account.model.ProfileUiModel
import com.settlex.android.presentation.dashboard.account.viewmodel.ProfileViewModel
import com.settlex.android.util.string.DateFormatter
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private val progressLoader by lazy { ProgressDialogManager(this) }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private var joinedDate: Timestamp? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initObservers()
    }

    private fun initViews() {
        StatusBar.setColor(this, R.color.white)
        initGalleryPermissionLauncher()
        initProfilePicPicker()

        binding.btnBackBefore.setOnClickListener { finish() }
        binding.btnChangeProfilePic.setOnClickListener { checkPermissionsAndOpenGallery() }
        binding.btnShowFullDate.setOnClickListener { showJoinedDateDialog() }
        binding.btnCopyPaymentId.setOnClickListener {
            StringFormatter.copyToClipboard(
                this,
                "Payment ID",
                binding.tvPaymentId.text.toString(),
                true
            )
        }
    }

    private fun initObservers() {
        observeUserSession()
        observeProfilePicUploadResult()
    }

    private fun showJoinedDateDialog() {
        joinedDate?.let {
            val title = "Member since"
            val message = DateFormatter.formatTimeStampToDate(it)

            DialogHelper.showSimpleAlertDialog(
                this, title, message
            )
        }
    }

    private fun observeUserSession() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userSessionState.collect { sessionState ->
                    when (sessionState) {
                        is UserSessionState.Authenticated -> onUserAuthenticated(sessionState.user)
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun onUserAuthenticated(user: ProfileUiModel) = with(binding) {
        ProfileService.loadProfilePic(user.photoUrl, ivProfilePhoto)
        tvPaymentId.text = user.paymentId?.addAtPrefix() ?: "Setup Payment ID"
        tvFullName.text = user.fullName.uppercase()
        tvEmail.text = user.email.maskEmail()
        tvPhoneNumber.text = user.phone.maskPhoneNumber()
        tvJoinedDate.text = DateFormatter.formatTimestampToRelative(user.createdAt)
        joinedDate = user.createdAt
    }

    private fun observeProfilePicUploadResult() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.setProfilePictureEvent.collect {
                    when (it) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> progressLoader.hide()
                        is UiState.Failure -> onProfilePicUploadStatusError(it.exception.message)
                    }
                }
            }
        }
    }

    private fun onProfilePicUploadStatusError(error: String?) {
        progressLoader.hide()
        binding.tvError.text = error
    }

    private fun initGalleryPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                openGalleryPicker()
                return@registerForActivityResult
            }
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initProfilePicPicker() {
        pickImageLauncher = registerForActivityResult(
            PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                viewModel.setProfilePicture(this, uri)
            } else Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGalleryPicker() {
        pickImageLauncher.launch(
            PickVisualMediaRequest.Builder()
                .setMediaType(ImageOnly)
                .build()
        )
    }

    private fun checkPermissionsAndOpenGallery() {
        val isAndroidTiramisuOrNewer = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE

        if (isAndroidTiramisuOrNewer) {
            openGalleryPicker()
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGalleryPicker()
            return
        }

        requestPermissionLauncher.launch(permission)
    }
}
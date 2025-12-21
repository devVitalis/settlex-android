package com.settlex.android.presentation.dashboard.account

import android.Manifest
import android.content.Intent
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
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.Timestamp
import com.settlex.android.R
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.remote.profile.ProfileService
import com.settlex.android.data.session.UserSessionState
import com.settlex.android.databinding.ActivityProfileBinding
import com.settlex.android.presentation.common.extensions.addAtPrefix
import com.settlex.android.presentation.common.extensions.maskEmail
import com.settlex.android.presentation.common.extensions.maskPhoneNumber
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.extensions.toDateString
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.presentation.dashboard.account.model.ProfileUiModel
import com.settlex.android.presentation.dashboard.account.viewmodel.ProfileViewModel
import com.settlex.android.util.string.DateFormatter
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private val progressLoader by lazy { ProgressDialogManager(this) }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var cropImageLauncher: ActivityResultLauncher<Intent>

    private var userJoinedDate: Timestamp? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initObservers()
    }

    private fun initViews() = with(binding) {
        StatusBar.setColor(this@ProfileActivity, R.color.white)
        initGalleryPermissionLauncher()
        initProfilePhotoPicker()
        initCropImageLauncher()

        btnBackBefore.setOnClickListener { finish() }
        btnChangeProfilePic.setOnClickListener { checkPermissionsAndOpenGallery() }
        ivMemberSinceInfo.setOnClickListener { showJoinedDateDialog() }
        btnCopyPaymentId.setOnClickListener {
            StringFormatter.copyToClipboard(
                this@ProfileActivity, "Payment ID",
                tvPaymentId.text.toString(),
                true
            )
        }
    }

    private fun initObservers() {
        observeUserSession()
        observeSetProfilePictureEvent()
    }

    private fun showJoinedDateDialog() {
        userJoinedDate?.also {
            DialogHelper.showSimpleAlertDialog(
                this, "Member since", it.toDateString()
            )
        }
    }

    private fun observeUserSession() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userSessionState.collect { state ->
                    when (state) {
                        is UserSessionState.Authenticated -> onUserAuthenticated(state.user)
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
        tvJoinedDate.text = DateFormatter.getTimeAgo(user.joinedDate)
        userJoinedDate = user.joinedDate
    }

    private fun observeSetProfilePictureEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.setProfilePictureEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> progressLoader.hide()
                        is UiState.Failure -> onProfilePhotoUploadFailure(state.exception)
                    }
                }
            }
        }
    }

    private fun onProfilePhotoUploadFailure(error: AppException) = with(binding) {
        progressLoader.hide()
        tvError.text = error.message
        tvError.show()
    }

    private fun initGalleryPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                openGalleryPicker()
                return@registerForActivityResult
            }
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initProfilePhotoPicker() {
        pickImageLauncher = registerForActivityResult(PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                launchCropActivity(uri)
            } else Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initCropImageLauncher() {
        cropImageLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    val croppedUri = UCrop.getOutput(result.data!!)
                    viewModel.setProfilePhoto(this, croppedUri!!)
                }

                UCrop.RESULT_ERROR -> {
                    val cropError = UCrop.getError(result.data!!)
                    Toast.makeText(this, "Crop failed: ${cropError?.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun launchCropActivity(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(
            File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        )

        val cropIntent = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(512, 512)
            .getIntent(this)

        cropImageLauncher.launch(cropIntent)
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
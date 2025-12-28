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
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
    private lateinit var galleryPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var cropImageLauncher: ActivityResultLauncher<Intent>

    private var userJoinedDate: Timestamp? = null
    private var userPhotoUrl: String? = null
    private var cameraImageUri: Uri? = null

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
        initCameraPermissionLauncher()
        initProfilePhotoPicker()
        initTakePictureLauncher()
        initCropImageLauncher()

        btnBackBefore.setOnClickListener { finish() }
        btnChangeProfilePic.setOnClickListener { showImageSourceBottomSheet() }
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
        userPhotoUrl = user.photoUrl
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
        galleryPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                openGalleryPicker()
                return@registerForActivityResult
            }
            Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initCameraPermissionLauncher() {
        cameraPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
                return@registerForActivityResult
            }
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initProfilePhotoPicker() {
        pickImageLauncher = registerForActivityResult(PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                launchCropActivity(it)
            } ?: Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initTakePictureLauncher() {
        takePictureLauncher = registerForActivityResult(TakePicture()) { success ->
            if (success) {
                launchCropActivity(cameraImageUri!!)
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
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

    private fun showImageSourceBottomSheet() {
        DialogHelper.showBottomSheetImageSource(this) { dialog, binding ->
            with(binding) {
                ProfileService.loadProfilePic(userPhotoUrl, ivProfilePhoto)
                tvGallery.setOnClickListener {
                    checkGalleryPermissionAndOpen()
                    dialog.dismiss()
                }

                tvCamera.setOnClickListener {
                    checkCameraPermissionAndOpen()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun checkGalleryPermissionAndOpen() {
        val isAndroidTiramisuOrNewer = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

        if (isAndroidTiramisuOrNewer) {
            openGalleryPicker()
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGalleryPicker()
            return
        }

        galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
            return
        }

        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun openGalleryPicker() {
        pickImageLauncher.launch(
            PickVisualMediaRequest.Builder()
                .setMediaType(ImageOnly)
                .build()
        )
    }

    private fun openCamera() {
        val photoFile = File(cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(cameraImageUri!!)
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
}
package com.settlex.android.ui.dashboard.fragment.home.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.data.remote.profile.ProfileService;
import com.settlex.android.databinding.ActivityProfileBinding;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.string.StringUtil;
import com.settlex.android.util.ui.StatusBarUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileActivity extends AppCompatActivity {
    private final String TAG = ProfileActivity.class.getSimpleName();

    private ActivityProfileBinding binding;
    private UserViewModel userViewModel;
    private ProgressLoaderController progressLoader;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        progressLoader = new ProgressLoaderController(this);

        observeAndLoadUserData();
        observeProfilePicUpload();
        setupUiActions();
    }

    // observers
    private void observeAndLoadUserData() {
        userViewModel.getUserLiveData().observe(this, user -> {
            if (user == null) return;

            switch (user.getStatus()) {
                case SUCCESS -> onUserDataSuccess(user.getData());
                case ERROR -> onUserDataError(user.getMessage());
            }
        });
    }

    private void onUserDataSuccess(UserUiModel user) {
        // display data
        ProfileService.loadProfilePic(user.getProfileUrl(), binding.profilePic);
        binding.paymentId.setText(user.getUsername() != null ? user.getUsername() : "Setup Payment ID");
        binding.fullName.setText(user.getFullName().toUpperCase());
        binding.email.setText(StringUtil.maskEmail(user.getEmail()));
        binding.phoneNumber.setText(user.getPhone());
        binding.username.setText(StringUtil.addAtToUsername(user.getUsername()));
    }

    private void onUserDataError(String error) {
        Log.e("Fragment", "User data error: " + error);
    }

    private void observeProfilePicUpload() {
        userViewModel.getProfilePicUploadResult().observe(this, upload -> {
            if (upload == null) return;

            switch (upload.getStatus()) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> progressLoader.hide();
                case ERROR -> onProfilePicUploadError(upload.getMessage());
            }
        });
    }

    private void onProfilePicUploadError(String error) {
        progressLoader.hide();
        binding.error.setText(error);
        Log.e(TAG, "Profile Pic Upload error: " + error);
    }


    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);
        initGalleryPermissionLauncher();
        initProfilePicPicker();

        binding.btnBackBefore.setOnClickListener(v -> finish());
        binding.btnChangeProfilePic.setOnClickListener(view -> checkPermissionsAndOpenGallery());
        binding.btnCopyPaymentId.setOnClickListener(v -> StringUtil.copyToClipboard(
                this,
                "Payment Id",
                binding.paymentId.getText().toString(),
                true));
    }

    private void initGalleryPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        openGalleryPicker();
                        return;
                    }
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void initProfilePicPicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        uploadProfilePic(uri);
                        return;
                    }
                    Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void uploadProfilePic(Uri uri) {
        userViewModel.uploadProfilePic(StringUtil.compressAndConvertImageToBase64(this, uri));
    }

    private void openGalleryPicker() {
        pickImageLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        );
    }

    private void checkPermissionsAndOpenGallery() {
        boolean IS_ANDROID_13_OR_HIGHER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

        if (IS_ANDROID_13_OR_HIGHER) {
            openGalleryPicker();
            return;
        }

        boolean isPermissionGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
        if (isPermissionGranted) {
            openGalleryPicker();
            return;
        }
        requestPermissionLauncher.launch(permission);
    }
}
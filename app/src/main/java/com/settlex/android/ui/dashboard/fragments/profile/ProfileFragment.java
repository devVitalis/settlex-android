package com.settlex.android.ui.dashboard.fragments.profile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.data.remote.profile.ProfileService;
import com.settlex.android.databinding.FragmentProfileBinding;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.string.StringUtil;
import com.settlex.android.util.ui.StatusBarUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    // dependencies
    private FragmentProfileBinding binding;
    private UserViewModel userViewModel;
    private ProgressLoaderController progressLoader;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        progressLoader = new ProgressLoaderController(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        observeAndLoadUserData();
        observeProfilePicUpload();
        setupUiActions();
        return binding.getRoot();
    }

    // observers
    private void observeAndLoadUserData() {
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
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
        binding.phoneNumber.setText(StringUtil.formatPhoneNumberWithCountryCode(user.getPhone()));
    }

    private void onUserDataError(String error) {
        Log.e("Fragment", "User data error: " + error);
    }

    private void observeProfilePicUpload() {
        userViewModel.getProfilePicUploadResult().observe(getViewLifecycleOwner(), upload -> {
            if (upload == null) return;

            switch (upload.getStatus()) {
                case LOADING -> onProfilePicUploadLoading();
                case SUCCESS -> onProfilePicUploadSuccess();
                case ERROR -> onProfilePicUploadError(upload.getMessage());
            }
        });
    }

    private void onProfilePicUploadLoading() {
        progressLoader.show();
    }

    private void onProfilePicUploadSuccess() {
        progressLoader.hide();
    }

    private void onProfilePicUploadError(String error) {
        progressLoader.hide();
        binding.error.setText(error);
        Log.e("ProfilePic", "Profile Pic Upload error: " + error);
    }


    private void setupUiActions() {
        initGalleryPermissionLauncher();
        initProfilePicPicker();

        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.white);
        binding.btnChangeProfilePic.setOnClickListener(view -> checkPermissionsAndOpenGallery());
        binding.btnBackBefore.setOnClickListener(v -> requireActivity().finish());
        binding.btnCopyPaymentId.setOnClickListener(v -> StringUtil.copyToClipboard(requireContext(), "Payment Id", binding.paymentId.getText().toString(), true));
    }

    private void initGalleryPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        openGalleryPicker();
                        return;
                    }
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void initProfilePicPicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        // set selected image
                        uploadProfilePic(uri);
                        return;
                    }
                    Toast.makeText(requireContext(), "No media selected", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void uploadProfilePic(Uri uri) {
        userViewModel.uploadProfilePic(StringUtil.compressAndConvertImageToBase64(requireContext(), uri));
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

        boolean isPermissionGranted = ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED;
        if (isPermissionGranted) {
            openGalleryPicker();
            return;
        }
        requestPermissionLauncher.launch(permission);
    }
}
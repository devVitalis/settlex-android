package com.settlex.android.presentation.common.util

import android.app.Activity
import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.settlex.android.R
import com.settlex.android.databinding.AlertDialogMessageBinding
import com.settlex.android.databinding.AlertDialogWithIconBinding
import com.settlex.android.databinding.BottomSheetImageSourceBinding
import com.settlex.android.databinding.BottomSheetSuccessDialogBinding

/**
 * Utility class for common UI operations
 * Provides reusable methods for displaying consistent UI components throughout the application.
 */
object DialogHelper {

    // Alert dialogs
    fun showCustomAlertDialogWithIcon(
        context: Context,
        config: (AlertDialog, AlertDialogWithIconBinding) -> Unit
    ) {
        val binding = AlertDialogWithIconBinding.inflate(LayoutInflater.from(context))
        val dialog =
            MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert_Rounded16dp)
                .setView(binding.getRoot())
                .setCancelable(false)
                .create()

        config(dialog, binding)
        dialog.show()
    }

    fun showCustomAlertDialog(
        context: Context,
        config: (AlertDialog, AlertDialogMessageBinding) -> Unit
    ) {
        val binding = AlertDialogMessageBinding.inflate(LayoutInflater.from(context))
        val dialog = MaterialAlertDialogBuilder(
            context,
            R.style.Theme_SettleX_Dialog_Alert_Rounded16dp
        ).apply {
            setView(binding.root)
            setCancelable(false)
        }.create()

        config(dialog, binding)
        dialog.show()
    }

    fun showSimpleAlertDialog(context: Context, title: String?, message: String) {
        MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert)
            .setCancelable(true)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }.show()
    }

    // Bottom sheet dialogs
    fun showSuccessBottomSheetDialog(
        context: Context,
        config: (BottomSheetDialog, BottomSheetSuccessDialogBinding) -> Unit
    ) {
        val binding = BottomSheetSuccessDialogBinding.inflate(LayoutInflater.from(context))
        val dialog = BottomSheetDialog(context, R.style.Theme_SettleX_Dialog_BottomSheet).apply {
            setContentView(binding.root)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }

        // Blur background on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val rootView = (context as Activity).window.decorView
            rootView.setRenderEffect(RenderEffect.createBlurEffect(5f, 5f, Shader.TileMode.CLAMP))
        }

        binding.anim.playAnimation()
        config(dialog, binding)
        dialog.show()
    }

    fun showBottomSheetImageSource(
        context: Context,
        config: (BottomSheetDialog, BottomSheetImageSourceBinding) -> Unit
    ) {
        val binding = BottomSheetImageSourceBinding.inflate(LayoutInflater.from(context))
        val dialog = BottomSheetDialog(context, R.style.Theme_SettleX_Dialog_BottomSheet).apply {
            setContentView(binding.root)
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }

        config(dialog, binding)
        dialog.show()
    }
}
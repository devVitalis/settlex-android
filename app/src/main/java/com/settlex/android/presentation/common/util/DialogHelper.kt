package com.settlex.android.presentation.common.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
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
import java.util.function.BiConsumer

/**
 * Utility class for common UI operations
 * Provides reusable methods for displaying consistent UI components throughout the application.
 */
object DialogHelper {

    // Alert dialogs
    fun showCustomAlertDialogWithIcon(
        context: Context,
        config: BiConsumer<AlertDialog, AlertDialogWithIconBinding>
    ) {
        val binding = AlertDialogWithIconBinding.inflate(LayoutInflater.from(context))
        val dialog =
            MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert_Rounded16dp)
                .setView(binding.getRoot())
                .setCancelable(false)
                .create()

        config.accept(dialog, binding)
        dialog.show()
    }

    fun showCustomAlertDialog(
        context: Context,
        config: BiConsumer<AlertDialog, AlertDialogMessageBinding>
    ) {
        val binding = AlertDialogMessageBinding.inflate(LayoutInflater.from(context))
        val dialog =
            MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert_Rounded16dp)
                .setView(binding.root)
                .setCancelable(false)
                .create()

        config.accept(dialog, binding)
        dialog.show()
    }

    fun showSimpleAlertDialog(context: Context, title: String?, message: String?) {
        MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert)
            .setCancelable(true)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }


    fun showNoInternetAlertDialog(context: Context) {
        MaterialAlertDialogBuilder(context, R.style.Theme_SettleX_Dialog_Alert)
            .setCancelable(true)
            .setTitle("Network Unavailable")
            .setMessage("Please check your Wi-Fi or cellular data and try again")
            .setPositiveButton(
                "OK"
            ) { dialog: DialogInterface?, i: Int -> dialog!!.dismiss() }
            .show()
    }


    // Bottom sheet dialogs
    fun showSuccessBottomSheetDialog(
        context: Context,
        config: BiConsumer<BottomSheetDialog, BottomSheetSuccessDialogBinding>
    ) {
        val binding = BottomSheetSuccessDialogBinding.inflate(LayoutInflater.from(context))
        val dialog = BottomSheetDialog(context, R.style.Theme_SettleX_Dialog_BottomSheet)
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        // Blur background on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val rootView = (context as Activity).window.decorView
            rootView.setRenderEffect(RenderEffect.createBlurEffect(5f, 5f, Shader.TileMode.CLAMP))
        }

        binding.anim.playAnimation()
        config.accept(dialog, binding)
        dialog.show()
    }

    fun showBottomSheetImageSource(
        context: Context,
        config: BiConsumer<BottomSheetDialog, BottomSheetImageSourceBinding>
    ) {
        val binding = BottomSheetImageSourceBinding.inflate(LayoutInflater.from(context))
        val dialog = BottomSheetDialog(context, R.style.Theme_SettleX_Dialog_BottomSheet).apply {
            setContentView(binding.root)
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }

        config.accept(dialog, binding)
        dialog.show()
    }
}
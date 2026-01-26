package com.settlex.android.presentation.common.components

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.settlex.android.R
import com.settlex.android.databinding.ProgressbarOverlayBinding

class ProgressDialogFragment : DialogFragment() {
    private var _zoomAnimator: AnimatorSet? = null
    private val zoomAnimator get() = _zoomAnimator!!
    private val decorView: View by lazy { activity!!.window.decorView }

    override fun onStart() {
        super.onStart()
        dialog!!.apply {
            this.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_SettleX_Dialog_Transparent)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window!!.requestFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setCanceledOnTouchOutside(false)

            // Intercept back key explicitly
            setOnKeyListener { _, keyCode: Int, _ ->
                keyCode == KeyEvent.KEYCODE_BACK // consume the event, do nothing
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ProgressbarOverlayBinding.inflate(inflater, container, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            decorView.setRenderEffect(
                RenderEffect.createBlurEffect(
                    5f,
                    5f,
                    Shader.TileMode.CLAMP
                )
            )
        }

        val logo = binding.getRoot().findViewById<ImageView>(R.id.iv_logo)

        // Animate logo
        val scaleX = ObjectAnimator.ofFloat<View?>(logo, View.SCALE_X, 1f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat<View?>(logo, View.SCALE_Y, 1f, 1.1f, 1f)

        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleX.repeatMode = ValueAnimator.RESTART
        scaleY.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatMode = ValueAnimator.RESTART

        _zoomAnimator = AnimatorSet()
        zoomAnimator.playTogether(scaleX, scaleY)
        zoomAnimator.setDuration(1000)
        zoomAnimator.start()

        return binding.getRoot()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        decorView.setRenderEffect(null)
        if (zoomAnimator.isRunning()) {
            zoomAnimator.cancel()
        }
    }
}

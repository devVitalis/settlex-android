package com.settlex.android.util.ui

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.settlex.android.presentation.common.components.ProgressDialogFragment

class ProgressDialogManager(activity: FragmentActivity) {
    private val fragmentManager: FragmentManager = activity.supportFragmentManager
    private var progressDialog: ProgressDialogFragment? = null

    fun show() {
        if (progressDialog == null || !progressDialog!!.isVisible) {
            progressDialog = ProgressDialogFragment()
            progressDialog!!.show(fragmentManager, "progress")
        }
    }

    fun hide() {
        if (progressDialog != null) {
            progressDialog!!.dismissAllowingStateLoss()
            progressDialog = null
        }
    }
}

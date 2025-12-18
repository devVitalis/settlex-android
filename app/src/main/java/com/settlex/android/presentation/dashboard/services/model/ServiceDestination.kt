package com.settlex.android.presentation.dashboard.services.model

import android.app.Activity

data class ServiceDestination(
    val activity: Class<out Activity>? = null,
    val navDestinationId: Int? = null
) {
    val isActivity get() = activity != null
    val isFragment get() = navDestinationId != null
}

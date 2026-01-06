package com.settlex.android.presentation.dashboard.services.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.settlex.android.R
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.dashboard.services.adapter.ServicesAdapter.ServiceViewHolder
import com.settlex.android.presentation.dashboard.services.model.ServiceUiModel

/**
 * RecyclerView adapter for displaying services in a grid/list format
 */
class ServicesAdapter(
    private val isHomeDashboard: Boolean,
    private val service: List<ServiceUiModel>,
    private val listener: OnServiceClickedListener
) : RecyclerView.Adapter<ServiceViewHolder>() {

    fun interface OnServiceClickedListener {
        fun onServiceClick(service: ServiceUiModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val layoutId = when {
            isHomeDashboard -> R.layout.item_service_home
            else -> R.layout.item_service_full
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) = with(holder) {
        val service = service[position]

        // Bind service data to views
        tvServiceName.text = service.name
        ivServiceIcon.setImageResource(service.iconResId)

        when {
            service.cashbackPercentage > 0 -> {
                "up to ${service.cashbackPercentage} %".also { holder.tvServiceBadge.text = it }
                tvServiceBadge.show()
            }

            !service.label.isNullOrEmpty() -> tvServiceBadge.text = service.label

            else -> tvServiceBadge.gone()
        }

        itemView.setOnClickListener { listener.onServiceClick(service) }
    }

    override fun getItemCount(): Int {
        return service.size
    }

    class ServiceViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var ivServiceIcon: ImageView = itemView.findViewById(R.id.iv_service_icon)
        var tvServiceName: TextView = itemView.findViewById(R.id.tv_service_name)
        var tvServiceBadge: TextView = itemView.findViewById(R.id.tv_service_badge)
    }
}
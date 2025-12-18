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
    private val isAllServices: Boolean,
    private val service: List<ServiceUiModel>,
    private val listener: OnServiceClickedListener
) : RecyclerView.Adapter<ServiceViewHolder>() {

    fun interface OnServiceClickedListener {
        fun onServiceClick(service: ServiceUiModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val layoutId = if (!isAllServices) R.layout.item_service else R.layout.item_service_all
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) = with(holder) {
        val service = service[position]

        // Bind service data to views
        name.text = service.name
        icon.setImageResource(service.iconResId)

        when {
            service.cashbackPercentage > 0 -> {
                "up to ${service.cashbackPercentage} %".also { holder.badge.text = it }
                badge.show()
            }

            !service.label.isNullOrEmpty() -> badge.text = service.label

            else -> badge.gone()
        }

        itemView.setOnClickListener { listener.onServiceClick(this@ServicesAdapter.service[position]) }
    }

    override fun getItemCount(): Int {
        return service.size
    }

    class ServiceViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var icon: ImageView = itemView.findViewById(R.id.icon)
        var name: TextView = itemView.findViewById(R.id.name)
        var badge: TextView = itemView.findViewById(R.id.badge)
    }
}
package com.settlex.android.ui.dashboard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.settlex.android.R;
import com.settlex.android.ui.dashboard.model.ServiceUiModel;

import java.util.List;

/**
 * RecyclerView adapter for displaying services in a grid/list format
 */
public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder> {
    private final List<ServiceUiModel> service;
    private final onItemClickedListener listener;
    private final boolean useAllService; // true = item_service_all

    public interface onItemClickedListener {
        void onServiceClick(ServiceUiModel service);
    }

    public ServicesAdapter(boolean useAllService, List<ServiceUiModel> service, onItemClickedListener listener) {
        this.useAllService = useAllService;
        this.service = service;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (!useAllService) ? R.layout.item_service : R.layout.item_service_all;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceUiModel service = this.service.get(position);

        // Bind service data to views
        holder.name.setText(service.getName());
        holder.icon.setImageResource(service.getIconResId());

        if (!(service.getCashbackPercentage() < 1)) {
            String CASHBACK = "up to " + service.getCashbackPercentage() + "%";
            holder.cashback.setText(CASHBACK);
            holder.cashback.setVisibility(View.VISIBLE);
        } else {
            holder.cashback.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(view -> listener.onServiceClick(this.service.get(position)));
    }

    @Override
    public int getItemCount() {
        return service.size();
    }

    /**
     * ViewHolder for service items containing icon and name
     */
    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, cashback;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            cashback = itemView.findViewById(R.id.cashback);
        }
    }
}
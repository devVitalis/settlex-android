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
    private final List<ServiceUiModel> serviceList;

    public ServicesAdapter(List<ServiceUiModel> serviceList) {
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_services, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceUiModel service = serviceList.get(position);

        // Bind service data to views
        holder.name.setText(service.getName());
        holder.icon.setImageResource(service.getIconResId());
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    /**
     * ViewHolder for service items containing icon and name
     */
    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
        }
    }
}
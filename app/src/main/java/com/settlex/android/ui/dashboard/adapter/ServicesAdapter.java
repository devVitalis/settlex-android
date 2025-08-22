package com.settlex.android.ui.dashboard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.settlex.android.R;
import com.settlex.android.ui.dashboard.model.ServicesModel;

import java.util.List;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder> {

    private List<ServicesModel> serviceList;

    public ServicesAdapter(List<ServicesModel> serviceList) {
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_services, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServicesModel service = serviceList.get(position);
        holder.serviceName.setText(service.getName());
        holder.serviceIcon.setImageResource(service.getIconResId());
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView serviceIcon;
        TextView serviceName;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceIcon = itemView.findViewById(R.id.serviceIcon);
            serviceName = itemView.findViewById(R.id.serviceName);
        }
    }
}


package com.motor.diagnostic.presentation.ui.motorcycle.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.motor.diagnostic.R;
import com.motor.diagnostic.domain.model.Motorcycle;

import java.util.List;

/**
 * Adapter for displaying motorcycle items in a RecyclerView
 */
public class MotorcycleAdapter extends RecyclerView.Adapter<MotorcycleAdapter.ViewHolder> {

    private List<Motorcycle> motorcycles;
    private final OnMotorcycleClickListener listener;

    /**
     * Interface for handling motorcycle item clicks
     */
    public interface OnMotorcycleClickListener {
        void onMotorcycleClick(Motorcycle motorcycle);
    }

    public MotorcycleAdapter(List<Motorcycle> motorcycles, OnMotorcycleClickListener listener) {
        this.motorcycles = motorcycles;
        this.listener = listener;
    }

    /**
     * Update the list of motorcycles and refresh the adapter
     * @param motorcycles new list of motorcycles
     */
    public void updateMotorcycles(List<Motorcycle> motorcycles) {
        this.motorcycles = motorcycles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_motorcycle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Motorcycle motorcycle = motorcycles.get(position);
        holder.bind(motorcycle, listener);
    }

    @Override
    public int getItemCount() {
        return motorcycles.size();
    }

    /**
     * ViewHolder for motorcycle items
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMotorcycleName;
        private final TextView tvMotorcycleModel;
        private final TextView tvMotorcycleYear;
        private final ImageView ivMotorcycle;
        private final View connectionStatusIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMotorcycleName = itemView.findViewById(R.id.tv_motorcycle_name);
            tvMotorcycleModel = itemView.findViewById(R.id.tv_motorcycle_model);
            tvMotorcycleYear = itemView.findViewById(R.id.tv_motorcycle_year);
            ivMotorcycle = itemView.findViewById(R.id.iv_motorcycle);
            connectionStatusIndicator = itemView.findViewById(R.id.connection_status_indicator);
        }

        public void bind(Motorcycle motorcycle, OnMotorcycleClickListener listener) {
            tvMotorcycleName.setText(motorcycle.getNickname());
            tvMotorcycleModel.setText(motorcycle.getModel());
            tvMotorcycleYear.setText(String.valueOf(motorcycle.getYear()));
            
            // Set connection status indicator color
            connectionStatusIndicator.setBackgroundResource(
                    motorcycle.isConnected() ? R.drawable.connected_indicator : R.drawable.disconnected_indicator);
            
            // Load motorcycle image using Glide if available
            if (motorcycle.getImageUrl() != null && !motorcycle.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(motorcycle.getImageUrl())
                        .placeholder(R.drawable.placeholder_motorcycle)
                        .error(R.drawable.placeholder_motorcycle)
                        .into(ivMotorcycle);
            } else {
                // Load default image
                ivMotorcycle.setImageResource(R.drawable.placeholder_motorcycle);
            }
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMotorcycleClick(motorcycle);
                }
            });
        }
    }
} 
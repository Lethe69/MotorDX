package com.motor.diagnostic.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.motor.diagnostic.R;
import com.motor.diagnostic.domain.model.Notification;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying notifications in a RecyclerView
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    
    private List<Notification> notifications;
    private final OnNotificationClickListener notificationClickListener;
    private final OnDeleteClickListener deleteClickListener;
    private final SimpleDateFormat dateFormat;
    
    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }
    
    public interface OnDeleteClickListener {
        void onDeleteClick(String notificationId);
    }
    
    public NotificationAdapter(List<Notification> notifications, 
                              OnNotificationClickListener notificationClickListener,
                              OnDeleteClickListener deleteClickListener) {
        this.notifications = notifications;
        this.notificationClickListener = notificationClickListener;
        this.deleteClickListener = deleteClickListener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }
    
    @Override
    public int getItemCount() {
        return notifications.size();
    }
    
    public void updateNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }
    
    class NotificationViewHolder extends RecyclerView.ViewHolder {
        
        private final TextView tvTitle;
        private final TextView tvMessage;
        private final TextView tvTimestamp;
        private final ImageView ivNotificationType;
        private final ImageView btnDelete;
        private final View unreadIndicator;
        
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTimestamp = itemView.findViewById(R.id.tvNotificationTime);
            ivNotificationType = itemView.findViewById(R.id.ivNotificationType);
            btnDelete = itemView.findViewById(R.id.btnDeleteNotification);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }
        
        public void bind(Notification notification) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());
            
            if (notification.getTimestamp() != null) {
                tvTimestamp.setText(dateFormat.format(notification.getTimestamp()));
            } else {
                tvTimestamp.setText("");
            }
            
            // Set appropriate icon based on notification type
            switch (notification.getType()) {
                case SUCCESS:
                    ivNotificationType.setImageResource(R.drawable.ic_notification);
                    break;
                case ERROR:
                    ivNotificationType.setImageResource(R.drawable.ic_notification);
                    break;
                case WARNING:
                    ivNotificationType.setImageResource(R.drawable.ic_notification);
                    break;
                case INFO:
                default:
                    ivNotificationType.setImageResource(R.drawable.ic_notification);
                    break;
            }
            
            // Show/hide unread indicator
            unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
            
            // Set up click listeners
            itemView.setOnClickListener(v -> {
                notificationClickListener.onNotificationClick(notification);
                notifyItemChanged(getAdapterPosition());
            });
            
            btnDelete.setOnClickListener(v -> {
                if (deleteClickListener != null && notification.getId() != null) {
                    deleteClickListener.onDeleteClick(notification.getId());
                }
            });
        }
    }
} 
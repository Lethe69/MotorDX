package com.motor.diagnostic.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.motor.diagnostic.domain.model.Notification;
import com.motor.diagnostic.domain.repository.NotificationRepository;
import com.motor.diagnostic.presentation.di.ViewModelModule;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for notification operations
 */
public class NotificationViewModel extends ViewModel {
    
    private final NotificationRepository notificationRepository;
    
    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public NotificationViewModel() {
        // Get repository instance through dependency injection
        this.notificationRepository = ViewModelModule.provideNotificationRepository();
    }
    
    /**
     * Load notifications for the current user
     */
    public void loadNotifications() {
        loading.setValue(true);
        
        notificationRepository.getUserNotifications(50)
                .thenAccept(notificationList -> {
                    notifications.postValue(notificationList);
                    loading.postValue(false);
                })
                .exceptionally(e -> {
                    errorMessage.postValue(e.getMessage());
                    loading.postValue(false);
                    return null;
                });
    }
    
    /**
     * Mark a notification as read
     * @param notificationId ID of the notification
     */
    public void markAsRead(String notificationId) {
        notificationRepository.markNotificationAsRead(notificationId)
                .thenAccept(success -> {
                    if (success) {
                        updateNotificationReadStatus(notificationId);
                    }
                })
                .exceptionally(e -> {
                    errorMessage.postValue(e.getMessage());
                    return null;
                });
    }
    
    /**
     * Mark all notifications as read
     */
    public void markAllAsRead() {
        notificationRepository.markAllNotificationsAsRead()
                .thenAccept(success -> {
                    if (success) {
                        updateAllNotificationsReadStatus();
                    }
                })
                .exceptionally(e -> {
                    errorMessage.postValue(e.getMessage());
                    return null;
                });
    }
    
    /**
     * Delete a notification
     * @param notificationId ID of the notification
     */
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteNotification(notificationId)
                .thenAccept(success -> {
                    if (success) {
                        removeNotificationFromList(notificationId);
                    }
                })
                .exceptionally(e -> {
                    errorMessage.postValue(e.getMessage());
                    return null;
                });
    }
    
    /**
     * Delete all notifications
     */
    public void deleteAllNotifications() {
        loading.setValue(true);
        
        notificationRepository.deleteAllNotifications()
                .thenAccept(success -> {
                    if (success) {
                        notifications.postValue(new ArrayList<>());
                    }
                    loading.postValue(false);
                })
                .exceptionally(e -> {
                    errorMessage.postValue(e.getMessage());
                    loading.postValue(false);
                    return null;
                });
    }
    
    // Helper methods for updating the UI state
    
    private void updateNotificationReadStatus(String notificationId) {
        List<Notification> currentNotifications = notifications.getValue();
        if (currentNotifications != null) {
            List<Notification> updatedNotifications = new ArrayList<>(currentNotifications);
            
            for (int i = 0; i < updatedNotifications.size(); i++) {
                Notification notification = updatedNotifications.get(i);
                if (notification.getId().equals(notificationId)) {
                    notification.setRead(true);
                    updatedNotifications.set(i, notification);
                    break;
                }
            }
            
            notifications.postValue(updatedNotifications);
        }
    }
    
    private void updateAllNotificationsReadStatus() {
        List<Notification> currentNotifications = notifications.getValue();
        if (currentNotifications != null) {
            List<Notification> updatedNotifications = new ArrayList<>();
            
            for (Notification notification : currentNotifications) {
                notification.setRead(true);
                updatedNotifications.add(notification);
            }
            
            notifications.postValue(updatedNotifications);
        }
    }
    
    private void removeNotificationFromList(String notificationId) {
        List<Notification> currentNotifications = notifications.getValue();
        if (currentNotifications != null) {
            List<Notification> updatedNotifications = new ArrayList<>(currentNotifications);
            
            updatedNotifications.removeIf(notification -> 
                    notification.getId() != null && notification.getId().equals(notificationId));
            
            notifications.postValue(updatedNotifications);
        }
    }
    
    // Getters for LiveData
    
    public LiveData<List<Notification>> getNotifications() {
        return notifications;
    }
    
    public LiveData<Boolean> getLoading() {
        return loading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up any resources if needed
    }
} 
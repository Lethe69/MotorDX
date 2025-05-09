package com.motor.diagnostic.presentation.ui.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.motor.diagnostic.databinding.FragmentNotificationsBinding;
import com.motor.diagnostic.domain.model.Notification;
import com.motor.diagnostic.presentation.adapter.NotificationAdapter;
import com.motor.diagnostic.presentation.viewmodel.NotificationViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying user notifications
 */
public class NotificationsFragment extends Fragment {
    
    private static final String TAG = "NotificationsFragment";
    
    private FragmentNotificationsBinding binding;
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            initializeViewModel();
            setupRecyclerView();
            setupClickListeners();
            observeViewModel();
            
            // Load notifications
            viewModel.loadNotifications();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing fragment", e);
            if (isAdded()) {
                Toast.makeText(requireContext(), "Error loading notifications", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
    }
    
    private void setupRecyclerView() {
        adapter = new NotificationAdapter(new ArrayList<>(), notification -> {
            // Handle notification click
            viewModel.markAsRead(notification.getId());
        }, notificationId -> {
            // Handle delete click
            viewModel.deleteNotification(notificationId);
        });
        
        binding.recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewNotifications.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        binding.btnClearAll.setOnClickListener(v -> {
            viewModel.deleteAllNotifications();
        });
    }
    
    private void observeViewModel() {
        // Observe notifications
        viewModel.getNotifications().observe(getViewLifecycleOwner(), this::updateNotifications);
        
        // Observe loading state
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe error message
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty() && isAdded()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void updateNotifications(List<Notification> notifications) {
        if (notifications.isEmpty()) {
            binding.tvEmptyNotifications.setVisibility(View.VISIBLE);
            binding.recyclerViewNotifications.setVisibility(View.GONE);
        } else {
            binding.tvEmptyNotifications.setVisibility(View.GONE);
            binding.recyclerViewNotifications.setVisibility(View.VISIBLE);
            adapter.updateNotifications(notifications);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 
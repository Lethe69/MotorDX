package com.motor.diagnostic.presentation.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.motor.diagnostic.R;
import com.motor.diagnostic.data.util.PreferencesManager;
import com.motor.diagnostic.data.util.ThemeManager;
import com.motor.diagnostic.databinding.FragmentSettingsBinding;

/**
 * Fragment for application settings
 */
public class SettingsFragment extends Fragment {
    
    private FragmentSettingsBinding binding;
    private NavController navController;
    private PreferencesManager preferencesManager;
    private static final String TAG = "SettingsFragment";
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            binding = FragmentSettingsBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            Log.e(TAG, "Error inflating settings layout", e);
            Toast.makeText(requireContext(), "Error creating settings view", Toast.LENGTH_SHORT).show();
            return new View(requireContext());
        }
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            // Initialize PreferencesManager
            preferencesManager = PreferencesManager.getInstance(requireContext());
            
            // Initialize NavController
            navController = Navigation.findNavController(view);
            
            // Load saved preferences
            loadSavedPreferences();
            
            // Set up click listeners
            setupClickListeners();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            Toast.makeText(requireContext(), "Error loading settings", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadSavedPreferences() {
        try {
            // Set switches based on saved preferences
            binding.switchNotifications.setChecked(preferencesManager.isNotificationsEnabled());
            binding.switchDarkTheme.setChecked(preferencesManager.isDarkThemeEnabled());
            binding.switchDataSync.setChecked(preferencesManager.isDataSyncEnabled());
        } catch (Exception e) {
            Log.e(TAG, "Error loading saved preferences", e);
        }
    }
    
    private void setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> navigateBack());
        
        // Toggle for notifications
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                // Only process if this is a user change (not loading saved settings)
                preferencesManager.setNotificationsEnabled(isChecked);
                String status = isChecked ? "enabled" : "disabled";
                Toast.makeText(requireContext(), "Notifications " + status, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error toggling notifications", e);
            }
        });
        
        // Toggle for dark theme
        binding.switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                // Save preference first
                preferencesManager.setDarkThemeEnabled(isChecked);
                
                // Let the user know the change will take effect after restart
                String status = isChecked ? "enabled" : "disabled";
                Toast.makeText(requireContext(), "Dark theme " + status + ", restarting app...", Toast.LENGTH_SHORT).show();
                
                // Use a handler with a longer delay to ensure changes are saved
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        // Use the ThemeManager to safely restart the app
                        ThemeManager.restartApp(requireActivity());
                    } catch (Exception e) {
                        Log.e(TAG, "Error restarting app after theme change", e);
                        Toast.makeText(requireContext(), "Error changing theme. Please restart the app manually.", Toast.LENGTH_LONG).show();
                    }
                }, 500); // Use a longer delay of 500ms to ensure preferences are saved
                
            } catch (Exception e) {
                Log.e(TAG, "Error toggling dark theme", e);
                Toast.makeText(requireContext(), "Error changing theme: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Reset the switch to previous state to avoid confusion
                binding.switchDarkTheme.setChecked(!isChecked);
            }
        });
        
        // Toggle for data sync
        binding.switchDataSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                preferencesManager.setDataSyncEnabled(isChecked);
                String status = isChecked ? "enabled" : "disabled";
                Toast.makeText(requireContext(), "Data synchronization " + status, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error toggling data sync", e);
            }
        });
        
        // About button
        binding.btnAbout.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "MotorDX v1.0.0", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void navigateBack() {
        try {
            navController.navigateUp();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating back", e);
            requireActivity().onBackPressed();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 
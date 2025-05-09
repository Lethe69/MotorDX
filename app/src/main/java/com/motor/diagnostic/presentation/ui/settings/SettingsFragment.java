package com.motor.diagnostic.presentation.ui.settings;

import android.os.Bundle;
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
import com.motor.diagnostic.databinding.FragmentSettingsBinding;

/**
 * Fragment for application settings
 */
public class SettingsFragment extends Fragment {
    
    private FragmentSettingsBinding binding;
    private NavController navController;
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
            // Initialize NavController
            navController = Navigation.findNavController(view);
            
            // Set up click listeners
            setupClickListeners();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            Toast.makeText(requireContext(), "Error loading settings", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> navigateBack());
        
        // Toggle for notifications
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "enabled" : "disabled";
            Toast.makeText(requireContext(), "Notifications " + status, Toast.LENGTH_SHORT).show();
        });
        
        // Toggle for dark theme
        binding.switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "enabled" : "disabled";
            Toast.makeText(requireContext(), "Dark theme " + status, Toast.LENGTH_SHORT).show();
        });
        
        // Toggle for data sync
        binding.switchDataSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "enabled" : "disabled";
            Toast.makeText(requireContext(), "Data synchronization " + status, Toast.LENGTH_SHORT).show();
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
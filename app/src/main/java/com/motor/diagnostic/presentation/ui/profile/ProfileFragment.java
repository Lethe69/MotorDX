package com.motor.diagnostic.presentation.ui.profile;

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

import com.motor.diagnostic.R;
import com.motor.diagnostic.databinding.FragmentProfileBinding;
import com.motor.diagnostic.presentation.di.ViewModelModule;
import com.motor.diagnostic.presentation.viewmodel.AuthViewModel;

/**
 * Fragment for displaying user profile information
 */
public class ProfileFragment extends Fragment {
    
    private FragmentProfileBinding binding;
    private AuthViewModel viewModel;
    private static final String TAG = "ProfileFragment";
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            binding = FragmentProfileBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            Log.e(TAG, "Error inflating profile layout", e);
            Toast.makeText(requireContext(), "Error creating profile view", Toast.LENGTH_SHORT).show();
            return new View(requireContext());
        }
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            // Initialize ViewModel
            viewModel = new ViewModelProvider(requireActivity(), ViewModelModule.provideViewModelFactory())
                    .get(AuthViewModel.class);
            
            // Set up UI with current user data
            setupUI();
            
            // Set up click listeners
            setupClickListeners();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            Toast.makeText(requireContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupUI() {
        // Get current user
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.tvUserName.setText(user.getFullName());
                binding.tvUserEmail.setText(user.getEmail());
                
                // Load user profile image if available
                if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                    // For now, just use a placeholder
                    binding.ivUserProfile.setImageResource(R.drawable.default_profile);
                }
            }
        });
    }
    
    private void setupClickListeners() {
        binding.btnEditProfile.setOnClickListener(v -> {
            // Navigate to edit profile screen
            // Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment);
            Toast.makeText(requireContext(), "Edit Profile - Feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        binding.btnNotifications.setOnClickListener(v -> {
            // Navigate to notifications screen
            // Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_notificationsFragment);
            Toast.makeText(requireContext(), "Notifications - Feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        binding.btnSettings.setOnClickListener(v -> {
            // Navigate to settings screen
            // Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_settingsFragment);
            Toast.makeText(requireContext(), "Settings - Feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        binding.btnLogout.setOnClickListener(v -> {
            // Log out the user
            viewModel.logout();
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 
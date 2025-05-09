package com.motor.diagnostic.presentation.ui.profile;

import android.content.Intent;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.motor.diagnostic.R;
import com.motor.diagnostic.databinding.FragmentProfileBinding;
import com.motor.diagnostic.presentation.di.ViewModelModule;
import com.motor.diagnostic.presentation.ui.authentication.LoginActivity;
import com.motor.diagnostic.presentation.viewmodel.AuthViewModel;

/**
 * Fragment for displaying user profile information
 */
public class ProfileFragment extends Fragment {
    
    private FragmentProfileBinding binding;
    private AuthViewModel viewModel;
    private NavController navController;
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
            // Initialize NavController
            navController = Navigation.findNavController(view);
            
            // Initialize ViewModel
            viewModel = new ViewModelProvider(requireActivity(), ViewModelModule.provideViewModelFactory())
                    .get(AuthViewModel.class);
            
            // Set up UI with current user data
            setupUI();
            
            // Set up click listeners
            setupClickListeners();
            
            // Observe loading status
            viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            });
            
            // Observe error messages
            viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
                if (error != null && !error.isEmpty()) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
            
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
            try {
                navController.navigate(R.id.action_profileFragment_to_editProfileFragment);
            } catch (Exception e) {
                Log.e(TAG, "Navigation to Edit Profile failed", e);
                Toast.makeText(requireContext(), "Navigation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnNotifications.setOnClickListener(v -> {
            // Navigate to notifications screen
            try {
                navController.navigate(R.id.action_profileFragment_to_notificationsFragment);
            } catch (Exception e) {
                Log.e(TAG, "Navigation to Notifications failed", e);
                Toast.makeText(requireContext(), "Navigation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnSettings.setOnClickListener(v -> {
            // Navigate to settings screen
            try {
                navController.navigate(R.id.action_profileFragment_to_settingsFragment);
            } catch (Exception e) {
                Log.e(TAG, "Navigation to Settings failed", e);
                Toast.makeText(requireContext(), "Navigation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnLogout.setOnClickListener(v -> {
            // Show loading
            binding.progressBar.setVisibility(View.VISIBLE);
            
            // Log out the user
            viewModel.logout();
            
            // Observe changes in user login status
            viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                if (user == null) {
                    // User is logged out, navigate to login screen
                    navigateToLogin();
                }
            });
            
            // Add a direct check for logged-in status after logout call
            if (!viewModel.isUserLoggedIn()) {
                navigateToLogin();
            }
        });
    }
    
    private void navigateToLogin() {
        try {
            Intent loginIntent = new Intent(requireActivity(), LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            requireActivity().finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to login", e);
            Toast.makeText(requireContext(), "Error logging out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 
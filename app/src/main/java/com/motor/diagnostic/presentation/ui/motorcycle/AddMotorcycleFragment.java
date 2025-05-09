package com.motor.diagnostic.presentation.ui.motorcycle;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.motor.diagnostic.R;
import com.motor.diagnostic.databinding.FragmentAddMotorcycleBinding;
import com.motor.diagnostic.domain.model.Motorcycle;
import com.motor.diagnostic.presentation.di.ViewModelModule;
import com.motor.diagnostic.presentation.viewmodel.MotorcycleViewModel;

/**
 * Fragment for adding a new motorcycle
 */
public class AddMotorcycleFragment extends Fragment {
    
    private FragmentAddMotorcycleBinding binding;
    private MotorcycleViewModel viewModel;
    private NavController navController;
    private Uri selectedImageUri;
    private static final String TAG = "AddMotorcycleFragment";
    
    // Image picker
    private final ActivityResultLauncher<String> imagePickerLauncher = 
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.ivMotorcycle.setImageURI(uri);
                }
            });
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            binding = FragmentAddMotorcycleBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            Log.e(TAG, "Error inflating add motorcycle layout", e);
            Toast.makeText(requireContext(), "Error creating add motorcycle view", Toast.LENGTH_SHORT).show();
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
                    .get(MotorcycleViewModel.class);
            
            // Setup click listeners
            setupClickListeners();
            
            // Observe ViewModel
            observeViewModel();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            Toast.makeText(requireContext(), "Error initializing add motorcycle screen", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> navigateBack());
        
        // Add photo button
        binding.btnAddPhoto.setOnClickListener(v -> openImagePicker());
        
        // Add motorcycle button
        binding.btnAddMotorcycle.setOnClickListener(v -> validateAndAddMotorcycle());
    }
    
    private void observeViewModel() {
        // Observe loading state
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnAddMotorcycle.setEnabled(!isLoading);
        });
        
        // Observe error message
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty() && isAdded()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        
        // Observe add motorcycle result
        viewModel.getAddMotorcycleResult().observe(getViewLifecycleOwner(), success -> {
            if (success && isAdded()) {
                Toast.makeText(requireContext(), "Motorcycle added successfully", Toast.LENGTH_SHORT).show();
                navigateBack();
            }
        });
    }
    
    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }
    
    private void validateAndAddMotorcycle() {
        // Get input values
        String nickname = binding.etNickname.getText().toString().trim();
        String make = binding.etMake.getText().toString().trim();
        String model = binding.etModel.getText().toString().trim();
        boolean isConnected = binding.switchConnect.isChecked();
        boolean termsAccepted = binding.cbTerms.isChecked();
        
        // Validate input
        if (nickname.isEmpty()) {
            binding.tilNickname.setError("Nickname is required");
            return;
        }
        
        if (make.isEmpty()) {
            binding.tilMake.setError("Make is required");
            return;
        }
        
        if (model.isEmpty()) {
            binding.tilModel.setError("Model is required");
            return;
        }
        
        if (!termsAccepted) {
            Toast.makeText(requireContext(), "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Clear errors
        binding.tilNickname.setError(null);
        binding.tilMake.setError(null);
        binding.tilModel.setError(null);
        
        // Create motorcycle object
        Motorcycle motorcycle = new Motorcycle();
        motorcycle.setNickname(nickname);
        motorcycle.setMake(make);
        motorcycle.setModel(model);
        motorcycle.setConnected(isConnected);
        
        // Add motorcycle
        if (selectedImageUri != null) {
            viewModel.addMotorcycle(motorcycle, selectedImageUri);
        } else {
            viewModel.addMotorcycle(motorcycle, null);
        }
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
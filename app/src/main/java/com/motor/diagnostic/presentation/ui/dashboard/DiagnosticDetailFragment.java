package com.motor.diagnostic.presentation.ui.dashboard;

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
import com.motor.diagnostic.databinding.FragmentDiagnosticDetailBinding;
import com.motor.diagnostic.domain.model.DiagnosticData;
import com.motor.diagnostic.presentation.di.ViewModelModule;
import com.motor.diagnostic.presentation.viewmodel.DashboardViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Fragment for displaying detailed diagnostic information based on type
 */
public class DiagnosticDetailFragment extends Fragment {
    
    private FragmentDiagnosticDetailBinding binding;
    private DashboardViewModel viewModel;
    private NavController navController;
    private static final String TAG = "DiagnosticDetailFrag";
    private String diagnosticType;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            binding = FragmentDiagnosticDetailBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            Log.e(TAG, "Error inflating diagnostic detail layout", e);
            Toast.makeText(requireContext(), "Error creating detail view", Toast.LENGTH_SHORT).show();
            return new View(requireContext());
        }
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            // Initialize NavController
            navController = Navigation.findNavController(view);
            
            // Get diagnostic type from arguments
            Bundle args = getArguments();
            if (args != null) {
                diagnosticType = args.getString("diagnosticType", "battery");
            } else {
                diagnosticType = "battery"; // Default fallback
            }
            
            // Initialize ViewModel
            viewModel = new ViewModelProvider(requireActivity(), ViewModelModule.provideViewModelFactory())
                    .get(DashboardViewModel.class);
            
            // Setup back button
            binding.btnBack.setOnClickListener(v -> navigateBack());
            
            // Configure UI based on diagnostic type
            setupDiagnosticTypeUI();
            
            // Observe current diagnostic data
            observeViewModel();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            Toast.makeText(requireContext(), "Error loading diagnostic detail", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupDiagnosticTypeUI() {
        // Set title and icon based on diagnostic type
        switch (diagnosticType) {
            case "battery":
                binding.tvTitle.setText(R.string.battery_detail_title);
                binding.tvDiagnosticTitle.setText(R.string.battery_status);
                binding.ivDiagnosticIcon.setImageResource(R.drawable.icon_battery);
                
                // Set parameters labels
                binding.tvParam1Label.setText(R.string.battery_voltage);
                binding.tvParam2Label.setText(R.string.battery_health);
                binding.tvParam3Label.setText(R.string.charging_status);
                break;
                
            case "oil":
                binding.tvTitle.setText(R.string.oil_detail_title);
                binding.tvDiagnosticTitle.setText(R.string.oil_status);
                binding.ivDiagnosticIcon.setImageResource(R.drawable.icon_oil);
                
                // Set parameters labels
                binding.tvParam1Label.setText(R.string.oil_level);
                binding.tvParam2Label.setText(R.string.oil_quality);
                binding.tvParam3Label.setText(R.string.oil_temperature);
                break;
                
            case "engine":
                binding.tvTitle.setText(R.string.engine_detail_title);
                binding.tvDiagnosticTitle.setText(R.string.engine_status);
                binding.ivDiagnosticIcon.setImageResource(R.drawable.icon_engine);
                
                // Set parameters labels
                binding.tvParam1Label.setText(R.string.engine_rpm);
                binding.tvParam2Label.setText(R.string.throttle_position);
                binding.tvParam3Label.setText(R.string.engine_load);
                break;
                
            case "temperature":
                binding.tvTitle.setText(R.string.temperature_detail_title);
                binding.tvDiagnosticTitle.setText(R.string.temperature_status);
                binding.ivDiagnosticIcon.setImageResource(R.drawable.icon_temperature);
                
                // Set parameters labels
                binding.tvParam1Label.setText(R.string.engine_temperature);
                binding.tvParam2Label.setText(R.string.ambient_temperature);
                binding.tvParam3Label.setText(R.string.coolant_temperature);
                break;
                
            default:
                binding.tvTitle.setText(R.string.diagnostic_details);
                binding.tvDiagnosticTitle.setText(R.string.unknown_status);
                binding.ivDiagnosticIcon.setImageResource(R.drawable.icon_dashboard);
                break;
        }
    }
    
    private void observeViewModel() {
        // Observe current diagnostic data
        viewModel.getCurrentDiagnosticData().observe(getViewLifecycleOwner(), this::updateDiagnosticUI);
        
        // Observe loading state
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty() && isAdded()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void updateDiagnosticUI(DiagnosticData diagnosticData) {
        if (!isAdded() || binding == null || diagnosticData == null) return;
        
        try {
            // Update last updated time
            if (diagnosticData.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
                binding.tvLastUpdated.setText(getString(R.string.last_updated, sdf.format(diagnosticData.getTimestamp())));
            }
            
            // Update parameters based on diagnostic type
            switch (diagnosticType) {
                case "battery":
                    updateBatteryDiagnostics(diagnosticData);
                    break;
                    
                case "oil":
                    updateOilDiagnostics(diagnosticData);
                    break;
                    
                case "engine":
                    updateEngineDiagnostics(diagnosticData);
                    break;
                    
                case "temperature":
                    updateTemperatureDiagnostics(diagnosticData);
                    break;
                    
                default:
                    // Just use some default values
                    binding.tvStatusValue.setText(R.string.status_unknown);
                    binding.tvStatusDescription.setText(R.string.status_unknown_description);
                    binding.tvParam1Value.setText(R.string.not_available);
                    binding.tvParam2Value.setText(R.string.not_available);
                    binding.tvParam3Value.setText(R.string.not_available);
                    binding.tvRecommendationsContent.setText(R.string.default_recommendation);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating diagnostic UI", e);
        }
    }
    
    private void updateBatteryDiagnostics(DiagnosticData data) {
        // For a real implementation, you'd use actual values from the diagnostic data
        // For now, we'll use dummy values
        binding.tvStatusValue.setText(R.string.status_good);
        binding.tvStatusDescription.setText(R.string.battery_status_good_description);
        
        binding.tvParam1Value.setText("12.7V");
        binding.tvParam2Value.setText(R.string.health_good);
        binding.tvParam3Value.setText(R.string.charging_normal);
        
        binding.tvRecommendationsContent.setText(R.string.battery_recommendation);
    }
    
    private void updateOilDiagnostics(DiagnosticData data) {
        binding.tvStatusValue.setText(R.string.status_good);
        binding.tvStatusDescription.setText(R.string.oil_status_good_description);
        
        binding.tvParam1Value.setText("80%");
        binding.tvParam2Value.setText(R.string.quality_good);
        binding.tvParam3Value.setText("85°C");
        
        binding.tvRecommendationsContent.setText(R.string.oil_recommendation);
    }
    
    private void updateEngineDiagnostics(DiagnosticData data) {
        binding.tvStatusValue.setText(R.string.status_good);
        binding.tvStatusDescription.setText(R.string.engine_status_good_description);
        
        binding.tvParam1Value.setText("1200 RPM");
        binding.tvParam2Value.setText("15%");
        binding.tvParam3Value.setText("30%");
        
        binding.tvRecommendationsContent.setText(R.string.engine_recommendation);
    }
    
    private void updateTemperatureDiagnostics(DiagnosticData data) {
        binding.tvStatusValue.setText(R.string.status_good);
        binding.tvStatusDescription.setText(R.string.temperature_status_good_description);
        
        binding.tvParam1Value.setText("90°C");
        binding.tvParam2Value.setText("28°C");
        binding.tvParam3Value.setText("85°C");
        
        binding.tvRecommendationsContent.setText(R.string.temperature_recommendation);
    }
    
    private void navigateBack() {
        try {
            navController.navigateUp();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating back", e);
            // Fallback if navigation fails
            requireActivity().onBackPressed();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 
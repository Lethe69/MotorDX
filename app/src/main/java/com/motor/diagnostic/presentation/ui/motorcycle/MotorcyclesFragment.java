package com.motor.diagnostic.presentation.ui.motorcycle;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.motor.diagnostic.R;
import com.motor.diagnostic.databinding.FragmentMotorcyclesBinding;
import com.motor.diagnostic.domain.model.Motorcycle;
import com.motor.diagnostic.presentation.di.ViewModelModule;
import com.motor.diagnostic.presentation.ui.motorcycle.adapter.MotorcycleAdapter;
import com.motor.diagnostic.presentation.util.NavigationUtil;
import com.motor.diagnostic.presentation.viewmodel.MotorcycleViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying and managing user's motorcycles
 */
public class MotorcyclesFragment extends Fragment {

    private FragmentMotorcyclesBinding binding;
    private MotorcycleViewModel viewModel;
    private MotorcycleAdapter motorcycleAdapter;
    private NavController navController;
    private static final String TAG = "MotorcyclesFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            binding = FragmentMotorcyclesBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            Log.e(TAG, "Error inflating motorcycles layout", e);
            Toast.makeText(requireContext(), "Error creating motorcycles view", Toast.LENGTH_SHORT).show();
            // Return an empty view to avoid crashes
            return new View(requireContext());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Initialize NavController
            navController = NavigationUtil.initNavController(this);

            // Initialize ViewModel
            try {
                viewModel = new ViewModelProvider(requireActivity(), ViewModelModule.provideViewModelFactory())
                        .get(MotorcycleViewModel.class);

                // Setup RecyclerView
                setupRecyclerView();

                // Setup click listeners
                setupClickListeners();

                // Observe ViewModel data
                observeViewModel();

                // Load motorcycles
                viewModel.loadUserMotorcycles();
            } catch (Exception e) {
                Log.e(TAG, "Error initializing ViewModel", e);
                Toast.makeText(requireContext(), "Error loading motorcycle data", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in onViewCreated", e);
        }
    }

    private void setupRecyclerView() {
        motorcycleAdapter = new MotorcycleAdapter(new ArrayList<>(), motorcycle -> {
            // Handle motorcycle selection
            viewModel.selectMotorcycle(motorcycle);
            // Navigate back to dashboard
            if (navController != null && navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            }
        });

        binding.recyclerViewMotorcycles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewMotorcycles.setAdapter(motorcycleAdapter);
    }

    private void setupClickListeners() {
        // Add motorcycle button
        binding.fabAddMotorcycle.setOnClickListener(v -> {
            NavigationUtil.navigateSafely(navController, R.id.action_motorcyclesFragment_to_addMotorcycleFragment, requireContext());
        });
    }

    private void observeViewModel() {
        // Observe motorcycles list
        viewModel.getUserMotorcycles().observe(getViewLifecycleOwner(), this::updateMotorcyclesList);

        // Observe loading state
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateMotorcyclesList(List<Motorcycle> motorcycles) {
        if (motorcycles == null || motorcycles.isEmpty()) {
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.recyclerViewMotorcycles.setVisibility(View.GONE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
            binding.recyclerViewMotorcycles.setVisibility(View.VISIBLE);
            motorcycleAdapter.updateMotorcycles(motorcycles);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 
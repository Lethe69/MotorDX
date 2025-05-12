package com.motor.diagnostic.presentation.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.motor.diagnostic.R;
import com.motor.diagnostic.databinding.ActivityMainBinding;
import com.motor.diagnostic.presentation.di.ViewModelModule;
import com.motor.diagnostic.presentation.ui.authentication.LoginActivity;
import com.motor.diagnostic.presentation.viewmodel.AuthViewModel;
import com.motor.diagnostic.presentation.viewmodel.DashboardViewModel;
import com.motor.diagnostic.data.util.FirebaseHelper;
import com.motor.diagnostic.data.util.ThemeManager;
import com.motor.diagnostic.data.util.PreferencesManager;

/**
 * Main activity that hosts the navigation components and bottom navigation
 */
public class MainActivity extends AppCompatActivity {
    
    private ActivityMainBinding binding;
    private NavController navController;
    private DashboardViewModel viewModel;
    private AuthViewModel authViewModel;
    private static final String TAG = "MainActivity";
    private static final int MAX_RETRY_COUNT = 3;
    private int navigationInitRetryCount = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            // Make sure we have the correct theme from the beginning
            boolean isDarkTheme = PreferencesManager.getInstance(this).isDarkThemeEnabled();
            
            // Apply theme at the right time in the activity lifecycle - must be before super.onCreate()
            if (isDarkTheme) {
                setTheme(R.style.Theme_MotorDiagnostic_Dark);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                setTheme(R.style.Theme_MotorDiagnostic);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            
            // Call super AFTER theme is set
            super.onCreate(savedInstanceState);
            
            // Initialize binding
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            
            // Check if this is the ESP32 demo mode (from Intent)
            boolean isEsp32DemoFromIntent = getIntent().getBooleanExtra("IS_ESP32_DEMO", false);
            
            // Also check from saved credentials as backup
            boolean isEsp32Demo = isEsp32DemoFromIntent || FirebaseHelper.isEsp32DemoUser(this);
            
            if (isEsp32Demo) {
                // LOG THIS CLEARLY
                Log.d(TAG, "============================================");
                Log.d(TAG, "ESP32 DEMO MODE ACTIVATED - USING SAFE PATH");
                Log.d(TAG, "============================================");
                
                // For ESP32 demo mode, use a completely separate, minimal initialization path
                trySafeInitializationForEsp32Demo();
                return;
            }
            
            // Regular initialization for normal users follows
            try {
                viewModel = new ViewModelProvider(this, ViewModelModule.provideViewModelFactory())
                        .get(DashboardViewModel.class);
                authViewModel = new ViewModelProvider(this, ViewModelModule.provideViewModelFactory())
                        .get(AuthViewModel.class);
                
                // Check authentication
                if (!authViewModel.isUserLoggedIn()) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return;
                }
                
                // Show progress indicator while navigation is loading
                binding.progressBar.setVisibility(View.VISIBLE);
                
                // Initialize navigation
                initializeNavigation();
            } catch (Exception e) {
                // ViewModel initialization failed
                Log.e(TAG, "ViewModel initialization failed", e);
                showErrorAndRestart("Error initializing dashboard: " + e.getMessage());
            }
        } catch (Exception e) {
            // Fatal initialization error
            Log.e(TAG, "Fatal initialization error", e);
            showErrorAndRestart("Application initialization failed: " + e.getMessage());
        }
    }
    
    /**
     * A completely separate, minimal initialization path for ESP32 demo mode
     * This avoids all the complex initialization that might be causing crashes
     */
    private void trySafeInitializationForEsp32Demo() {
        try {
            // Show progress while initializing
            binding.progressBar.setVisibility(View.VISIBLE);
            
            // Very simple initialization with minimal dependencies
            Log.d(TAG, "Using minimal initialization for ESP32 demo mode");
            Toast.makeText(this, "ESP32 Demo Mode", Toast.LENGTH_SHORT).show();
            
            // Set up navigation with delayed initialization to ensure view is ready
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    Log.d(TAG, "Setting up navigation for ESP32 demo mode");
                    
                    // Simple NavController setup
                    if (findViewById(R.id.nav_host_fragment) != null) {
                        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
                        
                        // Add destination change listener to handle bottom navigation visibility
                        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                            // Hide bottom navigation when on diagnostic detail screens or other detail screens
                            int destinationId = destination.getId();
                            if (destinationId == R.id.diagnosticDetailFragment || 
                                destinationId == R.id.notificationsFragment ||
                                destinationId == R.id.editProfileFragment ||
                                destinationId == R.id.settingsFragment) {
                                // Hide bottom navigation when viewing detail screens
                                binding.bottomNavigationView.setVisibility(View.GONE);
                            } else {
                                // Show bottom navigation for all other screens
                                binding.bottomNavigationView.setVisibility(View.VISIBLE);
                            }
                        });
                        
                        // Hide progress when done
                        binding.progressBar.setVisibility(View.GONE);
                    } else {
                        // If navigation setup fails, at least hide the progress
                        Log.e(TAG, "Navigation view not found for ESP32 demo");
                        binding.progressBar.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    // Even if navigation setup fails, don't crash
                    Log.e(TAG, "Error in ESP32 demo navigation setup", e);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Demo mode active with limited functionality", Toast.LENGTH_LONG).show();
                }
            }, 1000);
        } catch (Exception e) {
            // Even if something goes wrong, try not to crash
            Log.e(TAG, "Error in ESP32 demo mode", e);
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error initializing demo mode", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Initialize navigation with retry mechanism
     */
    private void initializeNavigation() {
        // Wait for the fragment container to be properly initialized
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                // Special check for ESP32 user to ensure we don't crash
                boolean isEsp32Demo = FirebaseHelper.isEsp32DemoUser(this);
                
                // First check if the fragment host exists and is properly inflated
                View navHostFragment = findViewById(R.id.nav_host_fragment);
                if (navHostFragment == null) {
                    throw new IllegalStateException("Nav host fragment not found");
                }
                
                try {
                    navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                    NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
                    
                    // Handle navigation state changes
                    navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
                        @Override
                        public void onDestinationChanged(@NonNull NavController controller,
                                                       @NonNull NavDestination destination,
                                                       @Nullable Bundle arguments) {
                            // Hide progress indicator once navigation is ready
                            binding.progressBar.setVisibility(View.GONE);
                            Log.d(TAG, "Navigation to: " + destination.getLabel());
                            
                            // Hide bottom navigation when on diagnostic detail screens or other detail screens
                            int destinationId = destination.getId();
                            if (destinationId == R.id.diagnosticDetailFragment || 
                                destinationId == R.id.notificationsFragment ||
                                destinationId == R.id.editProfileFragment ||
                                destinationId == R.id.settingsFragment) {
                                // Hide bottom navigation when viewing detail screens
                                binding.bottomNavigationView.setVisibility(View.GONE);
                            } else {
                                // Show bottom navigation for all other screens
                                binding.bottomNavigationView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    
                    // Reset retry count on success
                    navigationInitRetryCount = 0;
                } catch (Exception e) {
                    Log.e(TAG, "Error setting up NavController: " + e.getMessage(), e);
                    binding.progressBar.setVisibility(View.GONE);
                    
                    // For ESP32 demo user, don't crash - just show a basic UI
                    if (isEsp32Demo) {
                        Toast.makeText(this, "Demo mode: Some features may be limited", Toast.LENGTH_LONG).show();
                    } else {
                        throw e; // Re-throw for normal users to trigger retry logic
                    }
                }
                
                // Load user's motorcycles
                if (viewModel != null) {
                    try {
                        viewModel.loadUserMotorcycles();
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading motorcycles: " + e.getMessage(), e);
                        // Don't crash the app if this fails
                        Toast.makeText(this, "Could not load motorcycles", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Navigation setup failed", e);
                binding.progressBar.setVisibility(View.GONE);
                
                // Retry initialization if under max retry count
                if (navigationInitRetryCount < MAX_RETRY_COUNT) {
                    navigationInitRetryCount++;
                    Log.d(TAG, "Retrying navigation setup. Attempt " + navigationInitRetryCount);
                    // Wait longer for each retry
                    initializeNavigation(500 * navigationInitRetryCount);
                } else {
                    showErrorAndRestart("Navigation setup failed after multiple attempts: " + e.getMessage());
                }
            }
        }, 500); // Give it 500ms to initialize properly
    }
    
    /**
     * Initialize navigation with a custom delay
     * 
     * @param delayMillis The delay in milliseconds
     */
    private void initializeNavigation(long delayMillis) {
        new Handler(Looper.getMainLooper()).postDelayed(this::initializeNavigation, delayMillis);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // Any additional context initialization if needed
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources
        binding = null;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Log activity lifecycle for debugging
        Log.d(TAG, "MainActivity onPause called");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Check if theme needs updating when coming back to the app
            boolean isDarkTheme = PreferencesManager.getInstance(this).isDarkThemeEnabled();
            int currentNightMode = AppCompatDelegate.getDefaultNightMode();
            boolean isCurrentlyDarkMode = (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES);
            
            // If there's a mismatch between saved preference and current mode, update it
            if (isDarkTheme != isCurrentlyDarkMode) {
                Log.d(TAG, "Theme change detected in onResume, updating...");
                // Just update the mode without recreating to avoid loops
                AppCompatDelegate.setDefaultNightMode(
                    isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
            }
            
            // Regular onResume code follows
            Log.d(TAG, "MainActivity onResume called");
            
            // Verify authentication status when resuming
            if (authViewModel != null) {
                // Check if user is still logged in
                if (!authViewModel.isUserLoggedIn()) {
                    Log.w(TAG, "User session lost during app resume, attempting to recover");
                    // Try to refresh auth token
                    new Thread(() -> {
                        boolean refreshed = FirebaseHelper.refreshAuthToken();
                        if (!refreshed || !authViewModel.isUserLoggedIn()) {
                            // If still not logged in, redirect to login
                            runOnUiThread(this::redirectToLogin);
                        }
                    }).start();
                } else {
                    Log.d(TAG, "User still authenticated on resume: " + 
                        FirebaseHelper.getCurrentUser().getEmail());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
            showErrorAndRestart("Error in onResume: " + e.getMessage());
        }
    }
    
    @Override
    protected void onRestart() {
        super.onRestart();
        // Apply theme if needed when user returns to app
        try {
            // Skip full recreation on restart, just apply night mode if needed
            boolean isDarkTheme = PreferencesManager.getInstance(this).isDarkThemeEnabled();
            int currentNightMode = AppCompatDelegate.getDefaultNightMode();
            boolean isCurrentlyDarkMode = (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES);
            
            // Only update if there's a mismatch
            if (isDarkTheme != isCurrentlyDarkMode) {
                Log.d(TAG, "Theme change detected in onRestart, updating...");
                AppCompatDelegate.setDefaultNightMode(
                    isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking theme in onRestart", e);
        }
        Log.d(TAG, "MainActivity onRestart called");
    }
    
    /**
     * Redirect to login screen
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Show error message and restart to login
     */
    private void showErrorAndRestart(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        // Go back to login
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
    
    // Add a callback to handle theme changes without crashing
    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        try {
            // Get the current theme setting
            boolean isDarkTheme = PreferencesManager.getInstance(this).isDarkThemeEnabled();
            
            // Get the current UI mode
            int currentNightMode = newConfig.uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            boolean isSystemDarkMode = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            
            // Only react if there's a mismatch between settings and current mode
            if (isDarkTheme != isSystemDarkMode) {
                Log.d(TAG, "Configuration change detected with theme mismatch, applying theme...");
                // Apply the theme but don't recreate to avoid loops
                if (isDarkTheme) {
                    setTheme(R.style.Theme_MotorDiagnostic_Dark);
                } else {
                    setTheme(R.style.Theme_MotorDiagnostic);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling configuration change", e);
        }
    }
}
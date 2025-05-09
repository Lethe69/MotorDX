package com.motor.diagnostic.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.motor.diagnostic.BuildConfig;
import com.motor.diagnostic.R;
import com.motor.diagnostic.data.util.AppVerifier;
import com.motor.diagnostic.data.util.FirebaseHelper;
import com.motor.diagnostic.databinding.ActivitySplashBinding;
import com.motor.diagnostic.presentation.di.ViewModelModule;
import com.motor.diagnostic.presentation.ui.authentication.LoginActivity;
import com.motor.diagnostic.presentation.viewmodel.AuthViewModel;

/**
 * Splash screen activity shown when the app is first launched
 */
public class SplashActivity extends AppCompatActivity {
    
    private ActivitySplashBinding binding;
    private AuthViewModel viewModel;
    private static final String TAG = "SplashActivity";
    
    // Splash timeout duration
    private static final long SPLASH_TIMEOUT = 2000; // 2 seconds
    
    // Enable app verification in debug mode
    private static final boolean ENABLE_APP_VERIFICATION = true;
    private boolean firebaseInitializationAttempted = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Initialize binding
            binding = ActivitySplashBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            
            // Setup UI immediately for better user experience
            setupUi();
            
            // Initialize Firebase on a background thread
            new Thread(() -> {
                try {
                    // Mark that we attempted Firebase initialization
                    firebaseInitializationAttempted = true;
                    
                    // Initialize Firebase safely
                    boolean success = FirebaseHelper.resetFirebaseAuth(getApplicationContext());
                    
                    Log.d(TAG, "Firebase initialization " + (success ? "successful" : "failed"));
                    
                    // Continue setup on UI thread after Firebase initialization
                    runOnUiThread(() -> initializeViewModel());
                } catch (Exception e) {
                    Log.e(TAG, "Firebase initialization error", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Authentication error. Please try again.", Toast.LENGTH_SHORT).show();
                        goToLoginActivity();
                    });
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "Initialization error", e);
            // Catch any exception during initialization
            Toast.makeText(this, "Error initializing application. Please try again.", Toast.LENGTH_SHORT).show();
            goToLoginActivity();
        }
    }
    
    private void setupUi() {
        // Add debug info
        if (BuildConfig.DEBUG) {
            binding.tvDebugInfo.setVisibility(View.VISIBLE);
            binding.tvDebugInfo.setText("Debug Build");
            
            // Make version clickable to show verification dialog in debug mode
            binding.tvVersion.setOnClickListener(v -> runAppVerification());
        } else {
            binding.tvDebugInfo.setVisibility(View.GONE);
        }
        
        // Set version text
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            binding.tvVersion.setText("v" + versionName);
        } catch (Exception e) {
            binding.tvVersion.setText("v1.0");
        }
    }
    
    private void initializeViewModel() {
        try {
            viewModel = new ViewModelProvider(this, ViewModelModule.provideViewModelFactory())
                    .get(AuthViewModel.class);
            
            // Run verification if enabled (for debugging)
            if (ENABLE_APP_VERIFICATION && BuildConfig.DEBUG) {
                runAppVerification();
            } else {
                // Normal flow - delay splash screen
                new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginStatus, SPLASH_TIMEOUT);
            }
        } catch (Exception e) {
            Log.e(TAG, "ViewModel initialization error", e);
            // If ViewModel creation fails, go to login activity by default
            Toast.makeText(this, "Error initializing application. Please try again.", Toast.LENGTH_SHORT).show();
            goToLoginActivity();
        }
    }
    
    /**
     * Run app verification for debugging purposes
     */
    private void runAppVerification() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        AppVerifier.verifyAppComponents(this, report -> {
            binding.progressBar.setVisibility(View.GONE);
            
            // Show verification report in a dialog
            new AlertDialog.Builder(this)
                    .setTitle("App Verification Report")
                    .setMessage(report)
                    .setPositiveButton("Continue", (dialog, which) -> {
                        dialog.dismiss();
                        checkLoginStatus();
                    })
                    .setCancelable(false)
                    .show();
        });
    }
    
    /**
     * Check if the user is logged in and navigate accordingly
     */
    private void checkLoginStatus() {
        try {
            // Only proceed if we've attempted Firebase initialization
            if (!firebaseInitializationAttempted) {
                Log.e(TAG, "Firebase not yet initialized, going to login screen");
                goToLoginActivity();
                finish();
                return;
            }
            
            // Use our FirebaseHelper to safely check login status
            boolean isLoggedIn = FirebaseHelper.isUserAuthenticated();
            
            if (viewModel != null && isLoggedIn) {
                // User is already logged in, go to main activity
                startActivity(new Intent(this, MainActivity.class));
            } else {
                // User is not logged in, go to login activity
                goToLoginActivity();
            }
            
            // Close this activity
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status", e);
            // If any error occurs, go to login activity
            goToLoginActivity();
            finish();
        }
    }
    
    /**
     * Navigate to the login activity
     */
    private void goToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }
} 
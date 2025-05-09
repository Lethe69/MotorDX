package com.motor.diagnostic.presentation.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.motor.diagnostic.R;
import com.motor.diagnostic.databinding.ActivityLoginBinding;
import com.motor.diagnostic.presentation.di.ViewModelModule;
import com.motor.diagnostic.presentation.ui.MainActivity;
import com.motor.diagnostic.presentation.viewmodel.AuthViewModel;
import com.motor.diagnostic.data.util.FirebaseHelper;

/**
 * Activity for user login
 */
public class LoginActivity extends AppCompatActivity {
    
    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;
    private static final String TAG = "LoginActivity";
    private boolean isAuthReset = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize ViewModel first so UI is responsive
        try {
            viewModel = new ViewModelProvider(this, ViewModelModule.provideViewModelFactory())
                    .get(AuthViewModel.class);
                    
            // Set up click listeners
            setupClickListeners();
            
            // Observe ViewModel LiveData
            observeViewModel();
            
            // Reset Firebase authentication as a background task
            resetFirebaseAuth();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing login. Please restart the app.", Toast.LENGTH_LONG).show();
        }
    }
    
    private void resetFirebaseAuth() {
        // Show that we're initializing
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvInitializing.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);
        binding.btnSignUp.setEnabled(false);
        
        new Thread(() -> {
            try {
                // Reset Firebase on background thread to avoid UI freezes
                isAuthReset = FirebaseHelper.resetFirebaseAuth(getApplicationContext());
                
                // Check login status on UI thread
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvInitializing.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                    binding.btnSignUp.setEnabled(true);
                    
                    // Check if user is already logged in after reset
                    if (isAuthReset && viewModel.isUserLoggedIn()) {
                        navigateToMainActivity();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvInitializing.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                    binding.btnSignUp.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Authentication service error. Please try again.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void setupClickListeners() {
        // Login button
        binding.btnLogin.setOnClickListener(v -> login());
        
        // Sign up button
        binding.btnSignUp.setOnClickListener(v -> navigateToRegisterActivity());
        
        // Forgot password
        binding.tvForgotPassword.setOnClickListener(v -> navigateToForgotPasswordActivity());
    }
    
    private void observeViewModel() {
        // Observe current user
        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                // User logged in successfully
                navigateToMainActivity();
            }
        });
        
        // Observe loading state
        viewModel.getLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
            binding.btnSignUp.setEnabled(!isLoading);
        });
        
        // Observe error message
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                
                // If we get a specific credential error, try to fix it automatically
                if (errorMessage.contains("credential") || errorMessage.contains("expired") || errorMessage.contains("token")) {
                    new Handler(Looper.getMainLooper()).postDelayed(this::resetFirebaseAuth, 1000);
                }
            }
        });
    }
    
    private void login() {
        // Get input values
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        
        // Validate input
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.hint_email) + " is required");
            return;
        }
        
        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.hint_password) + " is required");
            return;
        }
        
        // Clear errors
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        
        // Only proceed if Firebase auth has been reset
        if (!isAuthReset) {
            Toast.makeText(this, "Please wait for authentication to initialize.", Toast.LENGTH_SHORT).show();
            resetFirebaseAuth();
            return;
        }
        
        // Show loading
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);
        binding.btnSignUp.setEnabled(false);
        
        // Login in a background thread
        new Thread(() -> {
            try {
                // Refresh the auth token for existing user if any
                FirebaseHelper.refreshAuthToken();
                
                // Login on UI thread
                runOnUiThread(() -> viewModel.login(email, password));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                    binding.btnSignUp.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void navigateToRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    
    private void navigateToForgotPasswordActivity() {
        // TODO: Implement forgot password activity
        Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show();
    }
} 
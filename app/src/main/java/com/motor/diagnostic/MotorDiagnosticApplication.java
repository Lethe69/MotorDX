package com.motor.diagnostic;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.motor.diagnostic.data.util.FirebaseHelper;
import com.motor.diagnostic.data.util.NetworkConnectionHandler;

/**
 * Main application class for the Motor Diagnostic app
 */
public class MotorDiagnosticApplication extends Application {
    
    private static final String TAG = "MotorDiagnosticApp";
    private NetworkConnectionHandler networkHandler;
    private boolean firebaseInitialized = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Initialize Firebase with safety checks using our helper
            initializeFirebase();
            
            // Initialize Network Connection Handler
            initializeNetworkHandler();
        } catch (Exception e) {
            Log.e(TAG, "Error during application initialization", e);
            Toast.makeText(this, "Error initializing application", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Initialize Firebase safely
     */
    private void initializeFirebase() {
        try {
            // Use our improved FirebaseHelper to safely initialize Firebase
            firebaseInitialized = FirebaseHelper.resetFirebaseAuth(getApplicationContext());
            
            if (firebaseInitialized) {
                Log.d(TAG, "Firebase initialized successfully");
                
                // For debug builds, use anonymous auth to ensure writing to Firebase works
                if (BuildConfig.DEBUG) {
                    // Run on a background thread to avoid ANR
                    new Thread(this::tryAnonymousAuth).start();
                }
            } else {
                Log.e(TAG, "Firebase initialization failed");
                // Try again after a delay
                new Handler(Looper.getMainLooper()).postDelayed(this::initializeFirebase, 3000);
            }
        } catch (Exception e) {
            firebaseInitialized = false;
            Log.e(TAG, "Firebase initialization failed with exception", e);
            // Try again after a delay
            new Handler(Looper.getMainLooper()).postDelayed(this::initializeFirebase, 3000);
        }
    }
    
    /**
     * Try to sign in anonymously for debug builds
     */
    private void tryAnonymousAuth() {
        try {
            if (FirebaseHelper.getCurrentUser() == null) {
                FirebaseAuth.getInstance().signInAnonymously()
                    .addOnSuccessListener(authResult -> Log.d(TAG, "Anonymous auth successful"))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Anonymous auth failed", e);
                        
                        if (e.getMessage() != null && 
                            (e.getMessage().contains("invalid credentials") || 
                            e.getMessage().contains("malformed") || 
                            e.getMessage().contains("expired"))) {
                            
                            // Auth credentials issue - try to fix by signing out and waiting
                            try {
                                FirebaseAuth.getInstance().signOut();
                                
                                // Try again after a delay
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    try {
                                        FirebaseAuth.getInstance().signInAnonymously()
                                            .addOnSuccessListener(result -> Log.d(TAG, "Retry anonymous auth successful"))
                                            .addOnFailureListener(error -> Log.e(TAG, "Retry anonymous auth failed", error));
                                    } catch (Exception retryError) {
                                        Log.e(TAG, "Error during retry anonymous auth", retryError);
                                    }
                                }, 2000);
                            } catch (Exception refreshError) {
                                Log.e(TAG, "Failed to sign out for retry", refreshError);
                            }
                        }
                    });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during anonymous auth attempt", e);
        }
    }
    
    /**
     * Initialize the network connection handler
     */
    private void initializeNetworkHandler() {
        try {
            networkHandler = NetworkConnectionHandler.getInstance(this);
            networkHandler.startMonitoring(new NetworkConnectionHandler.NetworkConnectionListener() {
                @Override
                public void onConnectionChanged(boolean isConnected) {
                    Log.d(TAG, "Network connection changed: " + (isConnected ? "Connected" : "Disconnected"));
                    
                    if (isConnected && !firebaseInitialized) {
                        // If network is available but Firebase isn't initialized, try to initialize it
                        initializeFirebase();
                    }
                }
            });
            Log.d(TAG, "Network connection handler initialized");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize network handler", e);
        }
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        
        try {
            // Initialize Multidex
            MultiDex.install(this);
            Log.d(TAG, "Multidex installed");
        } catch (Exception e) {
            Log.e(TAG, "Error installing Multidex", e);
        }
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        
        // Clean up resources
        if (networkHandler != null) {
            networkHandler.stopMonitoring();
        }
        
        Log.d(TAG, "Application terminated");
    }
    
    /**
     * Get the network connection handler
     * 
     * @return The network connection handler
     */
    public NetworkConnectionHandler getNetworkHandler() {
        return networkHandler;
    }
    
    /**
     * Check if Firebase is initialized
     * 
     * @return true if Firebase is initialized, false otherwise
     */
    public boolean isFirebaseInitialized() {
        return firebaseInitialized;
    }
} 
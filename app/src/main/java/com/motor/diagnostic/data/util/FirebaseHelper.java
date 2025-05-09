package com.motor.diagnostic.data.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Helper class for Firebase authentication operations
 */
public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static boolean isInitializing = false;
    
    /**
     * Reset Firebase authentication completely
     *
     * @param context Application context
     * @return true if successful, false otherwise
     */
    public static synchronized boolean resetFirebaseAuth(@NonNull Context context) {
        // Check if we're already initializing to prevent concurrent initialization attempts
        if (isInitializing) {
            Log.d(TAG, "Firebase initialization already in progress, skipping");
            return true;
        }
        
        isInitializing = true;
        
        try {
            Log.d(TAG, "Resetting Firebase authentication");
            
            // Sign out current user if any
            try {
                if (FirebaseAuth.getInstance() != null) {
                    FirebaseAuth.getInstance().signOut();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error signing out user", e);
            }
            
            // Check if Firebase is already initialized
            if (!FirebaseApp.getApps(context).isEmpty()) {
                Log.d(TAG, "Firebase already initialized, skipping reset");
                isInitializing = false;
                return true;
            }
            
            // Clear preferences only if we're initializing for the first time
            try {
                context.getSharedPreferences("com.google.firebase.auth.api.Store", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
                
                context.getSharedPreferences("FirebaseAppHeartBeat", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
            } catch (Exception e) {
                Log.e(TAG, "Error clearing Firebase preferences", e);
            }
            
            // Initialize Firebase with explicit configuration
            try {
                // First check if there's already a default instance
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseOptions.Builder optionsBuilder = new FirebaseOptions.Builder()
                        .setApplicationId("1:416414084979:android:9e4ea084d59b500a2fdf06")
                        .setApiKey("AIzaSyAu03tJppiS2QdQK38TZlD5Dz5AIdHNp94")
                        .setDatabaseUrl("https://motor-b1a63-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .setStorageBucket("motor-b1a63.firebasestorage.app")
                        .setProjectId("motor-b1a63");
                    
                    FirebaseApp.initializeApp(context, optionsBuilder.build());
                    Log.d(TAG, "Firebase initialized with custom options");
                } else {
                    Log.d(TAG, "Using existing Firebase instance");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Firebase with custom options", e);
                // Fall back to default initialization if not already initialized
                try {
                    if (FirebaseApp.getApps(context).isEmpty()) {
                        FirebaseApp.initializeApp(context);
                        Log.d(TAG, "Firebase initialized with default options");
                    }
                } catch (Exception e2) {
                    Log.e(TAG, "Failed to initialize Firebase with default options", e2);
                    isInitializing = false;
                    return false;
                }
            }
            
            // Initialize Firebase components with try-catch for each
            try {
                FirebaseAuth.getInstance();
            } catch (Exception e) {
                Log.e(TAG, "Error initializing FirebaseAuth", e);
            }
            
            try {
                // Only enable persistence if not already enabled
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                try {
                    database.setPersistenceEnabled(true);
                } catch (Exception e) {
                    // This often fails if persistence was already enabled which is fine
                    Log.w(TAG, "Note: Firebase persistence already enabled or couldn't be enabled", e);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error accessing FirebaseDatabase", e);
            }
            
            isInitializing = false;
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to reset Firebase", e);
            isInitializing = false;
            return false;
        }
    }
    
    /**
     * Check if a user is currently authenticated
     *
     * @return true if a user is authenticated, false otherwise
     */
    public static boolean isUserAuthenticated() {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            return currentUser != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking authentication state", e);
            return false;
        }
    }
    
    /**
     * Force refresh the current user's authentication token
     * 
     * @return true if refresh was attempted, false otherwise
     */
    public static boolean refreshAuthToken() {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                currentUser.getIdToken(true)
                    .addOnSuccessListener(result -> Log.d(TAG, "Token refreshed successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to refresh token", e));
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing auth token", e);
            return false;
        }
    }
    
    /**
     * Safe method to get current Firebase user
     * 
     * @return FirebaseUser or null if not authenticated or on error
     */
    public static FirebaseUser getCurrentUser() {
        try {
            return FirebaseAuth.getInstance().getCurrentUser();
        } catch (Exception e) {
            Log.e(TAG, "Error getting current user", e);
            return null;
        }
    }
} 